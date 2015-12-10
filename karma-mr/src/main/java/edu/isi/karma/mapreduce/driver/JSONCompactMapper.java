package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class JSONCompactMapper extends Mapper<Writable, Text, Text, Text> {
	private static Logger LOG = LoggerFactory.getLogger(JSONCompactMapper.class);
	protected Object jsonLdContext;
	protected Text outputText = new Text();
	protected String jsonLdContextFile;
	protected String jsonLdContextURL;
	
	@Override
	public void setup(Context context) throws IOException
	{
		jsonLdContextFile = context.getConfiguration().get("jsonld.context.file");
		jsonLdContextURL = context.getConfiguration().get("jsonld.context.url");
		if(jsonLdContextFile != null)
		{
			InputStream in = new FileInputStream(new File(jsonLdContextFile));
			jsonLdContext = JsonUtils.fromInputStream(in);
			in.close();
		}
		else if(jsonLdContextURL != null)
		{
			InputStream in = new URL(jsonLdContextURL).openStream();
			jsonLdContext = JsonUtils.fromInputStream(in);
			in.close();
		}
		else
		{
			throw new IOException("No context provided");
		}
		
		  
	}
	@Override
	public void map(Writable key, Text value, Context context) throws IOException, InterruptedException {
		try {
			String valueString = value.toString();
			JSONObject obj = new JSONObject(valueString);
			Object outobj = JsonLdProcessor.compact(JsonUtils.fromString(valueString), jsonLdContext, new JsonLdOptions(""));
			if(outobj instanceof Map && jsonLdContextURL != null)
			{
				Map outjsonobj = (Map) outobj;
				outjsonobj.put("@context", jsonLdContextURL);
			}
			outputText.set(JsonUtils.toString(outobj));
			if (obj.has("uri")) {
				context.write(new Text(obj.getString("uri")), outputText);
			}
			else if (obj.has("@id")) {
				context.write(new Text(obj.getString("@id")), outputText);
			}
			else {
				context.write(new Text(obj.toString()), outputText);
			}
		}catch(Exception e) {
			LOG.error("something is wrong", e);
		}
	}
}
