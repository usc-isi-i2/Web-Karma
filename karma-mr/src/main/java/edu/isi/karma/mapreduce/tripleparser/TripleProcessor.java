package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleProcessor {

	private final static Logger logger = LoggerFactory.getLogger(TripleProcessor.class);

	private TripleProcessor() {
	}

	public static void parseTriples(Configuration conf, Path input, Path output) throws IOException {

		FileSystem fs = FileSystem.get(conf);

		try {

			Job job = Job.getInstance(conf, "parse triples");

			job.setJarByClass(Neo4jCSVGenerator.class);
			job.setMapperClass(TripleMapper.class);
			job.setReducerClass(TripleReducer.class);
			job.setNumReduceTasks(6);

			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(NullWritable.class);

			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);

			if (fs.exists(output)) {
				fs.delete(output, true);
			}
			job.setInputFormatClass(KeyValueTextInputFormat.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
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
