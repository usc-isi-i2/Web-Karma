package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

public class JSONMapper extends BaseRDFMapper {
	private Text reusableOutputValue = new Text("");
	private Text reusableOutputKey = new Text("");
	private String atId = "@id";
	@Override
	protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
		PrintWriter pw = new PrintWriter(sw);
		KR2RMLRDFWriter outWriter = new JSONKR2RMLRDFWriter(pw, karma.getBaseURI());
		ContextIdentifier contextId = karma.getContextId();
		try {
			atId = getAtId(karma.getGenerator().loadContext(contextId).getJSONObject(("@context")));
		} catch(Exception e)
		{}
		return outWriter;
	}

	@Override
	protected void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException {
		JSONArray generatedObjects = new JSONArray(results);
		for(int i = 0; i < generatedObjects.length(); i++)
		{
			if (generatedObjects.getJSONObject(i).has(atId)) {
				reusableOutputKey.set(generatedObjects.getJSONObject(i).getString(atId));
			}
			else {
				reusableOutputKey.set(generatedObjects.getJSONObject(i).toString());
			}
			reusableOutputValue.set(generatedObjects.getJSONObject(i).toString());
			context.write(reusableOutputKey, new Text(reusableOutputValue));
		}
	}

	private String getAtId(JSONObject c) {
		@SuppressWarnings("rawtypes")
		Iterator itr = c.keys();
		while (itr.hasNext()) {
			String key = itr.next().toString();
			try {
				if (c.get(key).toString().equals("@id")) {
					return key;
				}
			}
			catch(Exception e) 
			{

			}
		}
		return atId;
	}
}
