package camnet.commons.util;

import camnet.model.Camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class that contains useful methods to execute against Camera instances
 */
public class CameraUtility {

  private static final Logger logger = LoggerFactory.getLogger(CameraUtility.class);


  /**
   * Returns true if the agent collecting for the camera is not heartbeating, as indicated by the
   * camera not being actively collected more than cyclesToCount times.
   *
   * @param camera the camera to inspect
   * @param cyclesToCount the number of cycles to consider before the camera is determined to be latent
   * @return true if the camera has been collected on within the number of cycles, false otherwise
   */
  public static boolean isCameraAgentDisconnected(Camera camera, int cyclesToCount) {
    long lastUpdateEpoch = camera.getLastUpdateEpoch();

    if (lastUpdateEpoch == 0) {
      return true;
    }


    long now = System.currentTimeMillis();

    int sleepTimeInSeconds = camera.getSleepTimeInSeconds();
    long totalPermissableSleepTimeInMilliSeconds = sleepTimeInSeconds * cyclesToCount * 1000;


    logger.info("camera lastUpdateepoch: " + lastUpdateEpoch);

    long projectedAcceptableFutureCollectTimeEpoch = lastUpdateEpoch + totalPermissableSleepTimeInMilliSeconds;

    return (now < projectedAcceptableFutureCollectTimeEpoch);
  }
}