package edu.isi.karma.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONLDUtilSimple {
	private static JSONLDReducerComparatorSimple comparator;
	
	static {
		comparator =  new JSONLDReducerComparatorSimple();
	}

	private JSONLDUtilSimple() {
	}
	
	public static JSONObject mergeJSONObjects(String left, String right, Map<String, String> provenanceProperties) throws ParseException
	{

		JSONParser parser = new JSONParser();
		return mergeJSONObjects((JSONObject)parser.parse(left), (JSONObject)parser.parse(right), provenanceProperties);
	}
	
	public static JSONObject mergeJSONObjects(Iterator<String> iterator) throws ParseException {
		return mergeJSONObjects(iterator, new HashMap<String, String>());
	}

	public static JSONObject mergeJSONObjects(Iterator<String> iterator, Map<String, String> provenanceProperties) throws ParseException {

		JSONParser parser = new JSONParser(); 
		JSONObject accumulatorObject = new JSONObject();
		
		while(iterator.hasNext())
		{

			String value = iterator.next();
			JSONObject object = (JSONObject)parser.parse(value);
			accumulatorObject = mergeJSONObjects(accumulatorObject, object, provenanceProperties);
		}
		
		return accumulatorObject;
	}

	public static JSONObject mergeJSONObjects(JSONObject left, JSONObject right, Map<String, String> provenanceProperties)
	{
		mergeJSONObjectsInner(left, right, provenanceProperties);
		return left;
	}
	
	private static boolean mergeJSONObjectsInner(JSONObject left, JSONObject right, Map<String, String> provenanceProperties)
	{
		boolean rightAdded = false;
		ArrayList<String> names = new ArrayList<>(right.keySet());
		
		//Move provenance properties to the end of the list
		for(String provProp: provenanceProperties.keySet()) {
			if(names.contains(provProp)) {
				names.remove(provProp);
				names.add(provProp);
			}
		}
		for(Object rawName : names)
		{
			String name = (String) rawName;
			boolean is_prov_property = provenanceProperties.containsKey(name);
			if(!left.containsKey(name))
			{
				Object rightObj = right.get(name);
				left.put(name, rightObj);
				if(rightObj instanceof String && !is_prov_property)
					rightAdded = true;
			}
			else
			{
				Object leftObject = left.get(name);
				Object rightObject = right.get(name);
				JSONArray newLeftArray = getJSONArray(leftObject);
				JSONArray newRightArray = getJSONArray(rightObject);
				boolean newAdded = mergeArrays(left, name, rightAdded, newLeftArray, newRightArray, provenanceProperties);
				if(!is_prov_property)
					rightAdded = rightAdded | newAdded;
			}
		}
		return rightAdded;
	}
	
	protected static JSONArray getJSONArray(Object obj) {
		if(obj instanceof JSONArray)
			return (JSONArray)obj;
		JSONArray arr = new JSONArray();
		arr.add(obj);
		return arr;
	}
	
	protected static boolean mergeArrays(JSONObject left, String name, boolean newAdded,
			JSONArray leftArray, JSONArray rightArray, Map<String, String> provenanceProperties) {
		LinkedList<Object> newArrayBuilder = new LinkedList<Object>();
		int leftIndex = 0;
		int rightIndex = 0;
		boolean rightAdded = false;
		boolean provenanceDateProperty = false;
		
		//If it is a provenance property and no new information is added, we do not need to add the property
		if(provenanceProperties.containsKey(name) && !newAdded) {
			String provType = provenanceProperties.get(name);
			if(provType == "date") {
				//For dates, we would like to min, max date
				provenanceDateProperty = true;
			} else {
				return false;
			}
		}
		
		while(leftIndex < leftArray.size() && rightIndex < rightArray.size() )
		{
			int result = comparator.compare(leftArray.get(leftIndex),rightArray.get(rightIndex));
			if(result < 0)
			{
				newArrayBuilder.add(leftArray.get(leftIndex++));
			}
			else if (result == 0)
			{
				Object tempLeft = leftArray.get(leftIndex++);
				Object tempRight = rightArray.get(rightIndex++);
				ObjectRightAdded mergedResultAdded = mergeStringsAndJSONObjects(
						tempLeft, tempRight, provenanceProperties);
				Object mergedResult = mergedResultAdded.object;
				rightAdded = rightAdded | mergedResultAdded.rightAdded;
				newArrayBuilder.add(mergedResult);
			}
			else
			{
				Object rightObj = rightArray.get(rightIndex++);
				newArrayBuilder.add(rightObj);
				if(rightObj instanceof String)
					rightAdded = true;
			}
		}
		while(leftIndex < leftArray.size())
		{
			newArrayBuilder.add(leftArray.get(leftIndex++));
		}
		while(rightIndex < rightArray.size())
		{
			Object rightObj = rightArray.get(rightIndex++);
			newArrayBuilder.add(rightObj);
			if(rightObj instanceof String)
				rightAdded = true;
		}
		if(newArrayBuilder.size() > 1 || name.equals("a"))
		{
			JSONArray newArray = new JSONArray();
			
			if(!provenanceDateProperty) {
				newArray.addAll(newArrayBuilder);
			} else {
				String min = null, max = null;
				for(Object obj : newArrayBuilder) {
					String s = obj.toString();
					if(min == null || s.compareTo(min) < 0) 
						min = s;
					if(max == null || s.compareTo(max) > 0)
						max = s;
				}
				newArray.add(min);
				if(min != max)
					newArray.add(max);
				
			}
			left.put(name, newArray);
		}
		else if(newArrayBuilder.size() == 1)
		{
			left.put(name, newArrayBuilder.get(0));
		}
		return rightAdded;
	}
	
	private static ObjectRightAdded mergeStringsAndJSONObjects(Object tempLeft,
			Object tempRight, Map<String, String> provenanceProperties) {
		Object mergedResult = null;
		boolean rightAdded = false;
		if(tempLeft instanceof String && tempRight instanceof String)
		{
			mergedResult = tempLeft;
		}
		else if(tempLeft instanceof JSONObject && tempRight instanceof String)
		{
			mergedResult = tempLeft;
		}
		else if(tempLeft instanceof String && tempRight instanceof JSONObject)
		{
			mergedResult = tempRight;
			rightAdded = false;	//We add right, but since its an object, the object will contain prov and we dont need to count it as merged
		}
		else if(tempLeft instanceof JSONObject && tempRight instanceof JSONObject)
		{
//			rightAdded = rightAdded |  mergeJSONObjectsInner((JSONObject)tempLeft, (JSONObject)tempRight, provenanceProperties);
			mergeJSONObjectsInner((JSONObject)tempLeft, (JSONObject)tempRight, provenanceProperties);
			mergedResult = tempLeft;
			rightAdded = false; //We add right, but since its an object, the object will contain prov and we dont need to count it as merged
		}
		else {
			if (tempLeft instanceof String) {
				mergedResult = tempRight.toString();
				if(tempRight instanceof String)
					rightAdded = true;
			}
			else {
				mergedResult = tempLeft.toString();
			}
		}
		return new ObjectRightAdded(mergedResult, rightAdded);
	}
		
	private static class ObjectRightAdded {
		public Object object;
		public boolean rightAdded;
		public ObjectRightAdded(Object object, boolean rightAdded) {
			this.object = object;
			this.rightAdded = rightAdded;
		}
	}
}
