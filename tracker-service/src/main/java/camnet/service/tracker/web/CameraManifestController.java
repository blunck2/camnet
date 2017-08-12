package camnet.service.tracker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


import camnet.model.CameraManifest;
import camnet.model.Camera;

import camnet.commons.util.CameraUtility;

import camnet.service.tracker.engine.TrackerEngine;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.ArrayList;


@RestController
@RequestMapping("/manifest")
public class CameraManifestController {
  @Autowired
  private TrackerEngine engine;

  private CameraManifest manifest;

  @Value("${CameraManifestController.CollectCyclesToCountBeforeAgentIsDisconnected}")
  private int collectCyclesToCountBeforeAgentIsDisconnected;

  private Logger logger = LoggerFactory.getLogger(CameraManifestController.class);

  @PostConstruct
  private void setUp() {
    manifest = engine.getCameraManifest();
  }

  public int getCollectCyclesToCountBeforeAgentIsDisconnected() {
    return collectCyclesToCountBeforeAgentIsDisconnected;
  }
  public void setCollectCyclesToCountBeforeAgentIsDisconnected(int collectCyclesToCountBeforeAgentIsDisconnected) {
    this.collectCyclesToCountBeforeAgentIsDisconnected = collectCyclesToCountBeforeAgentIsDisconnected;
  }

  @RequestMapping("/cameras")
  public List<Camera> getAllCameras() {
    logger.info("camera size: " + manifest.getAllCameras().size());
    return manifest.getAllCameras();
  }

  @RequestMapping("/cameras/environment/{environment}")
  public List<Camera> getCamerasByEnvironment(@PathVariable("environment") String environment) {
    List<Camera> response = manifest.getCamerasByEnvironment(environment);
    return response;
  }

  @RequestMapping("/cameras/environment/start/{environment}")
  public List<Camera> getCamerasToStartByEnvironment(@PathVariable("environment") String environment) {
    List<Camera> camerasForEnvironment = manifest.getCamerasByEnvironment(environment);
    List<Camera> camerasToStart = new ArrayList<>();

    for (Camera camera : camerasForEnvironment) {
      boolean startCamera = CameraUtility.isCameraAgentDisconnected(camera, collectCyclesToCountBeforeAgentIsDisconnected);

      if (startCamera) {
        camerasToStart.add(camera);
      }
    }

    return camerasToStart;
  }

  @RequestMapping("/cameras/environment/initial/{environment}")
  public List<Camera> getInitialCamerasByEnvironment(@PathVariable("environment") String environment) {
    List<Camera> camerasForEnvironment = manifest.getCamerasByEnvironment(environment);
    List<Camera> inActiveCameras = new ArrayList<>();

    for (Camera camera : camerasForEnvironment) {
      boolean cameraIsInActive = CameraUtility.isCameraAgentDisconnected(camera, collectCyclesToCountBeforeAgentIsDisconnected);

      if (cameraIsInActive) {
        inActiveCameras.add(camera);
      }
    }

    return inActiveCameras;
  }


  @PostMapping("/cameras/environment/{environment}/camera/{cameraId}/posted")
  public Camera setCameraPostTime(@PathVariable("environment") String environment,
                                  @PathVariable("cameraId") String cameraId) {
    Camera camera = manifest.getCameraById(environment, cameraId);
    long currentTimeMillis = System.currentTimeMillis();
    logger.trace("setting lastUpdateEpoch for camera '" + camera.getDisplayName() + "' to: " + currentTimeMillis);
    camera.setLastUpdateEpoch(currentTimeMillis);

    return camera;
  }

  @RequestMapping("/cameras/environment/{environment}/camera/{cameraId}")
  public Camera getCameraById(@PathVariable("environment") String environment,
                              @PathVariable("cameraId") String cameraId) {
    return manifest.getCameraById(environment, cameraId);
  }

  @PostMapping("/cameras/environment/{environment}/camera/{cameraId}/sleepTimeInSeconds/{sleepTimeInSeconds}")
  public Camera setCameraBySleepTimeInSeconds(@PathVariable("environment") String environment,
                                              @PathVariable("cameraId") String cameraId,
                                              @PathVariable("sleepTimeInSeconds") Integer sleepTimeInSeconds) {
    Camera camera = manifest.getCameraById(environment, cameraId);
    int oldSleepTimeInSeconds = camera.getSleepTimeInSeconds();
    if (oldSleepTimeInSeconds != sleepTimeInSeconds) {
      logger.info("sleep time changed for '" + camera.getDisplayName()+ "' camera: " +
          oldSleepTimeInSeconds + "s -> " + sleepTimeInSeconds + "s");
    }

    camera.setSleepTimeInSeconds(sleepTimeInSeconds);
    return camera;
  }

  @PostMapping("/cameras/environment/{environment}/camera/{cameraId}")
  public Camera setCameraById(@PathVariable("environment") String environment,
                              @PathVariable("cameraId") String cameraId,
                              @RequestBody Camera camera) {
    manifest.addCamera(camera);
    return manifest.getCameraById(environment, camera.getId());
  }

  @PostMapping("/cameras")
  public void setAllCameras(@RequestBody List<Camera> cameras) {
    for (Camera camera : cameras) {
      String environment = camera.getEnvironment();
      String cameraId = camera.getId();
      setCameraById(environment, cameraId, camera);
      logger.info("loading camera: " + environment + "/" + cameraId);
    }

    logger.info("camera manifest size: " + manifest.getAllCameras().size());
  }
}
