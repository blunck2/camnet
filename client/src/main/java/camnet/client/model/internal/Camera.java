package camnet.client.model.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;


public class Camera {
	private String id;
	private String fileName;
	private String environment;
	private String url;
	private String userName;
	private String password;
	private int sleepTimeInSeconds;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName= fileName; }

	public String getEnvironment() { return environment; }
	public void setEnvironment(String environment) { this.environment = environment; }

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public int getSleepTimeInSeconds() { return sleepTimeInSeconds; }
	public void setSleepTimeInSeconds(int sleepTimeInSeconds) { this.sleepTimeInSeconds = sleepTimeInSeconds; }

	public String getUserName() { return userName; }
	public void setUserName(String userName) { this.userName = userName; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getDisplayName() {
		return environment + "/" + id;
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
					.append(environment, rhs.environment)
            	    .append(url, rhs.url)
					.append(userName, rhs.userName)
					.append(password, rhs.password)
                 	.append(sleepTimeInSeconds, rhs.sleepTimeInSeconds)
                 	.isEquals();
        return isEquals;
	}

}