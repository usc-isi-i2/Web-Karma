package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishJSONCommandFactory extends CommandFactory {
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

		PublishJSONCommand comm = new PublishJSONCommand(getNewId(workspace), worksheetId,
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

}
