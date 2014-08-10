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
	
	@Override
	public void setup(Context context)
	{
		comparator =  new JSONReducerComparator();
	}
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		Iterator<Text> iterator = values.iterator();
		JSONObject accumulatorObject = new JSONObject();
		while(iterator.hasNext())
		{
			String value = iterator.next().toString();
			JSONObject object = new JSONObject(value);
			object = mergeJSONObjects(accumulatorObject, object);			
		}
		
		context.write(key, new Text(accumulatorObject.toString()));
	}
	
	private static JSONObject mergeJSONObjects(JSONObject left, JSONObject right)
	{
		String[] names = JSONObject.getNames(right);
		for(String name : names)
		{
			if(!left.has(name))
			{
				left.accumulate(name, right.get(name));
			}
			else
			{
				Object leftObject = left.get(name);
				Object rightObject = right.get(name);
				if(leftObject instanceof String)
				{
					if(rightObject instanceof String)
					{
						if(!((String)leftObject).equalsIgnoreCase((String)rightObject))
						{
							left.accumulate(name, rightObject);
						}
						
					}
					else if (rightObject instanceof JSONObject){
						if(!((String)leftObject).equalsIgnoreCase(((JSONObject)rightObject).getString("@id")))
						{
							left.accumulate(name, rightObject);
						}
						else
						{
							left.put(name, rightObject);
						}
					}
					else if(rightObject instanceof JSONArray)
					{
						JSONArray rightObjectArray = (JSONArray)rightObject;
						JSONArray newArray = new JSONArray();
						newArray.put(leftObject);
						for(int i = 0; i < rightObjectArray.length(); i++)
						{
							newArray.put(rightObjectArray.get(i));
						}
						left.put(name, newArray);
					}
					
				}
				else if (leftObject instanceof JSONObject)
				{
					if(rightObject instanceof String)
					{
						if(!((String)leftObject).equalsIgnoreCase((String)rightObject))
						{
							left.accumulate(name, rightObject);
						}
						
					}
					else if (rightObject instanceof JSONObject){
						if(!((String)leftObject).equalsIgnoreCase(((JSONObject)rightObject).getString("@id")))
						{
							left.accumulate(name, rightObject);
						}
						else
						{
							left.put(name, mergeJSONObjects((JSONObject) leftObject, (JSONObject) rightObject));
						}
					}
					else if(rightObject instanceof JSONArray)
					{
						JSONArray rightObjectArray = (JSONArray)rightObject;
						JSONArray newArray = new JSONArray();
						newArray.put(leftObject);
						for(int i = 0; i < rightObjectArray.length(); i++)
						{
							newArray.put(rightObjectArray.get(i));
						}
						left.put(name, newArray);
					}
				}
				else if(leftObject instanceof JSONArray)
				{
					JSONArray leftObjectArray = (JSONArray) leftObject;
					boolean found = false;
					if(rightObject instanceof String)
					{
						String rightObjectString = (String)rightObject;
						for(int i = 0; i < leftObjectArray.length(); i++)
						{
							found |= 0 == comparator.compare(leftObjectArray.get(i), rightObjectString);
							if(found)
								break;
						}
						if(!found)
							leftObjectArray.put(rightObject);
					}
					else if(rightObject instanceof JSONObject)
					{
						JSONObject rightObjectJSONobject = (JSONObject) rightObject;
						for(int i = 0; i < leftObjectArray.length(); i++)
						{
							found |= 0 == comparator.compare(leftObjectArray.get(i), rightObjectJSONobject);
							if(found)
							{
								if(leftObjectArray.get(i) instanceof JSONObject)
								{
									leftObjectArray.put(i, mergeJSONObjects(leftObjectArray.getJSONObject(i), rightObjectJSONobject));
								}
								break;
							}
						}
						if(!found)
							leftObjectArray.put(rightObject);
					}
					else if(rightObject instanceof JSONArray)
					{
						JSONArray newArray = new JSONArray();
						int leftIndex = 0;
						int rightIndex = 0;
						JSONArray leftArray = (JSONArray) leftObject;
						JSONArray rightArray = (JSONArray) rightObject;
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
					}
					
				}
			}
		}
		return left;
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
				return ((JSONObject)o1).getString("@id").compareToIgnoreCase((String)o2);
			}
			else if(o1 instanceof String && o2 instanceof JSONObject)
			{
				return (((String)o1).compareToIgnoreCase(((JSONObject)o2).getString("@id")));
			}
			else if(o1 instanceof JSONObject && o2 instanceof JSONObject)
			{
				return ((JSONObject)o1).getString("@id").compareTo(((JSONObject)o2).getString("@id"));
			}
			return 0;
		}
		
	}
}