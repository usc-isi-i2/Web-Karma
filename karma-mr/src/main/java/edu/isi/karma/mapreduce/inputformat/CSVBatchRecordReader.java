package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class CSVBatchRecordReader
extends RecordReader<Writable, Text> {

	private final LineRecordReader 
	recordReader;
	protected String header = null;
	private LongWritable key = null;
	List<String> data = new LinkedList<>();
	private static final int batchSize = 10000;
	public CSVBatchRecordReader()
			throws IOException {
		recordReader =
				new LineRecordReader();
	}

	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		recordReader.initialize(split, context);
	}

	@Override
	public LongWritable getCurrentKey() 
			throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() 
			throws IOException, InterruptedException {
		StringBuilder builder = new StringBuilder();
		
		builder.append(header);
		if(!header.endsWith("\n"))
			builder.append("\n");
		for (String obj : data) {
			builder.append(obj);
			if(!obj.endsWith("\n"))
			builder.append("\n");
		}
		return new Text(builder.toString());
	}

	public synchronized boolean nextKeyValue() 
			throws IOException, InterruptedException {
		int count = 0;
		data.clear();
		while (recordReader.nextKeyValue()) {
			String value = recordReader.getCurrentValue().toString();
			if(header == null)
			{
				header = value;
			}
			else{
				data.add(value);
				count++;
				if (count == batchSize) {
					break;
				}
			}
			key = recordReader.getCurrentKey();
		}
		return (!data.isEmpty());
	}

	public float getProgress() throws IOException,  InterruptedException {
		return recordReader.getProgress();
	}

	public synchronized void close() throws IOException {
		recordReader.close();
	}
}
