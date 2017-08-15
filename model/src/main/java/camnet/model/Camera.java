package camnet.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class Camera {
	private String id;
	private String fileName;
	private String environment;
	private String cameraName;
	private String displayName;
	private String url;
	private String userName;
	private String passWord;
	private int sleepTimeInSeconds;
	private long lastUpdateEpoch;
	private AgentServiceEndpoint agent;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }

	public String getEnvironment() { return environment; }
	public void setEnvironment(String environment) { this.environment = environment; }

	public String getCameraName() { return cameraName; }
	public void setCameraName(String cameraName) { this.cameraName = cameraName; }

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public String getUserName() { return userName; }
	public void setUserName(String userName) { this.userName = userName; }

	public String getPassWord() { return passWord; }
	public void setPassWord(String passWord) { this.passWord = passWord; }

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getSleepTimeInSeconds() { return sleepTimeInSeconds; }
	public void setSleepTimeInSeconds(int sleepTimeInSeconds) { this.sleepTimeInSeconds = sleepTimeInSeconds; }

	public long getLastUpdateEpoch() {
		return lastUpdateEpoch;
	}

	public void setLastUpdateEpoch(long lastUpdateEpoch) {
		this.lastUpdateEpoch = lastUpdateEpoch;
	}

	public AgentServiceEndpoint getAgentServiceEndpoint() {
		return agent;
	}

	public void setAgentServiceEndpoint(AgentServiceEndpoint agent) {
		this.agent = agent;
	}

	public boolean isLatent() {
		if (lastUpdateEpoch == 0) {
			return true;
		}

		long nowEpoch = System.currentTimeMillis();
		long futureEpoch = lastUpdateEpoch + (sleepTimeInSeconds * 1000);

		return (nowEpoch > futureEpoch);
	}

	@Override public boolean equals(Object obj) {
   		if (obj == null) { return false; }
   		if (obj == this) { return true; }
   		if (obj.getClass() != getClass()) {
     		return false;
   		}

   		Camera rhs = (Camera) obj;

   		boolean isEquals = new EqualsBuilder()
        	        .appendSuper(super.equals(obj))
            	    .append(id, rhs.id)
                	.append(fileName, rhs.fileName)
                 	.append(environment, rhs.environment)
                 	.append(cameraName, rhs.cameraName)
									.append(url, rhs.url)
									.append(userName, rhs.userName)
									.append(passWord, rhs.passWord)
                 	.append(sleepTimeInSeconds, rhs.sleepTimeInSeconds)
									.append(lastUpdateEpoch, rhs.lastUpdateEpoch)
									.append(agent, rhs.agent)
                 	.isEquals();
        return isEquals;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("fileName", fileName)
				.append("environment", environment)
				.append("cameraName", cameraName)
				.append("url", url)
				.append("userName", userName)
				.append("passWord", passWord)
				.append("sleepTimeInSeconds", sleepTimeInSeconds)
				.append("lastUpdateEpoch", lastUpdateEpoch)
				.append("agent", agent)
				.toString();
	}

}