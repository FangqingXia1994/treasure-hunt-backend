package com.treasurehunt.treasurehunt.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;
import com.treasurehunt.treasurehunt.db.elasticsearch.Elasticsearch;
import com.treasurehunt.treasurehunt.db.mysql.MySQL;
import com.treasurehunt.treasurehunt.db.mysql.MySQLException;
import com.treasurehunt.treasurehunt.entity.DeleteListingRequestBody;
import com.treasurehunt.treasurehunt.entity.Listing;
import com.treasurehunt.treasurehunt.entity.User;
import com.treasurehunt.treasurehunt.utils.JwtTokenMissingException;
import com.treasurehunt.treasurehunt.utils.ServletUtil;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;

@MultipartConfig
@WebServlet(name = "ListingServlet", urlPatterns = {"/api/listing"})
public class ListingServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ListingServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // Verify token
        String authorizedUserId;
        try {
            authorizedUserId = ServletUtil.getAuthorizedUserIdFromRequest(request);
        } catch (JwtTokenMissingException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }
        // Get sellerId from request body as foreign key
        String sellerId = request.getParameter("seller_user_id");
        // Verify the two id's are equal
        if (!authorizedUserId.equals(sellerId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Invalid token");
            return;
        }

        // Get UUID as ListingId
        String listingId = String.valueOf(System.currentTimeMillis());

        // Upload pictures and get urls
        LinkedHashMap<String, String> pictureUrls = new LinkedHashMap<>();
        Storage storage = (Storage) request.getServletContext().getAttribute("gcs-client");

        // Upload new pictures to GCS
        ServletUtil.uploadNewPictures(storage, pictureUrls, request);

        // Connect to MySQL
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");

        // Get fullName, address, and geolocation of seller from userDB
        User user = null;
        try (Connection conn = pool.getConnection()) {
            user = MySQL.getUser(conn, sellerId);
        } catch (SQLException e) {
            logger.warn("Error while attempting to add new listing to MySQL db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully create listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        // return if cannot find user in user db
        if (user == null) {
            logger.warn("Cannot find seller's info {} in user db", sellerId);
            response.setStatus(500);
            response.getWriter().write("Cannot locate user information in the database");
            return;
        }

        // Read info from request body, and add fullName, address, and geolocation of seller
        Listing.Builder builder = new Listing.Builder();
        builder.setListingId(listingId)
               .setTitle(request.getParameter("title"))
               .setPrice(Double.parseDouble(request.getParameter("price")))
               .setCategory(request.getParameter("category"))
               .setSellerId(sellerId)
               .setDescription(request.getParameter("description"))
               .setItemCondition(request.getParameter("item_condition"))
               .setBrand(request.getParameter("brand"))
               .setPictureUrls(pictureUrls)
               .setSellerName(String.format("%s %s", user.getFirstName(), user.getLastName()))
               .setSellerEmail(user.getEmail())
               .setAddress(user.getAddress())
               .setGeocodeLocation(user.getGeocodeLocation())
               .setCityAndState(user.getCityAndState())
               .setDate(Instant.now().toString());

        // Build a java object which contains all listing info
        Listing listing = builder.build();

        boolean isListingAdded = false;
        // Add listing obj to MySQL database
        try (Connection conn = pool.getConnection()) {
            isListingAdded = MySQL.createListing(conn, listing);
        } catch (SQLException e) {
            logger.warn("Error while attempting to add new listing to MySQL db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully create listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        if (!isListingAdded) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().print("Unable to create listing");
            return;
        }

        // Add listing to Elasticsearch
        RestHighLevelClient elasticsearchClient = (RestHighLevelClient) request.getServletContext()
                                                                               .getAttribute("es-client");
        try {
            Elasticsearch.addListing(elasticsearchClient, listing);
        } catch (ElasticsearchException e) {
            logger.warn("Error while attempting to add new listing to Elasticsearch db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully create listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        // ListingId is return as the respondBody
        // so no need to serialize Java objects into JSON string
        response.setStatus(201);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(listingId);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        String listingId = request.getParameter("listing_id");
        Listing listing;

        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");
        try (Connection conn = pool.getConnection()) {
            listing = MySQL.getListing(conn, listingId);
        } catch (MySQLException e) {
            logger.info("Listing not found: {}", listingId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Listing not found!");
            return;
        } catch (SQLException e) {
            logger.warn("Error while attempting to get listing from MySQL db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully get listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(listing));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        String authorizedUserId;
        try {
            authorizedUserId = ServletUtil.getAuthorizedUserIdFromRequest(request);
        } catch (JwtTokenMissingException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }
        // Get sellerId from request param as foreign key
        String sellerId = request.getParameter("seller_user_id");
        // Verify the two id's are equal
        if (!authorizedUserId.equals(sellerId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Invalid token");
            return;
        }

        // Get existing listing from database
        String listingId = request.getParameter("listing_id");
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");
        Listing oldListing = new Listing();
        try (Connection conn = pool.getConnection()) {
            oldListing = MySQL.getListing(conn, listingId);
        } catch (SQLException e) {
            logger.warn("Error while attempting to get original listing from MySQL db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully get listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        // Check if existing seller_id is the same as the seller_id, if not, set unauthorized
        if (!sellerId.equals(oldListing.getSellerId())) {
            logger.info("Seller ids do not match {}, {}", sellerId, oldListing.getSellerId());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unable to edit someone else's listing!");
            return;
        }

        // Delete existing pictures from GCS
        Storage storage = (Storage) request.getServletContext().getAttribute("gcs-client");
        ServletUtil.deleteExistingPictures(storage, oldListing.getPictureUrls());
        // Add new pictures to GCS
        LinkedHashMap<String, String> newPictureUrls = new LinkedHashMap<>();
        ServletUtil.uploadNewPictures(storage, newPictureUrls, request);

        // Read info from request body, and add fullName, address, and geolocation of seller
        Listing.Builder builder = new Listing.Builder();
        builder.setListingId(listingId)
               .setSellerId(sellerId)
               .setTitle(request.getParameter("title"))
               .setPrice(Double.parseDouble(request.getParameter("price")))
               .setCategory(request.getParameter("category"))
               .setDescription(request.getParameter("description"))
               .setItemCondition(request.getParameter("item_condition"))
               .setBrand(request.getParameter("brand"))
               .setPictureUrls(newPictureUrls)
               .setDate(Instant.now().toString());

        // Build a java object which contains all listing info
        Listing updatedListing = builder.build();

        boolean isListingUpdated = false;
        // Update listing obj in MySQL database
        try (Connection conn = pool.getConnection()) {
            isListingUpdated = MySQL.updateListing(conn, updatedListing);
        } catch (SQLException e) {
            logger.warn("Error while attempting to update listing to MySQL db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully create listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        if (!isListingUpdated) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().print("Unable to update listing");
            return;
        }

        // Update listing in Elasticsearch
        RestHighLevelClient elasticsearchClient = (RestHighLevelClient) request.getServletContext()
                                                                               .getAttribute("es-client");
        try {
            Elasticsearch.updateListing(elasticsearchClient, updatedListing);
        } catch (ElasticsearchException e) {
            logger.warn("Error while attempting to add new listing to Elasticsearch db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully create listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        // ListingId is return as the respondBody
        // so no need to serialize Java objects into JSON string
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(listingId);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // Get authorizedUserId from token
        String authorizedUserId;
        try {
            authorizedUserId = ServletUtil.getAuthorizedUserIdFromRequest(request);
        } catch (JwtTokenMissingException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        // Parse request body
        DeleteListingRequestBody body = ServletUtil.readRequestBody(DeleteListingRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // Verify the two id's are equal
        if (!authorizedUserId.equals(body.getUserId())) {
            logger.warn("Unauthorized: user_id {} is not the same as authorized user {}", body
                    .getUserId(), authorizedUserId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Token invalid");
            return;
        }

        String listingId = body.getListingId();
        Listing listing = new Listing();

        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");

        // First get the listing, we need this to propagate changes to ES and GCS
        try (Connection conn = pool.getConnection()) {
            listing = MySQL.getListing(conn, listingId);
        } catch (SQLException e) {
            logger.warn("Error while attempting to find listing from MySQL db", e);
            response.setStatus(500);
            response.getWriter().write("Unable to successfully get listing! Please check the application logs for " +
                    "more details.");
            return;
        }

        // Attempt to delete the listing from MySQL
        String userId = body.getUserId();
        try (Connection conn = pool.getConnection()) {
            // delete listing from MySQL
            MySQL.deleteListing(conn, userId, listingId);
            // delete all related saved-records from MySQL
            MySQL.deleteAllSavedRecords(conn, listingId);
        } catch (SQLException e) {
            logger.warn("Error while attempting to delete listing from MySQL db", e);
            response.setStatus(500);
            response.getWriter()
                    .write("Unable to successfully delete listing! Please check the application logs for more details");
            return;
        }

        // delete from ES
        RestHighLevelClient elasticsearchClient = (RestHighLevelClient) request.getServletContext()
                                                                               .getAttribute("es-client");
        Elasticsearch.deleteListing(elasticsearchClient, listing);

        // delete from GCS
        Storage storage = (Storage) request.getServletContext().getAttribute("gcs-client");
        ServletUtil.deleteExistingPictures(storage, listing.getPictureUrls());

        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print("Successfully deleted a listing!");
    }
}
