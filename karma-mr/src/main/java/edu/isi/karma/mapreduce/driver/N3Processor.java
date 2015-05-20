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

public class N3Processor extends KarmaProcessor {

	 public Job configure(Properties p ) throws Exception
	 {
		
		Configuration conf = getConf();

		this.configureCommonSettings(conf, p);
		Job job = Job.getInstance(conf);
        job.setInputFormatClass(SequenceFileAsTextInputFormat.class);
        job.setJarByClass(N3Processor.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setMapperClass(N3Mapper.class);
        job.setReducerClass(N3Reducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.setInputPaths(job, new Path(p.getProperty("input.directory")));
        FileOutputFormat.setOutputPath(job, new Path(p.getProperty("output.directory")));
        
        job.setNumReduceTasks(0);
        return job;
	 }
	 
       public static void main(String[] args) throws Exception {
    	   System.exit(ToolRunner.run(new Configuration(), new N3Processor(), args));
       }

}
