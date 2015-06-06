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

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.model.serialization.DataSourceLoader;
import edu.isi.karma.model.serialization.DataSourcePublisher;
import edu.isi.karma.model.serialization.Repository;
import edu.isi.karma.model.serialization.WebServiceLoader;
import edu.isi.karma.model.serialization.WebServicePublisher;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.metadata.MetadataContainer;
import edu.isi.karma.rep.sources.DataSource;
import edu.isi.karma.rep.sources.WebService;

public class PublishModelCommand extends WorksheetCommand{

	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(PublishModelCommand.class.getSimpleName());

	public PublishModelCommand(String id, String model, String worksheetId) {
		super(id, model, worksheetId);
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		Worksheet wk = workspace.getWorksheet(worksheetId);

		WebService service = null;
		DataSource source = null;
		
		if (!wk.containService()) { 
			logger.info("The worksheet does not have a service object.");
//			return new UpdateContainer(new ErrorUpdate(
//				"Error occured while publishing the model. The worksheet does not have a service object."));
		} else
			service = wk.getMetadataContainer().getService();
		
		AlignmentManager mgr = AlignmentManager.Instance();
		String alignmentId = mgr.constructAlignmentId(workspace.getId(), worksheetId);
		Alignment al = mgr.getAlignment(alignmentId);
		
		if (al == null) { 
			logger.error("The alignment model is null.");
			if (service == null)
				return new UpdateContainer(new ErrorUpdate(
					"Error occured while publishing the source. The alignment model is null."));
		} 
		
		DirectedWeightedMultigraph<Node, LabeledLink> tree = null;
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
			if (service != null) {
				WebServicePublisher servicePublisher = new WebServicePublisher(service, workspace.getContextId());
				servicePublisher.publish(Repository.Instance().LANG, true);
				logger.info("Service model has successfully been published to repository: " + service.getId());
				return new UpdateContainer(new InfoUpdate(
				"Service model has successfully been published to repository: " + service.getId()));
			} else { //if (source != null) {
				DataSourcePublisher sourcePublisher = new DataSourcePublisher(source, workspace.getFactory(), wk.getMetadataContainer().getSourceInformation());
				sourcePublisher.publish(Repository.Instance().LANG, true);
				logger.info("Source model has successfully been published to repository: " + source.getId());
				return new UpdateContainer(new InfoUpdate(
				"Source model has successfully been published to repository: " + source.getId()));
			}

		} catch (IOException e) {
			logger.error("Error occured while publishing the source/service ", e);
			return new UpdateContainer(new ErrorUpdate(
			"Error occured while publishing the source/service "));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {

		Worksheet wk = workspace.getWorksheet(worksheetId);

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
