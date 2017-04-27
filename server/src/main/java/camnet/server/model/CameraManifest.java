package camnet.server.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import org.apache.log4j.Logger;


@Component
@ConfigurationProperties(prefix="manifest")
public class CameraManifest {
	private List<Camera> cameras;

	private Logger logger = Logger.getLogger(CameraManifest.class);


	public CameraManifest() { cameras = new ArrayList<>(); }

	public List<Camera> getCameras() { return cameras; }

	public void setCameras(List<Camera> cameras) { this.cameras = cameras; }
 
  	public Camera getCameraById(String id) {
  		for (Camera camera : getCameras()) {
  			if (id.equals(camera.getId())) {
  				return camera;
  			}
  		}

  		return null;
  	}

  	public void removeCameraById(String id) {
		logger.info("looking for camera");
  		Camera existing = getCameraById(id);
  		logger.info("is camera null? " + existing);
  		logger.info("pre-remove size: " + cameras.size());
  		cameras.remove(existing);
  		logger.info("post-remove size: " + cameras.size());
  	}

  	public void addCamera(Camera camera) {
		logger.info("removing camera with id: " + camera.getId());
  		removeCameraById(camera.getId());
  		logger.info("adding camera");
  		cameras.add(camera);
  	}

}