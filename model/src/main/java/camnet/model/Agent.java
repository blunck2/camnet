package camnet.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;


public class Agent {
  private String id;
  private List<String> environments;
  private AgentServiceEndpoint serviceEndpoint;
  private long lastHeartBeatEpoch;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getEnvironments() {
    return environments;
  }

  public void setEnvironments(List<String> environments) {
    this.environments = environments;
  }

  public AgentServiceEndpoint getServiceEndpoint() {
    return serviceEndpoint;
  }

  public void setServiceEndpoint(AgentServiceEndpoint serviceEndpoint) {
    this.serviceEndpoint = serviceEndpoint;
  }

  public long getLastHeartBeatEpoch() {
    return lastHeartBeatEpoch;
  }

  public void setLastHeartBeatEpoch(long lastHeartBeatEpoch) {
    this.lastHeartBeatEpoch = lastHeartBeatEpoch;
  }

  public void heartBeat() {
    this.lastHeartBeatEpoch = System.currentTimeMillis();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    Agent rhs = (Agent) obj;

    boolean isEquals = new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(id, rhs.id)
        .append(environments, rhs.environments)
        .append(serviceEndpoint, rhs.serviceEndpoint)
        .append(lastHeartBeatEpoch, rhs.lastHeartBeatEpoch)
        .isEquals();

    return isEquals;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("environments", environments)
        .append("serviceEndpoint", serviceEndpoint)
        .append("lastHeartBeatEpoch", lastHeartBeatEpoch)
        .toString();
  }
}