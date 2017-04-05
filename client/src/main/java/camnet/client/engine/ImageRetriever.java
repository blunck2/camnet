package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.springframework.stereotype.Component;


@Component
public class ImageRetriever {

	public byte[] retrieveImage(Camera camera) throws ImageRetrievalException {
		return new byte[0];
	}

}