package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.JSONArray;
import org.json.JSONObject;

public class N3ToJSONReducer extends Reducer<Text,Text,Text,Text>{
	private Text reusableOutputValue = new Text("");
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		Iterator<Text> iterator = values.iterator();
		Set<String> allTriples = new HashSet<String>();
		while(iterator.hasNext())
		{
			String value = iterator.next().toString();
			String[] triples = value.split("(\r\n|\n)");
			for(String triple : triples)
			{
				allTriples.add(triple);
			}
			
		}
		JSONObject output = new JSONObject();
		String id = key.toString().trim();
		if(id.startsWith("<") && id.endsWith(">"))
		{
			id = id.substring(1, id.length()-1);
		}
		output.accumulate("@id", id);
		
		for(String triple : allTriples)
		{
			int firstSpaceIndex = triple.indexOf(' ');
			int secondSpaceIndex = triple.indexOf(' ', firstSpaceIndex+1);
			String predicate = triple.substring(firstSpaceIndex, secondSpaceIndex).trim();
			if(predicate.equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
			{
				predicate = "@type";
			}
			else if(predicate.startsWith("<") && predicate.endsWith(">"))
			{
				predicate = predicate.substring(1, predicate.length()-1);
			}
			int periodIndex = triple.lastIndexOf(".");
			if(periodIndex < 0)
			{
				periodIndex = triple.length();
			}
			String object = triple.substring(secondSpaceIndex, periodIndex).trim();
			if((object.startsWith("<") && object.endsWith(">")))
			{
				object = object.substring(1, object.length()-1);
			}
			int typeIndex = object.indexOf("^^");
			if(typeIndex > 0)
			{
				object = object.substring(0, typeIndex);
			}
			if(object.startsWith("\"") && object.endsWith("\""))
			{
				object = object.substring(1, object.length()-1);
			}
			if(!output.has(predicate))
			{
				output.put(predicate, object);
			}
			else 
			{	
				Object value = output.get(predicate);
				if(value instanceof String)
				{
					JSONArray newArray = new JSONArray();
					if(((String) value).compareTo(object) <= 0)
					{
						newArray.put(value);
						newArray.put(object);
					}
					else
					{
						newArray.put(object);
						newArray.put(value);
					}
					output.put(predicate, newArray);
				}
				else if(value instanceof JSONArray)
				{
					JSONArray array = (JSONArray)value;
					String swapValue = object;
					for(int i = 0; i < array.length(); i++)
					{
						if(object.compareTo(array.getString(i)) <= 0)
						{
							swapValue = array.getString(i);
							array.put(i, object);
							break;
						}
					}
					array.put(swapValue);
				}
			}
		}
		reusableOutputValue.set(output.toString());
		context.write(new Text(id), reusableOutputValue);
	}

}
