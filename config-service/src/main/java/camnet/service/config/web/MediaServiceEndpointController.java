package camnet.service.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import camnet.model.MediaServiceEndpoint;


@RestController
@RequestMapping("/media")
public class MediaServiceEndpointController {
  private List<MediaServiceEndpoint> endpoints;

  private Logger logger = LoggerFactory.getLogger(MediaServiceEndpointController.class);


  @PostConstruct
  public void setUp() {
    endpoints = new ArrayList<>();
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

  @PostMapping("/endpoint/add")
  public MediaServiceEndpoint addServiceEndpoint(@RequestBody MediaServiceEndpoint endpoint) {
    logger.info("adding endpoint: " + endpoint);

    if (! endpoints.contains(endpoint)) {
      endpoints.add(endpoint);
    }

    return endpoint;
  }
}