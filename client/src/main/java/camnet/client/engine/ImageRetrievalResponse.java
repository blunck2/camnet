package camnet.client.engine;

public class ImageRetrievalResponse {
	private int returnCode;
	private String message;
	private Throwable error;
	private byte[] image;
	private int sleepTimeInSeconds;

	public static ImageRetrievalResponse createSuccessfulImageRetrievalResponse(byte[] image, int sleepTimeInSeconds) {
		return new ImageRetrievalResponse(0, null, null, image, sleepTimeInSeconds);
	}

	public static ImageRetrievalResponse createFailedImageRetrievalResponse(String message, Throwable error) {
		return new ImageRetrievalResponse(1, message, error, null, 60);
	}

	private ImageRetrievalResponse(int returnCode,
			 					  String message,
			 					  Throwable error,
								  byte[] image,
								  int sleepTimeInSeconds) {
		this.returnCode = returnCode;
		this.message = message;
		this.error = error;
		this.image = image;
		this.sleepTimeInSeconds = sleepTimeInSeconds;
	}

	public int getReturnCode() { return returnCode; }
	public String getMessage() { return message; }
	public Throwable getError() { return error; }
	public byte[] getImage() { return image; }
	public int getSleepTimeInSeconds() { return sleepTimeInSeconds; }

}