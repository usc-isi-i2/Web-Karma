package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.isi.karma.util.RandomGUID;

public class TripleReducer extends
		Reducer<Text, NullWritable, Text, Text> {

	@Override
	public void reduce(Text key, Iterable<NullWritable> values, Context context)
			throws IOException, InterruptedException {
			
		
		String triple = key.toString();
		if (triple.length() == 0) return;
		
		String[] parts = triple.split("\\|\\|\\|");
		if (parts == null || parts.length != 3) return;
		
		String relationship;
		
		String source = parts[0]; //subject
		String predicate = parts[1]; //predicate
		String target;
		
		String object = parts[2]; 
		String literalValue;
		
		String prefix = "Literal:";
		if (object.startsWith(prefix)) { // object is literal
			target = new RandomGUID().toString();
			literalValue = object.substring(prefix.length());
		} else {
			target = object;
			literalValue = "";
		}
		
		relationship = source + "\t" + predicate + "\t" + target;

		context.write(new Text(relationship), new Text(literalValue));
	}


}
