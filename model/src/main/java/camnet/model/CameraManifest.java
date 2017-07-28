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

import org.apache.commons.lang3.builder.ToStringBuilder;


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
 
	public Camera getCameraById(String environment, String id) {
		logger.trace("looking for camera environment: " + environment + "; id: " + id);
		for (Camera camera : getCamerasForEnvironment(environment)) {
			logger.trace("got camera environment: " + camera.getEnvironment() + "; id: " + camera.getId());
			if (id.equals(camera.getId())) {
				return camera;
			}
		}

		return null;
	}

	public List<Camera> getCamerasForEnvironment(String environment) {
		return cameras.get(environment);
	}
	public void setCamerasForEnvironment(String environment, List<Camera> cameraList) { cameras.put(environment, cameraList); }

	public void removeCameraById(String id) {
		for (String environment : cameras.keySet()) {
			List<Camera> camerasForEnvironment = cameras.get(environment);

			for (Camera camera : camerasForEnvironment) {
				if (camera.getId().equals(id)) {
					camerasForEnvironment.remove(camera);
					break;
				}
			}
		}
	}

	public Set<String> getEnvironments() {
		return cameras.keySet();
	}

	public void addCamera(Camera camera) {
		String environment = camera.getEnvironment();
		removeCameraById(camera.getId());

		List<Camera> existingCameras = cameras.get(environment);
		if (existingCameras == null) {
			existingCameras = new ArrayList<>();
			cameras.put(environment, existingCameras);
		}

		for (Camera existing : existingCameras) {
			if (existing.getId() == camera.getId()) {
				throw new IllegalArgumentException("camera already exists");
			}
		}

		existingCameras.add(camera);
	}

	public List<Camera> getCamerasByEnvironment(String environment) {
		return cameras.get(environment);
	}

	public List<Camera> getAllCameras() {
		List<Camera> allCameras = new ArrayList<>();

		for (String environment : cameras.keySet()) {
			List<Camera> environmentCameras = cameras.get(environment);
			allCameras.addAll(environmentCameras);
		}

		return allCameras;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("cameras", cameras)
				.toString();
	}
}