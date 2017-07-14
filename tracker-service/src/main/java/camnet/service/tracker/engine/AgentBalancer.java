package camnet.service.tracker.engine;

import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.concurrent.TimeUnit.*;

import camnet.model.CameraManifest;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.*;


public class AgentBalancer implements Runnable {
  private CameraManifest manifest;

  private RestTemplate configService;

  private int cycleTimeInSeconds;

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

  public void start() {
    logger.info("scheduling at fixed rate: " + cycleTimeInSeconds);
    scheduler.scheduleAtFixedRate(this, 0, cycleTimeInSeconds, SECONDS);
  }


  public void run() {
    logger.info("examining agents...");
  }



}