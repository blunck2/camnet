package camnet.client.engine;

import camnet.client.model.internal.Camera;
import camnet.client.model.internal.CameraManifest;

import camnet.model.MediaServiceEndpoint;
import camnet.model.TrackerServiceEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.*;
import java.lang.Runnable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.web.client.RestTemplate;


@Component
public class CameraPublishingEngine {
	@Autowired
	private CameraManifest manifest;

	@Value("${CameraPublishingEngine.configurationServiceUrl}")
	private String configurationServiceUrl;

	@Value("${CameraPublishingEngine.configurationServiceUserName}")
	private String configurationServiceUserName;

	@Value("${CameraPublishingEngine.configurationServicePassWord}")
	private String configurationServicePassword;

	private List<MediaServiceEndpoint> mediaServiceEndpoints;
	private List<TrackerServiceEndpoint> trackerServiceEndpoints;

	@Value("#{'${CameraPublishingEngine.environments}'.split(',')}")
	private List<String> environments;

	private RestTemplate configService;

	private List<Runnable> retrievers;

	private ScheduledExecutorService scheduler;

	private ImagePublisher publisher;

	private RestTemplate template;

	private ObjectMapper mapper;

	private Logger logger = LoggerFactory.getLogger(CameraPublishingEngine.class);


	public CameraPublishingEngine() {
		template = new RestTemplate();
		retrievers = new ArrayList<>();
	}

	public void setConfigurationServiceUrl(String restEndpoint) {
		this.configurationServiceUrl = restEndpoint;
	}

	public String getConfigurationServiceUrl() {
		return configurationServiceUrl;
	}

	public String getConfigurationServiceUserName() {
		return configurationServiceUserName;
	}

	public void setConfigurationServiceUserName(String userName) {
		this.configurationServiceUserName = userName;
	}

	public String getConfigurationServicePassword() {
		return configurationServicePassword;
	}

	public void setConfigurationServicePassword(String passWord) {
		this.configurationServicePassword = passWord;
	}

	public void setEnvironments(List<String> environments) { this.environments = environments; }
	public List<String> getEnvironments() { return environments; }

	private List<Camera> getCamerasForEnvironment(String environment) {

		// TODO: implement failover to backup trackers
		TrackerServiceEndpoint endpoint = trackerServiceEndpoints.get(0);

		String trackerServiceUrl = endpoint.getUrl();
		String trackerServiceUserName = endpoint.getUserName();
		String trackerServicePassWord = endpoint.getPassWord();
		String url = trackerServiceUrl + "/manifest/cameras/environment/" + environment;
		logger.info("retrieving camera manifests from: " + url);
//		template.getInterceptors().add(new BasicAuthorizationInterceptor(trackerServiceUserName, trackerServicePassWord));
		ResponseEntity<Camera[]> responseEntity = template.getForEntity(url, Camera[].class);
		List<Camera> cameras = new ArrayList<>();
		for (Camera camera : responseEntity.getBody()) {
			cameras.add(camera);
		}

		return cameras;
	}

	@PostConstruct
	public void init() {
		configService = new RestTemplate();
		logger.trace("retrieving tracker service endpoint from configuration service");
		ResponseEntity<TrackerServiceEndpoint[]> trackerResponseEntities = configService.getForEntity(configurationServiceUrl + "/tracker/endpoints", TrackerServiceEndpoint[].class);
		trackerServiceEndpoints = new ArrayList<>();
		int count = 0;
		for (TrackerServiceEndpoint trackerServiceEndpoint : trackerResponseEntities.getBody()) {
			trackerServiceEndpoints.add(trackerServiceEndpoint);
			count++;
		}
		logger.trace("loaded " + count + " trackerservice endpoints");

		logger.trace("retrieving media service endpoint from configuration service");
		ResponseEntity<MediaServiceEndpoint[]> mediaResponseEntities = configService.getForEntity(configurationServiceUrl + "/media/endpoints", MediaServiceEndpoint[].class);
		mediaServiceEndpoints = new ArrayList<>();
		count = 0;
		for (MediaServiceEndpoint mediaServiceEndpoint : mediaResponseEntities.getBody()) {
			mediaServiceEndpoints.add(mediaServiceEndpoint);
			count++;
		}
		logger.trace("loaded " + count + " media service endpoints");

		MediaServiceEndpoint mediaServiceEndpoint = mediaServiceEndpoints.get(0);
		String mediaServiceUrl = mediaServiceEndpoint.getUrl();
		String mediaServiceUserName = mediaServiceEndpoint.getUserName();
		String mediaServicePassWord = mediaServiceEndpoint.getPassWord();
		publisher = new ImagePublisher(mediaServiceUrl, mediaServiceUserName, mediaServicePassWord);

		List<Camera> allCameras = new ArrayList<>();

		for (String environment : environments) {
			logger.info("getting cameras for environment: " + environment);
			List<Camera> cameras = getCamerasForEnvironment(environment);
			logger.info("retrieved " + cameras.size() + " cameras");
			allCameras.addAll(cameras);
		}


		manifest.setCameras(allCameras);

		int cameraCount = allCameras.size();

		ThreadFactoryBuilder builder = new ThreadFactoryBuilder().setNameFormat("img-producer-%d");

		scheduler = Executors.newScheduledThreadPool(cameraCount, builder.build());

		List<Camera> cameras = manifest.getCameras();
		logger.info("there are " + cameras.size() + " cameras to poll and publish");
		for (Camera camera : cameras) {
			logger.info("starting camera: " + camera.getDisplayName());
			startCamera(camera);
		}
	}


	private void startCamera(Camera camera) {
		Runnable retriever = new ScheduledImageRetriever(scheduler, camera, publisher);
		Thread t = new Thread(retriever);
		t.start();

		retrievers.add(retriever);
	}


}