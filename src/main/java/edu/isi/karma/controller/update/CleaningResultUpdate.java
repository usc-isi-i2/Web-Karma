package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class CleaningResultUpdate extends AbstractUpdate {

	private Vector<String> jsons;
	private HashMap<String,Vector<String>> js2tps;
	private String hNodeId = "";
	private String bestRes;
	private Set<String> topkey = new HashSet<String>();

	public enum JsonKeys {
		worksheetId, hNodeId, result
	}

	private static Logger logger = LoggerFactory
			.getLogger(CleaningResultUpdate.class);

	public CleaningResultUpdate(String hNodeId, Vector<String> js,HashMap<String,Vector<String>> jstp,String bestRes,Set<String> keys) {
		this.hNodeId = hNodeId;
		jsons = js;
		js2tps = jstp;
		this.bestRes = bestRes; 
		topkey = keys;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), getUpdateType());
			obj.put(JsonKeys.hNodeId.name(), hNodeId);

			JSONArray jsa = new JSONArray();
			for(String s:jsons)
			{
				JSONObject pac = new JSONObject();
				JSONObject jo = new JSONObject(s);
				Vector<String> tps = js2tps.get(s);
				JSONArray jstps = new JSONArray();
				JSONObject tpsjo = new JSONObject();
				for(int i = 0; i<tps.size();i++)
				{		
					tpsjo.put("rule"+i, tps.get(i));
				}
				pac.put("data", jo);
				pac.put("tps", tpsjo);
				jsa.put(pac);
			}
			JSONObject jsBest = new JSONObject(bestRes);
			JSONObject bestpac = new JSONObject();
			JSONArray jba = new JSONArray();
			for(String key:topkey)
			{
				jba.put(key);
			}
			bestpac.put("data", jsBest);
			bestpac.put("tps",new JSONObject());
			bestpac.put("top", jba);
			jsa.put(0,bestpac);//put the best one as the first
			obj.put(JsonKeys.result.name(), jsa);
			pw.print(obj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON!", e);
		}
	}
}
