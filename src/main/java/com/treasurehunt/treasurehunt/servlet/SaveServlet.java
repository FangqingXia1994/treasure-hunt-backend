package com.treasurehunt.treasurehunt.servlet;

import com.treasurehunt.treasurehunt.db.mysql.MySQL;
import com.treasurehunt.treasurehunt.entity.SaveRequestBody;
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

@WebServlet(name = "SaveServlet", urlPatterns = {"/api/save"})
public class SaveServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SaveServlet.class);

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

        // Read data from request
        SaveRequestBody body = ServletUtil.readRequestBody(SaveRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // Verify the two id's are equal
        if (!authorizedUserId.equals(body.getUserId())) {
            logger.warn("Unauthorized: user_id {} is not the same as authorized user {}", body
                    .getUserId(), authorizedUserId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Invalid token");
            return;
        }
        // Get a connection from MySQL connection pool
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");
        try (Connection conn = pool.getConnection()) {
            // Save the listing
            MySQL.saveListing(conn, body.getUserId(), body.getListingId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        response.setStatus(201);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
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

        // Read data from request
        SaveRequestBody body = ServletUtil.readRequestBody(SaveRequestBody.class, request);
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
        // Get a connection from MySQL connection pool
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");
        try (Connection conn = pool.getConnection()) {
            // Unsave the listing
            MySQL.unsaveListing(conn, body.getUserId(), body.getListingId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
