package camnet.client.engine;

import org.springframework.beans.factory.annotation.Autowired;

import camnet.client.model.internal.Camera;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix="scheduledImageProducer")

public class ScheduledImageProducer implements Runnable {
    private String restEndpoint;

    private ImagePublisher publisher;

	private ImageProducer producer;

	private Camera camera;

	private ScheduledExecutorService scheduler;

	private ScheduledFuture<ImageProductionResponse> future;

	private boolean continueRunning;

	private Logger logger = Logger.getLogger(ScheduledImageProducer.class);

	@PostConstruct
    public void init() {
	    logger.info("producer null?  " + producer == null);
        publisher = new ImagePublisher(restEndpoint, camera);
    }

    public void setRestEndpoint(String restEndpoint) { logger.info("rest endpoint set: " + restEndpoint); this.restEndpoint = restEndpoint; }
    public String getRestEndpoint() { return restEndpoint; }

	public ScheduledImageProducer(Camera camera) {
		this.camera = camera;
		scheduler = Executors.newScheduledThreadPool(1);
		continueRunning = true;

		producer = new ImageProducer(restEndpoint, camera);
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

		if (scheduler == null) {
			logger.error("scheduler is null");
		}

		if (producer == null) {
			logger.error("producer is null");
		}

        if (publisher == null) {
            logger.error("publisher is null");
        }

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
			camera = publisher.publishImage(image);
		} catch (ImagePublishingException e) {
			logger.warn("failed to publish image", e);
			return sleepTimeInSeconds;
		}

		return sleepTimeInSeconds;
	}
}