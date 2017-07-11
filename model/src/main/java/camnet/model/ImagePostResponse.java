package camnet.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * The response from an image post.
 */
public class ImagePostResponse implements Serializable {
	private int code;
	private String message;
	private int sleepTimeInSeconds;

	public ImagePostResponse() { }

	public int getCode() { return code; }
	public void setCode(int code) { this.code = code; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public int getSleepTimeInSeconds() { return sleepTimeInSeconds; }
	public void setSleepTimeInSeconds(int sleepTimeInSeconds) { 
		this.sleepTimeInSeconds = sleepTimeInSeconds; 
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
				append("code", code).
				append("message", message).
				append("sleepTimeInSeconds", sleepTimeInSeconds).
				toString();
	}
}