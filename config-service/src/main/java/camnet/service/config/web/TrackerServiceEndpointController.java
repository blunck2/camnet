package camnet.service.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import javax.annotation.PostConstruct;

import camnet.model.TrackerServiceEndpoint;


@RestController
@RequestMapping("/tracker")
public class TrackerServiceEndpointController {
  private TrackerServiceEndpoint endpoint;

  private Logger logger = LoggerFactory.getLogger(TrackerServiceEndpointController.class);


  @PostConstruct
  public void setUp() {
  }

  @RequestMapping("/endpoint")
  public TrackerServiceEndpoint getServiceEndpoint() {
    return endpoint;
  }

  @PostMapping("/endpoint")
  public TrackerServiceEndpoint setServiceEndpoint(@RequestBody TrackerServiceEndpoint endpoint) {
    logger.info("tracker endpoint: " + endpoint.getUrl());

    this.endpoint = endpoint;
    return endpoint;
  }
}