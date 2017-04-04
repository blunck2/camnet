package camnet.server.model;

import org.apache.commons.lang3.builder.EqualsBuilder;


public class Camera {
	private String id;
	private String fileName;
	private String houseName;
	private String cameraName;
	private int sleepTimeInSeconds;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }

	public String getHouseName() { return houseName; }
	public void setHouseName(String houseName) { this.houseName = houseName; }

	public String getCameraName() { return cameraName; }
	public void setCameraName(String cameraName) { this.cameraName = cameraName; }

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
                	.append(fileName, rhs.fileName)
                 	.append(houseName, rhs.houseName)
                 	.append(cameraName, rhs.cameraName)
                 	.append(sleepTimeInSeconds, rhs.sleepTimeInSeconds)
                 	.isEquals();
        return isEquals;
	}

}