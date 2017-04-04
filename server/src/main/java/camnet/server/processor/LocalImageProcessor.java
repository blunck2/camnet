package camnet.server.processor;

import camnet.server.model.Camera;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;

@Component
@ConfigurationProperties(prefix="imageProcessor")
public class LocalImageProcessor implements ImageProcessor {

	private String rootImageDirectory;


	public LocalImageProcessor() { }

	public void setRootImageDirectory(String rootImageDirectory) { 
		this.rootImageDirectory = rootImageDirectory;
	}
	public String getRootImageDirectory() { return rootImageDirectory; }


	public int processImage(Camera camera, MultipartFile image) throws ImageProcessingException {
  	String baseName = camera.getFileName();
  	String houseNameLowerCase = camera.getHouseName().toLowerCase();
  	String dirName = rootImageDirectory + "/" + houseNameLowerCase;

		// create the base directory
  	File baseDirectory = new File(dirName);
  	baseDirectory.mkdirs();

		// error out if we can't write to the target filename
  	String fileName = dirName + "/" + baseName;
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
  		e.printStackTrace(System.out);
  		throw new ImageProcessingException("failed to write file '" + fileName + "': " + e.getMessage());
  	}

  	return byteCount;

	}
}