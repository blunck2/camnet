package camnet.client.model.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;


public class Camera {
	private String id;
	private String houseName;
	private String url;
	private int sleepTimeInSeconds;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getHouseName() { return houseName; }
	public void setHouseName(String houseName) { this.houseName = houseName; }

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public int getSleepTimeInSeconds() { return sleepTimeInSeconds; }
	public void setSleepTimeInSeconds(int sleepTimeInSeconds) { this.sleepTimeInSeconds = sleepTimeInSeconds; }

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
					.append(houseName, rhs.houseName)
            	    .append(url, rhs.url)
                 	.append(sleepTimeInSeconds, rhs.sleepTimeInSeconds)
                 	.isEquals();
        return isEquals;
	}

}