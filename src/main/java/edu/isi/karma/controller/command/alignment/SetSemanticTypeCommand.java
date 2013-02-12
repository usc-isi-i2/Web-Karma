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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeTrainingThread;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class SetSemanticTypeCommand extends Command {

	private final String hNodeId;
	private final String vWorksheetId;
	private final boolean trainAndShowUpdates;
	private CRFColumnModel oldColumnModel;
	private SynonymSemanticTypes oldSynonymTypes;
	private JSONArray typesArr;
	private SynonymSemanticTypes newSynonymTypes;
	private final boolean isPartOfKey;
	private Link newLink;
	
	private SemanticType oldType;
	private SemanticType newType;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	protected SetSemanticTypeCommand(String id, String vWorksheetId, String hNodeId, 
			boolean isPartOfKey, JSONArray typesArr, boolean trainAndShowUpdates) {
		super(id);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		this.isPartOfKey = isPartOfKey;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.typesArr = typesArr;

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
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(ontMgr);
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}
		
		/*** Add the appropriate nodes and links in alignment graph ***/
		HNode hnode = vWorkspace.getRepFactory().getHNode(hNodeId);
		String columnName = hnode.getColumnName();
		ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
		if (columnNode == null) {
			columnNode = alignment.addColumnNode(hNodeId, columnName);
		}
		
		SemanticType newType = null;
		List<SemanticType> typesList = new ArrayList<SemanticType>();
		for (int i = 0; i < typesArr.length(); i++) {
			try {
				JSONObject type = typesArr.getJSONObject(i);
				System.out.println("FULL TYPE:" + type.getString(ClientJsonKeys.FullType.name()));
				System.out.println("Domain: " + type.getString(ClientJsonKeys.Domain.name()));
				
				// Look if the domain value exists. If it exists, then it is a domain of a data property. If not
				// then the value in FullType has the the value which indicates if a new class instance is needed
				// or an existing class instance should be used (this is the case when just the class is chosen as a sem type.
				Label domainName = null;
				Node classNode = null;
				String domainValue = type.getString(ClientJsonKeys.Domain.name());
				if (!domainValue.equals("")) {
					// Check if the domain is an existing instance
					classNode = alignment.getNodeById(domainValue);
					
					if (classNode == null) {
						Label domainLabel = ontMgr.getUriLabel(domainValue);
						classNode = alignment.addInternalClassNode(domainLabel);
					}
					domainName = classNode.getLabel();
				}
				
				String fullTypeValue = type.getString(ClientJsonKeys.FullType.name());
				if (type.getBoolean(ClientJsonKeys.isPrimary.name())) {
					// Add a class link if the domain is null
					if (domainName == null) {
						// Check if it is an existing class instance node
						classNode = alignment.getNodeById(fullTypeValue);
						if (classNode == null) {
							Label classLabel = ontMgr.getUriLabel(fullTypeValue);
							classNode = alignment.addInternalClassNode(classLabel);
						}
						
						LinkKeyInfo keyInfo = isPartOfKey ? LinkKeyInfo.PartOfKey : LinkKeyInfo.None;
						ClassInstanceLink clsLink = alignment.addClassInstanceLink(classNode, columnNode, keyInfo);
						alignment.align();
						newLink = clsLink;
						
						// Create the semantic type object
						newType = new SemanticType(hNodeId, classNode.getLabel(),domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
					} 
					// Add a property link if both type (property) and domain (class) is present 
					else {
						Label propertyLabel = ontMgr.getUriLabel(fullTypeValue);
						DataPropertyLink propLink = alignment.addDataPropertyLink(classNode, columnNode, propertyLabel, isPartOfKey);
						alignment.align();
						newLink = propLink;

						// Create the semantic type object
						newType = new SemanticType(hNodeId, propertyLabel,domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
					}
				} else { // Synonym semantic type
					SemanticType synType = new SemanticType(hNodeId, ontMgr.getUriLabel(fullTypeValue),domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
					typesList.add(synType);
				}
			} catch (JSONException e) {
				logger.error("JSON Exception occured", e);
			}
		}
		
		UpdateContainer c = new UpdateContainer();
		CRFModelHandler crfModelHandler = vWorkspace.getWorkspace().getCrfModelHandler();

		// Save the old SemanticType object and CRF Model for undo
		oldType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(hNodeId);
		oldColumnModel = worksheet.getCrfModel().getModelByHNodeId(hNodeId);
		oldSynonymTypes = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(hNodeId);

		// Update the SemanticTypes data structure for the worksheet
		worksheet.getSemanticTypes().addType(newType);

		// Update the synonym semanticTypes
		newSynonymTypes = new SynonymSemanticTypes(typesList);
		worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), newSynonymTypes);

		if(trainAndShowUpdates) {
			// Train the semantic type in a separate thread
			Thread t = new Thread(new SemanticTypeTrainingThread(crfModelHandler, worksheet, newType));
			t.start();

			c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
			try {
				// Add the visualization update
				List<String> hNodeIdList = new ArrayList<String>();
				VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
				List<HNodePath> columns = vw.getColumns();
				for(HNodePath path:columns)
					hNodeIdList.add(path.getLeaf().getId());
				c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorksheetId, alignmentId, alignment, hNodeIdList));
			} catch (Exception e) {
				logger.error("Error occured while setting the semantic type!", e);
				return new UpdateContainer(new ErrorUpdate(
						"Error occured while setting the semantic type!"));
			}
			return c;
			
		} else {
			// Just do the alignment, no training and update JSON required.
//			AlignToOntology align = new AlignToOntology(worksheet, vWorkspace, vWorksheetId);
//			align.align(false);
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
