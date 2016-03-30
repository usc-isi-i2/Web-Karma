package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class N3ToJSONReducer extends Reducer<Text,Text,Text,Text>{
	
	private static Logger LOG = LoggerFactory.getLogger(JSONCompactMapper.class);
	private Text reusableOutputKey = new Text("");
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
		StringBuilder triplesDocument = new StringBuilder();
		for(String triple : allTriples)
		{
			triplesDocument.append(triple);
			triplesDocument.append("\n");
		}
		
		try {
			Object output = JsonLdProcessor.fromRDF(triplesDocument.toString());
			JsonLdOptions jlo = new JsonLdOptions();
			String result = JsonUtils.toString(JsonLdProcessor.compact(output, null, jlo));
			reusableOutputValue.set(result);
			String id = key.toString().trim();
			if(id.startsWith("<") && id.endsWith(">"))
			{
				id = id.substring(1, id.length()-1);
			}
			reusableOutputKey.set(id);
			context.write(reusableOutputKey, reusableOutputValue);
		} catch (JsonLdError e) {
			LOG.error("Unable to convert " + key.toString() + " to JSON-LD", e);
		}
		
	}

}
