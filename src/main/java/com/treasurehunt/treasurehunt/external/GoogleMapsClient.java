package com.treasurehunt.treasurehunt.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasurehunt.treasurehunt.entity.GeocodeResult;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

public class GoogleMapsClient {
    private static final String GEOCODE_ADDRESS_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s" +
            "&key=%s";
    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsClient.class);

    // Build the request url which will be used when calling the Google Maps Geocode API
    private static String buildGeocodeUrl(String url, String address) throws GoogleMapsException {
        // If address is empty string, throw error
        if (address.equals("")) {
            throw new GoogleMapsException("Address cannot be empty");
        }

        // Get API Key from properties
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream =
                GoogleMapsClient.class.getClassLoader().getResourceAsStream(propFileName);
        try {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GoogleMapsException("Failed to load props");
        }

        String api_key = prop.getProperty("google_maps_geocode_api_key");

        // Encode address string
        try {
            address = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new GoogleMapsException("Failed to encode address");
        }
        return String.format(url, address, api_key);
    }

    // Send HTTP request to Google Maps Geocode API based on the given URL, and returns the body of the HTTP response
    private static String getRawGeocodeResult(String url) throws GoogleMapsException {

        // Define the response handler to parse and return HTTP response body returned from Google Maps
        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                logger.warn("Google Maps Response status: {}", response.getStatusLine().getReasonPhrase());
                throw new GoogleMapsException("Failed to get result from Google Maps Geocode API");
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new GoogleMapsException("Failed to get result from Google Maps Geocode API");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.toString();
        };

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Define the HTTP request
            HttpGet request = new HttpGet(url);
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GoogleMapsException("Failed to get geocode result from Google Maps API");
        }
    }

    // Convert JSON format data returned from Google Maps to a GeocodeResult object
    public static GeocodeResult getGeocodeResult(String address) throws GoogleMapsException {
        String data = getRawGeocodeResult(buildGeocodeUrl(GEOCODE_ADDRESS_URL, address));
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, GeocodeResult.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new GoogleMapsException("Failed to parse geocode data from Google Maps API");
        }
    }
}
