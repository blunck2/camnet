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
import camnet.service.tracker.util.TaskingUtility;
import camnet.service.tracker.util.TaskingException;

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
    logger.info("agent manifest HC: " + agentManifest.hashCode());
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
//      reassignDisconnectedCameras();
    } catch (Throwable t) {
      logger.error("unexpected error occurred rebalancing cameras", t);
    }
  }

  private void reassignCamerasDueToDisconnectedAgent() {
    logger.trace("examining manifest: agents");
    logger.info("agent manifest: " + agentManifest.hashCode());

    // FIXME: change 60 to be some variable that indicates how long we should wait for an agent to time out before we reassign it's cameras
    List<Agent> inActiveAgents = agentManifest.getInActiveAgents(60);

    for (Agent agent : inActiveAgents) {
      reassignCamerasDueToDisconnectedAgent(agent);
    }
  }


  private void reassignCamerasDueToDisconnectedAgent(Agent disconnectedAgent) {
    List<Camera> camerasToReassign = cameraManifest.getCamerasForAgent(disconnectedAgent);

    for (Camera cameraToReassign : camerasToReassign) {
      Agent newAgent = chooseAgent(cameraToReassign.getEnvironment());

      if (newAgent == null) {
        logger.error("agent is inactive: " + disconnectedAgent);
        logger.error("no other agents are available");
        return;
      }

      try {
        TaskingUtility.reassign(cameraToReassign, disconnectedAgent, newAgent, agentManifest, cameraManifest);
      } catch (TaskingException e) {
        logger.warn("failed to reassign camera: " + cameraToReassign.getDisplayName());
      }
    }
  }


  private void reassignDisconnectedCameras() {
    logger.trace("examining manifest: cameras");

    List<Camera> allCameras = cameraManifest.getAllCameras();
    for (Camera camera : allCameras) {
      long lastUpdateMillis = camera.getLastUpdateEpoch();
      long sleepTimeMillis = camera.getSleepTimeInSeconds() * 1000;
      long now = System.currentTimeMillis();

      if (now > (lastUpdateMillis + sleepTimeMillis)) {
        //reassignCameraDueToTimeOutFromLocalAgent(camera);
      }
    }
  }


  private Agent chooseAgent(String environment) {
    Agent selectedAgent = agentManifest.findAgent(environment);

    /*
    TODO: if no agents are available for the environment requested then choose an agent that is not local
    if (selectedAgent == null) {
      selectedAgent = agentManifest.getActiveAgent();
    }
    */

    return selectedAgent;
  }

}