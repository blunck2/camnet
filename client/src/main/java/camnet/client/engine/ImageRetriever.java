package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;



public class ImageRetriever {
	private Camera camera;
	private CloseableHttpClient client;
	private HttpGet httpGet;

	private static final Logger logger = Logger.getLogger(ImageRetriever.class);


	public ImageRetriever(Camera camera) {
		this.camera = camera;

		if ((camera.getUserName() != null) && (camera.getPassword() != null)) {
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(camera.getUserName(), camera.getPassword());
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, credentials);
			client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

		} else {
			client = HttpClients.createDefault();
		}

		httpGet = new HttpGet(camera.getUrl());
	}


	public byte[] retrieveImage() throws ImageRetrievalException {
		try {
			CloseableHttpResponse response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toByteArray(entity);
		} catch (Throwable e) {
			throw new ImageRetrievalException("failed to retrieve image from camera '" + camera.getHouseName() + "/" + camera.getId() + " at URL: " + camera.getUrl(), e);
		}

	}

}