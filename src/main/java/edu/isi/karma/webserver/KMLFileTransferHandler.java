package edu.isi.karma.webserver;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.util.FileUtil;

public class KMLFileTransferHandler extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LoggerFactory.getLogger(KMLFileTransferHandler.class);

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		File file = FileUtil.downloadFileFromHTTPRequest(request);

		// Move the file to the webapp directory so that it is public
		File dir = new File("./src/main/webapp/KML/"+file.getName());
		if(dir.exists())
			dir.delete();
		file.renameTo(dir);
		try {
			Writer writer = response.getWriter();
			writer.write("done");
			response.flushBuffer();
		} catch (IOException e) {
			logger.error("Error occured while downloading the file!",e);
		}
	}
}
