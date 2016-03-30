package edu.isi.karma.mapreduce.function;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MergeJSON extends UDF{
	private static Logger LOG = LoggerFactory.getLogger(MergeJSON.class);
	public Text evaluate(Text target, Text source, Text path, Text Id) {
		try {
			if (source == null || path == null)
				return target;
			String atId = "@id";
			if (Id != null) {
				atId = Id.toString();
			}
			String sourceString = source.toString();
			String targetString = target.toString();
			String pathString = path.toString();
			String targetResultString = mergeJSON(sourceString, targetString,
					pathString, atId);
			return new Text(targetResultString);
		}catch(Exception e) {
			LOG.error("something wrong",e );
			return target;
		}
	}
	protected static String mergeJSON(String sourceString, String targetString,
			String pathString, String atId) {
		List<String> sourceStrings = new LinkedList<>();
		sourceStrings.add(sourceString);
		return mergeJSON(sourceStrings, targetString, pathString, atId);
	}
	protected static String mergeJSON(List<String> sourceStrings, String targetString,
			String pathString, String atId) {
		String[] array = CollectJSONObject.splitPath(pathString);//pathString.split("\\.");
		
		JSONObject rootTargetObj = new JSONObject(targetString);
		JSONObject targetResult = rootTargetObj;
		ArrayList<String> pathElements = new ArrayList<>();
		for(String element:array)
		{
			if(element.equals("$"))
				continue;
			pathElements.add(element);
		}
		List<JSONObject> targetObjs = collectObjectsFromJSONPath(pathElements, rootTargetObj);
		String lastLevel = array[array.length - 1];
		for(JSONObject targetObj : targetObjs )
		{
			if (targetObj.has(lastLevel)) {
				Object obj = targetObj.get(lastLevel);
				for(String sourceString : sourceStrings)
				{
					mergeJSON(sourceString, targetObj, lastLevel, obj, atId);
				}
			}
		}
		String targetResultString = targetResult.toString();
		return targetResultString;
	}
	private static void mergeJSON(String sourceString, JSONObject targetObj,
			String lastLevel, Object obj, String atId) {
		JSONObject sourceObj = new JSONObject(sourceString);
		mergeByReplacingId(targetObj, sourceObj, lastLevel, obj, atId);
		mergeJSONObject(sourceObj, obj, atId);
		mergeIntoJSONArray(sourceObj, obj, atId);
	}
	protected static List<JSONObject> collectObjectsFromJSONPath(List<String> pathElements, Object targetObj)
	{
		List<JSONObject> collectedObjects = new LinkedList<>();
		collectObjectsFromJSONPath(collectedObjects, pathElements, targetObj);
		return collectedObjects;
	}
	
	protected static void collectObjectsFromJSONPath(List<JSONObject> collectedObjects, List<String> pathElements, Object targetObj)
	{
			String element = pathElements.get(0);
			element = element.replaceAll("\\[.*\\]","");
			if(targetObj instanceof JSONObject)
			{
				JSONObject obj = (JSONObject) targetObj;
				if(pathElements.size() == 1)
				{
					if(obj.has(element))
					{
						collectedObjects.add(obj);
					}
						
				}
				else
				{
					if(obj.has(element))
					{
						collectObjectsFromJSONPath(collectedObjects, pathElements.subList(1, pathElements.size()), ((JSONObject) targetObj).get(element));
					}
				}
			}
			else if(targetObj instanceof JSONArray)
			{
				JSONArray array = (JSONArray) targetObj;
				for(int i = 0; i < array.length(); i++)
				{
					collectObjectsFromJSONPath(collectedObjects, pathElements, array.get(i));
				}
			}
	
	}
	protected static JSONObject traverseJSONPath(String[] array, JSONObject targetObj) {
		int index = 0;
		while (index < array.length - 1 && targetObj.has(array[index])) {
			targetObj = targetObj.getJSONObject(array[index++]);
		}
		return targetObj;
	}
	protected static void mergeByReplacingId(JSONObject targetObj, JSONObject sourceObj,
			String lastLevel, Object obj, String atId) {
		if (obj instanceof String && obj.equals(sourceObj.get(atId))) {
			targetObj.put(lastLevel, sourceObj);
		}
	}
	protected static void mergeIntoJSONArray(JSONObject sourceObj, Object obj, String atId) {
		if (obj instanceof JSONArray) {
			JSONArray tmpArray = (JSONArray)obj;
			for (int i = 0; i < tmpArray.length(); i++) {
				if (tmpArray.get(i).equals(sourceObj.get(atId))) {
					tmpArray.put(i, sourceObj);
				}
				Object o = tmpArray.get(i);
				mergeJSONObject(sourceObj, o, atId);
			}
		}
	}
	protected static void mergeJSONObject(JSONObject sourceObj, Object o, String atId) {
		if (o instanceof JSONObject) {
			JSONObject t = (JSONObject)o;
			if (t.has(atId) && t.get(atId).equals(sourceObj.get(atId))) {
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
