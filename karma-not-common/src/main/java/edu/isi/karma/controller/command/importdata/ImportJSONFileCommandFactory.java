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
package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;

public class ImportJSONFileCommandFactory extends CommandFactory {
	
	static Logger logger = LoggerFactory.getLogger(ImportJSONFileCommandFactory.class);
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
				
		File uploadedFile = FileUtil.downloadFileFromHTTPRequest(request);
                
                if (request.getParameter("revisedWorksheet") == null) {
		return new ImportJSONFileCommand(getNewId(workspace), uploadedFile);
                }
                
                return new ImportJSONFileCommand(getNewId(workspace), request.getParameter("revisedWorksheet"), uploadedFile);
	}
}
