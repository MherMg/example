package am.ejob.backend.api.config;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Config.class);


    @Bean
    public AmazonS3 s3(@Value("${s3.access.key}") String accessKey,
                       @Value("${s3.secret.key}") String secretKey) {
        AWSCredentials credentials;
        try {
            credentials = new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return accessKey;
                }

                @Override
                public String getAWSSecretKey() {
                    return secretKey;
                }
            };
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials.properties from the credential profiles file. " +
                            "Please make sure that your credentials.properties file is at the correct " +
                            "location (~/.aws/credentials.properties), and is in valid format.",
                    e);
        }
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                "storage.yandexcloud.net", "ru-central1"
                        )
                )
                .build();


        LOGGER.debug("Getting Started with Amazon S3");
        return s3;
    }
}