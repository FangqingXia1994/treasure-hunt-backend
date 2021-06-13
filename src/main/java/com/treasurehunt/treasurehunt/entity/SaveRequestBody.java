package com.treasurehunt.treasurehunt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveRequestBody {
    private final String userId;
    private final String listingId;

    @JsonCreator
    public SaveRequestBody(@JsonProperty("user_id") String userId, @JsonProperty("listing_id") String listingId) {
        this.userId = userId;
        this.listingId = listingId;
    }

    public String getUserId() {
        return userId;
    }

    public String getListingId() {
        return listingId;
    }
}
