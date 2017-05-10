package camnet.server.processor;


import camnet.server.model.Camera;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;


@Component
@ConfigurationProperties(prefix="s3ImageProcessor")
public class S3ImageProcessor implements ImageProcessor {

    private static final Logger logger = Logger.getLogger(S3ImageProcessor.class);

    private String accessKey;
    private String secretKey;
    private String serviceEndpoint;
    private String bucketName;

    private AmazonS3Client client;


    public String getAccessKey() {
        return accessKey;
    }
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }
    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getBucketName() {
        return bucketName;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public AmazonWebServiceClient getClient() {
        return client;
    }
    public void setClient(AmazonS3Client client) {
        this.client = client;
    }

    public S3ImageProcessor() {
    }

    @PostConstruct
    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        client = new AmazonS3Client(credentials);
    }

    public int processImage(Camera camera, MultipartFile image) throws ImageProcessingException {
        String objectId = camera.getHouseName() + "-" + camera.getId();
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(image.getInputStream());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

            logger.info("uploading to: " + getBucketName() + "/" + objectId);
            client.putObject(getBucketName(), objectId, stream, metadata);
        } catch (Throwable t) {
            throw new ImageProcessingException("failed to send to s3", t);
        }

        return bytes.length;
    }

}
