package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

public class IdentityJSONMapper extends Mapper<BytesWritable, Text, Text, Text> {
	@Override
	public void map(BytesWritable key, Text value, Context context) throws IOException, InterruptedException {
		JSONObject obj = new JSONObject(value.toString());
		context.write(new Text(obj.getString("@id")), value);
	}
}
