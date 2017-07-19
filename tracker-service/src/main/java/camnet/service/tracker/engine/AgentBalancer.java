package camnet.service.tracker.engine;

import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.concurrent.TimeUnit.*;

import camnet.model.CameraManifest;
import camnet.model.Camera;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import static java.util.concurrent.TimeUnit.*;


public class AgentBalancer implements Runnable {
  private CameraManifest manifest;

  private RestTemplate configService;

  private int cycleTimeInSeconds;

  private int cycleMissCountBeforeReassignment;

  private ScheduledExecutorService scheduler;

  private Logger logger = LoggerFactory.getLogger(AgentBalancer.class);



  public AgentBalancer() {
    configService = new RestTemplate();
    scheduler = Executors.newScheduledThreadPool(1);
  }

  public CameraManifest getManifest() {
    return manifest;
  }

  public void setManifest(CameraManifest manifest) {
    this.manifest = manifest;
  }

  public int getCycleTimeInSeconds() {
    return cycleTimeInSeconds;
  }

  public void setCycleTimeInSeconds(int cycleTimeInSeconds) {
    this.cycleTimeInSeconds = cycleTimeInSeconds;
  }

  public int getCycleMissCountBeforeReassignment() {
    return cycleMissCountBeforeReassignment;
  }

  public void setCycleMissCountBeforeReassignment(int cycleMissCountBeforeReassignment) {
    this.cycleMissCountBeforeReassignment = cycleMissCountBeforeReassignment;
  }

  public void start() {
    logger.info("scheduling at fixed rate: " + cycleTimeInSeconds);
    scheduler.scheduleAtFixedRate(this, 0, cycleTimeInSeconds, SECONDS);
  }


  public void run() {
    List<Camera> allCameras = manifest.getAllCameras();
    for (Camera camera : allCameras) {
      int sleepTimeInMillis = camera.getSleepTimeInSeconds() * 1000;
      long lastUpdateMillis = camera.getLastUpdateEpoch();
      long nowMillis = System.currentTimeMillis();

      long delayMillis = nowMillis - lastUpdateMillis;

      long projectedMillisAfterTimeout = lastUpdateMillis + (cycleMissCountBeforeReassignment * cycleTimeInSeconds * 1000);

      if (nowMillis > projectedMillisAfterTimeout) {
        deTask(camera);
        task(camera);
      }

    }
    logger.info("examining agents...");
  }


  private void deTask(Camera camera) {
    logger.info("detasking camera: " + camera);
  }

  private void task(Camera camera) {
    logger.info("tasking camera: " + camera);
  }



}