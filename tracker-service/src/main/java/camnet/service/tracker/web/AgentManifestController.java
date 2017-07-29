package camnet.service.tracker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


import camnet.model.AgentManifest;
import camnet.model.Agent;

import camnet.service.tracker.engine.TrackerEngine;
import camnet.service.tracker.engine.AgentBalancerEngine;

import javax.annotation.PostConstruct;
import java.util.List;


@RestController
@RequestMapping("/manifest")
public class AgentManifestController {
  @Autowired
  private TrackerEngine trackerEngine;

  @Autowired
  private AgentBalancerEngine agentBalancerEngine;

  @Autowired
  private AgentBalancerEngine balancer;

  private AgentManifest manifest;

  private Logger logger = LoggerFactory.getLogger(AgentManifestController.class);

  @PostConstruct
  private void setUp() {
    manifest = trackerEngine.getAgentManifest();
  }

  @RequestMapping("/agents")
  public List<Agent> getAllAgents() {
    AgentManifest manifest = trackerEngine.getAgentManifest();
    logger.info("agent size: " + manifest.getAllAgents().size());
    return manifest.getAllAgents();
  }

  @RequestMapping("/agents/environment/{environment}")
  public List<Agent> getAgentsByEnvironment(@PathVariable("environment") String environment) {
    List<Agent> response = manifest.getAgentsByEnvironment(environment);
    return response;
  }

  @RequestMapping("/agents/environment/{environment}/agent/{agentId}")
  public Agent getAgentById(@PathVariable("environment") String environment,
                            @PathVariable("agentId") String agentId) {
    return manifest.getAgentById(environment, agentId);
  }

  @PostMapping("/agents/environment/{environment}/agent/{agentId}")
  public Agent setAgentById(@PathVariable("environment") String environment,
                            @PathVariable("agentId") String agentId,
                            @RequestBody Agent agent) {
    manifest.addAgent(agent);
    return manifest.getAgentById(agent.getEnvironment(), agent.getId());
  }

  @PostMapping("/agents/add")
  public void addAgent(@RequestBody Agent agent) {
    String environment = agent.getEnvironment();
    String agentId = agent.getId();
    setAgentById(environment, agentId, agent);

    logger.info("loading agent: " + environment + "/" + agentId);
    logger.info("agent manifest size: " + manifest.getAllAgents().size());
  }

  @PostMapping("/agents")
  public void setAllAgents(@RequestBody List<Agent> agents) {
    for (Agent agent : agents) {
      String environment = agent.getEnvironment();
      String agentId = agent.getId();
      setAgentById(environment, agentId, agent);
      logger.info("loading agent: " + environment + "/" + agentId);
    }

    logger.info("agent manifest size: " + manifest.getAllAgents().size());
  }

  @PostMapping("/agents/environment/{environment}/agent/{agentiId}/heartbeat")
  public Agent sendHeartBeat(@PathVariable("environment") String environment,
                             @PathVariable("agentId") String agentId) {
    Agent agent = manifest.getAgentById(environment, agentId);
    agent.heartBeat();

    return agent;
  }
}
