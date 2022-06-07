package am.ejob.backend.api.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Service
public class S3FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3FileService.class);

    @Value("${s3.bucket.name}")
    String bucketName;

    private final AmazonS3 amazonS3;

    @Autowired
    public S3FileService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public boolean upload(File file) {


        LOGGER.debug("Getting Started with Amazon S3");

        try {

            LOGGER.debug("Uploading a new object to S3 from a file\n");

            this.amazonS3.putObject(new PutObjectRequest(bucketName, file.getName(), file));
        } catch (AmazonClientException ace) {
            LOGGER.error("Error Message: " + ace.getMessage());
            return false;
        }
        return true;
    }

    public InputStream download(String key) {
        try {
            S3Object object = this.amazonS3.getObject(bucketName, key);
            return object.getObjectContent();
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to download file [" + bucketName + "/" + key + "]", e);
        }
    }

    public String getPreSignedUrl(String key) {

        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return this.amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    public void delete(String key) {
        try {
            this.amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            LOGGER.debug("File deleted: {}", key);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to delete file [" + bucketName + "/" + key + "]", e);
        }
    }
}
