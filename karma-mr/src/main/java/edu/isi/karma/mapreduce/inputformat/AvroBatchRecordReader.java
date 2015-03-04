package edu.isi.karma.mapreduce.inputformat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.mapreduce.AvroRecordReaderBase;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroBatchRecordReader<T> extends AvroRecordReaderBase<Text, Text, T>{

	private static final int batchSize = 10000;
	private static Logger LOG = LoggerFactory.getLogger(AvroBatchRecordReader.class);
	protected AvroBatchRecordReader(Schema readerSchema) {
		super(readerSchema);
	}
	List<JSONObject> data = new LinkedList<JSONObject>();
	
	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
		      throws IOException, InterruptedException {
		try {
			Field defaultCharsetField = Charset.class.getDeclaredField("defaultCharset");
			defaultCharsetField.setAccessible(true);
			defaultCharsetField.set(null, Charset.forName("UTF-8"));
		} catch (Exception e) {
			LOG.error("something wrong", e);
		} 
		super.initialize(inputSplit, context);
	}

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
		return (data.size() != 0);
	}

}
