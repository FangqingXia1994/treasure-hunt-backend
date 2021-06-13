package com.treasurehunt.treasurehunt.db.gcs;

import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class GCS {

    // Deployment: change for deployment
    private static final String bucketName = "treasure-hunt-listing-pictures";
    private static final Logger logger = LoggerFactory.getLogger(GCS.class);

    // Upload picture to listing-pictures bucket in GCS
    public static String uploadPicture(Storage storage, String pictureId, InputStream fileInputStream) throws GCSException {
        try {
            // Create blobId and blobInfo
            BlobId blobId = BlobId.of(bucketName, pictureId);
            BlobInfo blobInfo = BlobInfo
                    .newBuilder(blobId)
                    .build();

            // Upload blob into GCS
            Blob blob = storage.createFrom(blobInfo, fileInputStream);

            // Return the media link of the uploaded blob
            logger.info("Picture uploaded to {} as {}", bucketName, pictureId);
            return blob.getMediaLink();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GCSException(String.format("Failed to upload %s to %s", pictureId, bucketName));
        }
    }

    // Remove uploaded picture from listing-pictures bucket in GCS
    public static void deletePicture(Storage storage, String pictureId) {
        try {

            // Delete picture from picture bucket
            storage.delete(bucketName, pictureId);

            logger.info("Picture {} was deleted from {}", pictureId, bucketName);
        } catch (StorageException e) {
            logger.warn("Failed to delete {} from {}", pictureId, bucketName);
        }
    }
}
