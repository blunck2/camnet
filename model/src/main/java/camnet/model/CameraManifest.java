package camnet.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class CameraManifest {
	private Map<String, List<Camera>> cameras;

	private Logger logger = LoggerFactory.getLogger(CameraManifest.class);

	public CameraManifest() { cameras = new HashMap<>(); }

	public Map<String, List<Camera>> getCameras() {
		return cameras;
	}

	public void setCameras(Map<String, List<Camera>> cameras) {
		this.cameras = cameras;
	}
 
	public Camera getCameraById(String environment, String id) {
		for (Camera camera : getCamerasForEnvironment(environment)) {
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

	public List<Camera> getCamerasForAgent(Agent agent) {
		List<Camera> cameras = new ArrayList<>();

		for (Camera camera : getAllCameras()) {

		}

		return cameras;
	}

	public List<Camera> getCamerasForAgentServiceEndpoint(AgentServiceEndpoint agentServiceEndpoint) {
		List<Camera> cameras = new ArrayList<>();

		String agentServiceEndpointUrl = agentServiceEndpoint.getUrl();

		for (Camera camera : getAllCameras()) {
			AgentServiceEndpoint cameraAgentServiceEndpoint = camera.getAgentServiceEndpoint();
			if (cameraAgentServiceEndpoint == null) {
				continue;
			}

			if (cameraAgentServiceEndpoint.getUrl().equals(agentServiceEndpointUrl)) {
				cameras.add(camera);
			}
		}

		return cameras;
	}

	public boolean cameraExists(Camera camera) {
		for (Camera existingCamera : getAllCameras()) {
			if (existingCamera.getId().equals(camera.getId())) {
				return true;
			}
		}

		return false;
	}

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
		addCamera(camera.getEnvironment(), camera);
	}

	public void addCamera(String environment, Camera camera) {
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



	private boolean isDisconnected(Camera camera, int collectCyclesBeforeCameraIsDisconnected) {
		long cameraLastUpdateEpoch = camera.getLastUpdateEpoch();
		if (cameraLastUpdateEpoch == 0) {
			return false;
		}

		long nowEpoch = System.currentTimeMillis();
		long delayInMs = collectCyclesBeforeCameraIsDisconnected * camera.getSleepTimeInSeconds() * 1000;
		long futureEpoch = cameraLastUpdateEpoch + delayInMs;

		return (nowEpoch > futureEpoch);
	}

	private boolean isLatent(Camera camera) {
		long cameraLastUpdateEpoch = camera.getLastUpdateEpoch();
		if (cameraLastUpdateEpoch == 0) {
			return true;
		}

		long nowEpoch = System.currentTimeMillis();
		long futureEpoch = cameraLastUpdateEpoch + (camera.getSleepTimeInSeconds() * 1000);

		return (nowEpoch > futureEpoch);
	}

	public List<Camera> getLatentCameras() {
		List<Camera> latentCameras = new ArrayList<>();

		for (Camera camera : getAllCameras()) {
			if (isLatent(camera)) {
				latentCameras.add(camera);
			}
		}

		return latentCameras;
	}


	public List<Camera> getDisconnectedCameras(int collectCyclesBeforeCameraIsDisconnected) {
		List<Camera> disconnectedCameras = new ArrayList<>();

		for (Camera camera : getAllCameras()) {
			if (isDisconnected(camera, collectCyclesBeforeCameraIsDisconnected)) {
				disconnectedCameras.add(camera);
			}
		}

		return disconnectedCameras;
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("cameras", cameras)
				.toString();
	}
}