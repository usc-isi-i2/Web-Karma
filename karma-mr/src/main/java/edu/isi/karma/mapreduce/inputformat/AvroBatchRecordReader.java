package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.mapreduce.AvroRecordReaderBase;
import org.apache.hadoop.io.Text;
import org.json.JSONObject;

public class AvroBatchRecordReader<T> extends AvroRecordReaderBase<Text, Text, T>{

	private static final int batchSize = 10000;
	protected AvroBatchRecordReader(Schema readerSchema) {
		super(readerSchema);
	}
	List<JSONObject> data = new LinkedList<>();

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return new Text("json");
	}

	@Override
	public Text getCurrentValue() throws IOException,
	InterruptedException {
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

	@Override
	public synchronized boolean nextKeyValue() throws IOException, InterruptedException {
		data.clear();
		int i = 0;
		while (super.nextKeyValue()) { 
			T tmp = getCurrentRecord();
			data.add(new JSONObject(tmp.toString()));
			i++;
			if (i == batchSize) {
				break;
			}
		}
		return (!data.isEmpty());
	}

}
