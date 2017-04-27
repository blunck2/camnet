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
  		return manifest.getCameras();
  	}

  	@RequestMapping("/cameras/{id}")
  	public Camera getCameraById(@PathVariable("id") String id) {
  		logger.info("retrieving camera with id: " + id);
		return manifest.getCameraById(id);
  	}

	@PostMapping("/cameras")
	public Camera setCameraById(@RequestBody Camera camera) {
  		logger.info("manifest hashCode: " + manifest.hashCode());
		manifest.addCamera(camera);
		return manifest.getCameraById(camera.getId());
	}


}
