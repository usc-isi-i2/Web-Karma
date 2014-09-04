package edu.isi.karma.mapreduce.function;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.json.JSONObject;
public class MergeJSON extends UDF{
	public Text evaluate(Text target, Text source, Text path) {
		if (source == null || path == null)
			return target;
		String[] array = path.toString().split("/");
		JSONObject targetObj = new JSONObject(target.toString());
		JSONObject sourceObj = new JSONObject(source.toString());
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
			if (obj instanceof JSONArray) {
				JSONArray tmpArray = (JSONArray)obj;
				for (int i = 0; i < tmpArray.length(); i++) {
					if (tmpArray.get(i).equals(sourceObj.get("@id"))) {
						tmpArray.put(i, sourceObj);
					}
				}
			}
		}
		return new Text(targetObj.toString());
	}
}
