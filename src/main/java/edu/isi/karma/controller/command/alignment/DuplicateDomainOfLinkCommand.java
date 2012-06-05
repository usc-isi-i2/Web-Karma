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

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rdf.WorksheetRDFGenerator;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class DuplicateDomainOfLinkCommand extends Command {

	private final String vWorksheetId;
	private final String edgeId;
	private final String alignmentId;

	protected DuplicateDomainOfLinkCommand(String id, String edgeId,
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
		return "Duplicate Domain of Link";
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
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();
		// Duplicate the domain of the edge
		alignment.duplicateDomainOfLink(edgeId);

		return getAlignmentUpdateContainer(alignment, worksheet, vWorkspace);
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

	private UpdateContainer getAlignmentUpdateContainer(Alignment alignment,
			Worksheet worksheet, VWorkspace vWorkspace){
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment
				.getSteinerTree();
		GraphUtil.printGraph(tree);

		List<String> hNodeIdList = new ArrayList<String>();
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		List<HNodePath> columns = vw.getColumns();
		for(HNodePath path:columns)
			hNodeIdList.add(path.getLeaf().getId());
		
		SVGAlignmentUpdate_ForceKarmaLayout svgUpdate = new SVGAlignmentUpdate_ForceKarmaLayout(vWorksheetId, alignmentId, alignment, hNodeIdList);
		
		//mariam
		try{
			WorksheetRDFGenerator.testRDFGeneration(vWorkspace.getWorkspace(), worksheet, alignment);
		}catch(KarmaException e){
			e.printStackTrace();
		}

		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
		c.add(svgUpdate);
		return c;
	}

}
