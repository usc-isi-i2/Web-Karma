package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class NodeReducer extends
		Reducer<Text, Text, Text, NullWritable> {

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		
		String label = "Uri";
		for (Text t : values) {
			if (t.toString().equalsIgnoreCase("Class")) {
				label = "Class";
				break;
			} else if (t.toString().equalsIgnoreCase("Literal")) {
				label = "Literal";
				break;
			}
		}
		
		String node = key.toString() + "\t" + label;
		context.write(new Text(node), NullWritable.get());
	}


}
