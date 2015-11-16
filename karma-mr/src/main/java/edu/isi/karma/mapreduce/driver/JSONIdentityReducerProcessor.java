package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.security.InvalidParameterException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileAsTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.isi.karma.rdf.CommandLineArgumentParser;
import edu.isi.karma.rdf.OfflineRdfGenerator;

public class JSONIdentityReducerProcessor extends Configured implements Tool {

	 public Job configure(Properties p ) throws Exception
	 {
		
		Configuration conf = getConf();
		Job job = Job.getInstance(conf);
		if(p.getProperty("file.type").equalsIgnoreCase("JL"))
		{
			job.setInputFormatClass(TextInputFormat.class);
		}
		else
		{
			job.setInputFormatClass(SequenceFileAsTextInputFormat.class);
		}
        job.setJarByClass(JSONIdentityReducerProcessor.class);
        
        
        job.setMapperClass(IdentityJSONMapper.class);
        job.setCombinerClass(JSONReducer.class);
        if(p.getProperty("file.type").equalsIgnoreCase("JL"))
        {
        	job.setReducerClass(ValueOnlyJSONReducer.class);
        	job.setOutputKeyClass(NullWritable.class);
        	job.setOutputFormatClass(TextOutputFormat.class);
        }
        else
        {
        	job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setReducerClass(JSONReducer.class);
        job.setOutputKeyClass(Text.class);
        }
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        
        job.setOutputValueClass(Text.class);
        String[] paths = p.getProperty("input.directory").split(",");
        Path[] array = new Path[paths.length];
        int i = 0;
        for (String path : paths) {
        	array[i++] = new Path(path);
        }
        FileInputFormat.setInputPaths(job, array);
        FileOutputFormat.setOutputPath(job, new Path(p.getProperty("output.directory")));
        
        job.setNumReduceTasks(1);
        return job;
	 }
	 
	 public int run(String[] args) throws Exception {
		
		 Properties p = new Properties();
		 if(args.length > 1)
		 {
			 Options options = createCommandLineOptions();
				CommandLine cl = CommandLineArgumentParser.parse(args, options, OfflineRdfGenerator.class.getSimpleName());
			if(cl == null)
			{
				return -1;
			}

			try {
				if(cl.getOptionValue("filetype") == null)
				{
					throw new InvalidParameterException("Missing argument --filetype");
				}
				if(cl.getOptionValue("inputdirectory") == null)
				{
					throw new InvalidParameterException("Missing argument --inputdirectory");
				}
				if(cl.getOptionValue("outputdirectory") == null)
				{
					throw new InvalidParameterException("Missing argument --outputdirectory");
				}
				p.setProperty("fs.default.name", cl.getOptionValue("fsdefaultname", "file:///"));
				p.setProperty("mapred.job.tracker", cl.getOptionValue("mapredjobtracker", "local"));
				p.setProperty("input.directory", cl.getOptionValue("inputdirectory"));
				p.setProperty("output.directory", cl.getOptionValue("outputdirectory"));
				p.setProperty("file.type", cl.getOptionValue("filetype", "SEQ"));
			}
			catch(Exception e )
			{
				System.err.println("Invalid arguments: " + e.getMessage());
				return -1;
			}
		 }
		 else
		 {
		 p.load(new FileInputStream(new File(args[0])));
		 }
		 
         
         Job job = configure(p);
         
         if(!job.waitForCompletion(false))
         {
        	 System.err.println("Unable to finish job");
        	 return -1;
         }
        
         return 0;
       }
       
       public static void main(String[] args) throws Exception {
    	   System.exit(ToolRunner.run(new Configuration(), new JSONIdentityReducerProcessor(), args));
       }

       

   	private static Options createCommandLineOptions() {

   		Options options = new Options();
   				
   		options.addOption(new Option("inputdirectory", "inputdirectory", true, "input directory"));
   		options.addOption(new Option("outputdirectory","outputdirectory", true, "output directory"));
   		options.addOption(new Option("filetype","filetype",true, "file type.  one of [SEQ, JL]"));
   		options.addOption(new Option("fsdefaultname","fsdefaultname",true, "file system name for hdfs"));
   		options.addOption(new Option("mapredjobtracker","mapredjobtracker",true, "location of mapreduce job tracker"));
   		options.addOption(new Option("help", "help", false, "print this message"));
   		return options;
   	}
}
