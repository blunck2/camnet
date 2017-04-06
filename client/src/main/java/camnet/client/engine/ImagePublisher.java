package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;



@ConfigurationProperties(prefix="imagePublisher")
public class ImagePublisher {
	private String restEndpoint;

	private RestTemplate template;

	public ImagePublisher() {
		template = new RestTemplate();
	}	


	public void setRestEndpoint(String restEndpoint) { this.restEndpoint = restEndpoint; }
	public String getRestEndpoint() { return restEndpoint; }


	public Camera publishImage(byte[] image, Camera camera) throws ImagePublishingException {
		String cameraId = camera.getId();

		String url = restEndpoint + "/image/ingest/" + cameraId;

		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		ByteArrayResource bar = new ByteArrayResource(image) {
        	@Override
        	public String getFilename() {
            	return "Camera-" + cameraId + "-size-" + image.length + ".jpg";
        	}
    	};
		map.add("file", bar);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = 
			new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);

		ResponseEntity<String> result = 
			template.exchange(url, 
							  HttpMethod.POST, 
							  requestEntity,
                    		  String.class);

		if (result.getStatusCode() != HttpStatus.OK) {
			throw new ImagePublishingException(result.toString());
		}

		String responseJson = result.toString();
		System.out.println("response: " + responseJson);

		// TODO: parse responseJson, extract sleepTime, set in Camera instance and return
		return camera;
	}

}