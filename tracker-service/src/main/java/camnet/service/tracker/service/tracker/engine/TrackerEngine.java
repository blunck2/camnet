package camnet.service.tracker.service.tracker.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TrackerEngine {
  @Value("${TrackerEngine.configurationRestEndpoint}")
  private String configurationRestEndpoint;

  @Value("${TrackerEngine.configurationUserName}")
  private String configurationUserName;

  @Value("${TrackerEngine.configurationPassWord}")
  private String configurationPassWord;

  public void setConfigurationRestEndpoint(String restEndpoint) {
    this.configurationRestEndpoint = restEndpoint;
  }

  public String getConfigurationRestEndpoint() {
    return configurationRestEndpoint;
  }

  public String getConfigurationUserName() {
    return configurationUserName;
  }

  public void setConfigurationUserName(String userName) {
    this.configurationUserName = userName;
  }

  public String getConfigurationPassWord() {
    return configurationPassWord;
  }

  public void setConfigurationPassWord(String passWord) {
    this.configurationPassWord = passWord;
  }

}
