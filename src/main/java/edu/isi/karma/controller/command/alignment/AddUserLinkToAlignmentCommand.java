package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AlignmentHeadersUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rdf.WorksheetRDFGenerator;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.alignmentHeadings.AlignmentForest;
import edu.isi.karma.webserver.KarmaException;

public class AddUserLinkToAlignmentCommand extends Command {

	private final String vWorksheetId;
	private final String edgeId;
	private final String alignmentId;

	// private String edgeLabel;

	public AddUserLinkToAlignmentCommand(String id, String edgeId,
			String alignmentId, String vWorksheetId) {
		super(id);
		this.edgeId = edgeId;
		this.alignmentId = alignmentId;
		this.vWorksheetId = vWorksheetId;
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
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();
		// Add the user provided edge
		alignment.addUserLink(edgeId);

		return getAlignmentUpdateContainer(alignment, worksheet, vWorkspace);
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Clear the user provided edge
		alignment.clearUserLink(edgeId);

		// Get the alignment update
		return getAlignmentUpdateContainer(alignment, worksheet, vWorkspace);
	}

	private UpdateContainer getAlignmentUpdateContainer(Alignment alignment,
			Worksheet worksheet, VWorkspace vWorkspace) {
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment
				.getSteinerTree();
		Vertex root = alignment.GetTreeRoot();

		List<HNode> sortedHeaders = worksheet.getHeaders().getSortedHNodes();
		// Convert the tree into a AlignmentForest
		AlignmentForest forest = AlignmentForest.constructFromSteinerTree(tree,
				root, sortedHeaders);
		AlignmentHeadersUpdate alignmentUpdate = new AlignmentHeadersUpdate(
				forest, vWorksheetId, alignmentId);
		GraphUtil.printGraph(tree);
		
		//mariam
		try{
		WorksheetRDFGenerator.testRDFGeneration(vWorkspace.getWorkspace(), worksheet, tree, root);
		}catch(KarmaException e){
			e.printStackTrace();
		}
		/////////////////////////

		// Create new vWorksheet using the new header order
		List<HNodePath> columnPaths = new ArrayList<HNodePath>();
		for (HNode node : sortedHeaders) {
			HNodePath path = new HNodePath(node);
			columnPaths.add(path);
		}
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,
				columnPaths, vWorkspace);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);

		UpdateContainer c = new UpdateContainer();
		c.add(alignmentUpdate);
		vw.update(c);
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
		return c;
	}
}
