package camnet.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CameraManifest {
	private Map<String, List<Camera>> cameras;

	private Logger logger = LogManager.getLogger();


	public CameraManifest() { cameras = new HashMap<>(); }

	public Map<String, List<Camera>> getCameras() {
		return cameras;
	}

	public void setCameras(Map<String, List<Camera>> cameras) {
		this.cameras = cameras;
	}
 
  	public Camera getCameraById(String houseName, String id) {
  		for (Camera camera : getCamerasForHouse(houseName)) {
  			if (id.equals(camera.getId())) {
  				return camera;
  			}
  		}

  		return null;
  	}

  	public List<Camera> getCamerasForHouse(String houseName) {
		return cameras.get(houseName);
	}

  	public void removeCameraById(String houseName, String id) {
		if (! cameras.keySet().contains(houseName)) { return; }

  		List<Camera> camerasForHouse = cameras.get(houseName);

		for (Iterator<Camera> iter = camerasForHouse.listIterator(); iter.hasNext();) {
			Camera camera = iter.next();
			if (camera.getId().equals(id)) {
				iter.remove();
			}
		}
  	}

  	public Set<String> getHouseNames() {
		return cameras.keySet();
	}

  	public void addCamera(Camera camera) {
		String houseName = camera.getHouseName();
 		removeCameraById(houseName, camera.getId());

 		List<Camera> existingCameras = cameras.get(houseName);
 		if (existingCameras == null) {
 			existingCameras = new ArrayList<>();
 			cameras.put(houseName, existingCameras);
		}

 		for (Camera existing : existingCameras) {
 			if (existing.getId() == camera.getId()) {
 				throw new IllegalArgumentException("camera already exists");
			}
		}

 		existingCameras.add(camera);
  	}

  	public List<Camera> getCamerasByHouseName(String houseName) {
		return cameras.get(houseName);
	}

	public List<Camera> getAllCameras() {
		List<Camera> allCameras = new ArrayList<>();

		for (String houseName : cameras.keySet()) {
			List<Camera> houseCameras = cameras.get(houseName);
			allCameras.addAll(houseCameras);
		}

		return allCameras;
	}
}