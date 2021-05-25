package com.treasurehunt.treasurehunt.db;

import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebListener("Creates a connection pool that is stored in the Servlet's context for later use")
public class ConnectionPoolContextListener implements ServletContextListener {

    private void createTable(DataSource pool) throws SQLException {
        // Safely attempt to create the table schema.
        try (Connection conn = pool.getConnection()) {
            String stmt = "CREATE TABLE IF NOT EXISTS users ( "
                    + "user_id VARCHAR(255) NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "first_name VARCHAR(255) NOT NULL, "
                    + "last_name VARCHAR(255) NOT NULL, "
                    + "email VARCHAR(255) NOT NULL, "
                    + "address VARCHAR(255), "
                    + "PRIMARY KEY (user_id) "
                    + ")";
            try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
                createTableStatement.execute();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        HikariDataSource pool = (HikariDataSource) event.getServletContext().getAttribute("my-pool");
        if (pool != null) {
            pool.close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        DataSource pool = (DataSource) servletContext.getAttribute("my-pool");
        if (pool == null) {
            try {
                MySQLConnectionPool mySQLConnectionPool = new MySQLConnectionPool();
                pool = mySQLConnectionPool.pool;
                servletContext.setAttribute("my-pool", pool);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to fetch MySQL login credentials");
            }
        }
        try {
            createTable(pool);
            System.out.println("Successfully created connection to MySQL");
        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Unable to verify table schema, please double check and try again.",
                    ex);

        }
    }
}