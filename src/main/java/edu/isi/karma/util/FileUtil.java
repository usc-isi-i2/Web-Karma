package edu.isi.karma.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	static public File downloadFileFromHTTPRequest (HttpServletRequest request) {
		//TODO Make it configurable through web.xml
		String DESTINATION_DIR_PATH ="UserUploadedFiles";
		File destinationDir;
		
		// Download the file to the upload file folder
		String realPath = DESTINATION_DIR_PATH;
		destinationDir = new File(realPath);
		logger.debug("File upload destination directory: " + destinationDir.getAbsolutePath());
		if(!destinationDir.isDirectory()) {
			destinationDir.mkdir();
		}
		
		DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();

		// Set the size threshold, above which content will be stored on disk.
		fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB

		//Set the temporary directory to store the uploaded files of size above threshold.
		fileItemFactory.setRepository(destinationDir);
 
		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		
		File uploadedFile = null;
		try {
			// Parse the request
			@SuppressWarnings("rawtypes")
			List items = uploadHandler.parseRequest(request);
			@SuppressWarnings("rawtypes")
			Iterator itr = items.iterator();
			while(itr.hasNext()) {
				FileItem item = (FileItem) itr.next();

				// Ignore Form Fields.
				if(item.isFormField()) {
					// Do nothing
				} else {
					//Handle Uploaded files. Write file to the ultimate location.
					uploadedFile = new File(destinationDir,item.getName());
					item.write(uploadedFile);
				}
			}
		} catch(FileUploadException ex) {
			logger.error("Error encountered while parsing the request",ex);
		} catch(Exception ex) {
			logger.error("Error encountered while uploading file",ex);
		}
		return uploadedFile;
	}

	public static void copyFiles(File destination, File source) throws FileNotFoundException, IOException{
		InputStream in = new FileInputStream(source);
		OutputStream out = new FileOutputStream(destination);

		byte[] buf = new byte[1024];
		int len;
		
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		logger.debug("Done copying contents of " + source.getName() + " to " + destination.getName());
	}

	public static void writePrettyPrintedJSONObjectToFile(JSONObject json, File jsonFile) 
		throws JSONException, IOException{
		String prettyPrintedJSONString = json.toString(4);
		FileWriter writer = new FileWriter(jsonFile);
		writer.write(prettyPrintedJSONString);
		writer.close();
		logger.debug("Done writing JSON Object into a File: " + jsonFile.getAbsolutePath());
	}
	
	public static String readFileContentsToString (File file) throws IOException {
		 FileInputStream stream = new FileInputStream(file);
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
	}
	
}
