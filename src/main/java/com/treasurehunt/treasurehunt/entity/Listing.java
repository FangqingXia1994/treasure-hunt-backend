package com.treasurehunt.treasurehunt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.LinkedHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = Listing.Builder.class)
public class Listing {
    @JsonProperty("listing_id")
    private String listingId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("price")
    private double price;
    @JsonProperty("category")
    private String category;
    @JsonProperty("seller_id")
    private String sellerId;
    @JsonProperty("seller_name")
    private String sellerName;
    @JsonProperty("seller_email")
    private String sellerEmail;
    @JsonProperty("description")
    private String description;
    @JsonProperty("item_condition")
    private String itemCondition;
    @JsonProperty("brand")
    private String brand;
    @JsonProperty("address")
    private String address;
    @JsonProperty("picture_urls")
    private LinkedHashMap<String, String> pictureUrls;
    @JsonProperty("date")
    private String date;
    @JsonProperty("geo_location")
    private GeocodeLocation geocodeLocation;
    @JsonProperty("city_and_state")
    private String cityAndState;

    public Listing(Builder builder) {
        this.listingId = builder.listingId;
        this.title = builder.title;
        this.price = builder.price;
        this.category = builder.category;
        this.sellerId = builder.sellerId;
        this.sellerName = builder.sellerName;
        this.sellerEmail = builder.sellerEmail;
        this.description = builder.description;
        this.itemCondition = builder.itemCondition;
        this.address = builder.address;
        this.brand = builder.brand;
        this.pictureUrls = builder.pictureUrls;
        this.date = builder.date;
        this.geocodeLocation = builder.geocodeLocation;
        this.cityAndState = builder.cityAndState;
    }

    public Listing() {
    }

    public String getListingId() {
        return listingId;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public String getDescription() {
        return description;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public String getBrand() {
        return brand;
    }

    public String getAddress() {
        return address;
    }

    public LinkedHashMap<String, String> getPictureUrls() {
        return pictureUrls;
    }

    public String getDate() {
        return date;
    }

    public String getCityAndState() {
        return cityAndState;
    }

    public GeocodeLocation getGeocodeLocation() {
        return geocodeLocation;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("listing_id")
        private String listingId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("price")
        private double price;

        @JsonProperty("category")
        private String category;

        @JsonProperty("seller_id")
        private String sellerId;

        @JsonProperty("seller_name")
        private String sellerName;

        @JsonProperty("seller_email")
        private String sellerEmail;

        @JsonProperty("description")
        private String description;

        @JsonProperty("item_condition")
        private String itemCondition;

        @JsonProperty("brand")
        private String brand;

        @JsonProperty("address")
        private String address;

        @JsonProperty("picture_urls")
        private LinkedHashMap<String, String> pictureUrls;

        @JsonProperty("date")
        private String date;

        @JsonProperty("geo_location")
        private GeocodeLocation geocodeLocation;

        @JsonProperty("city_and_state")
        private String cityAndState;

        public Builder setListingId(String listingId) {
            this.listingId = listingId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }


        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setSellerId(String sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder setSellerName(String sellerName) {
            this.sellerName = sellerName;
            return this;
        }

        public Builder setSellerEmail(String sellerEmail) {
            this.sellerEmail = sellerEmail;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setItemCondition(String itemCondition) {
            this.itemCondition = itemCondition;
            return this;
        }

        public Builder setBrand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setPictureUrls(LinkedHashMap<String, String> pictureUrls) {
            this.pictureUrls = pictureUrls;
            return this;
        }

        public Builder setDate(String date) {
            this.date = date;
            return this;
        }

        public Builder setGeocodeLocation(GeocodeLocation geocodeLocation) {
            this.geocodeLocation = geocodeLocation;
            return this;
        }

        public Builder setCityAndState(String cityAndState) {
            this.cityAndState = cityAndState;
            return this;
        }

        public Listing build() {
            return new Listing(this);
        }
    }
}