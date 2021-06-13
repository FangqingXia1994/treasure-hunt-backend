package com.treasurehunt.treasurehunt.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;

public class DbUtils {

    private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);

    // Convert raw picture urls to a LinkedHashMap
    public static LinkedHashMap<String, String> readPictureUrls(String rawPictureUrls) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<LinkedHashMap<String, String>> typeRef = new TypeReference<LinkedHashMap<String, String>>() {
        };

        try {
            return mapper.readValue(rawPictureUrls, typeRef);
        } catch (JsonParseException | JsonMappingException e) {
            logger.warn("Cannot parse/map the following pictureUrls: {}", rawPictureUrls);
            return null;
        }
    }

}
