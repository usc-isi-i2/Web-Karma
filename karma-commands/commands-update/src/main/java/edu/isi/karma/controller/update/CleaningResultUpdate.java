package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class CleaningResultUpdate extends AbstractUpdate {

	private HashMap<String,HashMap<String, String>> map = new HashMap<>();
	private String hNodeId = "";
	private String varString;
	private Set<String> topkey = new HashSet<>();
	public enum JsonKeys {
		worksheetId, hNodeId, result
	}
	private static Logger logger = LoggerFactory
			.getLogger(CleaningResultUpdate.class);
	public CleaningResultUpdate(String hNodeId, HashMap<String,HashMap<String, String>> store,String vars,Set<String> keys) {
		this.hNodeId = hNodeId;
		topkey = keys;
		varString = vars;
		this.map = store;
	}
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), getUpdateType());
			obj.put(JsonKeys.hNodeId.name(), hNodeId);

			JSONArray jsa = new JSONArray();
			JSONObject bestpac = new JSONObject();
			if(!map.keySet().isEmpty())
			{
				JSONObject jsBest = new JSONObject(map);
				bestpac.put("data", jsBest);
			}
			JSONArray jba = new JSONArray();
			for(String key:topkey)
			{
				jba.put(key);
			}
			bestpac.put("tps",new JSONObject());
			bestpac.put("top", jba);
			jsa.put(0,bestpac);//put the best one as the first
			if(varString.compareTo("")!=0)
			{
				/*JSONObject jsBest = new JSONObject(varString);
				JSONObject varpac = new JSONObject();
				JSONArray jba = new JSONArray();
				varpac.put("data", jsBest);
				varpac.put("tps",new JSONObject());
				jsa.put(1,varpac);//put the var as the second
				*/
			}
			
			obj.put(JsonKeys.result.name(), jsa);
			pw.print(obj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON!", e);
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof CleaningResultUpdate) {
			CleaningResultUpdate t = (CleaningResultUpdate)o;
			return t.hNodeId.equals(hNodeId);
		}
		return false;
	}
}
