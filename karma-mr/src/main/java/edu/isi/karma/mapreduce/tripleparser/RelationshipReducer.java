package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class RelationshipReducer extends
		Reducer<Text, Text, Text, NullWritable> {

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
			
		context.write(new Text(key), NullWritable.get());
	}


}
