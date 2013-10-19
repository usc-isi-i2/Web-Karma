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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.view.VWorkspace;

public class UnassignSemanticTypeCommand extends Command {

	private final String vWorksheetId;
	private final String hNodeId;
	private String columnName;
	private SemanticType oldSemanticType;
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, Link> oldGraph;

	private static Logger logger = LoggerFactory
			.getLogger(UnassignSemanticTypeCommand.class);

	public UnassignSemanticTypeCommand(String id, String hNodeId, String vWorksheetId) {
		super(id);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		
		addTag(CommandTag.Modeling);
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Save the old SemanticType object for undo
		SemanticTypes types = worksheet.getSemanticTypes();
		oldSemanticType = types.getSemanticTypeForHNodeId(hNodeId);
		types.unassignColumnSemanticType(hNodeId);

		// Save the original alignment for undo
		Alignment alignment = AlignmentManager.Instance().getAlignment(vWorkspace.getWorkspace().getId(), vWorksheetId);
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, Link>)alignment.getGraph().clone();
		
		// Remove it from the alignment
		ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
		if (columnNode != null) {
			Link currentLink = alignment.getCurrentIncomingLinksToNode(columnNode.getId()).iterator().next();
			String domainNodeId = currentLink.getSource().getId();
			// Remove the existing link
			alignment.removeLink(currentLink.getId());
			// Remove the column node
			alignment.removeNode(columnNode.getId());
			// Remove the source node
			alignment.removeNode(domainNodeId);
			
		}
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
		Collection<edu.isi.karma.rep.Node> nodes = new ArrayList<edu.isi.karma.rep.Node>();
		worksheet.getDataTable().collectNodes(currentPath, nodes);
		Set<String> nodeIds = new HashSet<String>();
		for (edu.isi.karma.rep.Node node : nodes) {
			nodeIds.add(node.getId());
		}
		vWorkspace.getWorkspace().getTagsContainer().getTag(TagName.Outlier)
				.removeNodeIds(nodeIds);

		// Update the container
		UpdateContainer c = new UpdateContainer();
		
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
		// Add the alignment update
		try {
			c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorkspace.getViewFactory().getVWorksheet(vWorksheetId), alignment));
		} catch (Exception e) {
			logger.error("Error occured while unassigning the semantic type!",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while unassigning the semantic type!"));
		}
		c.add(new TagsUpdate());
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Add the old SemanticType object if it is not null
		SemanticTypes types = worksheet.getSemanticTypes();
		if (oldSemanticType != null)
			types.addType(oldSemanticType);

		// Update the container
		UpdateContainer c = new UpdateContainer();
		
		// Update with old alignment
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);
		try {
			c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, oldAlignment));
			c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorkspace.getViewFactory().getVWorksheet(vWorksheetId), oldAlignment));
		} catch (Exception e) {
			logger.error("Error occured during undo of unassigning the semantic type!", e);
			return new UpdateContainer(new ErrorUpdate("Error occured during undo of unassigning the semantic type!"));
		}
		return c;
	}

}
