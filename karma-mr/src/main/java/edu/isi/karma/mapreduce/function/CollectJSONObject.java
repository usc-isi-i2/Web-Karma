package edu.isi.karma.mapreduce.function;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectJSONObject extends UDF {
	private static Logger LOG = LoggerFactory.getLogger(MergeJSON.class);
	public Text evaluate(Text target, Text path) {
		try {
			if (path == null)
				return target;
			String targetString = target.toString();
			String pathString = path.toString().replaceAll("\\[.*\\]","");
			JSONObject obj = new JSONObject(targetString);
			JSONArray array = new JSONArray();
			for (String aPath : pathString.split(",")) {
				String[] levels = aPath.split("\\.");
				collectJSONObject(obj, levels, 1, array);
			}
			return new Text(array.toString());
		}catch(Exception e) {
			LOG.error("something wrong",e );
			return target;
		}
	}

	public void collectJSONObject(JSONObject obj, String[] levels, int i, JSONArray array) {
		if (i < levels.length && obj.has(levels[i])) {
			if (i == levels.length - 1) {
				Object value = obj.get(levels[i]);
				if (value instanceof JSONArray) {
					JSONArray t = (JSONArray)value;
					for (int j = 0; j < t.length(); j++) {
						array.put(t.get(j));
					}
				}
				else {
					array.put(value);
				}
			}
			else {
				Object value = obj.get(levels[i]);
				if (value instanceof JSONArray) {
					JSONArray t = (JSONArray)value;
					for (int j = 0; j < t.length(); j++) {
						try {
							JSONObject o = t.getJSONObject(j);
							collectJSONObject(o, levels, i + 1, array);
						}catch (Exception e) {

						}
					}
				}
				else if (value instanceof JSONObject){
					collectJSONObject((JSONObject)value, levels, i + 1, array);
				}

			}
		}
	}
}
