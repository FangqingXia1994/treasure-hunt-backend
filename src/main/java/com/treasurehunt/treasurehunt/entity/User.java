package com.treasurehunt.treasurehunt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = User.Builder.class)

public class User {
    @JsonProperty("user_id")
    private final String userId;
    @JsonProperty("first_name")
    private final String firstName;
    @JsonProperty("last_name")
    private final String lastName;
    @JsonProperty("email")
    private final String email;
    @JsonProperty("address")
    private String address;
    @JsonProperty("password")
    private String password;
    @JsonProperty("password_salt")
    private String passwordSalt;
    @JsonProperty("geo_location")
    private GeocodeLocation geocodeLocation;
    @JsonProperty("city_and_state")
    private String cityAndState;


    private User(Builder builder) {
        this.userId = builder.userId;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.address = builder.address;
        this.geocodeLocation = builder.geocodeLocation;
        this.passwordSalt = builder.passwordSalt;
        this.cityAndState = builder.cityAndState;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public User setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public User setAddress(String address) {
        this.address = address;
        return this;
    }

    public GeocodeLocation getGeocodeLocation() {
        return geocodeLocation;
    }

    public User setGeocodeLocation(GeocodeLocation geocodeLocation) {
        this.geocodeLocation = geocodeLocation;
        return this;
    }

    public String getCityAndState() {
        return cityAndState;
    }

    public User setCityAndState(String cityAndState) {
        this.cityAndState = cityAndState;
        return this;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("password")
        private String password;

        @JsonProperty("password_salt")
        private String passwordSalt;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("email")
        private String email;

        @JsonProperty("address")
        private String address;

        @JsonProperty("geo_location")
        private GeocodeLocation geocodeLocation;

        @JsonProperty("city_and_state")
        private String cityAndState;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder passwordSalt(String passwordSalt) {
            this.passwordSalt = passwordSalt;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public User build() {
            return new User(this);
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder geocodeLocation(GeocodeLocation geocodeLocation) {
            this.geocodeLocation = geocodeLocation;
            return this;
        }

        public Builder cityAndState(String cityAndState) {
            this.cityAndState = cityAndState;
            return this;
        }

    }
}
