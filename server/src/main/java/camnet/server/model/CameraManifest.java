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
 
  	public Camera getCameraById(String houseName, String id) {
  		for (Camera camera : getCameras()) {
  			if (houseName.equals(camera.getHouseName()) && id.equals(camera.getId())) {
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
 		removeCameraById(camera.getHouseName(), camera.getId());
  		cameras.add(camera);
  	}

  	public List<Camera> getCamerasByHouseName(String houseName) {
		List<Camera> camerasToReturn = new ArrayList<>();

		for (Camera camera : cameras) {
			if (camera.getHouseName().equals(houseName)) {
				camerasToReturn.add(camera);
			}
		}

		return camerasToReturn;
	}

}