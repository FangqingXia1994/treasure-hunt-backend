package com.treasurehunt.treasurehunt.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasurehunt.treasurehunt.db.mysql.MySQL;
import com.treasurehunt.treasurehunt.entity.LoginRequestBody;
import com.treasurehunt.treasurehunt.entity.LoginResponseBody;
import com.treasurehunt.treasurehunt.entity.User;
import com.treasurehunt.treasurehunt.utils.AuthUtils;
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

@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        // Read user data and password from request
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Get user from MySQL db that corresponds to user_id
        User user = null;
        DataSource pool = (DataSource) request.getServletContext().getAttribute("mysql-pool");
        try (Connection conn = pool.getConnection()) {
            user = MySQL.getUser(conn, body.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Unsuccessful login attempt for user: {}", body.getUserId());
        }

        // If password does not match, send response
        if (user == null || !PasswordUtils
                .verifyUserPassword(body.getPassword(), user.getPassword(), user.getPasswordSalt())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // if match, generate token
        String token = AuthUtils.generateToken(user);
        // write token to response and send back
        LoginResponseBody loginResponseBody = new LoginResponseBody(token, user.getFirstName());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(loginResponseBody));
    }
}
