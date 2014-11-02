package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.hadoop.io.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

public class JSONAvroMapper extends BaseAvroMapper {
	private Text reusableOutputValue = new Text("");
	private Text reusableOutputKey = new Text("");
	private String atId = "@id";
	@Override
	protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
		PrintWriter pw = new PrintWriter(sw);
		KR2RMLRDFWriter outWriter = new JSONKR2RMLRDFWriter(pw, karma.getBaseURI());
		ContextIdentifier contextId = karma.getContextId();
		if (contextId != null) {
			try {
				JSONObject obj = new JSONObject(new JSONTokener(contextId.getLocation().openStream()));
				((JSONKR2RMLRDFWriter)outWriter).setGlobalContext(obj, contextId);
				atId = ((JSONKR2RMLRDFWriter)outWriter).getAtId();
			}
			catch(Exception e)
			{
				
			}
		}
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
}
