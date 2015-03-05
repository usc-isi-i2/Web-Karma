package edu.isi.karma.util;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONLDUtil {
	private static JSONLDReducerComparator comparator;
	
	static {
		comparator =  new JSONLDReducerComparator();
	}
	
	public static JSONObject mergeJSONObjects(Iterator<String> iterator) {

		JSONObject accumulatorObject = new JSONObject();
		
		while(iterator.hasNext())
		{

			String value = iterator.next().toString();
			JSONObject object = new JSONObject(value);
			accumulatorObject = mergeJSONObjects(accumulatorObject, object);
		}
		
		return accumulatorObject;
	}

	public static JSONObject mergeJSONObjects(JSONObject left, JSONObject right)
	{
		String[] names = JSONObject.getNames(right);
		for(String name : names)
		{
			if(!left.has(name))
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
						newRightArray.put(rightObject);
						mergeArrays(left, name, (JSONArray) leftObject, newRightArray);
					}

				}
				else
				{
					if(rightObject instanceof JSONArray)
					{
						JSONArray newLeftArray = new JSONArray();
						newLeftArray.put(leftObject);
						mergeArrays(left, name, newLeftArray, (JSONArray)rightObject);
					}
					else
					{
						JSONArray newLeftArray = new JSONArray();
						JSONArray newRightArray = new JSONArray();
						newLeftArray.put(leftObject);
						newRightArray.put(rightObject);
						mergeArrays(left, name, newLeftArray, newRightArray);
					}
				}
			}
		}
		return left;
	}
	protected static void mergeArrays(JSONObject left, String name,
			JSONArray leftArray, JSONArray rightArray) {
		JSONArray newArray = new JSONArray();
		int leftIndex = 0;
		int rightIndex = 0;
		while(leftIndex < leftArray.length() && rightIndex < rightArray.length() )
		{
			int result = comparator.compare(leftArray.get(leftIndex),rightArray.get(rightIndex));
			if(result < 0)
			{
				newArray.put(leftArray.get(leftIndex++));
			}
			else if (result == 0)
			{
				Object tempLeft = leftArray.get(leftIndex++);
				Object tempRight = rightArray.get(rightIndex++);
				Object mergedResult = mergeStringsAndJSONObjects(
						tempLeft, tempRight);
				newArray.put(mergedResult);
			}
			else
			{
				newArray.put(rightArray.get(rightIndex++));
			}
		}
		while(leftIndex < leftArray.length())
		{
			newArray.put(leftArray.get(leftIndex++));
		}
		while(rightIndex < rightArray.length())
		{
			newArray.put(rightArray.get(rightIndex++));
		}
		if(newArray.length() > 1)
		{
			left.put(name, newArray);
		}
		else if(newArray.length() == 1)
		{
			left.put(name, newArray.get(0));
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
