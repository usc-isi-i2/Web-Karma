package edu.isi.karma.mapreduce.driver;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileAsTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;


public class JSONProcessor extends KarmaProcessor {

	public Job configure(Properties p ) throws Exception
	{

		Configuration conf = getConf();
		configureCommonSettings(conf, p);
		
		Job job = Job.getInstance(conf);
		job.setInputFormatClass(SequenceFileAsTextInputFormat.class);
		job.setJarByClass(JSONProcessor.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapperClass(JSONMapper.class);
		job.setReducerClass(JSONReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.setInputPaths(job, new Path(p.getProperty("input.directory")));
		FileOutputFormat.setOutputPath(job, new Path(p.getProperty("output.directory")));

		job.setNumReduceTasks(1);
		return job;
	}


	public static void main(String[] args) throws Exception {
		LogManager.getRootLogger();
        Configurator.setRootLevel(Level.ERROR);
		long start = System.currentTimeMillis();
		int status = ToolRunner.run(new Configuration(), new JSONProcessor(), args);
		System.out.println((System.currentTimeMillis() - start) + " msec");
		System.exit(status);
	}

}
