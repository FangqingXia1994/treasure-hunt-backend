package com.treasurehunt.treasurehunt.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;
import com.treasurehunt.treasurehunt.db.gcs.GCS;
import com.treasurehunt.treasurehunt.entity.Listing;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

public class ServletUtil {
    private static final Logger logger = LoggerFactory.getLogger(ServletUtil.class);

    // Helper method to write listings to servlet response body
    public static void writeListings(HttpServletResponse response, List<Listing> listings) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().print(new ObjectMapper().writeValueAsString(listings));
        } catch (IOException e) {
            logger.warn("Failed to write listings {} into response", listings);
        }
    }

    // Convert request body to object of class T
    public static <T> T readRequestBody(Class<T> cl, HttpServletRequest request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(request.getReader(), cl);
        } catch (JsonParseException | JsonMappingException e) {
            logger.warn("Cannot parse/map the following request: {}", request.getReader());
            return null;
        }
    }

    // Get authorized userId from request
    public static String getAuthorizedUserIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new JwtTokenMissingException("No JWT token found in request headers");
        }

        String authToken = header.substring(7); // strip Bearer from header

        return AuthUtils.getUserIdFromToken(authToken);
    }

    // Create short pictureUrl
    public static String getShortPictureUrl(String fullPictureUrl) {
        if (StringUtils
                .startsWith(fullPictureUrl, "https://storage.googleapis" +
                        ".com/download/storage/v1/b/treasure-hunt-listing-pictures/o/")) {

            return StringUtils
                    .removeStart(fullPictureUrl, "https://storage.googleapis" +
                            ".com/download/storage/v1/b/treasure-hunt-listing-pictures/o/");
        } else {
            return fullPictureUrl;
        }
    }

    public static void deleteExistingPictures(Storage storage, LinkedHashMap<String, String> pictureUrls) {
        if (pictureUrls == null) {
            return;
        }
        for (String pictureId : pictureUrls.keySet()) {
            GCS.deletePicture(storage, pictureId);
        }
    }

    public static void uploadNewPictures(Storage storage, LinkedHashMap<String, String> pictureUrls,
                                         HttpServletRequest request) throws IOException, ServletException {

        String fileNamePrefix = "picture_%s";
        for (int i = 1; i < 11; i++) {

            Part filePart = request.getPart(String.format(fileNamePrefix, i));
            // In case that seller didn't upload all three pictures
            if (filePart == null || filePart.getSubmittedFileName().equals("")) {
                logger.info("Stopping, no file found at {}", String.format(fileNamePrefix, i));
                return;
            }

            String fileName = System.currentTimeMillis() + filePart.getSubmittedFileName();
            InputStream fileInputStream = filePart.getInputStream();

            String fullUrl = GCS.uploadPicture(storage, fileName, fileInputStream);
            String shortUrl = ServletUtil.getShortPictureUrl(fullUrl);

            pictureUrls.put(fileName, shortUrl);
        }
    }
}
