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
package edu.isi.karma.controller.command.publish;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

import javax.servlet.http.HttpServletRequest;

public class PublishRDFCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId, addInverseProperties, rdfPrefix, rdfNamespace, saveToStore,hostName,dbName,userName,password,modelName, 
		tripleStoreUrl, graphUri, replaceContext
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		String addInverseProperties = request.getParameter(Arguments.addInverseProperties
				.name());
		String rdfPrefix = request.getParameter(Arguments.rdfPrefix
				.name());
		String rdfNamespace = request.getParameter(Arguments.rdfNamespace
				.name());

		PublishRDFCommand comm = new PublishRDFCommand(getNewId(workspace), worksheetId,
				ServletContextParameterMap
				.getParameterValue(ContextParameter.PUBLIC_RDF_ADDRESS),
				rdfPrefix, rdfNamespace, addInverseProperties,
				request.getParameter(Arguments.saveToStore.name()),
				request.getParameter(Arguments.hostName.name()),
				request.getParameter(Arguments.dbName.name()),
				request.getParameter(Arguments.userName.name()),
				request.getParameter(Arguments.password.name()),
				request.getParameter(Arguments.modelName.name()),
				request.getParameter(Arguments.tripleStoreUrl.name()),
				request.getParameter(Arguments.graphUri.name()),
				Boolean.parseBoolean(request.getParameter(Arguments.replaceContext.name()))
				);
		
		return comm;
	}

	@Override
	protected Class<? extends Command> getCorrespondingCommand()
	{
		return PublishRDFCommand.class;
	}
}
