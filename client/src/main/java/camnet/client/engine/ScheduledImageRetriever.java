package camnet.client.engine;

import java.util.List;

import camnet.client.model.internal.Camera;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


public class ScheduledImageRetriever implements Runnable {
    private ImagePublisher publisher;

	private ImageRetriever retriever;

	private Camera camera;

	private ScheduledExecutorService scheduler;

	private ScheduledFuture<ImageProductionResponse> future;

	private boolean continueRunning;

	private Logger logger = Logger.getLogger(ScheduledImageRetriever.class);

	private static final int DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS = 60;


	public ScheduledImageRetriever(ScheduledExecutorService scheduler, Camera camera, ImagePublisher publisher) {
		this.camera = camera;
		this.scheduler = scheduler;

		continueRunning = true;

		retriever = new ImageRetriever(camera);
		this.publisher = publisher;
    }


	public void run() {
		ImageProductionResponse response;

		logger.info("getting image...");
		byte[] image = null;
		try {
			image = retriever.retrieveImage();

		} catch (ImageRetrievalException e) {
			logger.error("failed to retrieve image", e);
			scheduleNext(DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS, TimeUnit.SECONDS);
		}

		try {
			camera = publisher.publishImage(camera, image);
		} catch (ImagePublishingException e) {
			logger.error("failed to publish image", e);
			scheduleNext(DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS, TimeUnit.SECONDS);
		}

		logger.info("sleeping for " + camera.getSleepTimeInSeconds() + " seconds before next run");
		scheduleNext(camera.getSleepTimeInSeconds(), TimeUnit.SECONDS);
	}

	private void scheduleNext(int sleepTime, TimeUnit unit) {
		scheduler.schedule(this, sleepTime, unit);
	}


	public void stop() {
		future.cancel(true);
		continueRunning = false;
	}

}