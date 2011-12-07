package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.controller.update.AlignmentHeadersUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.alignmentHeadings.AlignmentForest;

public class AlignToOntology {
	private Worksheet worksheet;
	private VWorkspace vWorkspace;
	private final String vWorksheetId;

	public AlignToOntology(Worksheet worksheet, VWorkspace vWorkspace,
			String vWorksheetId) {
		super();
		this.worksheet = worksheet;
		this.vWorkspace = vWorkspace;
		this.vWorksheetId = vWorksheetId;
	}

	public void update(UpdateContainer c, boolean replaceExistingAlignment) {
		String alignmentId = getAlignmentId();
		// Get the previous alignment
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		// If we need to use the previous alignment (if it exists)
		if (!replaceExistingAlignment) {
			// If the alignment does not exists, create a new one
			if (alignment == null) {
				alignment = getNewAlignment(worksheet);
			}
		} else {
			alignment = getNewAlignment(worksheet);
		}

		// Get the list of sorted column names
		List<HNode> sortedHeaderNodes = worksheet.getHeaders()
				.getSortedHNodes();

		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment
				.getSteinerTree();
		Vertex root = alignment.GetTreeRoot();
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);

		// Convert the tree into a AlignmentForest
		if (root != null) {
			AlignmentForest forest = AlignmentForest.constructFromSteinerTree(
					tree, root, sortedHeaderNodes);
			AlignmentHeadersUpdate alignmentUpdate = new AlignmentHeadersUpdate(
					forest, vWorksheetId, alignmentId);

			// Create new vWorksheet using the new header order
			List<HNodePath> columnPaths = new ArrayList<HNodePath>();
			for (HNode node : sortedHeaderNodes) {
				HNodePath path = new HNodePath(node);
				columnPaths.add(path);
			}

			vWorkspace.getViewFactory().updateWorksheet(vWorksheetId,
					worksheet, columnPaths, vWorkspace);
			VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(
					vWorksheetId);

			// Debug
			GraphUtil.printGraph(tree);

			c.add(alignmentUpdate);
			vw.update(c);
			c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
		} else {
			// TODO Return an error update showing that no columns were
			// semantically typed
		}
	}

	private Alignment getNewAlignment(Worksheet worksheet2) {
		SemanticTypes semTypes = worksheet.getSemanticTypes();
		// Get the list of semantic types
		List<SemanticType> types = new ArrayList<SemanticType>();
		for (SemanticType type : semTypes.getTypes().values()) {
			types.add(type);
		}

		return new Alignment(types);
	}

	private String getAlignmentId() {
		return vWorksheetId + "AL";
	}
}
