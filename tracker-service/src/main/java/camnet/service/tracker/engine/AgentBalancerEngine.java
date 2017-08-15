package camnet.service.tracker.engine;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.concurrent.TimeUnit.*;

import camnet.model.AgentManifest;
import camnet.model.Agent;
import camnet.model.AgentServiceEndpoint;
import camnet.model.CameraManifest;
import camnet.model.Camera;
import camnet.model.NoSuchAgentException;
import camnet.service.tracker.util.TaskingUtility;
import camnet.service.tracker.util.TaskingException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import static java.util.concurrent.TimeUnit.*;

import javax.annotation.PostConstruct;


@Component
public class AgentBalancerEngine implements Runnable {
  private AgentManifest agentManifest;

  private CameraManifest cameraManifest;

  @Autowired
  private TrackerEngine trackerEngine;

  private RestTemplate agentService;


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
    agentService = new RestTemplate();
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
      reassignLatentCameras();
    } catch (Throwable t) {
      logger.error("unexpected error occurred rebalancing cameras", t);
    }
  }

  private void reassignLatentCameras() {
    logger.info("looking for latent cameras");
    List<Camera> latentCameras = cameraManifest.getLatentCameras();

    logger.trace("reassigning latent cameras: " + latentCameras);

    for (Camera camera : latentCameras) {
      reassignLatentCamera(camera);
    }

    logger.trace("finished reassigning latent cameras");
  }

  private int chooseRandom(int minimum, int maximum) {
    if (minimum == maximum) {
      return minimum;
    }

    int randomNumber = ThreadLocalRandom.current().nextInt(minimum, maximum);
    return randomNumber;
  }

  private Agent pickAgent(List<Agent> agents, String environment) {
    // attempt to find an Agent local to the environment provided
    for (Agent agent : agents) {
      List<String> supportedEnvironments = agent.getEnvironments();
      if (supportedEnvironments.contains(environment)) {
        return agent;
      }
    }

    // if a local Agent could not be located just pick a random Agent
    int agentPos = chooseRandom(0, agents.size());
    return agents.get(agentPos);
  }

  private void reassignLatentCamera(Camera camera) {
    logger.info("attempting to reassign latent camera: " + camera);
    String environment = camera.getEnvironment();
    List<Agent> agents = agentManifest.findActiveAgents(environment);
    logger.info("available agents that support environment '" + environment + "': " + agents);
    if (agents.size() == 0) {
      logger.info("unable to reassign camera '" + camera.getDisplayName() + "' to new agent.  no active agents available.");
      return;
    }

    Agent agent = pickAgent(agents, environment);
    logger.info("selected agent: " + agent);

    logger.trace("assigning agent endpoint for camera '" + camera.getDisplayName() + "': " + agent.getServiceEndpoint());
    camera.setAgentServiceEndpoint(agent.getServiceEndpoint());

    String agentServiceEndpointUrl = agent.getServiceEndpoint().getUrl();
    String url = agentServiceEndpointUrl + "/agent/camera/add";

    logger.trace("registering POST'ing camera to agent with url: " + url);

    agentService.postForObject(url, camera, Camera.class, new HashMap<String, String>());
  }


}