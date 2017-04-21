package camnet.client.model.internal;

import java.util.List;
import java.util.ArrayList;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import org.apache.log4j.Logger;


@Component
@ConfigurationProperties(prefix="manifest")
public class CameraManifest {
	private List<Camera> cameras;

	private Logger logger = Logger.getLogger(CameraManifest.class);

	public CameraManifest() { cameras = new ArrayList<>(); }

	public List<Camera> getCameras() {
		return cameras;
	}

	public void setCameras(List<Camera> cameras) {
		this.cameras = cameras;
	}
 
  	public Camera getCameraById(String id) {
  		for (Camera camera : getCameras()) {
  			if (id.equals(camera.getId())) {
  				return camera;
  			}
  		}

  		return null;
  	}

  	public void removeCameraById(String id) {
  		Camera existing = getCameraById(id);
  		cameras.remove(existing);
  	}

  	public void addCamera(Camera camera) {
		logger.info("addCamera called");
  		removeCameraById(camera.getId());
  		cameras.add(camera);
  	}

}