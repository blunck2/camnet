package camnet.service.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import camnet.model.AgentServiceEndpoint;


@RestController
@RequestMapping("/agent")
public class AgentServiceEndpointController {
  private List<AgentServiceEndpoint> endpoints;

  private Logger logger = LoggerFactory.getLogger(AgentServiceEndpointController.class);


  @PostConstruct
  public void setUp() {
    endpoints = new ArrayList<>();
  }

  @RequestMapping("/endpoints")
  public List<AgentServiceEndpoint> getServiceEndpoints() {
    return endpoints;
  }

  @PostMapping("/endpoints")
  public List<AgentServiceEndpoint> setServiceEndpoints(@RequestBody List<AgentServiceEndpoint> endpoints) {
    logger.info("agent endpoints: " + endpoints);

    this.endpoints = endpoints;
    return endpoints;
  }

  @PostMapping("/endpoint/add")
  public AgentServiceEndpoint addServiceEndpoint(@RequestBody AgentServiceEndpoint endpoint) {
    logger.info("adding endpoint: " + endpoint);

    if (! endpoints.contains(endpoint)) {
      endpoints.add(endpoint);
    }

    return endpoint;
  }

}