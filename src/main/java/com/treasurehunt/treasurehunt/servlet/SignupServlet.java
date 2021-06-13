package com.treasurehunt.treasurehunt.servlet;

import com.treasurehunt.treasurehunt.db.mysql.MySQL;
import com.treasurehunt.treasurehunt.entity.GeocodeResult;
import com.treasurehunt.treasurehunt.entity.User;
import com.treasurehunt.treasurehunt.external.GoogleMapsClient;
import com.treasurehunt.treasurehunt.external.GoogleMapsException;
import com.treasurehunt.treasurehunt.utils.PasswordUtils;
import com.treasurehunt.treasurehunt.utils.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "SignupServlet", urlPatterns = {"/api/signup"})
public class SignupServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SignupServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        // Read user data from the request body
        User user = ServletUtil.readRequestBody(User.class, request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.warn("Cannot parse signup request: {}", request);
            return;
        }

        String userId = user.getUserId();

        // Check if user_id already exists in MySQL db
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");
        try (Connection conn = pool.getConnection()) {
            User existingUser = MySQL.getUser(conn, userId);
            if (existingUser != null) {
                logger.warn("User {} already exists", userId);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().print(String.format("User ID %s already exists", userId));
                return;
            }
        } catch (SQLException e) {
            logger.warn("Cannot add {} to users db", userId);
            throw new ServletException(e);
        }

        // Generate and set secure password and salt for user
        PasswordUtils.generateSecurePasswordForUser(user);

        // Query for clean address and geoLocation from Google Maps Geocode API
        try {
            GeocodeResult geocodeResult = GoogleMapsClient.getGeocodeResult(user.getAddress());
            // Set formatted address and geocode location returned from Geocode API
            user.setAddress(geocodeResult.getFormattedAddress()).setGeocodeLocation(geocodeResult.getGeocodeLocation())
                .setCityAndState(geocodeResult.getCityAndState());
        } catch (GoogleMapsException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(String.format("Invalid address: %s%n", user.getAddress()));
            logger.warn("Geocode API unable to parse address: {}", user.getAddress());
            return;
        }

        // Save user in MySQL users db
        boolean isUserAdded;
        try (Connection conn = pool.getConnection()) {
            isUserAdded = MySQL.addUser(conn, user);
        } catch (SQLException e) {
            logger.warn("Cannot add {} to users db", userId);
            throw new ServletException(e);
        }

        // Notify if there is existing user_id
        if (!isUserAdded) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().print("user_id taken");
        }

        response.setStatus(201);
    }
}
