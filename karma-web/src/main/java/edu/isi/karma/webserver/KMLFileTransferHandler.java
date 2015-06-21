/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
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
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

// TODO This class is broken.  Where is the data supposed to go?
public class KMLFileTransferHandler extends HttpServlet {
	/**
	 *   
	 */
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LoggerFactory.getLogger(KMLFileTransferHandler.class);
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		File file = FileUtil.downloadFileFromHTTPRequest(request, contextParameters.getParameterValue(ContextParameter.USER_UPLOADED_DIR));

		// Move the file to the webapp directory so that it is public
		File dir = new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
				"KML/"+file.getName());
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
