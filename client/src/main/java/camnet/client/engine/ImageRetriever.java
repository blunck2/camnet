package camnet.client.engine;

import camnet.client.model.internal.Camera;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.web.client.RestTemplate;


public class ImageRetriever {
	private Camera camera;
	private CloseableHttpClient client;
	private HttpGet httpGet;

	private static final Logger logger = LogManager.getLogger();

	private static final String[] VALUABLE_HEADERS = new String[] { "Content-type" };


	public ImageRetriever(Camera camera) {
		this.camera = camera;
		logger.trace(camera.getDisplayName() + " url: " + camera.getUrl());

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder =
				requestConfigBuilder.setConnectTimeout(2000)
				.setConnectionRequestTimeout(2000)
				.setSocketTimeout(2000);

		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestConfigBuilder.build());

		if ((camera.getUserName() != null) && (camera.getPassword() != null)) {
			String username = camera.getUserName();
			String password = camera.getPassword();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			logger.trace(camera.getDisplayName() + " username/password: " + username + "/" + password);
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, credentials);

			builder.setDefaultCredentialsProvider(provider).build();
		}

		client = builder.build();

		httpGet = new HttpGet(camera.getUrl());
	}

	private Map<String, String> extractValuableHeaders(CloseableHttpResponse response) {
		Map<String, String> valuableHeaders = new HashMap<>();

		for (String valuableHeaderName : VALUABLE_HEADERS) {
			Header[] headers = response.getHeaders(valuableHeaderName);
			Header header = headers[0];
			String value = header.getValue();

			valuableHeaders.put(valuableHeaderName, value);
		}

		return valuableHeaders;
	}


	public ImageRetrievalResponse retrieveImage() throws ImageRetrievalException {
		byte[] bytes;
		Map<String, String> valuableHeaders;

		try {
			logger.trace(camera.getDisplayName() + " issuing GET request");
			CloseableHttpResponse response = client.execute(httpGet);
			logger.trace(camera.getDisplayName() + " received response");
			valuableHeaders = extractValuableHeaders(response);
			logger.trace(camera.getDisplayName() + " extracted headers.  count: " + valuableHeaders.keySet().size());

			logger.trace(camera.getDisplayName() + " getting http entity");
			HttpEntity entity = response.getEntity();
			logger.trace(camera.getDisplayName() + " reading bytes");
			bytes = EntityUtils.toByteArray(entity);
			logger.trace(camera.getDisplayName() + " " + bytes.length + " bytes received.");
		} catch (Throwable e) {
			throw new ImageRetrievalException("failed to retrieve image from camera '" + camera.getHouseName() + "/" + camera.getId() + " at URL: " + camera.getUrl(), e);
		}

		ImageRetrievalResponse response = new ImageRetrievalResponse();
		response.setContent(bytes);
		response.setHeaders(valuableHeaders);
		return response;
	}

}