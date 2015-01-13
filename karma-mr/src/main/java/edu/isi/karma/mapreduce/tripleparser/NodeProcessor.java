package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeProcessor implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(NodeProcessor.class);
	Configuration conf;
	Path input; 
	Path output;
	
	public NodeProcessor(Configuration conf, Path input, Path output) {
		this.conf = conf;
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void run() {

		FileSystem fs;
		try {
			fs = FileSystem.get(conf);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
			return;
		}

		try {

			Job job = Job.getInstance(conf, "export triples to Neo4j nodes CSV file");

			job.setJarByClass(Neo4jCSVGenerator.class);
			job.setMapperClass(NodeMapper.class);
			job.setReducerClass(NodeReducer.class);
			job.setNumReduceTasks(1);

			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(Text.class);

			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(NullWritable.class);

			if (fs.exists(output)) {
				fs.delete(output, true);
			}
			job.setInputFormatClass(SequenceFileInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			FileInputFormat.addInputPath(job, input);
			FileOutputFormat.setOutputPath(job, output);
			job.waitForCompletion(true);
		}
		catch (IOException e) {
			logger.error("i/o exception loading data sources", e);
		}
		catch (InterruptedException e) {
			logger.debug("Hadoop job interrupted", e);
		}
		catch (ClassNotFoundException e) {
			logger.debug("Cannot find mapper/reducer class", e);
		}
		
	}

}
