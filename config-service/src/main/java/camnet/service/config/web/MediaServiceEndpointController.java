package camnet.service.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.annotation.PostConstruct;

import camnet.model.MediaServiceEndpoint;


@RestController
@RequestMapping("/media")
public class MediaServiceEndpointController {
  private List<MediaServiceEndpoint> endpoints;

  private Logger logger = LoggerFactory.getLogger(MediaServiceEndpointController.class);


  @PostConstruct
  public void setUp() {
  }

  @RequestMapping("/endpoints")
  public List<MediaServiceEndpoint> getServiceEndpoints() {
    return endpoints;
  }

  @PostMapping("/endpoints")
  public List<MediaServiceEndpoint> setServiceEndpoints(@RequestBody List<MediaServiceEndpoint> endpoints) {
    logger.info("media endpoints: " + endpoints);

    this.endpoints = endpoints;
    return endpoints;
  }
}