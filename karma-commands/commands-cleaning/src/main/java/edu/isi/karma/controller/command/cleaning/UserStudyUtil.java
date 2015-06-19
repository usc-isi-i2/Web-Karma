package edu.isi.karma.controller.command.cleaning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.rep.cleaning.TransformationExample;

public class UserStudyUtil {
	public static void logStart(JSONObject store, long start_time){
		store.put("start", start_time);
	}
	public static void logEnd(JSONObject store, long end_time){
		store.put("end", end_time);
	}
	public static void logOneiteration(JSONObject store, String wid, long time,Vector<TransformationExample> examples, List<String> recmdIds, HashMap<String, HashMap<String, String>> resdata){
		JSONObject data = UserStudyUtil.createIterationBody(time, examples, recmdIds, resdata);
		if(!store.isNull(wid)){
			JSONArray ws = (JSONArray) store.get(wid);
			ws.put(data);
		}
		else{
			JSONArray ws = new JSONArray();
			ws.put(data);
			store.put(wid, ws);
		}
	}
	private static JSONObject createIterationBody(long time,Vector<TransformationExample> examples, List<String> recmdIds, HashMap<String, HashMap<String, String>> resdata){
		JSONObject ret = new JSONObject();
		ret.put("time", time);
		JSONArray array = new JSONArray();
		for(TransformationExample exp: examples){
			JSONObject tmp = new JSONObject();
			tmp.put("ID", exp.getNodeId());
			tmp.put("Origin", exp.getBefore());
			tmp.put("Transformed", exp.getAfter());
			array.put(tmp);
		}
		ret.put("examples", array);
		JSONArray rmdIds = new JSONArray();
		for(String id: recmdIds){
			rmdIds.put(id);
		}
		ret.put("recommend", rmdIds);
		
		JSONArray sampleResult = new JSONArray();
		for(String key: resdata.keySet()){
			JSONObject obj = new JSONObject();
			obj.put("ID", key);
			obj.put("Origin", resdata.get(key).get("Org"));
			obj.put("Transformed", resdata.get(key).get("Tar"));
			sampleResult.put(obj);
		}
		ret.put("sample", sampleResult);
		return ret;
	}
	public static void storeStudyData(String name, JSONObject data){
		UserStudyUtil.logEnd(data, System.currentTimeMillis());
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(
					"./log/"+name+".txt")));
			out.write(data.toString()+"\n");
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
