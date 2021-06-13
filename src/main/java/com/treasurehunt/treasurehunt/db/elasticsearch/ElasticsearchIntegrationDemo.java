package com.treasurehunt.treasurehunt.db.elasticsearch;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticsearchIntegrationDemo {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(ElasticsearchIntegrationDemo.class);
        try {
            // Start a client in try-with-resources so client will auto-close after execution
            try (RestHighLevelClient client = ElasticsearchClient.createElasticsearchClient()) {

                // Indexing
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("description", "this is a test");
                jsonMap.put("timestamp", new Date());
                IndexRequest indexRequest = new IndexRequest("testing")
                        .id("1").source(jsonMap);
                client.index(indexRequest, RequestOptions.DEFAULT);

                logger.debug("Indexed document to index \"{}\" with id {}",
                        indexRequest.index(),
                        indexRequest.id());

                // Getting a Document
                GetRequest getRequest = new GetRequest("testing", "1");
                GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

                logger.debug("Retrieved document: {Id: {}, Description: {}, Test Time: {}}",
                        getResponse.getId(),
                        getResponse.getSource().get("description"),
                        getResponse.getSource().get("timestamp"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
