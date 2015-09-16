package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputFileDirectoryLoader extends Configured implements Tool{

	private static Logger logger = LoggerFactory.getLogger(InputFileDirectoryLoader.class);

	public void configure(Properties p) throws Exception {

		Configuration conf = getConf();
		conf.setIfUnset("fs.default.name", p.getProperty("fs.default.name"));

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
        SequenceFile.Writer writer = SequenceFile.createWriter(getConf(),Writer.keyClass(Text.class),
                Writer.valueClass(Text.class), Writer.file(outputPath));
        for(File document : f.listFiles())
        {
        	if(document.isFile())
        	{
        		String contents = FileUtils.readFileToString(document);
        		writer.append(new Text(document.getName()), new Text(contents));
        	}
        }
        writer.close();
        return 0;
      }
      
      public static void main(String[] args) throws Exception {
        // Let ToolRunner handle generic command-line options 
        int res = ToolRunner.run(new Configuration(), new InputFileDirectoryLoader(), args);
        
        System.exit(res);
      }
}
