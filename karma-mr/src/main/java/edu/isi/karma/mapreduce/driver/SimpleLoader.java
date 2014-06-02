package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLoader extends Configured implements Tool{

	private static Logger logger = LoggerFactory.getLogger(SimpleLoader.class);
	Properties p;
	 public void configure(Properties p ) throws Exception
	 {
		
		 Configuration conf = getConf();
		 conf.set("fs.default.name", p.getProperty("fs.default.name"));
		 
	 }
	 
	 public int run(String[] args) throws Exception {
        // Configuration processed by ToolRunner
		 Properties p = new Properties();
		 p.load(new FileInputStream(new File(args[0])));
		 
		 configure(p);
		 String inputDirectoryName = p.getProperty("input.directory");
        File f = new File(inputDirectoryName);
        if(!f.exists() || !f.isDirectory()){
        	logger.error("Invalid input directory: " + inputDirectoryName);
        	return -1;
        }
        String outputFileName = p.getProperty("output.file");
        Path outputPath = new Path(outputFileName);
        FileSystem fs = outputPath.getFileSystem(getConf());
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, getConf(),
                outputPath, Text.class, Text.class);
        for(File document : f.listFiles())
        {
        	String contents = FileUtils.readFileToString(document);
        	writer.append(new Text(document.getName()), new Text(contents));
        }
        writer.close();
        return 0;
      }
      
      public static void main(String[] args) throws Exception {
        // Let ToolRunner handle generic command-line options 
        int res = ToolRunner.run(new Configuration(), new SimpleLoader(), args);
        
        System.exit(res);
      }
}
