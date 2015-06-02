package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;

public abstract class KarmaProcessor extends Configured implements Tool{

	public abstract Job configure(Properties p ) throws Exception;
	public void configureCommonSettings(Configuration conf, Properties p)
	{

		if(p.getProperty("fs.default.name") != null)
		{
			conf.setIfUnset("fs.default.name", p.getProperty("fs.default.name"));
		}
		if(p.getProperty("mapred.job.tracker")!= null)
		{
			conf.setIfUnset("mapred.job.tracker", p.getProperty("mapred.job.tracker"));
		}
		if(p.getProperty("model.uri") != null)
		{
			conf.setIfUnset("model.uri", p.getProperty("model.uri"));
		}
		if(p.getProperty("model.file") != null)
		{
			conf.setIfUnset("model.file", p.getProperty("model.file"));
		}
		if(p.getProperty("karma.input.type") != null)
		{
			conf.setIfUnset("karma.input.type", p.getProperty("karma.input.type"));
		}
		if(p.getProperty("context.uri") != null)
		{
			conf.setIfUnset("context.uri", p.getProperty("context.uri"));
		}
		if(p.getProperty("rdf.generation.root") != null)
		{
			conf.setIfUnset("rdf.generation.root", p.getProperty("rdf.generation.root"));
		}
		if(p.getProperty("karma.input.header") != null)
		{
			conf.setIfUnset("karma.input.header", p.getProperty("karma.input.header"));
		}
		if(p.getProperty("karma.input.delimiter")!= null)
		{
			conf.setIfUnset("karma.input.delimiter", p.getProperty("karma.input.delimiter"));
		}
		if(p.getProperty("rdf.generation.selection") != null)
		{
			conf.setIfUnset("rdf.generation.selection", p.getProperty("rdf.generation.selection"));
		}
		if(p.getProperty("base.uri") != null)
		{
			conf.setIfUnset("base.uri", p.getProperty("base.uri"));
		}
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
