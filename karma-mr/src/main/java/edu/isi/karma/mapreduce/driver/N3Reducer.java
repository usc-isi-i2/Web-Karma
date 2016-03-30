package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class N3Reducer extends Reducer<Text,Text,Text,Text>{
	private Text reusableOutputValue = new Text("");
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		Iterator<Text> iterator = values.iterator();
		Set<String> allTriples = new HashSet<>();
		while(iterator.hasNext())
		{
			String value = iterator.next().toString();
			String[] triples = value.split("(\r\n|\n)");
			for(String triple : triples)
			{
				allTriples.add(triple);
			}
			
		}
		StringBuilder sb = new StringBuilder();
		for(String triple : allTriples)
		{
				sb.append(triple);
				sb.append("\n");
		}
		reusableOutputValue.set(sb.toString());
		context.write(key, reusableOutputValue);
	}
}
