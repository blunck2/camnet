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
    logger.info("manifest HC: " + manifest.hashCode());
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
    logger.info ("returning agent for environment '" + environment + "' and id '" + agentId + "'");
    Agent agent = manifest.getAgentById(environment, agentId);
    logger.debug("agent: " + agent.toString());
    return agent;
  }

  @PostMapping("/agents")
  public void setAllAgents(@RequestBody List<Agent> agents) {
    logger.info("setting all agents: " + agents);

    for (Agent agent : agents) {
      List<String> environments = agent.getEnvironments();
      String agentId = agent.getId();
      logger.info("adding agent in locations " + environments + ":" + agentId);

      manifest.addAgent(agent);
    }

    logger.info("agent manifest size: " + manifest.getAllAgents().size());
  }

  @PostMapping("/agents/add")
  public void addAgent(@RequestBody Agent agent) {
    logger.info("adding agent: " + agent);

    logger.info("--------- adding agent to agent manifest with HC: " + manifest.hashCode());

    manifest.addAgent(agent);

    logger.info("agent manifest size: " + manifest.getAllAgents().size());
  }


  @PostMapping("/agents/environment/{environment}/agent/{agentId}/heartbeat")
  public Agent receiveHeartBeat(@PathVariable("environment") String environment,
                                @PathVariable("agentId") String agentId) {
    logger.info("heartbeat received!");
    logger.info("heartbeat received.  updating agent manifest: " + manifest.hashCode());
    Agent agent = manifest.getAgentById(environment, agentId);
    agent.heartBeat();

    return agent;
  }
}
