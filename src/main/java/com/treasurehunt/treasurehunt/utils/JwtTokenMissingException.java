package com.treasurehunt.treasurehunt.utils;

public class JwtTokenMissingException extends RuntimeException {
    public JwtTokenMissingException(String errorMessage) {
        super(errorMessage);
    }
}
