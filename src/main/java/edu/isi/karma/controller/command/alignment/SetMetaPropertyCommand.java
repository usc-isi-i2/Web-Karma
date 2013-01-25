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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.Command.CommandType;
import edu.isi.karma.controller.command.alignment.SetMetaPropertyCommandFactory.METAPROPERTY_NAME;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeTrainingThread;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.rep.alignment.UriOfClassLink;
import edu.isi.karma.view.VWorkspace;

public class SetMetaPropertyCommand extends Command {

	private final String hNodeId;
	private final String vWorksheetId;
	private final boolean trainAndShowUpdates;
	private METAPROPERTY_NAME metaPropertyName;
	private String metaPropertyValue;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	protected SetMetaPropertyCommand(String id, String vWorksheetId, String hNodeId, 
			METAPROPERTY_NAME metaPropertyName, String metaPropertyValue, boolean trainAndShowUpdates) {
		super(id);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.metaPropertyName = metaPropertyName;
		this.metaPropertyValue = metaPropertyValue;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Set Semantic Type";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		/*** Get the Alignment for this worksheet ***/
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		
		/*** Add the appropriate nodes and links in alignment graph ***/
		ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
		if (columnNode == null) {
			columnNode = alignment.createColumnNode(hNodeId);
		}
		
		if (metaPropertyName.equals(METAPROPERTY_NAME.isUriOfClass)) {
			Node classNode = alignment.getNodeById(metaPropertyValue);
			UriOfClassLink mpLink = alignment.createURIOfClassMetaPropertyLink(classNode, columnNode);
			alignment.addLinkAndUpdateAlignment(mpLink);
		} else if (metaPropertyName.equals(METAPROPERTY_NAME.isSpecializationForEdge)) {
			
			
			
		} else if (metaPropertyName.equals(METAPROPERTY_NAME.isSubclassOfClass)) {
			Node classNode = alignment.getNodeById(metaPropertyValue);
			SubClassLink mpLink = alignment.createSubclassOfNodeMetaPropertyLink(classNode, columnNode);
			alignment.addLinkAndUpdateAlignment(mpLink);
		}
		
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		CRFModelHandler crfModelHandler = vWorkspace.getWorkspace().getCrfModelHandler();

		// Save the old SemanticType object and CRF Model for undo
//		oldColumnModel = worksheet.getCrfModel().getModelByHNodeId(hNodeId);
//		oldSynonymTypes = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(newType.getHNodeId());

		// Update the SemanticTypes data structure for the worksheet
//		worksheet.getSemanticTypes().addType(newType);

		// Update the synonym semanticTypes
//		worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), newSynonymTypes);

		if(trainAndShowUpdates) {
			// Train the semantic type in a separate thread
			Thread t = new Thread(new SemanticTypeTrainingThread(crfModelHandler, worksheet, newType));
			t.start();

			c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
			// Get the alignment update if any
			AlignToOntology align = new AlignToOntology(worksheet, vWorkspace, vWorksheetId);
			
			try {
				align.alignAndUpdate(c, false);
			} catch (Exception e) {
				logger.error("Error occured while setting the semantic type!", e);
				return new UpdateContainer(new ErrorUpdate(
						"Error occured while setting the semantic type!"));
			}
			return c;
			
		} else {
			// Just do the alignment, no training and update JSON required.
			AlignToOntology align = new AlignToOntology(worksheet, vWorkspace, vWorksheetId);
			align.align(false);
		}
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
