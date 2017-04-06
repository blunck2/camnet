package camnet.client.engine;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import camnet.client.model.internal.Camera;


public class ImageProducer implements Callable {
	private Camera camera;

	@Autowired
	private ImageRetriever retriever;

	@Autowired
	private ImagePublisher publisher;

	public ImageProducer(Camera camera) {
		this.camera = camera;
	}

	public ImageProductionResponse call() {
		try {
			byte[] bytes = retriever.retrieveImage(camera);
			publisher.publishImage(bytes, camera);
			return ImageProductionResponse.createSuccessfulResponse(bytes, camera.getSleepTimeInSeconds());
		} catch (ImageRetrievalException e) {
			return ImageProductionResponse.createFailedResponse("failed to retrieve camera image: " + camera.getUrl(), e);
		} catch (ImagePublishingException e) {
			return ImageProductionResponse.createFailedResponse("failed to publish camera image to endpoint: " + publisher.getRestEndpoint(), e);
		}
	}
}