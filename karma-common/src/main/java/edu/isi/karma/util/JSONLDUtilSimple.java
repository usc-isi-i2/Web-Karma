package edu.isi.karma.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.collections.IteratorUtils;
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
	
	public static JSONObject mergeJSONObjects(String left, String right) throws ParseException
	{

		JSONParser parser = new JSONParser();
		return mergeJSONObjects((JSONObject)parser.parse(left), (JSONObject)parser.parse(right));
	}

	public static JSONObject mergeJSONObjects(Iterator<String> iterator) throws ParseException {

		JSONParser parser = new JSONParser(); 
		JSONObject accumulatorObject = new JSONObject();
		
		while(iterator.hasNext())
		{

			String value = iterator.next();
			JSONObject object = (JSONObject)parser.parse(value);
			accumulatorObject = mergeJSONObjects(accumulatorObject, object);
		}
		
		return accumulatorObject;
	}

	public static JSONObject mergeJSONObjects(JSONObject left, JSONObject right)
	{
		Collection names = right.keySet();
		for(Object rawName : names)
		{
			String name = (String) rawName;
			if(!left.containsKey(name))
			{
				left.put(name, right.get(name));
			}
			else
			{
				Object leftObject = left.get(name);
				Object rightObject = right.get(name);
				if(leftObject instanceof JSONArray)
				{
					if(rightObject instanceof JSONArray)
					{
						mergeArrays(left, name, (JSONArray) leftObject, (JSONArray) rightObject);
					}
					else
					{
						JSONArray newRightArray = new JSONArray();
						newRightArray.add(rightObject);
						mergeArrays(left, name, (JSONArray) leftObject, newRightArray);
					}

				}
				else
				{
					if(rightObject instanceof JSONArray)
					{
						JSONArray newLeftArray = new JSONArray();
						newLeftArray.add(leftObject);
						mergeArrays(left, name, newLeftArray, (JSONArray)rightObject);
					}
					else
					{
						JSONArray newLeftArray = new JSONArray();
						JSONArray newRightArray = new JSONArray();
						newLeftArray.add(leftObject);
						newRightArray.add(rightObject);
						mergeArrays(left, name, newLeftArray, newRightArray);
					}
				}
			}
		}
		return left;
	}
	protected static void mergeArrays(JSONObject left, String name,
			JSONArray leftArray, JSONArray rightArray) {
		LinkedList<Object> newArrayBuilder = new LinkedList<Object>();
		int leftIndex = 0;
		int rightIndex = 0;
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
				Object mergedResult = mergeStringsAndJSONObjects(
						tempLeft, tempRight);
				newArrayBuilder.add(mergedResult);
			}
			else
			{
				newArrayBuilder.add(rightArray.get(rightIndex++));
			}
		}
		while(leftIndex < leftArray.size())
		{
			newArrayBuilder.add(leftArray.get(leftIndex++));
		}
		while(rightIndex < rightArray.size())
		{
			newArrayBuilder.add(rightArray.get(rightIndex++));
		}
		if(newArrayBuilder.size() > 1)
		{
			JSONArray newArray = new JSONArray();
			newArray.addAll(newArrayBuilder);
			left.put(name, newArray);
		}
		else if(newArrayBuilder.size() == 1)
		{
			left.put(name, newArrayBuilder.get(0));
		}
	}
	private static Object mergeStringsAndJSONObjects(Object tempLeft,
			Object tempRight) {
		Object mergedResult = null;
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
		}
		else if(tempLeft instanceof JSONObject && tempRight instanceof JSONObject)
		{
			mergedResult = mergeJSONObjects((JSONObject)tempLeft, (JSONObject)tempRight);
		}
		else {
			if (tempLeft instanceof String) {
				mergedResult = tempRight.toString();
			}
			else {
				mergedResult = tempLeft.toString();
			}
		}
		return mergedResult;
	}
}
