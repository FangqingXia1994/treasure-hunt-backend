package com.treasurehunt.treasurehunt.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasurehunt.treasurehunt.db.elasticsearch.Elasticsearch;
import com.treasurehunt.treasurehunt.db.elasticsearch.ElasticsearchException;
import com.treasurehunt.treasurehunt.entity.Listing;
import com.treasurehunt.treasurehunt.entity.SearchListingsRequestBody;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SearchServlet", urlPatterns = {"/api/search"})
public class SearchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        // Parse params into generic json object
        JSONObject jsonObj = new JSONObject();
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String v[] = entry.getValue();
            Object o = (v.length == 1) ? v[0] : v;
            jsonObj.put(entry.getKey(), o);
        }
        logger.info("converted with JSONObject: {}", jsonObj.toString());

        // Parse json object into SearchListingsRequestBody
        SearchListingsRequestBody requestBody;
        try {
            requestBody = objectMapper
                    .readValue(jsonObj.toString(), SearchListingsRequestBody.class);
            logger.info("Converted params to class {}", objectMapper.writeValueAsString(requestBody));
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid inputs, please check and resubmit");
            return;
        }
//
        // Parse for bad inputs
        if (requestBody.getMaxPrice() < 0 || requestBody.getTimeInterval() < 0 || requestBody
                .getMaxPrice() < requestBody.getMinPrice()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Errors in entries, please try again");
            return;
        }

        // Get search results
        RestHighLevelClient client = (RestHighLevelClient) request.getServletContext().getAttribute("es-client");
        List<Listing> finalResult;

        try {
            finalResult = Elasticsearch.getSearchResults(client, requestBody);
        } catch (ElasticsearchException e) {
            logger.warn("Failed to get search results from Elasticsearch", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("Failed to get search results from Elasticsearch");
            return;
        }
        // Write search results into response body
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(finalResult));
    }
}
