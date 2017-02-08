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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;

public class UnassignSemanticTypeCommand extends WorksheetCommand {

	private final String hNodeId;
	private String columnName;
	private ArrayList<SemanticType> oldSemanticTypes;
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
	public enum JsonKeys {
		edgeSourceId, edgeId, edgeTargetId, edgeSourceUri, edgeTargetUri
	}
	
	private static Logger logger = LoggerFactory
			.getLogger(UnassignSemanticTypeCommand.class);

	public UnassignSemanticTypeCommand(String id, String model, String hNodeId, String worksheetId) {
		super(id, model, worksheetId);
		this.hNodeId = hNodeId;
		addTag(CommandTag.SemanticType);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Unassign Semantic Type";
	}

	@Override
	public String getDescription() {
		return columnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		// Save the old SemanticType object for undo
		SemanticTypes types = worksheet.getSemanticTypes();
		oldSemanticTypes = types.getSemanticTypeForHNodeId(hNodeId);
		HashMap<String, SemanticType> semanticIdMap = new HashMap<>();
		for(SemanticType type : oldSemanticTypes) {
			String semId = LinkIdFactory.getLinkId(type.getType().getUri(), type.getDomainId(), type.getHNodeId());
			semanticIdMap.put(semId, type);
		}
		
		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>)alignment.getGraph().clone();
		
		ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
		Set<LabeledLink> alignmentEdges = alignment.getCurrentIncomingLinksToNode(hNodeId);
		for(LabeledLink edge: alignmentEdges) {
			String linkId = edge.getId();
			SemanticType type = semanticIdMap.get(linkId);
			types.removeType(type);
			columnNode.unassignUserType(type);
			columnNode.setForced(false);
			
			LabeledLink currentLink = alignment.getLinkById(linkId);
			alignment.removeLink(linkId);
			Node domain = currentLink.getSource();
			if (domain != null) {
				String domainId = domain.getId();
				if (alignment.isNodeIsolatedInTree(domainId))
					alignment.removeNode(domainId);
			}
		}
		
		if(!this.isExecutedInBatch())
			alignment.align();
		
		// Get the column name
		HNodePath currentPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				currentPath = path;
				columnName = path.getLeaf().getColumnName();
				break;
			}
		}

		// Remove the nodes (if any) from the outlier tag
		Collection<edu.isi.karma.rep.Node> nodes = new ArrayList<>();
		//TODO What does that mean? 
		worksheet.getDataTable().collectNodes(currentPath, nodes, SuperSelectionManager.DEFAULT_SELECTION);
		Set<String> nodeIds = new HashSet<>();
		for (edu.isi.karma.rep.Node node : nodes) {
			nodeIds.add(node.getId());
		}
		workspace.getTagsContainer().getTag(TagName.Outlier)
				.removeNodeIds(nodeIds);

		// Update the container
		UpdateContainer c = new UpdateContainer();
		
		c.add(new SemanticTypesUpdate(worksheet, worksheetId));
		// Add the alignment update
		try {
			c.add(new AlignmentSVGVisualizationUpdate(worksheetId));
		} catch (Exception e) {
			logger.error("Error occured while unassigning the semantic type!",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while unassigning the semantic type!"));
		}
		c.add(new TagsUpdate());
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		// Add the old SemanticType object if it is not null
		SemanticTypes types = worksheet.getSemanticTypes();
		if (oldSemanticTypes != null) {
			types.setType(oldSemanticTypes);
		}
		// Update the container
		UpdateContainer c = new UpdateContainer();
		
		// Update with old alignment
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);
		try {
			c.add(new SemanticTypesUpdate(worksheet, worksheetId));
			c.add(new AlignmentSVGVisualizationUpdate(worksheetId));
		} catch (Exception e) {
			logger.error("Error occured during undo of unassigning the semantic type!", e);
			return new UpdateContainer(new ErrorUpdate("Error occured during undo of unassigning the semantic type!"));
		}
		return c;
	}

	public ArrayList<SemanticType> getOldSemanticTypes() {
		return oldSemanticTypes;
	}

	@Override
	public Set<String> getInputColumns() {
		return new HashSet<>(Arrays.asList(hNodeId));
	}

	@Override
	public Set<String> getOutputColumns() {
		return new HashSet<>(Arrays.asList(hNodeId));
	}

}
