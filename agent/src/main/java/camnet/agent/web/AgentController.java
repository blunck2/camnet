package camnet.agent.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import camnet.model.Camera;
import camnet.model.CameraManifest;


import camnet.agent.engine.AgentEngine;

import javax.annotation.PostConstruct;
import java.util.List;


@RestController
@RequestMapping("/agent")
public class AgentController {
  @Autowired
  private AgentEngine engine;

  private CameraManifest cameraManifest;

  private Logger logger = LoggerFactory.getLogger(AgentController.class);

  @PostConstruct
  public void setUp() {
    cameraManifest = engine.getCameraManifest();
  }

  @RequestMapping("/cameras")
  public List<Camera> getAllCameras() {
    logger.info("camera size: " + cameraManifest.getAllCameras().size());
    return cameraManifest.getAllCameras();
  }

  @PostMapping("/camera/add")
  public Camera add(@RequestBody Camera camera) {
    logger.info("adding camera: " + camera);

    engine.startCamera(camera);

    return camera;
  }

  @RequestMapping(value = "camera/{id}", method = RequestMethod.DELETE)
  public List<Camera> delete(@PathVariable("id") String id) {
    cameraManifest.removeCameraById(id);
    return cameraManifest.getAllCameras();

  }
}