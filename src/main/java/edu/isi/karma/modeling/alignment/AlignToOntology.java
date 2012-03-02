package edu.isi.karma.modeling.alignment;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.AlignmentHeadersUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rdf.WorksheetRDFGenerator;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.alignmentHeadings.AlignmentForest;
import edu.isi.karma.webserver.KarmaException;

public class AlignToOntology {
	private Worksheet worksheet;
	private VWorkspace vWorkspace;
	private final String vWorksheetId;
	
	private static Logger logger = LoggerFactory.getLogger(AlignToOntology.class);
	
	public AlignToOntology(Worksheet worksheet, VWorkspace vWorkspace,
			String vWorksheetId) {
		super();
		this.worksheet = worksheet;
		this.vWorkspace = vWorkspace;
		this.vWorksheetId = vWorksheetId;
	}

	public void update(UpdateContainer c, boolean replaceExistingAlignment) throws KarmaException {
		final String alignmentId = getAlignmentId();
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
		List<HNode> sortedHeaderNodes = new ArrayList<HNode>(); 
		worksheet.getHeaders().getSortedLeafHNodes(sortedHeaderNodes);
		
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment
				.getSteinerTree();
		Vertex root = alignment.GetTreeRoot();
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);

		if (root != null) {			
			//mariam
			WorksheetRDFGenerator.testRDFGeneration(vWorkspace.getRepFactory(), worksheet, tree, root);
			/////////////////////////
			
			// Convert the tree into a AlignmentForest			
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
			// Add an empty alignment headers update
			c.add(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray emptyEdgesArray = new JSONArray();

				try {
					obj.put(AbstractUpdate.GenericJsonKeys.updateType.name(), AlignmentHeadersUpdate.class.getSimpleName());
					obj.put(AlignmentHeadersUpdate.JsonKeys.worksheetId.name(), vWorksheetId);
					obj.put(AlignmentHeadersUpdate.JsonKeys.alignmentId.name(), alignmentId);
					obj.put(AlignmentHeadersUpdate.JsonKeys.rows.name(), emptyEdgesArray);
					pw.println(obj.toString(4));
				} catch (JSONException e) {
					logger.error("Error generating JSON!");
				}
			}
		});
			logger.error("Alignment returned null root!");
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
		return vWorkspace.getWorkspace().getId() + ":" + vWorksheetId + "AL";
	}
}
