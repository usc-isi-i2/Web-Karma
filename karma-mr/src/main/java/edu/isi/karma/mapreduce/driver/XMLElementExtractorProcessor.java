package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.isi.karma.mapreduce.inputformat.ZIPInputFormat;

public class XMLElementExtractorProcessor extends Configured implements Tool{
	
	public  Job configure(Properties p ) throws Exception
	{

		Configuration conf = getConf();
		if(p.getProperty("fs.default.name") != null)
		{
			conf.setIfUnset("fs.default.name", p.getProperty("fs.default.name"));
		}
		if(p.getProperty("mapred.job.tracker")!= null)
		{
			conf.setIfUnset("mapred.job.tracker", p.getProperty("mapred.job.tracker"));
		}
		if(p.getProperty("karma.extraction.xml.tag") != null)
		{
			conf.setIfUnset("karma.extraction.xml.tag", p.getProperty("karma.extraction.xml.tag"));
		}
		if(p.getProperty("karma.extraction.xml.prologue") != null)
		{
			conf.setIfUnset("karma.extraction.xml.prologue", p.getProperty("karma.extraction.xml.prologue"));
		}
		if(p.getProperty("karma.extraction.xml.epilogue") != null)
		{
			conf.setIfUnset("karma.extraction.xml.epilogue", p.getProperty("karma.extraction.xml.epilogue"));
		}
		Job job = Job.getInstance(conf);
    job.setInputFormatClass(ZIPInputFormat.class);
    job.setJarByClass(XMLElementExtractorProcessor.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);
    job.setMapperClass(XMLElementExtractorMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    String[] paths = p.getProperty("input.directory").split(",");
    Path[] array = new Path[paths.length];
    int i = 0;
    for (String path : paths) {
    	array[i++] = new Path(path);
    }
    FileInputFormat.setInputPaths(job, array);
    FileOutputFormat.setOutputPath(job, new Path(p.getProperty("output.directory")));
    
    job.setNumReduceTasks(0);
    return job;
 }
 
   public static void main(String[] args) throws Exception {
	   System.exit(ToolRunner.run(new Configuration(), new XMLElementExtractorProcessor(), args));
   }
	
	 public int run(String[] args) throws Exception {
		 Properties p = new Properties();
		 p.load(new FileInputStream(new File(args[0])));
         
         Job job = configure(p);
         
         if(!job.waitForCompletion(true))
         {
        	 System.err.println("Unable to finish job");
        	 return -1;
         }
        
         return 0;
       }

}
