package camnet.agent.engine;

import java.util.List;
import java.util.ArrayList;

import camnet.model.Camera;
import camnet.model.Agent;
import camnet.model.TrackerServiceEndpoint;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;



public class ScheduledHeartBeatSender implements Runnable {
  private String userName;
  private String passWord;

  private ScheduledExecutorService scheduler;

  private int sleepTimeInSeconds;

  private RestTemplate template;

  private TrackerServiceEndpoint trackerServiceEndpoint;

  private List<String> environments;

  private Agent localAgent;

  private Logger logger = LoggerFactory.getLogger(ScheduledHeartBeatSender.class);


  public ScheduledHeartBeatSender(ScheduledExecutorService scheduler,
                                  int sleepTimeInSeconds,
                                  TrackerServiceEndpoint trackerServiceEndpoint,
                                  String userName,
                                  String passWord,
                                  List<String> environments,
                                  Agent localAgent) {
    this.userName = userName;
    this.passWord = passWord;
    this.scheduler = scheduler;
    this.sleepTimeInSeconds = sleepTimeInSeconds;
    this.environments = environments;
    this.localAgent = localAgent;
    this.template = new RestTemplate();
    this.trackerServiceEndpoint = trackerServiceEndpoint;
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
    logger.info("in sendHeartBeat()");
    String trackerEndpoint = trackerServiceEndpoint.getUrl();

    for (String environment : environments) {
      String url = trackerEndpoint + "/manifest/agents/environment/" + environment + "/agent/" + localAgent.getId() + "/heartbeat";
      logger.info("sending heartbeat: " + url);

      ResponseEntity<String> result =
          template.exchange(url,
              HttpMethod.POST,
              null,
              String.class);

    }
  }

  private void scheduleNext(int sleepTime, TimeUnit unit) {
    scheduler.schedule(this, sleepTime, unit);
  }


  public void stop() {
  }

}