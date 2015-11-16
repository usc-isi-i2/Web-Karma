package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.JSONObject;

import edu.isi.karma.util.JSONLDUtil;

public class ValueOnlyJSONReducer extends Reducer<Text,Text,NullWritable,Text>{

	private Text reusableOutputValue = new Text("");
	@Override
	public void setup(Context context)
	{
	}
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		JSONObject accumulatorObject = JSONLDUtil.mergeJSONObjects(new TextToStringIterator(values.iterator()));
		reusableOutputValue.set(accumulatorObject.toString());
		context.write(NullWritable.get(), reusableOutputValue);
	}
	
}
