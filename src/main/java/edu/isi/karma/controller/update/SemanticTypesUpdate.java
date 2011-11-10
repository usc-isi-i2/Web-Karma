package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SemanticTypesUpdate extends AbstractUpdate {
	private Worksheet worksheet;
	private String vWorksheetId;

	public enum JsonKeys {
		HNodeId, FullType, ConfidenceLevel, Origin, FullCRFModel, DisplayLabel, Domain
	}

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

			writer.key("worksheetId").value(vWorksheetId).key("updateType")
					.value("SemanticTypesUpdate");

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
					writer.key(JsonKeys.HNodeId.name())
							.value(type.getHNodeId())
							.key(JsonKeys.FullType.name())
							.value(type.getType())
							.key(JsonKeys.Origin.name())
							.value(type.getOrigin().name())
							.key(JsonKeys.ConfidenceLevel.name())
							.value(type.getConfidenceLevel().name())
							.key(JsonKeys.DisplayLabel.name())
							.value(SemanticTypeUtil.removeNamespace(type
									.getType()))
							.key(JsonKeys.Domain.name())
							.value(SemanticTypeUtil.removeNamespace(type
									.getDomain()));
				} else {
					writer.key(JsonKeys.HNodeId.name()).value(nodeId)
							.key(JsonKeys.FullType.name()).value("")
							.key(JsonKeys.Origin.name()).value("")
							.key(JsonKeys.ConfidenceLevel.name()).value("");
				}

				// Populate the CRF Model
				if (type != null) {
					CRFColumnModel colModel = worksheet.getCrfModel()
							.getModelByHNodeId(nodeId);
					writer.key(JsonKeys.FullCRFModel.name()).value(
							colModel.getAsJSONObject());
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
