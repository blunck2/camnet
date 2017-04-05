package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="imagePublisher")
public class ImagePublisher {
	public ImagePublisher() {}

	private String restEndpoint;

	public void setRestEndpoint(String restEndpoint) { this.restEndpoint = restEndpoint; }
	public String getRestEndpoint() { return restEndpoint; }


	public void publishImage(byte[] image, Camera camera) throws ImagePublishingException {
		String cameraId = camera.getId();

		// TODO:  RestTemplate post of byte[], get response and return
	}

}