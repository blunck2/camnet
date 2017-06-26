package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
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

import camnet.model.ImagePostResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImagePublisher {
	private String restEndpoint;
	private String userName;
	private String password;

	private RestTemplate template;

	private Camera camera;

	private ObjectMapper mapper;

	private static final Logger logger = LogManager.getLogger();


	public ImagePublisher(String restEndpoint, String userName, String password) {
		this.restEndpoint = restEndpoint;
		this.userName = userName;
		this.password = password;

		template = new RestTemplate();
		ClientHttpRequestInterceptor loggingRequestInterceptor = new LoggingRequestInterceptor();
		List<ClientHttpRequestInterceptor> requestInterceptors = new ArrayList<>();
		requestInterceptors.add(loggingRequestInterceptor);
		requestInterceptors.add(new BasicAuthorizationInterceptor(this.userName, this.password));
		template.setInterceptors(requestInterceptors);
		mapper = new ObjectMapper();
	}

	public String getRestEndpoint() { return restEndpoint; }


 	public Camera publishImage(Camera camera, byte[] image, Map<String, String> sourceImageHeaders) throws ImagePublishingException {
		String houseName = camera.getHouseName();
		String cameraId = camera.getId();

		String url = restEndpoint + "/image/ingest/house/" + houseName + "/camera/" + cameraId;

		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		ByteArrayResource bar = new ByteArrayResource(image) {
        	@Override
        	public String getFilename() {
            	return "Camera-" + cameraId + ".jpg";
        	}
    	};
		map.add("file", bar);
		//map.add("contentType", sourceImageHeaders.get("Content-Type"));

		//logger.info("my map: " + map.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = 
			new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);

		logger.trace(camera.getDisplayName() + " POSTing image");
		ResponseEntity<String> result =
			template.exchange(url, 
							  HttpMethod.POST, 
							  requestEntity,
                    		  String.class);
		logger.trace(camera.getDisplayName() + " back from POST");

		if (result.getStatusCode() != HttpStatus.OK) {
			logger.trace(camera.getDisplayName() + " http status code was not 200");
			throw new ImagePublishingException(result.toString());
		}

		String json = result.getBody();

		ImagePostResponse response = null;
		try {
			response = mapper.readValue(json, ImagePostResponse.class);
		} catch (IOException e) {
			throw new ImagePublishingException("failed to publish image", e);
		} catch (Throwable t) {
			throw new ImagePublishingException("failed to publish image", t);
		}

		camera.setSleepTimeInSeconds(response.getSleepTimeInSeconds());
		return camera;
	}

}