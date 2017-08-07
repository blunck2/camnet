package camnet.service.tracker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import camnet.model.Agent;
import camnet.model.AgentManifest;
import camnet.model.Camera;
import camnet.model.CameraManifest;

/**
 * Facilitates the tasking and detasking of Cameras as well as the details of updating manifests.
 */
public class TaskingUtility {

  private static final Logger logger = LoggerFactory.getLogger(TaskingUtility.class);


  /**
   * Private constructor to enforce non-instantiability
   */
  private TaskingUtility() {}

  /**
   * Reassigns the camera from the existingAgent to the newAgent while notifying the corresponding
   * manifests of the change
   *
   * @param camera         the camera to reassign
   * @param existingAgent  the previous agent responsible for collecting the camera
   * @param newAgent       the new agent that will collect the camera
   * @param agentManifest  the manifest that holds all of the agents
   * @param cameraManifest the manifest that holds all of the cameras
   * @throws TaskingException if an error occurs while reassigning
   */
  public static void reassign(Camera camera,
                              Agent existingAgent,
                              Agent newAgent,
                              AgentManifest agentManifest,
                              CameraManifest cameraManifest) throws TaskingException {

    /*
     * step #1: attempt to detask the camera from the existing agent.
     * this may fail if the existing agent is down and cannot accept tasking requests
     */
    try {
      deTaskCamera(camera, existingAgent);
    } catch (TaskingException e) {
      logger.warn("unable to detask camera '" + camera.getDisplayName() + "' from agent: " + existingAgent);
      logger.warn("this occurs when the agent is down and unable to accept tasking requests");
    }

    /*
     * step #2: attempt to task the new agent with the camera.
     * this may fail if the new agent is down and if that occurs a TaskingException will be
     * thrown and that will then terminate this method.  this is fine because the manifests
     * will not be updated and another rebalancing or offline detection will then attempt
     * to reassign the camera
     */
    taskCamera(camera, newAgent);

    /*
     * step #3: reassign the camera in the AgentManifest
     * remove the camera from the existing agent and assign the camera to the new agent
     */
    changeAssignedAgent(camera, existingAgent, newAgent, agentManifest);




  }

  /**
   * Detasks the camera from the agent provided
   *
   * @param camera the camera to detask
   * @param agent  the agent to detask the camera from
   * @throws TaskingException if an error occurs contacting the agent
   */
  private static void deTaskCamera(Camera camera, Agent agent) throws TaskingException {
    logger.info("TODO:  call the Agent, detask the camera");
  }


  /**
   * Tasks the camera provided to the agent provider
   *
   * @param camera the camera to task
   * @param agent  the agent to task the camera to
   * @throws TaskingException if the agent cannot be tasked
   */
  private static void taskCamera(Camera camera, Agent agent) throws TaskingException {
    logger.info("TODO:  call the Agent, task the camera");
  }


  /**
   * Updates the AgentManifest by removing the Camera from the existingAgent and adding it to the newAgent.
   * After this method is invoked the agentServiceEndpoint field in the Camera mutates to the newAgent's service URL
   *
   * @param camera        the camera to modify
   * @param existingAgent the agent to remove the camera from
   * @param newAgent      the agent to add the camera to
   * @param manifest      the AgentManifest in which to make the changes
   */
  private static void changeAssignedAgent(Camera camera, Agent existingAgent, Agent newAgent, AgentManifest manifest) {
    logger.info("TODO:  remove camera '" + camera.getDisplayName() + "' from existing agent: " + existingAgent);
    logger.info("TODO:  add camera '" + camera.getDisplayName() + "' to new agent: " + newAgent);
    logger.info("TODO:  update camera's agentServiceEndpoint to the newAgent's service url");
  }


  /**
   * Updates the CameraManifest with the Camera provided
   * @param camera the Camera to update in the manifest
   * @param manifest the manifest to update
   */
  private static void updateCameraManifest(Camera camera, CameraManifest manifest) {
    logger.info("TODO:  replace the Camera in the manifest with the instance provided");
  }


}