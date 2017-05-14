package camnet.server.web;

import camnet.server.processor.S3ImageProcessor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import camnet.server.processor.LocalImageProcessor;
import camnet.server.processor.ImageProcessingException;

import camnet.common.model.ImagePostResponse;

import camnet.server.model.CameraManifest;
import camnet.server.model.Camera;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/image")
public class ImageController {
	@Autowired
	private CameraManifest manifest;

	@Autowired
	private LocalImageProcessor localImageProcessor;

	@Autowired
	private S3ImageProcessor s3ImageProcessor;

	private Logger logger = Logger.getLogger(ImageController.class);

  	@PostMapping("/ingest/house/{houseName}/camera/{cameraId}")
  	public ImagePostResponse ingest(@RequestParam("file") MultipartFile file,
									  @PathVariable("houseName") String houseName,
									  @PathVariable("cameraId") String cameraId) {
		logger.info("incoming image: " + houseName + "/" + cameraId);

		// error out if the manifest is null
		if (manifest == null) {
			return createServerErrorResponse("null camera manifest");
		}

		// error out if the localImageProcessor is null
		if (localImageProcessor == null) {
			return createServerErrorResponse("null localImageProcessor");
		}

		// error out if the camera has not been defined
		Camera camera = manifest.getCameraById(houseName, cameraId);
		if (camera == null) {
			return createServerErrorResponse("house name '" + houseName + "' and camera id '" +
					cameraId + "' not found");
		}

		Map<String, String> fileHeaders = new HashMap<>();
		fileHeaders.put("contentType", "image/jpeg");

		int byteCount = 0;
		try {
			byteCount = localImageProcessor.processImage(camera, file, fileHeaders);
  			s3ImageProcessor.processImage(camera, file, fileHeaders);
  		} catch (ImageProcessingException e) {
			logger.error("error occurred processing image", e);

		  	ImagePostResponse response = new ImagePostResponse();
  			response.setCode(1);
  			response.setMessage(e.getMessage());
  			response.setSleepTimeInSeconds(60);
  			return response;
  		}

  		// nominal response
	  	ImagePostResponse response = new ImagePostResponse();
  		response.setCode(0);
  		response.setMessage("wrote image: " + byteCount + " bytes");
  		response.setSleepTimeInSeconds(camera.getSleepTimeInSeconds());

    	return response;
  	}


  	private ImagePostResponse createServerErrorResponse(String message) {
  		ImagePostResponse response = new ImagePostResponse();
  		response.setCode(1);
  		response.setMessage("server error occurred: " + message);
  		response.setSleepTimeInSeconds(300);

  		return response;
  	}
}
