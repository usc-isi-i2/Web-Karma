package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileAsTextInputFormat;
import org.apache.hadoop.mapred.SequenceFileAsTextRecordReader;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class SequenceFileAsLineInputFormat extends SequenceFileAsTextInputFormat {

  public SequenceFileAsLineInputFormat() {
    super();
  }

  @Override
  public org.apache.hadoop.mapred.RecordReader<Text,Text> getRecordReader(InputSplit split, JobConf job, Reporter reporter) throws IOException {
	  
	  reporter.setStatus(split.toString());

	  return new SequenceRecorderLineReader(job, (FileSplit) split);
	}
  
  	public class SequenceRecorderLineReader extends SequenceFileAsTextRecordReader {


	  	public SequenceRecorderLineReader(Configuration conf, FileSplit split)
	  			throws IOException {
	  		super(conf, split);
	  	}
	
	  		
	  	@Override
	  	/** Read key/value pair in a line. */
	    public synchronized boolean next(Text key, Text value) throws IOException {
	  	  boolean returnVal = super.next(key, value);
	  	  if(returnVal) {
	  		  String valueStr = value.toString().replace("\n", " ").replace("\r", " ");
	  		  value.set(valueStr);
	  	  }
	      
	      return returnVal;
	    }
	  	
	}	
}
