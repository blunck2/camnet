package camsink.processor;

import org.springframework.web.multipart.MultipartFile;

import camsink.model.internal.Camera;


public interface ImageProcessor {
	public int processImage(Camera camera, MultipartFile image) throws ImageProcessingException;
}