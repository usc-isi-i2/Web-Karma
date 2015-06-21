package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSequenceFileFromKeyValuePairs  extends Configured implements Tool{

	private static Logger logger = LoggerFactory.getLogger(CreateSequenceFileFromKeyValuePairs.class);

	public void configure(Properties p) throws Exception {

		Configuration conf = getConf();
		conf.setIfUnset("fs.default.name", p.getProperty("fs.default.name"));

	}
 
	 public int run(String[] args) throws Exception {
        // Configuration processed by ToolRunner
		 Properties p = new Properties();
		 p.load(new FileInputStream(new File(args[0])));
		 
		 configure(p);
		 String outputFileName = p.getProperty("output.file");
	        Path outputPath = new Path(outputFileName);
	        SequenceFile.Writer writer = SequenceFile.createWriter(getConf(),Writer.keyClass(Text.class),
	                Writer.valueClass(Text.class), Writer.file(outputPath),Writer.compression(CompressionType.NONE));
	      
	    if(null != p.getProperty("input.directory"))
	    {
			 String inputDirectoryName = p.getProperty("input.directory");
	        File f = new File(inputDirectoryName);
	        if(!f.exists() || !f.isDirectory()){
	        	logger.error("Invalid input directory: " + inputDirectoryName);
	        	return -1;
	        }
	        for(File document : f.listFiles())
	        {
	        	addDocumentToSequenceFile(writer, document);
	        }
	    }
	    if(null != p.getProperty("input.file"))
	    {
	    	String inputFileName = p.getProperty("input.file");
	        File document = new File(inputFileName);
	        if(!document.exists()){
	        	logger.error("Invalid input: " + inputFileName);
	        	return -1;
	        }
	        addDocumentToSequenceFile(writer, document);
	    }
        writer.close();
        return 0;
      }

	private void addDocumentToSequenceFile(SequenceFile.Writer writer,
			File document) throws IOException {
		String contents = FileUtils.readFileToString(document);
		String[] kvPairs = contents.split("\n");
		for(String kvPair : kvPairs){
			int splitLocation = kvPair.indexOf(',');
			
			writer.append(new Text(kvPair.substring(1, splitLocation)), new Text(kvPair.substring(splitLocation+1, kvPair.length()-1)));
		}
	}
      
      public static void main(String[] args) throws Exception {
        // Let ToolRunner handle generic command-line options 
        int res = ToolRunner.run(new Configuration(), new CreateSequenceFileFromKeyValuePairs(), args);
        
        System.exit(res);
      }
}
