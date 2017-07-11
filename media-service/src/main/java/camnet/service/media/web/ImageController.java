package camnet.service.media.web;

import camnet.service.media.processor.ImageProcessingException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import camnet.service.media.processor.LocalImageProcessor;
import camnet.service.media.processor.S3ImageProcessor;

import camnet.model.ImagePostResponse;

import camnet.model.CameraManifest;
import camnet.model.Camera;
import camnet.model.TrackerServiceEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/image")
@ConfigurationProperties("imageController")
public class ImageController {
	private String configurationServiceUrl;
	private String configurationServiceUserName;
	private String configurationServicePassWord;

	private List<TrackerServiceEndpoint> trackerServiceEndpoints;

	private CameraManifest manifest;

	private RestTemplate configService;
	private RestTemplate trackerService;

	@Autowired
	private LocalImageProcessor localImageProcessor;

	@Autowired
	private S3ImageProcessor s3ImageProcessor;

	private Logger logger = LoggerFactory.getLogger(ImageController.class);

	@PostConstruct
	public void setUp() {
		loadCameraManifest();
	}

	public String getConfigurationServiceUrl() {
		return configurationServiceUrl;
	}

	public void setConfigurationServiceUrl(String configurationServiceUrl) {
		this.configurationServiceUrl = configurationServiceUrl;
	}

	public String getConfigurationServiceUserName() {
		return configurationServiceUserName;
	}

	public void setConfigurationServiceUserName(String userName) {
		this.configurationServiceUserName = userName;
	}

	public String getConfigurationServerPassword() {
		return configurationServicePassWord;
	}

	public void setConfigurationServicePassWord(String passWord) {
		this.configurationServicePassWord = passWord;
	}

	/**
	 * Retrieves the camera manifest from the configuration server
	 */
	private void loadCameraManifest() {
		manifest = new CameraManifest();

		configService = new RestTemplate();
		logger.trace("retrieving tracker service endpoint from configuration service");
		ResponseEntity<TrackerServiceEndpoint[]> trackerResponseEntity = configService.getForEntity(configurationServiceUrl, TrackerServiceEndpoint[].class);
		trackerServiceEndpoints = new ArrayList<>();
		int count = 0;
		for (TrackerServiceEndpoint trackerServiceEndpoint : trackerResponseEntity.getBody()) {
			trackerServiceEndpoints.add(trackerServiceEndpoint);
			count++;
		}

		// FIXME: add failover to other tracker service endpoints
		TrackerServiceEndpoint trackerServiceEndpoint = trackerServiceEndpoints.get(0);
		String trackerServiceEndpointUrl = trackerServiceEndpoint.getUrl();

		trackerService = new RestTemplate();
		logger.trace("retrieving cameras from tracker service");
		ResponseEntity<Camera[]> responseEntity = trackerService.getForEntity(trackerServiceEndpointUrl + "/manifest/cameras", Camera[].class);
		List<Camera> cameras = new ArrayList<>();
		count = 0;
		for (Camera camera : responseEntity.getBody()) {
			manifest.addCamera(camera);
			count++;
		}

		logger.trace(count + " camera configurations loaded.  house names: " + manifest.getEnvironments());
	}


	@RequestMapping("/cameras")
	public List<Camera> getAllCameras() {
		if (manifest == null) {
			return new ArrayList<>();
		} else {
			return manifest.getAllCameras();
		}
	}


	@PostMapping("/ingest/environment/{environment}/camera/{cameraId}")
	public ImagePostResponse ingest(@RequestParam("file") MultipartFile file,
																	@PathVariable("environment") String environment,
																	@PathVariable("cameraId") String cameraId) {
		logger.info("incoming image: " + environment + "/" + cameraId);

		// error out if the manifest is null
		if (manifest == null) {
			return createServerErrorResponse("null camera manifest");
		}

		// error out if the localImageProcessor is null
		if (localImageProcessor == null) {
			return createServerErrorResponse("null localImageProcessor");
		}

		// error out if the camera has not been defined
		Camera camera = manifest.getCameraById(environment, cameraId);
		if (camera == null) {
			return createServerErrorResponse("environment name '" + environment+ "' and camera id '" +
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
