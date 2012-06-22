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
package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorkspace;
import edu.isi.mediator.gav.util.MediatorUtil;

public class ResetModelCommand extends Command {

	private final String vWorksheetId;
	private SemanticTypes oldTypes;
	private Alignment oldAlignment;
	private String alignmentId;
	private String oldCRFModel;
	private String crfModelFilePath;
	
	private static Logger logger = LoggerFactory
			.getLogger(ResetModelCommand.class);

	public ResetModelCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Reset Model";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		//save CRF model for undo
		try{
			crfModelFilePath = vWorkspace.getWorkspace().getCrfModelHandler().getModelFilePath();
			oldCRFModel=MediatorUtil.getFileAsString(crfModelFilePath);
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		//reset CRF model
		vWorkspace.getWorkspace().getCrfModelHandler().removeAllLabels();
		
		// Save the old SemanticType object for undo
		SemanticTypes types = worksheet.getSemanticTypes();
		oldTypes = types;
		worksheet.clearSemanticTypes();
		//System.out.println("OLD TYPES=" + oldTypes.getTypes());
		
		//save old alignment for undo operation
//		alignmentId = vWorkspace.getWorkspace().getId() + ":" + vWorksheetId + "AL";
		alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
		oldAlignment = AlignmentManager.Instance().getAlignment(alignmentId);
		
		// Remove the nodes (if any) from the outlier tag
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
		Set<String> nodeIds = new HashSet<String>();
		for(Row r:rows){
			//for each node in the row
			//logger.debug("Process ROW="+i++);
			for (Node n : r.getNodes()) {
				nodeIds.add(n.getId());
			}
		}
		vWorkspace.getWorkspace().getTagsContainer().getTag(TagName.Outlier)
				.removeNodeIds(nodeIds);

		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Update the alignment
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		try {
			align.alignAndUpdate(c, true);
		} catch (Exception e) {
			logger.error("Error occured while resetting model!",
					e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while resetting model!"));
		}
		c.add(new TagsUpdate());
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
		.getVWorksheet(vWorksheetId).getWorksheet();

		//reset CRF model
		try{
			MediatorUtil.saveStringToFile(oldCRFModel, crfModelFilePath);
			vWorkspace.getWorkspace().getCrfModelHandler().readModelFromFile(crfModelFilePath);
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		//reset old semantic types
		worksheet.setSemanticTypes(oldTypes);

		//set the old alignment
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		
		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Update the alignment
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		try {
			align.alignAndUpdate(c, true);
		} catch (Exception e) {
			logger.error("Error occured while undoing alignment!",
					e);
			return new UpdateContainer(new ErrorUpdate(
			"Error occured while undoing alignment!"));
		}
		return c;

	}

}
