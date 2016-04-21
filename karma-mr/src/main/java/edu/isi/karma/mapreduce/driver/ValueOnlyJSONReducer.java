package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import edu.isi.karma.util.JSONLDUtilSimple;

public class ValueOnlyJSONReducer extends Reducer<Text,Text,NullWritable,Text>{

	private Text reusableOutputValue = new Text("");
	@Override
	public void setup(Context context)
	{
	}
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		JSONObject accumulatorObject = null;
		try {
			accumulatorObject = JSONLDUtilSimple.mergeJSONObjects(new TextToStringIterator(values.iterator()));
		} catch (ParseException e) {
			throw new IOException(e);
		}
		reusableOutputValue.set(accumulatorObject.toString());
		context.write(NullWritable.get(), reusableOutputValue);
	}
	
}
