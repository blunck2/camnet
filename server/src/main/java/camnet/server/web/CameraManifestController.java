package camnet.server.web;

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

  	@RequestMapping("/cameras")
  	public List<Camera> getAllCameras() {
  		return manifest.getCameras();
  	}

  	@RequestMapping("/cameras/{id}")
  	public Camera getCameraById(@PathVariable("id") String id) {
  		return manifest.getCameraById(id);
  	}

	@PostMapping("/cameras")
	public Camera setCameraById(@RequestBody Camera camera) {
		manifest.addCamera(camera);
		return manifest.getCameraById(camera.getId());
	}


}
