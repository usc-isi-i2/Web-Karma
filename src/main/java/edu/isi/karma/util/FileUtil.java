package edu.isi.karma.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
		}catch(FileUploadException ex) {
			logger.error("Error encountered while parsing the request",ex);
		} catch(Exception ex) {
			logger.error("Error encountered while uploading file",ex);
		}
		return uploadedFile;
	}
	
}
