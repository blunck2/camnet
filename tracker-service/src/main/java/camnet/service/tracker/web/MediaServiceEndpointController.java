package camnet.service.tracker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;


import javax.annotation.PostConstruct;

import camnet.model.MediaServiceEndpoint;


@RestController
@RequestMapping("/server")
public class MediaServiceEndpointController {
  private MediaServiceEndpoint endpoint;

  private Logger logger = LoggerFactory.getLogger(MediaServiceEndpointController.class);


  @PostConstruct
  public void setUp() {
  }

  @RequestMapping("/endpoint")
  public MediaServiceEndpoint getEndpoint() {
    return endpoint;
  }

  @PostMapping("/endpoint")
  public MediaServiceEndpoint setEndpoint(@RequestBody MediaServiceEndpoint endpoint) {
    logger.info("media service loaded: " + endpoint.getUrl());

    endpoint = endpoint;

    return endpoint;
  }
}