package camnet.service.media.processor;

import camnet.model.Camera;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

import java.util.Map;


@Component
@ConfigurationProperties(prefix="s3ImageProcessor")
public class S3ImageProcessor implements ImageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(S3ImageProcessor.class);

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
        client.setEndpoint(serviceEndpoint);
    }

    public int processImage(Camera camera, MultipartFile image, Map<String, String> imageHeaders) throws ImageProcessingException {
        String objectId = camera.getId();
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(image.getInputStream());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            String contentType = imageHeaders.get("contentType");
            metadata.setContentType(contentType);
            metadata.setContentDisposition(camera.getEnvironment() + "-" + camera.getId());
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

            AccessControlList acl = new AccessControlList();
            acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);

            logger.info("uploading to: " + getBucketName() + "/" + objectId);
            PutObjectRequest request = new PutObjectRequest(getBucketName(), objectId, stream, metadata);
            request.withAccessControlList(acl);

            client.putObject(request);

            String url = client.getResourceUrl(getBucketName(), objectId);
            logger.trace("url: " + url);
        } catch (Throwable t) {
            throw new ImageProcessingException("failed to send to s3", t);
        }

        return bytes.length;
    }

}
