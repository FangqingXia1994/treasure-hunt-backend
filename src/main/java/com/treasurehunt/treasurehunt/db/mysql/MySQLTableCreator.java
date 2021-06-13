package com.treasurehunt.treasurehunt.db.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class MySQLTableCreator {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(MySQLTableCreator.class);
        // Step 1 Connect to MySQL.
        try {
            logger.debug("Connecting to Cloud SQL");
            MySQLConnectionPool mySQLConnectionPool = new MySQLConnectionPool();
            DataSource pool = mySQLConnectionPool.pool;

            // conn opened here so that it auto closes at the end of try block
            try (Connection conn = pool.getConnection()) {
                if (conn == null) {
                    return;
                }

                // Step 2 Drop tables in case they exist
                Statement statement = conn.createStatement();
                String sql = "DROP TABLE IF EXISTS saved_records";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS listings";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS users";
                statement.executeUpdate(sql);

                // Step 3 Create new tables
                sql = "CREATE TABLE users ( "
                        + "user_id VARCHAR(255) NOT NULL, "
                        + "password VARCHAR(255) NOT NULL, "
                        + "password_salt VARCHAR(255) NOT NULL, "
                        + "first_name VARCHAR(255) NOT NULL, "
                        + "last_name VARCHAR(255) NOT NULL, "
                        + "email VARCHAR(255) NOT NULL, "
                        + "address VARCHAR(255), "
                        + "geo_location VARCHAR(255), "
                        + "city_and_state VARCHAR(255), "
                        + "PRIMARY KEY (user_id) "
                        + ")";
                statement.executeUpdate(sql);

                sql = "CREATE TABLE listings ( "
                        + "listing_id VARCHAR(255) NOT NULL, "
                        + "title VARCHAR(255) NOT NULL, "
                        + "price DOUBLE NOT NULL, "
                        + "category VARCHAR(255) NOT NULL, "
                        + "description TEXT NOT NULL, "
                        + "item_condition VARCHAR(255) NOT NULL, "
                        + "brand VARCHAR(255), "
                        + "picture_urls JSON, "
                        + "seller_id VARCHAR(255) NOT NULL, "
                        + "seller_name VARCHAR(255) NOT NULL, "
                        + "seller_email VARCHAR(255) NOT NULL, "
                        + "address VARCHAR(255) NOT NULL, "
                        + "date VARCHAR(255) NOT NULL, "
                        + "geo_location VARCHAR(255) NOT NULL, "
                        + "city_and_state VARCHAR(255) NOT NULL, "
                        + "PRIMARY KEY (listing_id), "
                        + "FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE"
                        + ")";
                statement.executeUpdate(sql);

                sql = "CREATE TABLE saved_records ("
                        + "user_id VARCHAR(255) NOT NULL,"
                        + "listing_id VARCHAR(255) NOT NULL,"
                        + "PRIMARY KEY (user_id, listing_id),"
                        + "FOREIGN KEY (user_id) REFERENCES users(user_id) "
                        + ")";
                statement.executeUpdate(sql);

                logger.debug("Tables Successfully Created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
