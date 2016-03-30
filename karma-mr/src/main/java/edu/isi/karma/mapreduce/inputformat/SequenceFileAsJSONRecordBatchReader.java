package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
import org.json.JSONObject;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class SequenceFileAsJSONRecordBatchReader
extends RecordReader<Text, Text> {

	private final SequenceFileRecordReader<WritableComparable<?>, Writable>
	sequenceFileRecordReader;

	List<JSONObject> data = new LinkedList<>();
	private static final int batchSize = 10000;
	public SequenceFileAsJSONRecordBatchReader()
			throws IOException {
		sequenceFileRecordReader =
				new SequenceFileRecordReader<>();
	}

	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		sequenceFileRecordReader.initialize(split, context);
	}

	@Override
	public Text getCurrentKey() 
			throws IOException, InterruptedException {
		return new Text("json");
	}

	@Override
	public Text getCurrentValue() 
			throws IOException, InterruptedException {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		boolean isFirst = true;
		for (JSONObject obj : data) {
			if (isFirst) {
				builder.append(obj.toString());
				isFirst = false;
			}
			else {
				builder.append(",");
				builder.append(obj.toString());
			}
		}
		builder.append("]");
		return new Text(builder.toString());
	}

	public synchronized boolean nextKeyValue() 
			throws IOException, InterruptedException {
		int count = 0;
		data.clear();
		while (sequenceFileRecordReader.nextKeyValue()) {
			JSONObject obj = new JSONObject(sequenceFileRecordReader.getCurrentValue().toString());
			data.add(obj);
			count++;
			if (count == batchSize) {
				break;
			}
		}
		return (!data.isEmpty());
	}

	public float getProgress() throws IOException,  InterruptedException {
		return sequenceFileRecordReader.getProgress();
	}

	public synchronized void close() throws IOException {
		sequenceFileRecordReader.close();
	}
}
