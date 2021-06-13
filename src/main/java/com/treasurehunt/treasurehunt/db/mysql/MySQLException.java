package com.treasurehunt.treasurehunt.db.mysql;

public class MySQLException extends RuntimeException {
    public MySQLException(String errorMessage) {
        super(errorMessage);
    }
}
