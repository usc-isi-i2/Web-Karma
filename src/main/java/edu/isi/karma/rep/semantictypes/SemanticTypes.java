package edu.isi.karma.rep.semantictypes;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SemanticTypes implements Jsonizable {
	// Map from the HNodeIds (for each column) to the semantic type
	private Map<String, SemanticType> types = new HashMap<String, SemanticType>();

	public Map<String, SemanticType> getTypes() {
		return types;
	}

	public SemanticType getSemanticTypeByHNodeId(String hNodeId) {
		return types.get(hNodeId);
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.array();
		for (SemanticType type : types.values()) {
			type.write(writer);
		}
		writer.endArray();
	}

	@SuppressWarnings("unused")
	private void initializeFromJSON() {

	}

	public void addType(SemanticType type) {
		types.put(type.getHNodeId(), type);
	}
}
