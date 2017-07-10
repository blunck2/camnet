package camnet.service.tracker.engine;

import camnet.model.CameraManifest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class TrackerEngine {
  private CameraManifest manifest;

  @Value("${TrackerEngine.configurationRestEndpoint}")
  private String configurationRestEndpoint;

  @Value("${TrackerEngine.configurationUserName}")
  private String configurationUserName;

  @Value("${TrackerEngine.configurationPassWord}")
  private String configurationPassWord;

  @PostConstruct
  public void setUp() {
    manifest = new CameraManifest();
  }

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

  public CameraManifest getCameraManifest() { return manifest; }
  public void setCameraManifest(CameraManifest manifest) { this.manifest = manifest; }

}
