package camnet.client.engine;

import camnet.client.model.internal.Camera;
import camnet.client.model.internal.CameraManifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.*;
import java.lang.Runnable;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.web.client.RestTemplate;


@Component
public class CameraPublishingEngine {
	@Autowired
	private CameraManifest manifest;

	@Value("${CameraPublishingEngine.configurationRestEndpoint}")
	private String configurationRestEndpoint;

	@Value("${CameraPublishingEngine.mediaRestEndpoint}")
	private String mediaRestEndpoint;

	@Value("${CameraPublishingEngine.userName}")
	private String userName;

	@Value("${CameraPublishingEngine.passWord}")
	private String passWord;

	@Value("#{'${CameraPublishingEngine.houses}'.split(',')}")
	private List<String> houses;


	private List<Runnable> retrievers;

	private ScheduledExecutorService scheduler;

	private ImagePublisher publisher;

	private RestTemplate template;

	private ObjectMapper mapper;

	private Logger logger = LogManager.getLogger();


	public CameraPublishingEngine() {
		template = new RestTemplate();
		retrievers = new ArrayList<>();
	}

	public void setConfigurationRestEndpoint(String restEndpoint) {
		this.configurationRestEndpoint = restEndpoint;
	}

	public String getConfigurationRestEndpoint() {
		return configurationRestEndpoint;
	}

	public void setMediaRestEndpoint(String restEndpoint) {
		this.mediaRestEndpoint = restEndpoint;
	}

	public String getMediaRestEndpoint() {
		return mediaRestEndpoint;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}


	public void setHouses(List<String> houses) { this.houses = houses; }
	public List<String> getHouses() { return houses; }

	private List<Camera> getCamerasForHouse(String house) {
		String url = configurationRestEndpoint + "/manifest/cameras/house/" + house;
		logger.trace("retrieving camera manifests from: " + url);
		template.getInterceptors().add(new BasicAuthorizationInterceptor(this.userName, this.passWord));
		ResponseEntity<Camera[]> responseEntity = template.getForEntity(url, Camera[].class);
		List<Camera> cameras = new ArrayList<>();
		for (Camera camera : responseEntity.getBody()) {
			cameras.add(camera);
		}

		return cameras;
	}

	@PostConstruct
	public void init() {
		publisher = new ImagePublisher(mediaRestEndpoint, userName, passWord);

		List<Camera> allCameras = new ArrayList<>();

		for (String house : houses) {
			logger.info("getting cameras for house: " + house);
			List<Camera> cameras = getCamerasForHouse(house);
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
			logger.info("starting camera: " + camera.getHouseName() + "/" + camera.getId());
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