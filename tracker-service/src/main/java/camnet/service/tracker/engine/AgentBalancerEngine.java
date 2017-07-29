package camnet.service.tracker.engine;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.concurrent.TimeUnit.*;

import camnet.model.AgentManifest;
import camnet.model.Agent;
import camnet.model.CameraManifest;
import camnet.model.Camera;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import static java.util.concurrent.TimeUnit.*;


@Component
public class AgentBalancerEngine implements Runnable {
  private AgentManifest agentManifest;

  private CameraManifest cameraManifest;


  @Value("${AgentBalancer.CycleTimeInSeconds}")
  private int cycleTimeInSeconds;

  @Value("${AgentBalancer.CycleMissCountBeforeReassignment}")
  private int cycleMissCountBeforeReassignment;



  private ScheduledExecutorService scheduler;

  private Logger logger = LoggerFactory.getLogger(AgentBalancerEngine.class);



  public AgentBalancerEngine() {
    scheduler = Executors.newScheduledThreadPool(1);
  }

  public AgentManifest getAgentManifest() {
    return agentManifest;
  }

  public void setAgentManifest(AgentManifest manifest) {
    this.agentManifest = manifest;
  }

  public void setCameraManifest(CameraManifest manifest) {
    this.cameraManifest = manifest;
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

  private AgentManifest retrieveAgentManifest() {
    // TODO: call localhost tracker to get the agent manifest
    return agentManifest;
  }


  public void run() {
    AgentManifest agentManifest = retrieveAgentManifest();

    List<Camera> allCameras = cameraManifest.getAllCameras();
    for (Camera camera : allCameras) {
      if (camera.getAgent() == null) {
        task(camera);
        continue;
      }

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

  private Agent chooseAgent(String environment) {
    // TODO:  want to choose the agent local to the environment but if the agent isn't recent then choose another

    return null;
  }


  private void deTask(Camera camera) {
    logger.info("detasking camera: " + camera);
    camera.setAgent(null);
  }

  private void task(Camera camera) {
    logger.info("tasking camera: " + camera);

    String environment = camera.getEnvironment();
    Agent agent = chooseAgent(environment);

  }



}