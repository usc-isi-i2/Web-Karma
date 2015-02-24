package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONTypeFilterMapper extends Mapper<Writable, Text, Text, Text> {
	private static Logger LOG = LoggerFactory.getLogger(JSONTypeFilterMapper.class);

	@Override
	public void map(Writable key, Text value, Context context) throws IOException, InterruptedException {
		try {
			String typeFilter = context.getConfiguration().get("type.filter");
			JSONObject obj = new JSONObject(value.toString());
			if(obj.has("@type"))
			{
				boolean match = false;
				Object typeObj = obj.get("@type");
				if(typeObj instanceof String)
				{
					match = ((String)typeObj).compareTo(typeFilter) == 0;
				}
				else if(typeObj instanceof JSONArray)
				{
					JSONArray types = (JSONArray) typeObj;
					for(int i = 0; i < types.length(); i++)
					{
						Object typeAti = types.get(i);
						match |= typeAti instanceof String && ((String) typeAti).compareTo(typeFilter) == 0;
						if(match)
							break;
					}
				}
				if(match)
				{
					if (obj.has("uri")) {
						context.write(new Text(obj.getString("uri")), value);
					}
					else if (obj.has("@id")) {
						context.write(new Text(obj.getString("@id")), value);
					}
					else {
						context.write(new Text(obj.toString()), value);
					}
				}
			}
		}catch(Exception e) {
			LOG.error("something is wrong", e);
		}
	}
}
