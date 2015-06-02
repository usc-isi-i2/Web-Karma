package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVBatchTextInputFormat extends FileInputFormat<Writable, Text> {

	private static Logger LOG = LoggerFactory.getLogger(CSVBatchTextInputFormat.class);
	@Override
	public RecordReader<Writable, Text> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		RecordReader<Writable, Text> recordReader = 
		 new CSVBatchRecordReader();
		recordReader.initialize(split, context);
		return recordReader;
	}
	
	@Override
	protected boolean isSplitable(JobContext context,
            Path filename)
            {
		return false;
            }
	static {
		try {
			Field defaultCharsetField = Charset.class.getDeclaredField("defaultCharset");
			defaultCharsetField.setAccessible(true);
			defaultCharsetField.set(null, Charset.forName("UTF-8"));
		} catch (Exception e) {
			LOG.error("something wrong", e);
		} 
	}

}
