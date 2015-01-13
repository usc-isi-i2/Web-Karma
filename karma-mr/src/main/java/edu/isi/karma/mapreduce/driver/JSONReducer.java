package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONReducer extends Reducer<Text,Text,Text,Text>{

	private static JSONReducerComparator comparator;
	private Text reusableOutputValue = new Text("");
	@Override
	public void setup(Context context)
	{
		comparator =  new JSONReducerComparator();
	}
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		JSONObject accumulatorObject = mergeJSONObjects(values.iterator());
		reusableOutputValue.set(accumulatorObject.toString());
		context.write(key, reusableOutputValue);
	}
	public static JSONObject mergeJSONObjects(Iterator<Text> iterator) {

		JSONObject accumulatorObject = new JSONObject();
		
		while(iterator.hasNext())
		{

			String value = iterator.next().toString();
			JSONObject object = new JSONObject(value);
			accumulatorObject = mergeJSONObjects(accumulatorObject, object);
		}
		
		return accumulatorObject;
	}

	public static JSONObject mergeJSONObjectsFromStrings(Iterator<String> iterator) {

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
	private class JSONReducerComparator implements Comparator<Object>
	{

		public JSONReducerComparator()
		{

		}
		@Override
		public int compare(Object o1, Object o2) {
			if(o1 instanceof String && o2 instanceof String)
			{
				return ((String)o1).compareToIgnoreCase((String)o2);
			}
			else if(o1 instanceof JSONObject && o2 instanceof String)
			{
				JSONObject t = (JSONObject)o1;
				if (t.has("uri")) {
					return t.getString("uri").compareToIgnoreCase((String)o2);
				}
				else if (t.has("@id")) {
					return t.getString("@id").compareToIgnoreCase((String)o2);
				}

				else {
					return t.toString().compareToIgnoreCase((String)o2);
				}
			}
			else if(o1 instanceof String && o2 instanceof JSONObject)
			{
				JSONObject t2 = (JSONObject)o2;
				if (t2.has("uri")) {
					return (((String)o1).compareToIgnoreCase(t2.getString("uri")));
				}
				else if (t2.has("@id")) {
					return (((String)o1).compareToIgnoreCase(t2.getString("@id")));
				}
				else {
					return o1.toString().compareToIgnoreCase(t2.toString());
				}
			}
			else if(o1 instanceof JSONObject && o2 instanceof JSONObject)
			{
				JSONObject t1 = (JSONObject)o1;
				JSONObject t2 = (JSONObject)o2;
				if (t1.has("uri") && t2.has("uri")) {
					return t1.getString("uri").compareTo(t2.getString("uri"));
				}
				else if (t1.has("@id") && t2.has("@id")) {
					return t1.getString("@id").compareTo(t2.getString("@id"));
				}
				else {
					return t1.toString().compareToIgnoreCase(t2.toString());
				}
			}
			return 0;
		}

	}
}