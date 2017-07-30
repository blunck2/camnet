package camnet.agent.engine;

import java.util.List;

import camnet.model.Camera;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;


public class ScheduledHeartBeatSender implements Runnable {

  private ScheduledExecutorService scheduler;

  private int sleepTimeInSeconds;

  private Logger logger = LoggerFactory.getLogger(ScheduledHeartBeatSender.class);


  public ScheduledHeartBeatSender(ScheduledExecutorService scheduler, int sleepTimeInSeconds) {
    this.scheduler = scheduler;
    this.sleepTimeInSeconds = sleepTimeInSeconds;
  }


  public int getSleepTimeInSeconds() {
    return sleepTimeInSeconds;
  }

  public void setSleepTimeInSeconds(int sleepTimeInSeconds) {
    this.sleepTimeInSeconds = sleepTimeInSeconds;
  }

  public void run() {
    sendHeartBeat();
    scheduleNext(sleepTimeInSeconds, TimeUnit.SECONDS);
  }

  private void sendHeartBeat() {
    logger.trace("sending heartbeat to tracker");
  }

  private void scheduleNext(int sleepTime, TimeUnit unit) {
    scheduler.schedule(this, sleepTime, unit);
  }


  public void stop() {
  }

}