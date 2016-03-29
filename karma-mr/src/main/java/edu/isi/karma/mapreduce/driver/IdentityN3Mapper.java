package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentityN3Mapper extends Mapper<Writable, Text, Text, Text> {
	private static Logger LOG = LoggerFactory.getLogger(IdentityN3Mapper.class);

	protected Text outputKeyText = new Text();
	@Override
	public void map(Writable key, Text value, Context context) throws IOException, InterruptedException {
		try {
			String valueString = value.toString();
			String subject = valueString.substring(0, valueString.indexOf(' '));
			outputKeyText.set(subject);
			context.write(outputKeyText, value);
		}catch(Exception e) {
			LOG.error("something is wrong", e);
		}
	}
}
