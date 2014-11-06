package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class AvroKeyBatchInputFormat<T> extends FileInputFormat<Iterable<AvroKey<T>>, NullWritable> {

	@Override
	public RecordReader<Iterable<AvroKey<T>>, NullWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		Schema readerSchema = AvroJob.getInputKeySchema(context.getConfiguration());
		return new AvroBatchRecordReader<T>(readerSchema);
	}

}
