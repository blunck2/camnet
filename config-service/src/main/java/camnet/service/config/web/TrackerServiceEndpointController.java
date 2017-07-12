package camnet.service.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

import java.util.List;
import java.util.ArrayList;

import camnet.model.TrackerServiceEndpoint;


@RestController
@RequestMapping("/tracker")
public class TrackerServiceEndpointController {
  private List<TrackerServiceEndpoint> endpoints;

  private Logger logger = LoggerFactory.getLogger(TrackerServiceEndpointController.class);

  @PostConstruct
  public void setUp() {
    endpoints = new ArrayList<>();
  }

  @RequestMapping("/endpoints")
  public List<TrackerServiceEndpoint> getServiceEndpoints() {
    return endpoints;
  }

  @PostMapping("/endpoints")
  public List<TrackerServiceEndpoint> setServiceEndpoints(@RequestBody List<TrackerServiceEndpoint> endpoints) {
    logger.info("tracker endpoints: " + endpoints);

    this.endpoints = endpoints;
    return endpoints;
  }

  @PostMapping("/endpoint/add")
  public TrackerServiceEndpoint addServiceEndpoint(@RequestBody TrackerServiceEndpoint endpoint) {
    logger.info("adding endpoint: " + endpoint);

    if (! endpoints.contains(endpoint)) {
      endpoints.add(endpoint);
    }

    return endpoint;
  }
}