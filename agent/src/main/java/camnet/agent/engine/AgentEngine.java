package camnet.agent.engine;

import camnet.model.Camera;
import camnet.model.CameraManifest;
import camnet.model.MediaServiceEndpoint;
import camnet.model.TrackerServiceEndpoint;
import camnet.model.AgentServiceEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.*;
import java.lang.Runnable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.web.client.RestTemplate;


@Component
public class AgentEngine {
	private CameraManifest cameraManifest;

	@Value("${AgentEngine.configurationServiceUrl}")
	private String configurationServiceUrl;

	@Value("${AgentEngine.configurationServiceUserName}")
	private String configurationServiceUserName;

	@Value("${AgentEngine.configurationServicePassWord}")
	private String configurationServicePassword;

	private List<MediaServiceEndpoint> mediaServiceEndpoints;
	private List<TrackerServiceEndpoint> trackerServiceEndpoints;

	@Value("#{'${AgentEngine.environments}'.split(',')}")
	private List<String> environments;

	private RestTemplate configService;
	private RestTemplate trackerService;

	private List<Runnable> retrievers;

	private ScheduledExecutorService scheduler;

	private ImagePublisher publisher;

	private ObjectMapper mapper;

	@Value("${server.port}")
	private String localListenPort;

	@Value("${server.contextPath}")
	private String localContextPath;

	private Logger logger = LoggerFactory.getLogger(AgentEngine.class);


	public AgentEngine() {
		configService = new RestTemplate();
		trackerService = new RestTemplate();

		retrievers = new ArrayList<>();
		cameraManifest = new CameraManifest();
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

	public CameraManifest getCameraManifest() {
		return cameraManifest;
	}

	public void setCameraManifest(CameraManifest cameraManifest) {
		this.cameraManifest = cameraManifest;
	}

	private List<Camera> getCamerasForEnvironment(String environment) {
		// TODO: implement failover to backup trackers
		TrackerServiceEndpoint endpoint = trackerServiceEndpoints.get(0);

		String trackerServiceUrl = endpoint.getUrl();
		String trackerServiceUserName = endpoint.getUserName();
		String trackerServicePassWord = endpoint.getPassWord();
		String url = trackerServiceUrl + "/manifest/cameras/environment/" + environment;

		logger.info("retrieving camera manifests from: " + url);

		// TODO: implement basic auth
		// trackerService.getInterceptors().add(new BasicAuthorizationInterceptor(trackerServiceUserName, trackerServicePassWord));

		ResponseEntity<Camera[]> responseEntity = trackerService.getForEntity(url, Camera[].class);
		List<Camera> cameras = new ArrayList<>();
		for (Camera camera : responseEntity.getBody()) {
			cameras.add(camera);
		}

		return cameras;
	}

	@PostConstruct
	public void init() {
		loadTrackerServiceEndpoints();
		loadMediaServiceEndpoints();
		registerWithConfigurationService();

		createImagePublisher();

		startCameras();
	}

	private void loadTrackerServiceEndpoints() {
		logger.trace("retrieving tracker service endpoint from configuration service");

		String url = configurationServiceUrl + "/tracker/endpoints";
		ResponseEntity<TrackerServiceEndpoint[]> trackerResponseEntities = configService.getForEntity(url, TrackerServiceEndpoint[].class);

		trackerServiceEndpoints = new ArrayList<>();

		int count = 0;
		for (TrackerServiceEndpoint trackerServiceEndpoint : trackerResponseEntities.getBody()) {
			trackerServiceEndpoints.add(trackerServiceEndpoint);
			count++;
		}

		logger.trace("loaded " + count + " tracker service endpoints");
	}


	private void loadMediaServiceEndpoints() {
		logger.trace("retrieving media service endpoint from configuration service");

		String url = configurationServiceUrl + "/media/endpoints";
		ResponseEntity<MediaServiceEndpoint[]> mediaResponseEntities = configService.getForEntity(url, MediaServiceEndpoint[].class);

		mediaServiceEndpoints = new ArrayList<>();

		int count = 0;
		for (MediaServiceEndpoint mediaServiceEndpoint : mediaResponseEntities.getBody()) {
			mediaServiceEndpoints.add(mediaServiceEndpoint);
			count++;
		}

		logger.trace("loaded " + count + " media service endpoints");
	}

	private void registerWithConfigurationService() {
		logger.trace("registering with configuration service");

		// resolve hostname to be used in callbacks
		String hostName = null;
		try {
			hostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		} catch (java.net.UnknownHostException e) {
			logger.error("failed to resolve DNS name.  using localhost.", e);
			hostName = "localhost";
		}

		String serviceEndpointUrl = "http://" + hostName + ":" + localListenPort + localContextPath;

		AgentServiceEndpoint endpoint = new AgentServiceEndpoint();
		endpoint.setUrl(serviceEndpointUrl);
		endpoint.setUserName("");
		endpoint.setPassWord("");

		String url = configurationServiceUrl + "/agent/endpoint/add";
		AgentServiceEndpoint result =
				configService.postForObject(url, endpoint, AgentServiceEndpoint.class, new HashMap<String, String>());
	}

	private void createImagePublisher() {
		int mediaServiceEndpointPos = pickMediaServiceEndpoint();

		MediaServiceEndpoint mediaServiceEndpoint = mediaServiceEndpoints.get(mediaServiceEndpointPos);

		String mediaServiceUrl = mediaServiceEndpoint.getUrl();
		String mediaServiceUserName = mediaServiceEndpoint.getUserName();
		String mediaServicePassWord = mediaServiceEndpoint.getPassWord();

		publisher = new ImagePublisher(mediaServiceUrl, mediaServiceUserName, mediaServicePassWord);
	}


	private void startCameras() {
		List<Camera> allCameras = new ArrayList<>();

		for (String environment : environments) {
			logger.info("getting cameras for environment: " + environment);
			List<Camera> cameras = getCamerasForEnvironment(environment);
			logger.info("retrieved " + cameras.size() + " cameras");
			cameraManifest.setCamerasForEnvironment(environment, cameras);
			allCameras.addAll(cameras);
		}

		int cameraCount = allCameras.size();

		ThreadFactoryBuilder builder = new ThreadFactoryBuilder().setNameFormat("img-producer-%d");

		scheduler = Executors.newScheduledThreadPool(cameraCount, builder.build());
		for (Camera camera : allCameras) {
			logger.trace("starting camera: " + camera.getDisplayName());
			startCamera(camera);
		}
	}

	private int pickMediaServiceEndpoint() {
		// TODO: pick a random media service
		return 0;
	}


	private void startCamera(Camera camera) {
		Runnable retriever = new ScheduledImageRetriever(scheduler, camera, publisher);
		Thread t = new Thread(retriever);
		t.start();

		retrievers.add(retriever);
	}


}