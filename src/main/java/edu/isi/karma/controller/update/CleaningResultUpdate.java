package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.cleaning.ValueCollection;
import edu.isi.karma.view.VWorkspace;

public class CleaningResultUpdate extends AbstractUpdate {

	private Vector<String> jsons;
	private HashMap<String,Vector<String>> js2tps;
	private String hNodeId = "";

	public enum JsonKeys {
		worksheetId, hNodeId, result
	}

	private static Logger logger = LoggerFactory
			.getLogger(CleaningResultUpdate.class);

	public CleaningResultUpdate(String hNodeId, Vector<String> js,HashMap<String,Vector<String>> jstp) {
		this.hNodeId = hNodeId;
		jsons = js;
		js2tps = jstp;
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
			obj.put(JsonKeys.result.name(), jsa);
			pw.print(obj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON!", e);
		}
	}
}
