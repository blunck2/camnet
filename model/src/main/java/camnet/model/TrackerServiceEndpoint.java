package camnet.model;


import org.apache.commons.lang3.builder.EqualsBuilder;

public class TrackerServiceEndpoint {
  private String url;
  private String userName;
  private String passWord;

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }

  public String getPassWord() { return passWord; }
  public void setPassWord(String password) { this.passWord = password; }

  @Override public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }

    TrackerServiceEndpoint rhs = (TrackerServiceEndpoint) obj;

    boolean isEquals = new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(url, rhs.url)
        .append(userName, rhs.userName)
        .append(passWord, rhs.passWord)
        .isEquals();
    return isEquals;
  }


}
