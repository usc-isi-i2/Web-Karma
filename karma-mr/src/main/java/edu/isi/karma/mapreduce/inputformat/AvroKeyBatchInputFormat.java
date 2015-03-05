package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import org.apache.avro.Schema;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroKeyBatchInputFormat<T> extends FileInputFormat<Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(AvroKeyBatchInputFormat.class);
	@Override
	public RecordReader<Text, Text> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		Schema readerSchema = AvroJob.getInputKeySchema(context.getConfiguration());
		return new AvroBatchRecordReader<T>(readerSchema);
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
