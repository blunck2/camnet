package camnet.client.model.internal;

import java.util.List;
import java.util.ArrayList;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@ConfigurationProperties(prefix="manifest")
public class CameraManifest {
	private List<Camera> cameras;

	private Logger logger = LoggerFactory.getLogger(CameraManifest.class);

	public CameraManifest() { cameras = new ArrayList<>(); }

	public List<Camera> getCameras() {
		return cameras;
	}

	public void setCameras(List<Camera> cameras) {
		this.cameras = cameras;
	}
 
  	public Camera getCameraById(String environment, String id) {
  		for (Camera camera : getCameras()) {
  			if (environment.equals(camera.getEnvironment()) && id.equals(camera.getId())) {
  				return camera;
  			}
  		}

  		return null;
  	}

  	public void removeCameraById(String houseName, String id) {
  		Camera existing = getCameraById(houseName, id);
  		cameras.remove(existing);
  	}

  	public void addCamera(Camera camera) {
  		removeCameraById(camera.getEnvironment(), camera.getId());
  		cameras.add(camera);
  	}

}