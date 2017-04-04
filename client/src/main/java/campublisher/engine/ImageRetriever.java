package campublisher.engine;

import campublisher.model.internal.Camera;

import org.springframework.stereotype.Component;


@Component
public class ImageRetriever {

	public byte[] retrieveImage(Camera camera) throws ImageRetrievalException {
		return new byte[0];
	}

}