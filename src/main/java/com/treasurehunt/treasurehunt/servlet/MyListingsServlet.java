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

@WebServlet(name = "MyListingsServlet", urlPatterns = {"/api/my-listings"})
public class MyListingsServlet extends HttpServlet {
    // TODO: Last edited by Ruichen

    private static final Logger logger = LoggerFactory.getLogger(MyListingsServlet.class);

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

        List<Listing> myListings;
        // Retrieve list of listings from MySQL
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");

        try (Connection conn = pool.getConnection()) {
            myListings = MySQL.getMyListings(conn, authorizedUserId);
            ServletUtil.writeListings(response, myListings);

        } catch (SQLException e) {
            logger.warn("Error while attempting to get listings posted by {}", authorizedUserId);
            response.setStatus(500);
            response.getWriter().write("Unable to get listings created by user");
        }
    }
}
