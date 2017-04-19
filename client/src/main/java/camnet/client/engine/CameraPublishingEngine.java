package camnet.client.engine;

import camnet.client.model.internal.Camera;
import camnet.client.model.internal.CameraManifest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.*;

import javax.annotation.PostConstruct;


@Component
public class CameraPublishingEngine {
	@Autowired
	private CameraManifest manifest;

	private List<ScheduledImageProducer> producers;

	private ScheduledExecutorService scheduler;


	public CameraPublishingEngine() { 
		producers = new ArrayList<>();
	}


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
		ScheduledImageProducer producer = new ScheduledImageProducer(camera);
		producers.add(producer);
		producer.start();
	}


}