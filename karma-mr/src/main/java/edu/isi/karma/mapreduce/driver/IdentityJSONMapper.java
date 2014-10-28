package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentityJSONMapper extends Mapper<Writable, Text, Text, Text> {
	private static Logger LOG = LoggerFactory.getLogger(IdentityJSONMapper.class);

	@Override
	public void map(Writable key, Text value, Context context) throws IOException, InterruptedException {
		try {
			JSONObject obj = new JSONObject(value.toString());
			if (obj.has("uri")) {
				context.write(new Text(obj.getString("uri")), value);
			}
			else if (obj.has("@id")) {
				context.write(new Text(obj.getString("@id")), value);
			}
			else {
				context.write(new Text(obj.toString()), value);
			}
		}catch(Exception e) {
			LOG.error("something is wrong", e);
		}
	}
}
