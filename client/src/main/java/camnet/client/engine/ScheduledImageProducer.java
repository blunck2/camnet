package camnet.client.engine;

import org.springframework.beans.factory.annotation.Autowired;

import camnet.client.model.internal.Camera;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;


public class ScheduledImageProducer implements Runnable {
	@Autowired
	private ImagePublisher publisher;

	@Autowired
	private ImageProducer producer;

	private Camera camera;

	private ScheduledExecutorService scheduler;

	private ScheduledFuture<ImageProductionResponse> future;

	private boolean continueRunning;

	private Logger logger = Logger.getLogger(ScheduledImageProducer.class);


	public ScheduledImageProducer(Camera camera) {
		this.camera = camera;
		scheduler = Executors.newScheduledThreadPool(1);
		continueRunning = true;
	}

	public void start() {
		int sleepTimeInSeconds = 0;
		while (continueRunning) {
			sleepTimeInSeconds = scheduleNext(sleepTimeInSeconds);
		}
	}

	public void stop() {
		future.cancel(true);
		continueRunning = false;
	}

	public void run() {
		start();
	}


	private int scheduleNext(int delayInSeconds) {
		future = scheduler.schedule(producer, delayInSeconds, TimeUnit.SECONDS);
		ImageProductionResponse response;

		try {
			response = future.get();
		} catch (InterruptedException e) {
			logger.error("retrieval of image failed", e);
			return -1;
		} catch (ExecutionException e) {
			logger.error("execution exception occurred", e);
			return -1;
		}

		int sleepTimeInSeconds = response.getSleepTimeInSeconds();
		if (response.getReturnCode() != 0) {
			logger.warn("failed to retreieve image: " + response.getMessage(), response.getError());
			return sleepTimeInSeconds;
		}

		byte[] image = response.getImage();
		try {
			camera = publisher.publishImage(image, camera);
		} catch (ImagePublishingException e) {
			logger.warn("failed to publish image", e);
			return sleepTimeInSeconds;
		}

		return sleepTimeInSeconds;
	}
}