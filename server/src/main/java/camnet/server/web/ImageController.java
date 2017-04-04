package camsink.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import camsink.processor.LocalImageProcessor;
import camsink.processor.ImageProcessingException;

import camsink.model.external.ImagePostResponse;

import camsink.model.internal.CameraManifest;
import camsink.model.internal.Camera;

import java.util.List;

import java.io.File;
import java.io.IOException;


@RestController
@RequestMapping("/image")
public class ImageController {
	@Autowired
	private CameraManifest manifest;

	@Autowired
	private LocalImageProcessor processor;

  	@PostMapping("/ingest/{id}")
  	public ImagePostResponse ingest(@RequestParam("file") MultipartFile file,
    			                    @PathVariable("id") String id) {
  		// error out if the manifest is null
  		if (manifest == null) {
  			return createServerErrorResponse(id, "null camera manifest");
  		}

  		// error out if the processor is null
  		if (processor == null) {
  			return createServerErrorResponse(id, "null processor");
  		}

  		// error out if the camera has not been defined
  		Camera camera = manifest.getCameraById(id);
  		if (camera == null) {
  			return createServerErrorResponse(id, "camera id '" + id + "' not found");
  		}

  		int byteCount = 0;
  		try {
  			byteCount = processor.processImage(camera, file);
  		} catch (ImageProcessingException e) {
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
  		response.setSleepTimeInSeconds(60);

    	return response;
  	}


  	private ImagePostResponse createServerErrorResponse(String id, String message) {
  		ImagePostResponse response = new ImagePostResponse();
  		response.setCode(1);
  		response.setMessage("server error occurred: " + message);
  		response.setSleepTimeInSeconds(300);

  		return response;
  	}
}
