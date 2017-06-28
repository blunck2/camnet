package camnet.service.media.processor;

import org.springframework.web.multipart.MultipartFile;

import camnet.model.Camera;

import java.util.Map;


public interface ImageProcessor {
	public int processImage(Camera camera, MultipartFile image, Map<String, String> imageHeaders) throws ImageProcessingException;
}