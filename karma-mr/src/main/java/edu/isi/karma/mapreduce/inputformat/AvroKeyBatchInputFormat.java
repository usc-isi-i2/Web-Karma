package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class AvroKeyBatchInputFormat<T> extends FileInputFormat<Text, Text> {

	@Override
	public RecordReader<Text, Text> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		Schema readerSchema = AvroJob.getInputKeySchema(context.getConfiguration());
		return new AvroBatchRecordReader<T>(readerSchema);
	}

}
