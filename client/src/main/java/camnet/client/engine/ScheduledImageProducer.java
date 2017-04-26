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


public class ScheduledImageProducer implements Runnable {
    private ImagePublisher publisher;

	private ImageProducer producer;

	private Camera camera;
	private String restEndpoint;

	private ScheduledExecutorService scheduler;

	private ScheduledFuture<ImageProductionResponse> future;

	private boolean continueRunning;

	private Logger logger = Logger.getLogger(ScheduledImageProducer.class);


	public ScheduledImageProducer(ScheduledExecutorService scheduler, Camera camera, String restEndpoint) {
		this.camera = camera;
		this.restEndpoint = restEndpoint;
		this.scheduler = scheduler;

		continueRunning = true;

		producer = new ImageProducer(restEndpoint, camera);
        publisher = new ImagePublisher(restEndpoint, camera);

    }


	private void start() {
	    logger.info("in start(), scheduling immediate call");
        future = scheduler.schedule(producer, 0, TimeUnit.SECONDS);
        int success = process(future);
	    logger.info("back from immediate call");
	}

	public void run() {
	    start();
    }

	public void stop() {
		future.cancel(true);
		continueRunning = false;
	}

	private int process(ScheduledFuture<ImageProductionResponse> future) {
        ImageProductionResponse response;

        logger.info("getting image...");
        try {
            response = future.get();
        } catch (InterruptedException e) {
            logger.error("retrieval of image failed", e);
            return -1;
        } catch (ExecutionException e) {
            logger.error("execution exception occurred", e);
            return -1;
        }
        logger.info("got image.");

        int sleepTimeInSeconds = response.getSleepTimeInSeconds();
        logger.info("going to sleep " + sleepTimeInSeconds + " seconds.");
        if (response.getReturnCode() != 0) {
            logger.warn("failed to publish image: " + response.getMessage(), response.getError());
            return sleepTimeInSeconds;
        }

        logger.info("publishing image...");
        byte[] image = response.getImage();
        try {
            camera = publisher.publishImage(image);
        } catch (ImagePublishingException e) {
            logger.warn("failed to publish image", e);
            return sleepTimeInSeconds;
        }
        logger.info("image published");

        logger.info("schedule for sleep time: " + sleepTimeInSeconds);
        scheduler.schedule(producer, sleepTimeInSeconds, TimeUnit.SECONDS);
        logger.info("return from the call to schedule");

        return sleepTimeInSeconds;
    }

	private int scheduleNext(int delayInSeconds) {
	    logger.info("scheduling next retrieval for " + delayInSeconds + " seconds");
		future = scheduler.schedule(producer, delayInSeconds, TimeUnit.SECONDS);
		logger.info("back from scheduling call.");
		ImageProductionResponse response;

		logger.info("getting image...");
		try {
			response = future.get();
		} catch (InterruptedException e) {
			logger.error("retrieval of image failed", e);
			return -1;
		} catch (ExecutionException e) {
			logger.error("execution exception occurred", e);
			return -1;
		}
		logger.info("got image.");

		int sleepTimeInSeconds = response.getSleepTimeInSeconds();
		logger.info("going to sleep " + sleepTimeInSeconds + " seconds.");
		if (response.getReturnCode() != 0) {
			logger.warn("failed to publish image: " + response.getMessage(), response.getError());
			return sleepTimeInSeconds;
		}

		logger.info("publishing image...");
		byte[] image = response.getImage();
		try {
			camera = publisher.publishImage(image);
		} catch (ImagePublishingException e) {
			logger.warn("failed to publish image", e);
			return sleepTimeInSeconds;
		}
		logger.info("image published");

        logger.info("schedule for sleep time: " + sleepTimeInSeconds);
		scheduler.schedule(producer, sleepTimeInSeconds, TimeUnit.SECONDS);
        logger.info("return from the call to schedule");

		return sleepTimeInSeconds;
	}
}