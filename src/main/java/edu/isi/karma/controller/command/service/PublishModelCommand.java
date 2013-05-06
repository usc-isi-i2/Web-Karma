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

package edu.isi.karma.controller.command.service;

import java.io.IOException;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.model.serialization.DataSourceLoader;
import edu.isi.karma.model.serialization.DataSourcePublisher;
import edu.isi.karma.model.serialization.Repository;
import edu.isi.karma.model.serialization.WebServiceLoader;
import edu.isi.karma.model.serialization.WebServicePublisher;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rdf.SourceDescription;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.metadata.MetadataContainer;
import edu.isi.karma.rep.sources.DataSource;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;
import edu.isi.karma.webserver.KarmaException;

public class PublishModelCommand extends Command{

	private final String vWorksheetId;

	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(PublishModelCommand.class.getSimpleName());

	public PublishModelCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish the Model";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		
		Workspace ws = vWorkspace.getWorkspace();
		Worksheet wk = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();

		WebService service = null;
		DataSource source = null;
		
		if (!wk.containService()) { 
			logger.info("The worksheet does not have a service object.");
//			return new UpdateContainer(new ErrorUpdate(
//				"Error occured while publishing the model. The worksheet does not have a service object."));
		} else
			service = wk.getMetadataContainer().getService();
		
		AlignmentManager mgr = AlignmentManager.Instance();
		String alignmentId = mgr.constructAlignmentId(ws.getId(), vWorksheetId);
		Alignment al = mgr.getAlignment(alignmentId);
		
//		/**
//		 * 
//		 */
//		// FIXME
//		String exportDir = "/Users/mohsen/Dropbox/Service Modeling/iswc2013/jgraph/";
//		try {
//			GraphUtil.serialize(al.getSteinerTree(), exportDir + wk.getTitle() + ".karma.initial.jgraph");
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		if (true) return null;
		
		if (al == null) { 
			logger.error("The alignment model is null.");
			if (service == null)
				return new UpdateContainer(new ErrorUpdate(
					"Error occured while publishing the source. The alignment model is null."));
		} 
		
		DirectedWeightedMultigraph<Node, Link> tree = null;
		if (al != null) 
			tree = al.getSteinerTree();
		
		if (tree == null) { 
			logger.error("The alignment tree is null.");
			if (service == null)
				return new UpdateContainer(new ErrorUpdate(
					"Error occured while publishing the source. The alignment tree is null."));
		}
		
		if (service != null) service.updateModel(tree);
		else {
			source = new DataSource(wk.getTitle(), tree);
			MetadataContainer metaData = wk.getMetadataContainer();
			if (metaData == null) {
				metaData = new MetadataContainer();
				wk.setMetadataContainer(metaData);
			}
			metaData.setSource(source);
			logger.info("Source added to the Worksheet.");
		}
		
		try {
			//construct the SD
			//get from preferences saved source prefix
			String sourceNamespace = "http://localhost/";
			String sourcePrefix = "s";
			String addInverseProperties = "true";
			try{
				ViewPreferences prefs = vWorkspace.getPreferences();
				JSONObject prefObject = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
				sourcePrefix = prefObject.getString("rdfPrefix");
				sourceNamespace = prefObject.getString("rdfNamespace");
				addInverseProperties= prefObject.getString("addInverseProperties");
			}catch(Exception e){
				//prefix not found, just use the default
				sourceNamespace = "http://localhost/";
				sourcePrefix = "s";
				addInverseProperties = "true";
			}
			if(sourceNamespace.trim().isEmpty())
				sourceNamespace = "http://localhost/";
			if(sourcePrefix.trim().isEmpty())
				sourcePrefix = "s";

			SourceDescription desc = new SourceDescription(ws, al, wk,
					sourcePrefix, sourceNamespace,Boolean.valueOf(addInverseProperties),false);
			String descString = desc.generateSourceDescription();
			/////////////////
			
			// Get the transformation commands JSON list
			WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(vWorksheetId, vWorkspace);
			List<String> commandsJSON = histReader.getJSONForCommands(CommandTag.Transformation);
			
			if (service != null) {
				service.setSourceDescription(descString);
				WebServicePublisher servicePublisher = new WebServicePublisher(service);
				servicePublisher.publish(Repository.Instance().LANG, true);
				logger.info("Service model has successfully been published to repository: " + service.getId());
				return new UpdateContainer(new ErrorUpdate(
				"Service model has successfully been published to repository: " + service.getId()));
			} else { //if (source != null) {
				DataSourcePublisher sourcePublisher = new DataSourcePublisher(source, descString, ws.getFactory(), commandsJSON, wk.getMetadataContainer().getSourceInformation());
				sourcePublisher.publish(Repository.Instance().LANG, true);
				logger.info("Source model has successfully been published to repository: " + source.getId());
				return new UpdateContainer(new ErrorUpdate(
				"Source model has successfully been published to repository: " + source.getId()));
			}

		} catch (IOException e) {
			logger.error("Error occured while publishing the source/service ", e);
			return new UpdateContainer(new ErrorUpdate(
			"Error occured while publishing the source/service "));
		}catch(KarmaException e){
			logger.error("Error occured while generating the source description. ", e);
			return new UpdateContainer(new ErrorUpdate(
			"Error occured while generating the source description."));
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {

		Worksheet wk = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();

		WebService service = null;
		DataSource source = null;
		
		if (!wk.containService()) { 
			logger.error("The worksheet does not have a service object.");
//			return new UpdateContainer(new ErrorUpdate(
//				"Error occured while deleting the model. The worksheet does not have a service object."));
		} else
			service = wk.getMetadataContainer().getService();
		
		if (!wk.containSource()) { 
			logger.error("The worksheet does not have a source object.");
//			return new UpdateContainer(new ErrorUpdate(
//				"Error occured while deleting the model. The worksheet does not have a source object."));
		} else
			source = wk.getMetadataContainer().getSource();
		
		try {

			// one way to un-publish is just set the service model to null and publish it again.
			// in this way the invocation part will be kept in the repository.
//			ServicePublisher servicePublisher = new ServicePublisher(service);
//			servicePublisher.publish("N3", true);

			// deleting the service completely from the repository.
			if (service != null) {
				WebServiceLoader.getInstance().deleteSourceByUri(service.getUri());
				logger.info("Service model has successfully been deleted from repository.");
				return new UpdateContainer(new ErrorUpdate(
						"Service model has successfully been deleted from repository."));
			}
			else {
				DataSourceLoader.getInstance().deleteSourceByUri(source.getUri());
				logger.info("Source model has successfully been deleted from repository.");
				return new UpdateContainer(new ErrorUpdate(
						"Source model has successfully been deleted from repository."));
			}

		} catch (Exception e) {
			logger.error("Error occured while deleting the source/service " + service.getId(), e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while deleting the source/service " + service.getId()));
		}
		
	}

}
