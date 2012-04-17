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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.service.Operation;
import edu.isi.karma.service.Service;
import edu.isi.karma.service.ServicePublisher;
import edu.isi.karma.view.VWorkspace;

public class PublishServiceModelCommand extends Command{

	private final String vWorksheetId;

	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(PublishServiceModelCommand.class.getSimpleName());

	public PublishServiceModelCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publishing the Service Model";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		
		Workspace ws = vWorkspace.getWorkspace();
		Worksheet wk = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();

		if (!wk.containService()) { 
			logger.error("The worksheet does not have a service object.");
			return new UpdateContainer(new ErrorUpdate(
				"Error occured while publishing the model. The worksheet does not have a service object."));
		}

		Service service = wk.getMetadataContainer().getService();
		if (service.getOperations() == null || service.getOperations().size() == 0) {
			logger.error("The service does not have any operation.");
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while publishing the model. The service in the worksheet does not have any operation"));
		}
		
		Operation op = service.getOperations().get(0);
		
		AlignmentManager mgr = AlignmentManager.Instance();
		String alignmentId = mgr.constructAlignmentId(ws.getId(), vWorksheetId);
		Alignment al = mgr.getAlignment(alignmentId);
		
		if (al == null) 
			logger.error("The alignment model is null.");
		else {
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = al.getSteinerTree();
			
			if (tree == null) 
				logger.error("The alignment tree is null.");
			else
				op.updateModel(tree);
		}
		
		
		try {
			ServicePublisher servicePublisher = new ServicePublisher(service);
			servicePublisher.publish("N3", true);

			logger.info("Service model has successfully been published to repository.");
			return new UpdateContainer(new ErrorUpdate(
					"Service model has successfully been published to repository."));

		} catch (IOException e) {
			logger.error("Error occured while importing CSV file.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while importing CSV File."));
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
