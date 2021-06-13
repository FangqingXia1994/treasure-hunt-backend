package com.treasurehunt.treasurehunt.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseBody {
    @JsonProperty("token")
    private final String token;

    @JsonProperty("name")
    private final String name;

    public LoginResponseBody(String token, String name) {
        this.token = token;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }
}
