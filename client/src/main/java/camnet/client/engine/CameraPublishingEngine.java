package camnet.client.engine;

import camnet.client.model.internal.Camera;
import camnet.client.model.internal.CameraManifest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import java.util.concurrent.*;

import javax.annotation.PostConstruct;


@Component
public class CameraPublishingEngine {
	@Autowired
	private CameraManifest manifest;

	@Autowired
	private ImagePublisher publisher;

	private ScheduledExecutorService scheduler;


	public CameraPublishingEngine() { }


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
		ImageProducer producer = new ImageProducer(camera);
	}


}