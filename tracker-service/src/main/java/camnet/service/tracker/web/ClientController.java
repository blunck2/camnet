package camnet.service.tracker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import camnet.model.CameraManifest;
import camnet.model.Camera;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/client")
@ConfigurationProperties("clientController")
public class ClientController {
  private CameraManifest manifest;

  private String configurationServerUrl;
  private String configurationServerUserName;
  private String configurationServerPassWord;

  private RestTemplate configServer;

  private Logger logger = LoggerFactory.getLogger(ClientController.class);

  public String getConfigurationServerUrl() {
    return configurationServerUrl;
  }

  public void setConfigurationServerUrl(String configurationServerUrl) {
    this.configurationServerUrl = configurationServerUrl;
  }

  public String getConfigurationServerUserName() {
    return configurationServerUserName;
  }

  public void setConfigurationServerUserName(String userName) {
    this.configurationServerUserName = userName;
  }

  public String getConfigurationServerPassword() {
    return configurationServerPassWord;
  }

  public void setConfigurationServerPassWord(String passWord) {
    this.configurationServerPassWord = passWord;
  }


  @PostConstruct
  public void setUp() {

  }


  @RequestMapping("/clients")
  public Integer getAllClients() {
    return 1;
  }

}
