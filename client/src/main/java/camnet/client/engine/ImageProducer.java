package camnet.client.engine;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import camnet.client.model.internal.Camera;


public class ImageProducer implements Callable {
	private Camera camera;

	@Autowired
	private ImageRetriever retriever;

	public ImageProducer(Camera camera) {
		this.camera = camera;
	}

	public ImageRetrievalResponse call() {
		try {
			byte[] bytes = retriever.retrieveImage(camera);
			return ImageRetrievalResponse.createSuccessfulImageRetrievalResponse(bytes, camera.getSleepTimeInSeconds());
		} catch (ImageRetrievalException e) {
			return ImageRetrievalResponse.createFailedImageRetrievalResponse("failed to download camera image: " + camera.getUrl(), e);
		}
	}
}