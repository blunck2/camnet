package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import camnet.common.model.ImagePostResponse;

import org.apache.log4j.Logger;

import java.io.IOException;

public class ImagePublisher {
	private String restEndpoint;

	private RestTemplate template;

	private Camera camera;

	private ObjectMapper mapper;

	private static final Logger logger = Logger.getLogger(ImagePublisher.class);


	public ImagePublisher(String restEndpoint, Camera camera) {
		template = new RestTemplate();
		this.camera = camera;
		this.restEndpoint = restEndpoint;
		mapper = new ObjectMapper();
	}

	public String getRestEndpoint() { return restEndpoint; }


	public Camera publishImage(byte[] image) throws ImagePublishingException {
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

		logger.info("publishing image for camera: " + cameraId);

		ResponseEntity<String> result =
			template.exchange(url, 
							  HttpMethod.POST, 
							  requestEntity,
                    		  String.class);

		if (result.getStatusCode() != HttpStatus.OK) {
			throw new ImagePublishingException(result.toString());
		}

		String json = result.getBody();

		ImagePostResponse response;
		try {
			response = mapper.readValue(json, ImagePostResponse.class);
		} catch (IOException e) {
			logger.error("failed to marshal response json string to object", e);
			return camera;
		}

		logger.info("sleep time for next retrieval: " + response.getSleepTimeInSeconds());
		camera.setSleepTimeInSeconds(response.getSleepTimeInSeconds());

		return camera;
	}

}