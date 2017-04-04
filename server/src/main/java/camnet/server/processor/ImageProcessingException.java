package camsink.processor;

public class ImageProcessingException extends Exception {
	public ImageProcessingException() { super(); }
	public ImageProcessingException(String message) { super(message); }
	public ImageProcessingException(String message, Throwable cause) { super(message, cause); }
	public ImageProcessingException(Throwable cause) { super(cause); }
}