package com.treasurehunt.treasurehunt.db.gcs;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebListener("Creates a GCS client that is stored in the Servlet's context for later use via attribute gcs-client")
public class GCSClientContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(GCSClientContextListener.class);

    private Storage createGCSClient() throws IOException {
        // get google_cloud_project_id from properties
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = GCSClientContextListener.class
                .getClassLoader()
                .getResourceAsStream(propFileName);
        prop.load(inputStream);

        String google_cloud_project_id = prop.getProperty("google_cloud_project_id");
        return StorageOptions
                .newBuilder()
                .setProjectId(google_cloud_project_id)
                .build()
                .getService();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // This function is called when the application starts and will safely create a GCS client that can be used
        // to connect to
        ServletContext servletContext = event.getServletContext();
        Storage storage = (Storage) servletContext.getAttribute("gcs-client");
        if (storage == null) {
            try {
                storage = createGCSClient();
                servletContext.setAttribute("gcs-client", storage);
                logger.info("Successfully created connection to GCS");
            } catch (Exception e) {
                throw new RuntimeException("Unable to connect to GCS. Please double check config and try again.");
            }
        }
    }
}
