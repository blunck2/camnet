package camnet.client.engine;

import camnet.client.model.internal.Camera;
import camnet.client.model.internal.CameraManifest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.*;
import java.lang.Runnable;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;


@Component
public class CameraPublishingEngine {
	@Autowired
	private CameraManifest manifest;

	@Value("${CameraPublishingEngine.restEndpoint}")
	private String restEndpoint;

	private List<Runnable> retrievers;

	private ScheduledExecutorService scheduler;

	private ImagePublisher publisher;

	private Logger logger = Logger.getLogger(CameraPublishingEngine.class);


	public CameraPublishingEngine() {
		retrievers = new ArrayList<>();
	}

	public void setRestEndpoint(String restEndpoint) { logger.info("rest endpoint set: " + restEndpoint); this.restEndpoint = restEndpoint; }
	public String getRestEndpoint() { return restEndpoint; }


	@PostConstruct
	public void init() {
		publisher = new ImagePublisher(restEndpoint);

		List<Camera> allCameras = manifest.getCameras();
		int cameraCount = allCameras.size();

		ThreadFactoryBuilder builder = new ThreadFactoryBuilder().setNameFormat("image-producer-%d");

		scheduler = Executors.newScheduledThreadPool(cameraCount, builder.build());

		List<Camera> cameras = manifest.getCameras();
		logger.info("there are " + cameras.size() + " cameras");
		for (Camera camera : cameras) {
			logger.info("starting camera with id: " + camera.getId());
			startCamera(camera);
			logger.info("back from call to startCamera()");
		}
	}


	private void startCamera(Camera camera) {
		logger.info("instantiating new ScheduledImageProducer with restEndpoint: " + restEndpoint);
		Runnable retriever = new ScheduledImageRetriever(scheduler, camera, publisher);
		logger.info("back from constructor.  constructing thread");
		Thread t = new Thread(retriever);
		logger.info("starting producer");
		t.start();
		retrievers.add(retriever);
		logger.info("back from add");
	}


}