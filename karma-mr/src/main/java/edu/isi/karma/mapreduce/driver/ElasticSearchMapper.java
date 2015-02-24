package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

public class ElasticSearchMapper extends
		Mapper<Writable, Text, NullWritable, Text> {

	@Override
	public void map(Writable key, Text value, Context context)
			throws IOException, InterruptedException {

		Text jsonDoc = new Text(value.toString());

		context.write(NullWritable.get(), jsonDoc);

	}

}
