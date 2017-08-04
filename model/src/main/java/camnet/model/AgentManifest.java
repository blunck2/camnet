package camnet.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.builder.ToStringBuilder;


public class AgentManifest {
  private Map<String, List<Agent>> agents;

  private Logger logger = LoggerFactory.getLogger(AgentManifest.class);

  public AgentManifest() { agents = new HashMap<>(); }

  public Map<String, List<Agent>> getAgents() {
    return agents;
  }

  public void setAgents(Map<String, List<Agent>> agents) {
    this.agents = agents;
  }

  public Agent getAgentById(String environment, String id) {
    logger.trace("looking for agent environment: " + environment + "; id: " + id);
    for (Agent agent : getAgentsForEnvironment(environment)) {
      logger.trace("got agentenvironment: " + environment + "; id: " + agent.getId());
      if (id.equals(agent.getId())) {
        return agent;
      }
    }

    return null;
  }

  public List<Agent> getAgentsForEnvironment(String environment) {
    List<Agent> agentsForEnvironment = new ArrayList<>();
    agentsForEnvironment.addAll(agents.get(environment));
    return agentsForEnvironment;
  }

  public void setAgentsForEnvironment(String environment, List<Agent> agentList) { agents.put(environment, agentList); }

  public void removeAgentById(String id) {
    for (String environment : agents.keySet()) {
      List<Agent> agentsForEnvironment = agents.get(environment);

      for (Agent agent : agentsForEnvironment) {
        if (agent.getId().equals(id)) {
          agentsForEnvironment.remove(agent);
          break;
        }
      }
    }
  }

  public Set<String> getEnvironments() {
    return agents.keySet();
  }

  public void addAgent(Agent agent) {
    for (String environment : agent.getEnvironments()) {
      List<Agent> existingAgents = agents.get(environment);
      boolean environmentUnknown = false;

      if (existingAgents == null) {
        existingAgents = new ArrayList<>();
        environmentUnknown = true;
      }

      existingAgents.add(agent);

      if (environmentUnknown) {
        agents.put(environment, existingAgents);
      }
    }
  }

  public List<Agent> getAgentsByEnvironment(String environment) {
    return agents.get(environment);
  }

  public List<Agent> getAllAgents() {
    List<Agent> allAgents = new ArrayList<>();

    for (String environment : agents.keySet()) {
      List<Agent> environmentAgents = agents.get(environment);
      allAgents.addAll(environmentAgents);
    }

    return allAgents;
  }

  public List<Agent> getActiveAgents(int recencyInSeconds) {
    List <Agent> activeAgents = new ArrayList<>();

    for (Agent agent : getAllAgents()) {
      long agentLastCheckInEpoch = agent.getLastHeartBeatEpoch();
      long nowEpoch = System.currentTimeMillis();

      long oldAgeTolleranceEpoch = nowEpoch - recencyInSeconds * 1000;
      if (agentLastCheckInEpoch > oldAgeTolleranceEpoch) {
        activeAgents.add(agent);
      }
    }

    return activeAgents;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("agents", agents)
        .toString();
  }
}