package com.treasurehunt.treasurehunt.db.mysql;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasurehunt.treasurehunt.entity.GeocodeLocation;
import com.treasurehunt.treasurehunt.entity.Listing;
import com.treasurehunt.treasurehunt.entity.User;
import com.treasurehunt.treasurehunt.utils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQL {

    private static final String USERS_DB = "users";
    private static final String LISTINGS_DB = "listings";
    private static final String SAVED_RECORDS_DB = "saved_records";
    private static final Logger logger = LoggerFactory.getLogger(MySQL.class);

    // Create user in users db
    public static boolean addUser(Connection conn, User user) {

        // Insert the new data to users db
        String sql = String.format("INSERT IGNORE INTO %s VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", USERS_DB);

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, user.getUserId());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getPasswordSalt());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getEmail());
            statement.setString(7, user.getAddress());
            statement.setString(8, new ObjectMapper().writeValueAsString(user.getGeocodeLocation()));
            statement.setString(9, user.getCityAndState());

            return statement.executeUpdate() == 1;
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            logger.warn("Failed to add {} to users db", user.getUserId());
            return false;
        }
    }

    // Create new listing in listings db
    public static boolean createListing(Connection conn, Listing listing) throws MySQLException {

        // Insert the new data to listings db
        String sql = String.format("INSERT INTO %s VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", LISTINGS_DB);

        try (PreparedStatement postListing = conn.prepareStatement(sql)) {

            ObjectMapper objectMapper = new ObjectMapper();

            postListing.setString(1, listing.getListingId());
            postListing.setString(2, listing.getTitle());
            postListing.setDouble(3, listing.getPrice());
            postListing.setString(4, listing.getCategory());
            postListing.setString(5, listing.getDescription());
            postListing.setString(6, listing.getItemCondition());
            postListing.setString(7, listing.getBrand());
            postListing.setString(8, objectMapper.writeValueAsString(listing.getPictureUrls()));
            postListing.setString(9, listing.getSellerId());
            postListing.setString(10, listing.getSellerName());
            postListing.setString(11, listing.getSellerEmail());
            postListing.setString(12, listing.getAddress());
            postListing.setString(13, listing.getDate());
            postListing.setString(14, objectMapper.writeValueAsString(listing.getGeocodeLocation()));
            postListing.setString(15, listing.getCityAndState());

            return postListing.executeUpdate() == 1;

        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            logger.warn("Failed to add the following listing to listings db: {}", listing.getTitle());
            return false;
        }
    }

    // Update an existing listing in listings db
    public static boolean updateListing(Connection conn, Listing listing) throws MySQLException {

        String sql = String.format("UPDATE %s " +
                "SET title = ?, "
                + "price = ?, "
                + "category = ?, "
                + "description = ?, "
                + "item_condition = ?, "
                + "brand = ?, "
                + "date = ?, "
                + "picture_urls = ? "
                + "WHERE listing_id = ? "
                + "AND seller_id = ?", LISTINGS_DB);

        try (PreparedStatement updateListing = conn.prepareStatement(sql)) {
            updateListing.setString(1, listing.getTitle());
            updateListing.setDouble(2, listing.getPrice());
            updateListing.setString(3, listing.getCategory());
            updateListing.setString(4, listing.getDescription());
            updateListing.setString(5, listing.getItemCondition());
            updateListing.setString(6, listing.getBrand());
            updateListing.setString(7, listing.getDate());
            updateListing.setString(8, new ObjectMapper().writeValueAsString(listing.getPictureUrls()));
            updateListing.setString(9, listing.getListingId());
            updateListing.setString(10, listing.getSellerId());

            return updateListing.executeUpdate() == 1;
        } catch (SQLException | JsonProcessingException e) {
            logger.warn("Failed to update listing", e);
            return false;
        }
    }

    // Delete an existing listing in listings db
    public static void deleteListing(Connection conn, String sellerId, String listingId) throws MySQLException {
        // TODO: Last edited by Ruichen

        try {
            String sql = "DELETE FROM listings WHERE seller_id = ? AND listing_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, sellerId);
            statement.setString(2, listingId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete Listing");
        }
    }

    // Get listings created by user
    public static List<Listing> getMyListings(Connection conn, String userId) throws MySQLException {
        // TODO: Last edited by Ruichen

        List<Listing> myListings = new ArrayList<>();

        try {
            // query from listings DB
            String sql = "SELECT listing_id, picture_urls, title, price, date, description "
                    + "FROM listings "
                    + "WHERE seller_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);

            // Add listings in ResultSet to myListings
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Listing.Builder builder = new Listing.Builder();
                builder.setListingId(rs.getString("listing_id"))
                       .setPictureUrls(DbUtils.readPictureUrls(rs.getString("picture_urls")))
                       .setTitle(rs.getString("title"))
                       .setPrice(rs.getDouble("price"))
                       .setDate(rs.getString("date"))
                       .setDescription(rs.getString("description"));
                myListings.add(builder.build());
            }
            return myListings;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get my listings");
        }
    }

    // Get listings saved by user
    public static List<Listing> getSavedListings(Connection conn, String userId) throws MySQLException {

        List<Listing> savedListings = new ArrayList<>();

        try {
            // query from listings DB
            String sql = "SELECT listings.listing_id, picture_urls, title, price, city_and_state, description "
                    + "FROM listings "
                    + "INNER JOIN saved_records ON saved_records.listing_id = listings.listing_id "
                    + "WHERE saved_records.user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);

            // Add listings in ResultSet to myListings
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Listing.Builder builder = new Listing.Builder();
                builder.setListingId(rs.getString("listing_id"))
                       .setPictureUrls(DbUtils.readPictureUrls(rs.getString("picture_urls")))
                       .setTitle(rs.getString("title"))
                       .setPrice(rs.getDouble("price"))
                       .setCityAndState(rs.getString("city_and_state"))
                       .setDescription(rs.getString("description"));
                savedListings.add(builder.build());
            }
            return savedListings;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get my listings");
        }
    }

    // Create a saved record in saved_records
    public static void saveListing(Connection conn, String userId, String listingId) throws MySQLException {
        try {
            // Build and execute SQL statement
            String sql = String.format("INSERT IGNORE INTO %s (user_id, listing_id) VALUES (?, ?)", SAVED_RECORDS_DB);
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, listingId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to save Listing");
        }
    }

    // Delete a saved record in saved_records
    public static void unsaveListing(Connection conn, String userId, String listingId) throws MySQLException {
        // TODO: Last edited by Ruichen

        try {
            String sql = String.format("DELETE FROM %s WHERE user_id = ? AND listing_id = ?", SAVED_RECORDS_DB);
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, listingId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to unsave Listing");
        }
    }

    // Get Listing from listings db
    public static Listing getListing(Connection conn, String listingId) throws MySQLException, SQLException {
        Listing listing = new Listing();

        String sql = "SELECT * FROM listings WHERE listing_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, listingId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Listing.Builder builder = new Listing.Builder();
                builder.setListingId(listingId).setTitle(rs.getString("title"))
                       .setPrice(rs.getDouble("price"))
                       .setCategory(rs.getString("category"))
                       .setDescription(rs.getString("description"))
                       .setItemCondition(rs.getString("item_condition"))
                       .setBrand(rs.getString("brand"))
                       .setPictureUrls(DbUtils.readPictureUrls(rs.getString("picture_urls")))
                       .setSellerId(rs.getString("seller_id"))
                       .setSellerName(rs.getString("seller_name"))
                       .setSellerEmail(rs.getString("seller_email"))
                       .setAddress(rs.getString("address"))
                       .setDate(rs.getString("date"))
                       .setGeocodeLocation(new ObjectMapper()
                               .readValue(rs.getString("geo_location"), GeocodeLocation.class))
                       .setCityAndState(rs.getString("city_and_state"));
                listing = builder.build();
                return listing;
            } else {
                throw new MySQLException("Listing does not exist");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new SQLException("Failed to get listing from DB");
        }
    }

    public static User getUser(Connection conn, String userId) throws MySQLException {

        String sql = String.format("SELECT * FROM %s WHERE user_id = ?", USERS_DB);

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new User.Builder()
                        .userId(rs.getString("user_id"))
                        .password(rs.getString("password"))
                        .passwordSalt(rs.getString("password_salt"))
                        .firstName(rs.getString("first_name"))
                        .lastName(rs.getString("last_name"))
                        .email(rs.getString("email"))
                        .address(rs.getString("address"))
                        .geocodeLocation(new ObjectMapper()
                                .readValue(rs.getString("geo_location"), GeocodeLocation.class))
                        .cityAndState(rs.getString("city_and_state"))
                        .build();
            } else {
                logger.info("User does not exist");
                return null;
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            logger.warn("Unable to find user {} in users db", userId);
            throw new MySQLException("Failed to find user in users db");
        }
    }

    // After seller deletes an listing, delete all related saved_records
    public static void deleteAllSavedRecords(Connection conn, String listingId) throws MySQLException {

        try {
            String sql = String.format("DELETE FROM %s WHERE listing_id = ?", SAVED_RECORDS_DB);
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, listingId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete saved records");
        }
    }
}
