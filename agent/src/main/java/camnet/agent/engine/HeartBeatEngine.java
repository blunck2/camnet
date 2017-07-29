package camnet.agent.engine;

import camnet.model.TrackerServiceEndpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.PostConstruct;



@Component
public class HeartBeatEngine {
  @Value("${ConfigurationServiceUrl}")
  private String configurationServiceUrl;

  @Value("${ConfigurationServiceUserName}")
  private String configurationServiceUserName;

  @Value("${ConfigurationServicePassWord}")
  private String configurationServicePassword;

  private RestTemplate configService;

  private List<TrackerServiceEndpoint> trackerServiceEndpoints;

  private Logger logger = LoggerFactory.getLogger(HeartBeatEngine.class);

  public HeartBeatEngine() {
    configService = new RestTemplate();
  }


  @PostConstruct
  public void init() {
    loadTrackerServiceEndpoints();

    startHeartBeats();
  }

  private void loadTrackerServiceEndpoints() {
    logger.trace("retrieving tracker service endpoint from configuration service");

    String url = configurationServiceUrl + "/tracker/endpoints";
    ResponseEntity<TrackerServiceEndpoint[]> trackerResponseEntities = configService.getForEntity(url, TrackerServiceEndpoint[].class);

    trackerServiceEndpoints = new ArrayList<>();

    int count = 0;
    for (TrackerServiceEndpoint trackerServiceEndpoint : trackerResponseEntities.getBody()) {
      trackerServiceEndpoints.add(trackerServiceEndpoint);
      count++;
    }

    logger.trace("loaded " + count + " tracker service endpoints");
  }


  private void startHeartBeats() {
    logger.info("starting heartbeat");
  }

}