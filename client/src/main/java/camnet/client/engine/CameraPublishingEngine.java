package camnet.client.engine;

import camnet.client.model.internal.Camera;
import camnet.client.model.internal.CameraManifest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.*;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;


@Component
public class CameraPublishingEngine {
	@Autowired
	private CameraManifest manifest;

	@Value("${CameraPublishingEngine.restEndpoint}")
	private String restEndpoint;

	private List<ScheduledImageProducer> producers;

	private ScheduledExecutorService scheduler;

	private Logger logger = Logger.getLogger(CameraPublishingEngine.class);


	public CameraPublishingEngine() { 
		producers = new ArrayList<>();
	}

	public void setRestEndpoint(String restEndpoint) { logger.info("rest endpoint set: " + restEndpoint); this.restEndpoint = restEndpoint; }
	public String getRestEndpoint() { return restEndpoint; }


	@PostConstruct
	public void init() {
		List<Camera> allCameras = manifest.getCameras();
		int cameraCount = allCameras.size();

		scheduler = Executors.newScheduledThreadPool(cameraCount);

		for (Camera camera : manifest.getCameras()) {
			startCamera(camera);
		}
	}


	private void startCamera(Camera camera) {
		logger.info("instantiating new ScheduledImageProducer with restEndpoint: " + restEndpoint);
		ScheduledImageProducer producer = new ScheduledImageProducer(camera, restEndpoint);
		producers.add(producer);
		producer.start();
	}


}