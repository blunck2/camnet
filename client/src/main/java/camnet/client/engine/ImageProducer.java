package camnet.client.engine;

import java.util.concurrent.Callable;

import camnet.client.model.internal.Camera;
import org.springframework.stereotype.Service;

import org.apache.log4j.Logger;


public class ImageProducer implements Callable {
	private Camera camera;

	private ImageRetriever retriever;

	private ImagePublisher publisher;

	private Logger logger = Logger.getLogger(ImageProducer.class);

	public ImageProducer(String restEndpoint, Camera camera) {
		this.camera = camera;
		publisher = new ImagePublisher(restEndpoint, camera);
		retriever = new ImageRetriever();
	}

	public ImageProductionResponse call() {
		try {
			logger.info("retrieving image...");
			byte[] bytes = retriever.retrieveImage(camera);
			logger.info("image retreived.  publishing...");
			publisher.publishImage(bytes);
			logger.info("back from publishing");
			return ImageProductionResponse.createSuccessfulResponse(bytes, camera.getSleepTimeInSeconds());
		} catch (ImageRetrievalException e) {
			return ImageProductionResponse.createFailedResponse("failed to retrieve camera image: " + camera.getUrl(), e);
		} catch (ImagePublishingException e) {
			return ImageProductionResponse.createFailedResponse("failed to publish camera image to endpoint: " + publisher.getRestEndpoint(), e);
		}
	}
}