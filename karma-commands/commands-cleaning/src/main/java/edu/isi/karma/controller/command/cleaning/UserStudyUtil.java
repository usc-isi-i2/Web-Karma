package edu.isi.karma.controller.command.cleaning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.rep.cleaning.TransformationExample;

public class UserStudyUtil {
	public static void logAccuracy(JSONObject store, double acc, String name) {
		try {
			store.put("accuracy", acc);
			BufferedWriter out = new BufferedWriter(new FileWriter(new File("./log/" + name + UserStudyUtil.getCurrentDate() + ".json")));
			out.write(store.toString() + "\n");
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logStart(JSONObject store, long start_time) {
		try {
			store.put("start", start_time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logEnd(JSONObject store, long end_time) {
		try {
			store.put("end", end_time);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void logOneiteration(JSONObject store, String wid, long time, Vector<TransformationExample> examples, List<String> recmdIds, HashMap<String, HashMap<String, String>> resdata) {
		try {
			JSONObject data = UserStudyUtil.createIterationBody(time, examples, recmdIds, resdata);
			String key = "Iteration";
			if (!store.isNull(key)) {
				JSONArray ws = (JSONArray) store.get(key);
				ws.put(data);
			} else {
				JSONArray ws = new JSONArray();
				ws.put(data);
				store.put(key, ws);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static JSONObject createIterationBody(long time, Vector<TransformationExample> examples, List<String> recmdIds, HashMap<String, HashMap<String, String>> resdata) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("time", time);
			JSONArray array = new JSONArray();
			for (TransformationExample exp : examples) {
				JSONObject tmp = new JSONObject();
				tmp.put("ID", exp.getNodeId());
				tmp.put("Origin", exp.getBefore());
				tmp.put("Transformed", exp.getAfter());
				array.put(tmp);
			}
			ret.put("examples", array);
			JSONArray rmdIds = new JSONArray();
			for (String id : recmdIds) {
				rmdIds.put(id);
			}
			ret.put("recommend", rmdIds);

			JSONArray sampleResult = new JSONArray();
			for (String key : resdata.keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("ID", key);
				obj.put("Origin", resdata.get(key).get("Org"));
				obj.put("Transformed", resdata.get(key).get("Tar"));
				sampleResult.put(obj);
			}
			ret.put("sample", sampleResult);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;

	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		Date date = new Date();
		return StringEscapeUtils.escapeJava(dateFormat.format(date));
	}

	public static void storeStudyData(JSONObject data) {
		UserStudyUtil.logEnd(data, System.currentTimeMillis());
	}
}
