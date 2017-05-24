package camnet.server.web;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


import camnet.server.model.CameraManifest;
import camnet.server.model.Camera;

import java.util.List;


@RestController
@RequestMapping("/manifest")
public class CameraManifestController {
	@Autowired
	private CameraManifest manifest;

	private Logger logger = Logger.getLogger(ImageController.class);

	@RequestMapping("/cameras")
  	public List<Camera> getAllCameras() {
  		return manifest.getAllCameras();
  	}

	@RequestMapping("/cameras/house/{houseName}")
	public List<Camera> getCamerasByHouseName(@PathVariable("houseName") String houseName) {

		List<Camera> response = manifest.getCamerasByHouseName(houseName);
		return response;
	}

  	@RequestMapping("/cameras/house/{houseName}/camera/{cameraId}")
  	public Camera getCameraById(@PathVariable("houseName") String houseName,
								  @PathVariable("cameraId") String cameraId) {
		return manifest.getCameraById(houseName, cameraId);
  	}

	@PostMapping("/cameras/house/{houseName}/camera/{cameraId}/sleepTimeInSeconds/{sleepTimeInSeconds}")
	public Camera setCameraBySleepTimeInSeconds(@PathVariable("houseName") String houseName,
												@PathVariable("cameraId") String cameraId,
												@PathVariable("sleepTimeInSeconds") Integer sleepTimeInSeconds) {
		Camera camera = manifest.getCameraById(houseName, cameraId);
		int oldSleepTimeInSeconds = camera.getSleepTimeInSeconds();
		if (oldSleepTimeInSeconds != sleepTimeInSeconds) {
			logger.info("sleep time changed for '" + houseName + "/" + cameraId + "' camera: " +
					oldSleepTimeInSeconds + "s -> " + sleepTimeInSeconds + "s");
		}

		camera.setSleepTimeInSeconds(sleepTimeInSeconds);
		return camera;
	}

	@PostMapping("/cameras/house/{houseName}/camera/{cameraId}")
	public Camera setCameraById(@PathVariable("houseName") String houseName,
								@PathVariable("cameraId") String cameraId,
								@RequestBody Camera camera) {
		manifest.addCamera(camera);
		return manifest.getCameraById(camera.getHouseName(), camera.getId());
	}

	@PostMapping("/cameras")
	public void setAllCameras(@RequestBody List<Camera> cameras) {
		for (Camera camera : cameras) {
			String houseName = camera.getHouseName();
			String cameraId = camera.getId();
			setCameraById(houseName, cameraId, camera);
			logger.info("loading camera: " + houseName + "/" + cameraId);
		}
		logger.info("camera manifest size: " + manifest.getAllCameras().size());
	}


}
