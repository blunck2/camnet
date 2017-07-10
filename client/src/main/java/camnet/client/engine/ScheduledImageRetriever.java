package camnet.client.engine;

import java.util.List;

import camnet.client.model.internal.Camera;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private Logger logger = LoggerFactory.getLogger(ScheduledImageRetriever.class);

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

		logger.info("retrieving and publishing image: " + camera.getDisplayName());

		byte[] image = null;
		Map<String, String> headers;
		ImageRetrievalResponse imageRetrievalResponse;
		try {
			imageRetrievalResponse = retriever.retrieveImage();
			image = imageRetrievalResponse.getContent();
			headers = imageRetrievalResponse.getHeaders();
		} catch (ImageRetrievalException e) {
			logger.error("failed to retrieve image", e);
			scheduleNext(DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS, TimeUnit.SECONDS);
			return;
		} catch (Throwable t) {
			logger.error("unanticipated error occurred while retrieving image", t);
			scheduleNext(DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS, TimeUnit.SECONDS);
			return;
		}

		int oldSleepTimeInSeconds = camera.getSleepTimeInSeconds();
		try {
			logger.trace(camera.getDisplayName() + " uploading image");
			camera = publisher.publishImage(camera, image, headers);
			logger.trace(camera.getDisplayName() + " image uploaded");
		} catch (ImagePublishingException e) {
			logger.error("failed to publish image", e);
			scheduleNext(DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS, TimeUnit.SECONDS);
			return;
		} catch (Throwable t) {
			logger.error("unanticipated error occurred while publishing image", t);
			scheduleNext(DEFAULT_WAIT_TIME_FOR_ERRORS_IN_SECONDS, TimeUnit.SECONDS);
			return;
		}

		int newSleepTimeInSeconds = camera.getSleepTimeInSeconds();
		if (oldSleepTimeInSeconds != newSleepTimeInSeconds) {
			logger.info("sleep time changed for '" + camera.getDisplayName() + ": " +
					oldSleepTimeInSeconds + "s -> " + newSleepTimeInSeconds + "s");
		}

		logger.trace(camera.getDisplayName() + " sleeping for " + camera.getSleepTimeInSeconds() + " seconds");
		scheduleNext(camera.getSleepTimeInSeconds(), TimeUnit.SECONDS);
	}

	private void scheduleNext(int sleepTime, TimeUnit unit) {
		logger.trace(camera.getDisplayName() + " scheduling for delayed execution in seconds: " + sleepTime);
		scheduler.schedule(this, sleepTime, unit);
		logger.trace(camera.getDisplayName() + " back from call to schedule");
	}


	public void stop() {
		future.cancel(true);
		continueRunning = false;
	}

}