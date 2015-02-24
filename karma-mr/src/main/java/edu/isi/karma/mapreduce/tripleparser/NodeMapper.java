package edu.isi.karma.mapreduce.tripleparser;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class NodeMapper extends Mapper<Text, Text, Text, Text> {

	@Override
	public void map(Text key, Text value, Context context)
			throws IOException, InterruptedException {


		String keyStr = key.toString();
		String[] parts = keyStr.split("\\t");
		
		if (parts == null || parts.length != 3) return;
		
		String subject = parts[0];
		String object = parts[2];
		String predicate = parts[1];
				
		String node1, node2, label;
		
		label = "Uri";
		node1 = subject + "\t" + subject; 
		context.write(new Text(node1), new Text(label));

		if (value.toString().length() == 0) {
			if (predicate.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				node2 = object + "\t" + object;
				label = "Class";
			} else {
				node2 = object + "\t" + object;
				label = "Uri";
			}
		} else {
			node2 = object + "\t" + value; 
			label = "Literal";
		}
		context.write(new Text(node2), new Text(label));
	}

}
