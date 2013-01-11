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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.alignment.SetSemanticTypeCommandFactory_v2.METAPROPERTY_NAME;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkType;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeTrainingThread;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ClassLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.PropertyLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SubclassOfNodeMetaPropertyLink;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;
import edu.isi.karma.rep.alignment.URI;
import edu.isi.karma.rep.alignment.URIOfClassMetaPropertyLink;
import edu.isi.karma.view.VWorkspace;

public class SetSemanticTypeCommand_v2 extends Command {

	private final String hNodeId;
	private final String vWorksheetId;
	private final boolean trainAndShowUpdates;
	private CRFColumnModel oldColumnModel;
	private SynonymSemanticTypes oldSynonymTypes;
	private JSONArray typesArr;
	private SynonymSemanticTypes newSynonymTypes;
	private final boolean isPartOfKey;
	private final boolean isMetaPropertyUsed;
	private METAPROPERTY_NAME metaPropertyName;
	private String metaPropertyValue;
	private Link newLink;
	

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	protected SetSemanticTypeCommand_v2(String id, String vWorksheetId, String hNodeId, boolean isMetaPropertyUsed,
			METAPROPERTY_NAME metaPropertyName, String metaPropertyValue, boolean isPartOfKey, JSONArray typesArr, 
			boolean trainAndShowUpdates) {
		super(id);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		this.isPartOfKey = isPartOfKey;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.typesArr = typesArr;
		this.isMetaPropertyUsed = isMetaPropertyUsed;
		this.metaPropertyName = metaPropertyName;
		this.metaPropertyValue = metaPropertyValue;
		addTag(CommandTag.Modeling);
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
		return "";
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
		
		/** Check if it is setting meta property **/
		if (isMetaPropertyUsed) {
			if (metaPropertyName.equals(METAPROPERTY_NAME.isUriOfClass)) {
				Node classNode = alignment.getNodeById(metaPropertyValue);
				URIOfClassMetaPropertyLink mpLink = alignment.createURIOfClassMetaPropertyLink(classNode, columnNode);
				alignment.addLinkAndUpdateAlignment(mpLink);
			} else if (metaPropertyName.equals(METAPROPERTY_NAME.isSpecializationForEdge)) {
				
				
				
			} else if (metaPropertyName.equals(METAPROPERTY_NAME.isSubclassOfClass)) {
				Node classNode = alignment.getNodeById(metaPropertyValue);
				SubclassOfNodeMetaPropertyLink mpLink = alignment.createSubclassOfNodeMetaPropertyLink(classNode, columnNode);
				alignment.addLinkAndUpdateAlignment(mpLink);
			}
		}
		/** If it is setting a normal semantic type **/
		else {
			for (int i = 0; i < typesArr.length(); i++) {
				JSONObject type = typesArr.getJSONObject(i);
				// Look for the primary semantic type
				URI typeName = ontMgr.getURIFromString(type.getString(ClientJsonKeys.FullType.name()));
				if(typeName == null) {
					logger.error("Could not find the resource " + type.getString(ClientJsonKeys.FullType.name()) + " in ontology model!");
					return null;
				}
				URI domainName = null;
				if (!type.getString(ClientJsonKeys.Domain.name()).equals(""))
					domainName = ontMgr.getURIFromString(type.getString(ClientJsonKeys.Domain.name()));

				if (type.getBoolean(ClientJsonKeys.isPrimary.name())) {
					// Add a class link if the domain is null
					if (domainName == null) {
						Node classNode = alignment.getNodeById(typeName.getUriString());
						if (classNode == null) {
							classNode = alignment.createInternalClassNode(typeName);
						}
						ClassLink clsLink = alignment.createClassLink(classNode, columnNode, isPartOfKey);
						alignment.addLinkAndUpdateAlignment(clsLink);
						newLink = clsLink;
					} 
					// Add a property link if both type (property) and domain (class) is present 
					else {
						Node classNode = alignment.getNodeById(domainName.getUriString());
						if (classNode == null) {
							classNode = alignment.createInternalClassNode(domainName);
						}
						PropertyLink propLink = alignment.createPropertyLink(classNode, columnNode, typeName, isPartOfKey);
						alignment.addLinkAndUpdateAlignment(propLink);
						newLink = propLink;
					}
				} else { // Synonym semantic type
					SemanticType synType = new SemanticType(hNodeId, typeName,domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
					typesList.add(synType);
				}
			}
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
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		if (oldType == null) {
			worksheet.getSemanticTypes().unassignColumnSemanticType(newType.getHNodeId());
		} else {
			worksheet.getSemanticTypes().addType(oldType);
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), oldSynonymTypes);
		}

		worksheet.getCrfModel().addColumnModel(newType.getHNodeId(),oldColumnModel);

		// Get the alignment update if any
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,vWorksheetId);
		try {
			align.alignAndUpdate(c, false);
		} catch (Exception e) {
			logger.error("Error occured while unsetting the semantic type!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while unsetting the semantic type!"));
		}
		return c;
	}
}
