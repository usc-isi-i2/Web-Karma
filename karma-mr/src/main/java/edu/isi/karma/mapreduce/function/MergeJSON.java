package edu.isi.karma.mapreduce.function;
import java.util.Iterator;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MergeJSON extends UDF{
	private static Logger LOG = LoggerFactory.getLogger(MergeJSON.class);
	public Text evaluate(Text target, Text source, Text path) {
		try {
			if (source == null || path == null)
				return target;
			String[] array = path.toString().split("/");
			JSONObject targetObj = new JSONObject(target.toString());
			JSONObject sourceObj = new JSONObject(source.toString());
			JSONObject targetResult = targetObj;
			int index = 0;
			while (index < array.length - 1 && targetObj.has(array[index])) {
				targetObj = targetObj.getJSONObject(array[index++]);
			}
			String lastLevel = array[array.length - 1];
			if (targetObj.has(lastLevel)) {
				Object obj = targetObj.get(lastLevel);
				if (obj instanceof String && obj.equals(sourceObj.get("@id"))) {
					targetObj.put(lastLevel, sourceObj);
				}
				if (obj instanceof JSONObject) {
					JSONObject t = (JSONObject)obj;
					if (t.has("@id") && t.get("@id").equals(sourceObj.get("@id"))) {
						@SuppressWarnings("rawtypes")
						Iterator itr = sourceObj.keys();
						while(itr.hasNext()) {
							String key = (String) itr.next();
							t.put(key, sourceObj.get(key));
						}
					}
				}
				if (obj instanceof JSONArray) {
					JSONArray tmpArray = (JSONArray)obj;
					for (int i = 0; i < tmpArray.length(); i++) {
						if (tmpArray.get(i).equals(sourceObj.get("@id"))) {
							tmpArray.put(i, sourceObj);
						}
						Object o = tmpArray.get(i);
						if (o instanceof JSONObject) {
							JSONObject t = (JSONObject)o;
							if (t.has("@id") && t.get("@id").equals(sourceObj.get("@id"))) {
								@SuppressWarnings("rawtypes")
								Iterator itr = sourceObj.keys();
								while(itr.hasNext()) {
									String key = (String) itr.next();
									t.put(key, sourceObj.get(key));
								}
							}
						}
					}
				}

			}
			return new Text(targetResult.toString());
		}catch(Exception e) {
			LOG.error("something wrong",e );
			return target;
		}
	}
}
