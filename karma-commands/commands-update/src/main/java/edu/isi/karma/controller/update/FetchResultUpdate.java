package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class FetchResultUpdate extends AbstractUpdate {

	private String hNodeId;
	private HashMap<String, HashMap<String, String>> rawData;
	public enum JsonKeys {
		worksheetId, hNodeId, result
	}
	private static Logger logger = LoggerFactory
			.getLogger(CleaningResultUpdate.class);
	public FetchResultUpdate(String HNodeId,HashMap<String, HashMap<String, String>> rows)
	{
		this.hNodeId = HNodeId;
		rawData = rows;
	}
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) 
	{
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), getUpdateType());
			obj.put(JsonKeys.hNodeId.name(), hNodeId);
			JSONArray jsa = new JSONArray();
			JSONObject jso = new JSONObject(this.rawData);
			JSONObject j = new JSONObject();
			j.put("data", jso);
			jsa.put(j);
			obj.put(JsonKeys.result.name(), jsa);
			pw.print(obj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON!", e);
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof FetchResultUpdate) {
			FetchResultUpdate t = (FetchResultUpdate)o;
			return t.hNodeId.equals(hNodeId) && t.rawData.equals(rawData);
		}
		return false;
	}
}
