package camnet.model;


import org.apache.commons.lang3.builder.EqualsBuilder;

public class TrackerService {
  private String url;
  private String userName;
  private String password;

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  @Override public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }

    TrackerService rhs = (TrackerService) obj;

    boolean isEquals = new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(url, rhs.url)
        .append(userName, rhs.userName)
        .append(password, rhs.password)
        .isEquals();
    return isEquals;
  }


}
