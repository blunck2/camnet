package camnet.agent.engine;

import camnet.model.Agent;
import camnet.model.AgentManifest;
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

	private Agent localAgent;

	@Value("${ConfigurationServiceUrl}")
	private String configurationServiceUrl;

	@Value("${ConfigurationServiceUserName}")
	private String configurationServiceUserName;

	@Value("${ConfigurationServicePassWord}")
	private String configurationServicePassword;

	@Value("${AgentEngine.heartBeatSleepTimeInSeconds}")
	private int heartBeatSleepTimeInSeconds;

	private List<MediaServiceEndpoint> mediaServiceEndpoints;
	private List<TrackerServiceEndpoint> trackerServiceEndpoints;

	@Value("#{'${AgentEngine.environments}'.split(',')}")
	private List<String> environments;

	private RestTemplate configService;
	private RestTemplate trackerService;

	private List<Runnable> retrievers;

	private ScheduledExecutorService cameraScheduler;
	private ScheduledExecutorService heartBeatScheduler;

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
		heartBeatScheduler = Executors.newScheduledThreadPool(1);
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

	public int getHeartBeatSleepTimeInSeconds() {
		return heartBeatSleepTimeInSeconds;
	}

	public void setHeartBeatSleepTimeInSeconds(int heartBeatSleepTimeInSeconds) {
		this.heartBeatSleepTimeInSeconds = heartBeatSleepTimeInSeconds;
	}

	public CameraManifest getCameraManifest() {
		return cameraManifest;
	}

	public void setCameraManifest(CameraManifest cameraManifest) {
		this.cameraManifest = cameraManifest;
	}

	private List<Camera> getCamerasToActivate(String environment) {
		// TODO: implement failover to backup trackers
		TrackerServiceEndpoint endpoint = pickTrackerServiceEndpoint();

		String trackerServiceUrl = endpoint.getUrl();
		String trackerServiceUserName = endpoint.getUserName();
		String trackerServicePassWord = endpoint.getPassWord();
		String url = trackerServiceUrl + "/manifest/cameras/environment/start/" + environment;

		logger.info("retrieving cameras to start from: " + url);

		// TODO: implement basic auth
		// trackerService.getInterceptors().add(new BasicAuthorizationInterceptor(trackerServiceUserName, trackerServicePassWord));

		ResponseEntity<Camera[]> responseEntity = trackerService.getForEntity(url, Camera[].class);
		Camera[] cameras = responseEntity.getBody();
		if ((cameras == null) || (cameras.length == 0)) {
			return new ArrayList<>();
		}

		List<Camera> camerasToReturn = new ArrayList<>();
		for (Camera camera : cameras) {
			camerasToReturn.add(camera);
		}

		logger.trace("cameras to activate: " + camerasToReturn);

		return camerasToReturn;
	}

	@PostConstruct
	public void init() {
		loadTrackerServiceEndpoints();
		loadMediaServiceEndpoints();

		registerWithTrackerService();

		startHeartBeating();

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

	private String getHostName() {
		// resolve hostname to be used in callbacks
		String hostName = null;
		try {
			hostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		} catch (java.net.UnknownHostException e) {
			logger.error("failed to resolve DNS name.  using localhost.", e);
			hostName = "localhost";
		}

		return hostName;
	}

	private void registerWithTrackerService() {
		logger.trace("registering with tracker service");

		String hostName = getHostName();

		String serviceEndpointUrl = "http://" + hostName + ":" + localListenPort + localContextPath;

		AgentServiceEndpoint agentEndpoint = new AgentServiceEndpoint();
		agentEndpoint.setUrl(serviceEndpointUrl);
		agentEndpoint.setUserName("");
		agentEndpoint.setPassWord("");

		localAgent = new Agent();
		localAgent.setEnvironments(environments);
		localAgent.setServiceEndpoint(agentEndpoint);
		localAgent.setId(createAgentId());

		TrackerServiceEndpoint trackerServiceEndpoint = pickTrackerServiceEndpoint();
		String trackerServiceUrl = trackerServiceEndpoint.getUrl();
		String url = trackerServiceUrl + "/manifest/agents/add";

		logger.trace("registering agent with tracker service located at: " + url);

		trackerService.postForObject(url, localAgent, Agent.class, new HashMap<String, String>());
	}

	private String createAgentId() {
		return getHostName() + "-" + System.currentTimeMillis();
	}

	private void createImagePublisher() {
		MediaServiceEndpoint mediaServiceEndpoint = pickMediaServiceEndpoint();

		String mediaServiceUrl = mediaServiceEndpoint.getUrl();
		String mediaServiceUserName = mediaServiceEndpoint.getUserName();
		String mediaServicePassWord = mediaServiceEndpoint.getPassWord();

		publisher = new ImagePublisher(mediaServiceUrl, mediaServiceUserName, mediaServicePassWord);
	}


	private void startCameras() {
		List<Camera> allCameras = new ArrayList<>();

		for (String environment : environments) {
			logger.info("getting cameras for environment: " + environment);

			List<Camera> camerasToActivate = getCamerasToActivate(environment);
			logger.info("retrieved " + camerasToActivate.size() + " cameras to activate");

			List<Camera> camerasForEnvironment = new ArrayList<>();
			for (Camera cameraToActivate : camerasToActivate) {
				Camera activatedCamera = activateCamera(cameraToActivate);
				camerasForEnvironment.add(activatedCamera);
				allCameras.add(activatedCamera);

				// TODO: we can't override ALL of the cameras for the environment based on a few to reassign...
				cameraManifest.setCamerasForEnvironment(environment, camerasForEnvironment);
			}
		}

		int cameraCount = allCameras.size();

		ThreadFactoryBuilder builder = new ThreadFactoryBuilder().setNameFormat("img-producer-%d");

		cameraScheduler = Executors.newScheduledThreadPool(cameraCount, builder.build());
		for (Camera camera : allCameras) {
			logger.trace("starting camera: " + camera.getDisplayName());
			startCamera(camera);
		}
	}

	private Camera activateCamera(Camera cameraToActivate) {
		// TODO:  call tracker service to activate the camera with this agent
		return cameraToActivate;
	}

	private void startHeartBeating() {
		logger.info("sending heartbeats every " + heartBeatSleepTimeInSeconds + " seconds.");
		logger.info("local agent: " + localAgent.toString());
		TrackerServiceEndpoint trackerServiceEndpoint = pickTrackerServiceEndpoint();

		Runnable heartBeater = new ScheduledHeartBeatSender(heartBeatScheduler, heartBeatSleepTimeInSeconds, trackerServiceEndpoint, "", "", environments, localAgent);
		Thread t = new Thread(heartBeater);
		t.start();
	}

	private int chooseRandom(int minimum, int maximum) {
		return minimum;
	}

	private MediaServiceEndpoint pickMediaServiceEndpoint() {
		int endpointCount = mediaServiceEndpoints.size();
		int selectedEndpointPosition = chooseRandom(0, endpointCount);

		return mediaServiceEndpoints.get(selectedEndpointPosition);
	}

	private TrackerServiceEndpoint pickTrackerServiceEndpoint() {
		int endpointCount = trackerServiceEndpoints.size();
		int selectedEndpointPosition = chooseRandom(0, endpointCount);

		return trackerServiceEndpoints.get(selectedEndpointPosition);
	}


	private void startCamera(Camera camera) {
		Runnable retriever = new ScheduledImageRetriever(cameraScheduler, camera, publisher);
		Thread t = new Thread(retriever);
		t.start();

		retrievers.add(retriever);
	}


}