package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.JSONObject;

import edu.isi.karma.util.JSONLDUtil;

public class JSONReducer extends Reducer<Text,Text,Text,Text>{

	private Text reusableOutputValue = new Text("");
	@Override
	public void setup(Context context)
	{
	}
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		JSONObject accumulatorObject = JSONLDUtil.mergeJSONObjects(new TextToStringIterator(values.iterator()));
		reusableOutputValue.set(accumulatorObject.toString());
		context.write(key, reusableOutputValue);
	}
	
}
