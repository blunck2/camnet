package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;



public class ImageRetriever {
	private Camera camera;

	public ImageRetriever(Camera camera) {
		this.camera = camera;
	}

	public byte[] retrieveImage() throws ImageRetrievalException {
		URL url;
		try {
			url = new URL(camera.getUrl());
		} catch (MalformedURLException e) {
			throw new ImageRetrievalException("malformed url", e);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
  			is = url.openStream ();
  			byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
  			int n;

  			while ( (n = is.read(byteChunk)) > 0 ) {
    			baos.write(byteChunk, 0, n);
  			}
		}
		catch (IOException e) {
  			System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
  			e.printStackTrace ();
		}
		finally {
  			if (is != null) {
  				try {
  					is.close(); 
  				} catch (IOException e) {
  					System.err.println("failed to close stream");
  				}
  			}
		}
		return baos.toByteArray();
	}

}