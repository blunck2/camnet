package camnet.server.media.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import camnet.server.media.processor.LocalImageProcessor;
import camnet.server.media.processor.ImageProcessingException;
import camnet.server.media.processor.S3ImageProcessor;

import camnet.model.ImagePostResponse;

import camnet.model.CameraManifest;
import camnet.model.Camera;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/image")
@ConfigurationProperties("imageController")
public class ImageController {
	private String configurationServerUrl;

	private CameraManifest manifest;

	private RestTemplate configServer;

	@Autowired
	private LocalImageProcessor localImageProcessor;

	@Autowired
	private S3ImageProcessor s3ImageProcessor;

	private Logger logger = LogManager.getLogger();

	@PostConstruct
	public void setUp() {
		loadManifest();
	}

	public String getConfigurationServerUrl() {
		return configurationServerUrl;
	}

	public void setConfigurationServerUrl(String configurationServerUrl) {
		this.configurationServerUrl = configurationServerUrl;
	}


	/**
	 * Retrieves the camera manifest from the configuration server
	 */
	private void loadManifest() {
		manifest = new CameraManifest();

		configServer = new RestTemplate();
		logger.trace("retrieving cameras from configuration server");
		ResponseEntity<Camera[]> responseEntity = configServer.getForEntity(configurationServerUrl, Camera[].class);
		List<Camera> cameras = new ArrayList<>();
		for (Camera camera : responseEntity.getBody()) {
			manifest.addCamera(camera);
		}

		logger.trace("********************** camera configurations loaded.  house names: " + manifest.getHouseNames());
	}


	private Camera parse(Map raw) {
		Camera cooked = new Camera();

		cooked.setId((String) raw.get("id"));
		cooked.setHouseName((String) raw.get("houseName"));
		cooked.setCameraName((String) raw.get("cameraName"));
		cooked.setUrl((String) raw.get("url"));
		cooked.setUserName((String) raw.get("userName"));
		cooked.setPassword((String) raw.get("passWord"));
		cooked.setSleepTimeInSeconds((Integer) raw.get("sleepTimeInSeconds"));

		return cooked;
	}


	@RequestMapping("/cameras")
	public List<Camera> getAllCameras() {
		if (manifest == null) {
			return new ArrayList<>();
		} else {
			return manifest.getAllCameras();
		}
	}


	@PostMapping("/ingest/house/{houseName}/camera/{cameraId}")
	public ImagePostResponse ingest(@RequestParam("file") MultipartFile file,
																	@PathVariable("houseName") String houseName,
																	@PathVariable("cameraId") String cameraId) {
		logger.info("incoming image: " + houseName + "/" + cameraId);

		// error out if the manifest is null
		if (manifest == null) {
			return createServerErrorResponse("null camera manifest");
		}

		// error out if the localImageProcessor is null
		if (localImageProcessor == null) {
			return createServerErrorResponse("null localImageProcessor");
		}

		// error out if the camera has not been defined
		Camera camera = manifest.getCameraById(houseName, cameraId);
		if (camera == null) {
			return createServerErrorResponse("house name '" + houseName + "' and camera id '" +
					cameraId + "' not found");
		}

		Map<String, String> fileHeaders = new HashMap<>();
		fileHeaders.put("contentType", "image/jpeg");

		int byteCount = 0;
		try {
			byteCount = localImageProcessor.processImage(camera, file, fileHeaders);
			s3ImageProcessor.processImage(camera, file, fileHeaders);
		} catch (ImageProcessingException e) {
			logger.error("error occurred processing image", e);

			ImagePostResponse response = new ImagePostResponse();
			response.setCode(1);
			response.setMessage(e.getMessage());
			response.setSleepTimeInSeconds(60);
			return response;
		}

		// nominal response
		ImagePostResponse response = new ImagePostResponse();
		response.setCode(0);
		response.setMessage("wrote image: " + byteCount + " bytes");
		response.setSleepTimeInSeconds(camera.getSleepTimeInSeconds());

		return response;
	}


	private ImagePostResponse createServerErrorResponse(String message) {
		ImagePostResponse response = new ImagePostResponse();
		response.setCode(1);
		response.setMessage("server error occurred: " + message);
		response.setSleepTimeInSeconds(300);

		return response;
	}
}
