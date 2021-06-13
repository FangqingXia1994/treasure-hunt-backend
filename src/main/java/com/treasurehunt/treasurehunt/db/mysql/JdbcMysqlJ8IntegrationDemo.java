package com.treasurehunt.treasurehunt.db.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

public class JdbcMysqlJ8IntegrationDemo {
    public static void main(String[] args) {
        String CONNECTION_NAME = "treasure-hunt-314706:us-central1:treasure-hunt-mysql-instance";
        String DB_NAME = "test";
        String DB_USER = "root";
        String DB_PASSWORD = "dhN8iO3kPJxeiv7b";

        String tableName;
        HikariDataSource connectionPool;

        // Set up url parameters
        String jdbcURL = String.format("jdbc:mysql:///%s", DB_NAME);
        Properties connProps = new Properties();
        connProps.setProperty("user", DB_USER);
        connProps.setProperty("password", DB_PASSWORD);
        connProps.setProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
        connProps.setProperty("cloudSqlInstance", CONNECTION_NAME);

        // Initialize connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcURL);
        config.setDataSourceProperties(connProps);
        config.setConnectionTimeout(10000);

        connectionPool = new HikariDataSource(config);
        tableName = "test";

        try (Connection conn = connectionPool.getConnection()) {
            String stmt = String.format("CREATE TABLE %s (", tableName)
                    + " ID CHAR(20) NOT NULL,"
                    + " TITLE TEXT NOT NULL"
                    + ");";
            try (PreparedStatement createTableStatement = conn.prepareStatement(stmt)) {
                createTableStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
