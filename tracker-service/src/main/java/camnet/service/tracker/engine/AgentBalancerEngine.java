package camnet.service.tracker.engine;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;


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
import java.util.ArrayList;
import static java.util.concurrent.TimeUnit.*;

import javax.annotation.PostConstruct;


@Component
public class AgentBalancerEngine implements Runnable {
  private AgentManifest agentManifest;

  private CameraManifest cameraManifest;

  @Autowired
  private TrackerEngine trackerEngine;


  @Value("${AgentBalancer.CycleTimeInSeconds}")
  private int cycleTimeInSeconds;

  @Value("${AgentBalancer.CameraCycleMissCountBeforeReassignment}")
  private int cameraCycleMissCountBeforeReassignment;

  @Value("${AgentBalancer.AgentDisconnectedDelayTimeInSeconds}")
  private int agentDisconnectedDelayTimeInSeconds;

  private ScheduledExecutorService scheduler;

  private Logger logger = LoggerFactory.getLogger(AgentBalancerEngine.class);

  @PostConstruct
  private void setUp() {
    agentManifest = trackerEngine.getAgentManifest();
    logger.info("------ got agent manifest with HC: " + agentManifest.hashCode());
    cameraManifest = trackerEngine.getCameraManifest();
  }



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

  public int getCameraCycleMissCountBeforeReassignment() {
    return cameraCycleMissCountBeforeReassignment;
  }

  public void setCameraCycleMissCountBeforeReassignment(int cameraCycleMissCountBeforeReassignment) {
    this.cameraCycleMissCountBeforeReassignment = cameraCycleMissCountBeforeReassignment;
  }

  public int getAgentDisconnectedDelayTimeInSeconds() {
    return agentDisconnectedDelayTimeInSeconds;
  }

  public void setAgentDisconnectedDelayTimeInSeconds(int agentDisconnectedDelayTimeInSeconds) {
    this.agentDisconnectedDelayTimeInSeconds = agentDisconnectedDelayTimeInSeconds;
  }

  public TrackerEngine getTrackerEngine() {
    return trackerEngine;
  }

  public void setTrackerEngine(TrackerEngine trackerEngine) {
    this.trackerEngine = trackerEngine;
  }

  @PostConstruct
  public void start() {
    logger.info("scheduling at fixed rate: " + cycleTimeInSeconds);
    scheduler.scheduleAtFixedRate(this, cycleTimeInSeconds, cycleTimeInSeconds, SECONDS);
  }

  private AgentManifest retrieveAgentManifest() {
    // TODO: call localhost tracker to get the agent manifest
    return agentManifest;
  }


  public void run() {
    try {
      reassignCamerasDueToDisconnectedAgent();
      reassignDisconnectedCameras();
    } catch (Throwable t) {
      logger.error("unexpected error occurred rebalancing cameras", t);
    }
  }

  private void reassignCamerasDueToDisconnectedAgent() {
    logger.trace("examining manifest: agents");
    // FIXME: change 60 to be some variable that indicates how long we should wait for an agent to time out before we reassign it's cameras
    List<Agent> allAgents = agentManifest.getActiveAgents(60);

    for (Agent agent : allAgents) {
      int heartBeatSleepTimeInMillis = 30;
      long lastUpdateMillis = agent.getLastHeartBeatEpoch();
      long nowMillis = System.currentTimeMillis();

      long delayMillis = nowMillis - lastUpdateMillis;

      long projectedMillisAfterTimeout = lastUpdateMillis + (agentDisconnectedDelayTimeInSeconds * 1000);

      if (nowMillis > projectedMillisAfterTimeout) {
        reassignCamerasDueToDisconnectedAgent(agent);
      }

    }
  }

  private void reassignCamerasDueToDisconnectedAgent(Agent agent) {
    deTaskCamerasForAgent(agent);
    taskCamerasForAgent(agent);
  }


  private void reassignDisconnectedCameras() {
    logger.trace("examining manifest: cameras");

    List<Camera> allCameras = cameraManifest.getAllCameras();
    for (Camera camera : allCameras) {
      long lastUpdateMillis = camera.getLastUpdateEpoch();
      long sleepTimeMillis = camera.getSleepTimeInSeconds() * 1000;
      long now = System.currentTimeMillis();

      if (now > (lastUpdateMillis + sleepTimeMillis)) {
        reassignCameraDueToTimeOutFromLocalAgent(camera);
      }
    }
  }

  private void reassignCameraDueToTimeOutFromLocalAgent(Camera camera) {
    logger.info("reassigning camera due to latency: " + camera);
  }


  private void deTaskCamerasForAgent(Agent agent) {
    logger.info("detasking cameras for agent: " + agent);

    List<Camera> allCameras = new ArrayList<>();

    for (Camera camera : cameraManifest.getAllCameras()) {
      logger.info("detasking camera: " + camera);
    }
  }


  private Agent taskCamerasForAgent(Agent agent) {
  logger.info("tasking cameras for agent: " + agent);

    for (String environment : agent.getEnvironments()) {
      List<Camera> cameras = cameraManifest.getCamerasForEnvironment(environment);

      for (Camera camera : cameraManifest.getAllCameras()) {
        logger.info("tasking camera: " + camera);
      }
    }

    return agent;
  }


  private void reassignLatentCameras() {
    logger.info("examining manifest: cameras");

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

      long projectedMillisAfterTimeout = lastUpdateMillis + (cameraCycleMissCountBeforeReassignment * cycleTimeInSeconds * 1000);

      if (nowMillis > projectedMillisAfterTimeout) {
        deTask(camera);
        task(camera);
      }
    }
  }

  private Agent chooseAgent(String environment) {
    return agentManifest.findAgent(environment);
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