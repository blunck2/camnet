package camnet.service.media.processor;

import camnet.model.Camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="localImageProcessor")
public class LocalImageProcessor implements ImageProcessor {

	private static final Logger logger = LoggerFactory.getLogger(LocalImageProcessor.class);


	private String rootImageDirectory;


	public LocalImageProcessor() { }

	public void setRootImageDirectory(String rootImageDirectory) { 
		this.rootImageDirectory = rootImageDirectory;
	}
	public String getRootImageDirectory() { return rootImageDirectory; }


	public int processImage(Camera camera, MultipartFile image, Map<String, String> imageHeaders) throws ImageProcessingException {
  		String baseName = camera.getFileName();
  		String houseNameLowerCase = camera.getHouseName().toLowerCase();
  		String dirName = rootImageDirectory + "/" + houseNameLowerCase;

  		File dirNameFile = new File(dirName);
  		if (! dirNameFile.isDirectory()) {
  			dirNameFile.mkdirs();
			}

			// create the base directory
	  	File baseDirectory = new File(dirName);
  		baseDirectory.mkdirs();

			// error out if we can't write to the target filename
	  	String fileName = dirName + "/" + baseName;
	  	logger.info("writing to: " + fileName);
  		File outputFile = new File(fileName);
  		if (outputFile.exists() && (! outputFile.canWrite())) {
  			throw new ImageProcessingException("unable to write to file: " + fileName);
  		}

  		// write the input file to the output file
  		int byteCount = 0;
  		try {
	  		FileOutputStream outputFileStream = new FileOutputStream(outputFile);
  			byte[] bytes = image.getBytes();
  			byteCount = bytes.length;
  			outputFileStream.write(bytes);
  			outputFileStream.close();
  		} catch (Exception e) {
  			logger.error("failed to write file: " + fileName, e);
  		}

	  	return byteCount;
	}
}