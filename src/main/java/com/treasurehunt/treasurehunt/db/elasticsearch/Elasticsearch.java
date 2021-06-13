package com.treasurehunt.treasurehunt.db.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasurehunt.treasurehunt.entity.Listing;
import com.treasurehunt.treasurehunt.entity.SearchListingsRequestBody;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Elasticsearch {

    // Deployment - double check here
    private static final String LISTINGS_INDEX = "listings";
    private static final int MAX_NUMBER_OF_SEARCH_RESULTS = 30;
    private static final Logger logger = LoggerFactory.getLogger(Elasticsearch.class);

    // Build request object for Elasticsearch query
    private static SearchRequest buildListingsSearchRequest(SearchListingsRequestBody requestBody) throws ElasticsearchException {
        SearchRequest searchRequest = new SearchRequest(LISTINGS_INDEX);

        // Configure searchRequest with data in the requestBody
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(MAX_NUMBER_OF_SEARCH_RESULTS);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));

        // changed will track if query is empty at the end
        boolean changed = false;

        // Configure searchQuery and filters

        // BoolQueryBuilder is used to assemble query conditions.
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (requestBody.getKeyword() != null && !requestBody.getKeyword().trim().equals("")) {
            // Fuzzy searches keyword in title and description fields
            String keyword = requestBody.getKeyword();
            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
            keywordQuery.should(QueryBuilders.matchQuery("title", keyword).fuzziness("AUTO"));
            keywordQuery
                    .should(QueryBuilders.matchQuery("category", StringUtils.capitalize(keyword)).fuzziness("AUTO"));
            keywordQuery.should(QueryBuilders.multiMatchQuery(keyword, "brand", "description"));
            boolBuilder.must(keywordQuery);
            changed = true;
        }

        if (requestBody.getCondition() != null && !requestBody.getCondition().trim().equals("")) {
            String queryCondition = requestBody.getCondition();
            List<String> strings = new ArrayList<>();
            boolean finished = false;
            int index = 0;
            String[] conditions = new String[]{"New", "Used - Like new", "Used - Good", "Used - Fair"};
            while (!finished && index < conditions.length) {
                strings.add(conditions[index]);
                if (queryCondition.equals(conditions[index++])) {
                    finished = true;
                }
            }
            String[] args = strings.toArray(new String[0]);
            logger.info("Got condition {} and generated args {}", requestBody.getCondition(), args);
            boolBuilder.filter(QueryBuilders.termsQuery("item_condition", args));
            changed = true;
        }

        if (requestBody.getLatitude() != 0 && requestBody.getLongitude() != 0) {
            String distance = "20";
            if (requestBody.getDistance() != null && !requestBody.getDistance().trim().equals("")) {
                distance = requestBody.getDistance();
            }
            boolBuilder.filter(QueryBuilders.geoDistanceQuery("geo_location")
                                            .point(requestBody.getLatitude(), requestBody.getLongitude())
                                            .distance(distance, DistanceUnit.MILES));
            changed = true;
        }

        if (requestBody.getMaxPrice() != 0.0) {
            boolBuilder.filter(QueryBuilders.rangeQuery("price").lte(requestBody.getMaxPrice()));
            changed = true;
        }

        if (requestBody.getMinPrice() != 0.0) {
            boolBuilder.filter(QueryBuilders.rangeQuery("price").gte(requestBody.getMinPrice()));
            changed = true;
        }

        if (requestBody.getTimeInterval() != 0) {
            Instant now = Instant.now();
            boolBuilder.filter(QueryBuilders.rangeQuery("date")
                                            .from(now.minus(requestBody.getTimeInterval(), ChronoUnit.DAYS)));
            changed = true;
        }

        if (requestBody.getCategory() != null && !requestBody.getCategory().trim().equals("")) {
            boolBuilder.filter(QueryBuilders.termQuery("category", requestBody.getCategory()));
            changed = true;
        }

        if (!changed) {
            logger.warn("Elasticsearch query does not contain keyword or category");
            throw new ElasticsearchException("Search query does not contain keyword or category");
        }

        sourceBuilder.query(boolBuilder);

        searchRequest.source(sourceBuilder);

        // return searchRequest object
        return searchRequest;
    }

    // Search the listings index and return a list of listings
    public static List<Listing> getSearchResults(RestHighLevelClient client,
                                                 SearchListingsRequestBody requestBody) throws ElasticsearchException {

        try {
            // Get raw search results from Elasticsearch
            SearchRequest searchRequest = buildListingsSearchRequest(requestBody);
            SearchResponse rawSearchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // Create list of search results
            List<Listing> searchResults = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();

            // Stream results into listingSearchResults
            Arrays.stream(rawSearchResponse.getHits().getHits()).forEach(hit -> {
                try {
                    searchResults
                            .add(objectMapper.readValue(hit.getSourceAsString(), Listing.class));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            // Return the responseBody
            return searchResults;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("Unable to parse search results sent from Elasticsearch");
        }
    }

    // Add listing to the listings index
    public static void addListing(RestHighLevelClient client, Listing listing) throws ElasticsearchException {
        try {
            // Create index request
            IndexRequest request = new IndexRequest(LISTINGS_INDEX).id(listing.getListingId());
            request.source(new ObjectMapper().writeValueAsString(listing), XContentType.JSON);
            // Execute the request
            client.index(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("Failed to create Listing");
        }
    }

    // Update existing listing in the listings index
    public static void updateListing(RestHighLevelClient client, Listing listing) throws ElasticsearchException {
        try {

            // Create update request
            UpdateRequest request = new UpdateRequest(LISTINGS_INDEX, listing.getListingId());
            request.doc(new ObjectMapper().writeValueAsString(listing), XContentType.JSON);
            // Execute the request
            client.update(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("Failed to update Listing");
        }
    }

    // Delete existing listing from the listings index
    public static void deleteListing(RestHighLevelClient client, Listing listing) throws ElasticsearchException {
        try {
            // Create delete request
            DeleteRequest request = new DeleteRequest(LISTINGS_INDEX, listing.getListingId());

            // Execute the request
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException("Failed to delete Listing");
        }
    }
}
