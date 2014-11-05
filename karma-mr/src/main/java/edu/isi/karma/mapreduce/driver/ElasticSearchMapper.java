package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ElasticSearchMapper extends Mapper<Writable,Text,NullWritable, Text>{
	
	private static Logger LOG = LoggerFactory.getLogger(ElasticSearchMapper.class);

	@Override
	public void map(Writable key, Text value, Context context
			) throws IOException, InterruptedException {
		String source = value.toString();
		LOG.info("input:" + source);
		
		Text jsonDoc = new Text(source);
		
		context.write(NullWritable.get(), jsonDoc);
		
	}

	

}
