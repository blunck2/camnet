package camnet.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class Agent {
  private String id;
  private String environment;
  private AgentServiceEndpoint serviceEndpoint;
  private long lastHeartBeatEpoch;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
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
        .append(environment, rhs.environment)
        .append(serviceEndpoint, rhs.serviceEndpoint)
        .append(lastHeartBeatEpoch, rhs.lastHeartBeatEpoch)
        .isEquals();

    return isEquals;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("environment", environment)
        .append("serviceEndpoint", serviceEndpoint)
        .append("lastHeartBeatEpoch", lastHeartBeatEpoch)
        .toString();
  }
}