package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SimpleReducer extends Reducer<Text,Text,Text,Text>{

	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		Iterator<Text> iterator = values.iterator();
		while(values.iterator().hasNext())
		{
			context.write(key, iterator.next());
		}
	}
}
