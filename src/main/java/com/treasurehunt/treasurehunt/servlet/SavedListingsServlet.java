package com.treasurehunt.treasurehunt.servlet;

import com.treasurehunt.treasurehunt.db.mysql.MySQL;
import com.treasurehunt.treasurehunt.entity.Listing;
import com.treasurehunt.treasurehunt.utils.JwtTokenMissingException;
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
import java.util.List;

@WebServlet(name = "SavedListingsServlet", urlPatterns = {"/api/saved-listings"})
public class SavedListingsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SavedListingsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // Verify token, and get userId in token
        String authorizedUserId;
        try {
            authorizedUserId = ServletUtil.getAuthorizedUserIdFromRequest(request);
        } catch (JwtTokenMissingException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        List<Listing> savedListings;
        // Retrieve list of listings from MySQL
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");

        try (Connection conn = pool.getConnection()) {
            savedListings = MySQL.getSavedListings(conn, authorizedUserId);
            ServletUtil.writeListings(response, savedListings);

        } catch (SQLException e) {
            logger.warn("Error while attempting to get listings posted by {}", authorizedUserId);
            response.setStatus(500);
            response.getWriter().write("Unable to get listings created by user");
        }
    }
}
