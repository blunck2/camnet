package camnet.service.tracker.engine;

import camnet.model.CameraManifest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import camnet.model.TrackerServiceEndpoint;

import org.springframework.web.client.RestTemplate;



@Component
public class TrackerEngine {
  private CameraManifest manifest;

  @Value("${TrackerEngine.configurationRestEndpoint}")
  private String configurationServiceUrl;

  @Value("${TrackerEngine.configurationUserName}")
  private String configurationServiceUserName;

  @Value("${TrackerEngine.configurationPassWord}")
  private String configurationServicePassWord;

  @Value("${server.port}")
  private String localListenPort;

  @Value("${server.contextPath}")
  private String localContextPath;

  private RestTemplate configService;

  private Logger logger = LoggerFactory.getLogger(TrackerEngine.class);

  public TrackerEngine() {
    configService = new RestTemplate();
  }

  @PostConstruct
  public void setUp() {
    manifest = new CameraManifest();

    registerWithConfigurationService();
  }

  public String getConfigurationServiceUrl() {
    return configurationServiceUrl;
  }

  public void setConfigurationServiceUrl(String configurationServiceUrl) {
    this.configurationServiceUrl = configurationServiceUrl;
  }

  public String getConfigurationServiceUserName() {
    return configurationServiceUserName;
  }

  public void setConfigurationServiceUserName(String configurationServiceUserName) {
    this.configurationServiceUserName = configurationServiceUserName;
  }

  public String getConfigurationServicePassWord() {
    return configurationServicePassWord;
  }

  public void setConfigurationServicePassWord(String configurationServicePassWord) {
    this.configurationServicePassWord = configurationServicePassWord;
  }

  public CameraManifest getCameraManifest() { return manifest; }
  public void setCameraManifest(CameraManifest manifest) { this.manifest = manifest; }


  private void registerWithConfigurationService() {
    logger.trace("registering with configuration service");

    // resolve hostname to be used in callbacks
    String hostName = null;
    try {
      hostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
    } catch (java.net.UnknownHostException e) {
      logger.error("failed to resolve DNS name.  using localhost.", e);
      hostName = "localhost";
    }

    String serviceEndpointUrl = "http://" + hostName + ":" + localListenPort + localContextPath;

    TrackerServiceEndpoint endpoint = new TrackerServiceEndpoint();
    endpoint.setUrl(serviceEndpointUrl);
    endpoint.setUserName("");
    endpoint.setPassWord("");

    String url = configurationServiceUrl + "/tracker/endpoint/add";
    TrackerServiceEndpoint result =
        configService.postForObject(url, endpoint, TrackerServiceEndpoint.class, new HashMap<String, String>());
  }

}
