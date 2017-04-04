package camnet.server.processor;

import org.springframework.web.multipart.MultipartFile;

import camnet.server.model.Camera;


public interface ImageProcessor {
	public int processImage(Camera camera, MultipartFile image) throws ImageProcessingException;
}