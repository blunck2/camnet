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


	public ImagePublisher(String restEndpoint) {
		template = new RestTemplate();
		this.restEndpoint = restEndpoint;
		mapper = new ObjectMapper();
	}

	public String getRestEndpoint() { return restEndpoint; }


	public Camera publishImage(Camera camera, byte[] image) throws ImagePublishingException {
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

		String json = result.getBody();

		ImagePostResponse response;
		try {
			response = mapper.readValue(json, ImagePostResponse.class);
		} catch (IOException e) {
			logger.error("failed to marshal response json string to object", e);
			return camera;
		}
		camera.setSleepTimeInSeconds(response.getSleepTimeInSeconds());

		return camera;
	}

}