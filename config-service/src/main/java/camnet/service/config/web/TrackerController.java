package camnet.service.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


import camnet.model.CameraManifest;
import camnet.model.Camera;

import javax.annotation.PostConstruct;
import java.util.List;


@RestController
@RequestMapping("/tracker")
public class TrackerController {
  @PostConstruct
  public void setUp() {

  }


}