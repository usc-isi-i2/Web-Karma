package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SemanticTypesUpdate extends AbstractUpdate {
	private Worksheet worksheet;
	private String vWorksheetId;

	private static Logger logger = LoggerFactory
			.getLogger(SemanticTypesUpdate.class);

	public SemanticTypesUpdate(Worksheet worksheet, String vWorksheetId) {
		super();
		this.worksheet = worksheet;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		SemanticTypes types = worksheet.getSemanticTypes();

		JSONStringer jsonStr = new JSONStringer();
		try {
			JSONWriter writer = jsonStr.object();

			writer.key("worksheetId").value(vWorksheetId)
					.key("updateType").value("SemanticTypesUpdate");

			writer.key("Types");
			writer.array();
			// Iterate through all the columns
			for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
				HNode node = path.getLeaf();
				String nodeId = node.getId();

				writer.object();

				// Check if a semantic type exists for the HNode
				SemanticType type = types.getSemanticTypeByHNodeId(nodeId);
				if (type != null) {
					writer.key("HNodeID").value(type.getHNodeId()).key("Type")
							.value(type.getType()).key("Origin")
							.value(type.getOrigin().name())
							.key("ConfidenceLevel")
							.value(type.getConfidenceLevel().name());
				} else {
					writer.key("HNodeId").value(nodeId).key("Type").value("")
							.key("Origin").value("").key("ConfidenceLevel")
							.value("");
				}

				// Populate the CRF Model
				if (type != null) {
					CRFColumnModel colModel = worksheet.getCrfModel()
							.getModelByHNodeId(nodeId);
					writer.key("FullCRFModel")
							.value(colModel.getAsJSONObject());
				}

				writer.endObject();
			}
			writer.endArray();
			writer.endObject();

			pw.print(writer.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error("Error occured while writing to JSON!", e);
		}
	}
}
