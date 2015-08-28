package edu.isi.karma.controller.update;
import edu.isi.karma.view.VWorkspace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CleaningResultUpdate extends AbstractUpdate {

	private HashMap<String,HashMap<String, String>> map = new HashMap<String, HashMap<String,String>>();
	private String hNodeId = "";
	private List<String> topkey = new ArrayList<String>();
	private List<String> minimal = new ArrayList<String>();
	private double coverage = 0.0;
	public enum JsonKeys {
		worksheetId, hNodeId, result
	}
	private static Logger logger = LoggerFactory
			.getLogger(CleaningResultUpdate.class);
	public CleaningResultUpdate(String hNodeId, HashMap<String,HashMap<String, String>> store,double coverage, List<String> keys, List<String> minimal) {
		this.hNodeId = hNodeId;
		topkey = keys;
		this.map = store;
		this.coverage = coverage;
		this.minimal = minimal;
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
			if(map.keySet().size()>0)
			{
				JSONObject jsBest = new JSONObject(map);
				bestpac.put("data", jsBest);
			}
			JSONArray recmd = new JSONArray();
			for(String key:topkey)
			{
				recmd.put(key);
			}
			JSONArray min = new JSONArray();
			for(String key: this.minimal){
				min.put(key);
			}
			bestpac.put("tps",new JSONObject());
			bestpac.put("top", recmd);
			bestpac.put("coverage", this.coverage);
			bestpac.put("minimal",min );
			jsa.put(0,bestpac);//put the best one as the first
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
