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

import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class AddUserLinkToAlignmentCommand extends Command {

	private final String vWorksheetId;
	private final String edgeId;
	private final String alignmentId;
	private Alignment 	 oldAlignment;
	private DirectedWeightedMultigraph<Node, Link> oldGraph;
	private String edgeLabel;
	
	// private String edgeLabel;
	private static Logger logger = LoggerFactory.getLogger(AddUserLinkToAlignmentCommand.class);

	public AddUserLinkToAlignmentCommand(String id, String edgeId,
			String alignmentId, String vWorksheetId) {
		super(id);
		this.edgeId = edgeId;
		this.alignmentId = alignmentId;
		this.vWorksheetId = vWorksheetId;
		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add User Link";
	}

	@Override
	public String getDescription() {
		return edgeLabel;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		if(alignment == null || alignment.getGraphLinks().size() == 0) {
			logger.error("Alignment cannot be null before calling this command since the alignment is created while " +
					"setting the semantic types.");
			return new UpdateContainer(new ErrorUpdate("Error occured while generating the model for the source."));
		}
		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, Link>)alignment.getGraph().clone();
		
		// Set the other links to the target node to normal
		LinkIdFactory.getLinkTargetId(edgeId);
		Set<Link> currentLinks = alignment.getCurrentLinksToNode(LinkIdFactory.getLinkTargetId(edgeId));
		if (currentLinks != null && !currentLinks.isEmpty()) {
			for (Link currentLink: currentLinks) {
				//if (currentLink.getSource().getId().equals(newLink.getSource().getId()))
					alignment.changeLinkStatus(currentLink.getId(), LinkStatus.Normal);
			}
		}
		
		// Change the status of the user selected edge
		alignment.changeLinkStatus(edgeId, LinkStatus.ForcedByUser);
		alignment.align();
		
		return getAlignmentUpdateContainer(alignment, worksheet, vWorkspace);
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Revert to the old alignment
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);
		
		// Get the alignment update
		return getAlignmentUpdateContainer(oldAlignment, worksheet, vWorkspace);
	}

	private UpdateContainer getAlignmentUpdateContainer(Alignment alignment,
			Worksheet worksheet, VWorkspace vWorkspace) {
		// Add the visualization update
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
		c.add(new SVGAlignmentUpdate_ForceKarmaLayout(
				vWorkspace.getViewFactory().getVWorksheet(vWorksheetId), alignment));
		return c;
	}
}
