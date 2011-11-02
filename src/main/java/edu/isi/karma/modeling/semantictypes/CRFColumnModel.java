package edu.isi.karma.modeling.semantictypes;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;
import edu.isi.karma.util.Util;

public class CRFColumnModel implements Jsonizable {

	private final HashMap<String, Double> scoreMap = new HashMap<String, Double>();

	public CRFColumnModel(ArrayList<String> labels, ArrayList<Double> scores) {
		for (int i = 0; i < labels.size(); i++) {
			scoreMap.put(labels.get(i), scores.get(i));
		}
	}

	public HashMap<String, Double> getScoreMap() {
		return scoreMap;
	}

	public Double getScoreForLabel(String label) {
		return scoreMap.get(label);
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		writer.array();
		for (String label : scoreMap.keySet()) {
			writer.object();
			writer.key("type").value(label);
			writer.key("probability").value(scoreMap.get(label));
			writer.endObject();
		}
		writer.endArray();
		writer.endObject();
	}

	public JSONObject getAsJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();

		// Need to sort
		HashMap<String, Double> sortedMap = Util.sortHashMap(scoreMap);

		for (String label : sortedMap.keySet()) {
			JSONObject oj = new JSONObject();
			oj.put("Type", label);
			oj.put("DisplayLabel", SemanticTypeUtil.removeNamespace(label));
			oj.put("Probability", scoreMap.get(label));
			arr.put(oj);
		}
		obj.put("Labels", arr);
		return obj;
	}
}
