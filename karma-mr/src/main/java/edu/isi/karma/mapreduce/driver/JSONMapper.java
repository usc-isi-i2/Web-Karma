package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.hadoop.io.Text;
import org.json.JSONArray;

import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

public class JSONMapper extends BaseRDFMapper {
	private Text reusableOutputValue = new Text("");
	private Text reusableOutputKey = new Text("");
	@Override
	protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
		PrintWriter pw = new PrintWriter(sw);
		KR2RMLRDFWriter outWriter = new JSONKR2RMLRDFWriter(pw, karma.getBaseURI());
		return outWriter;
	}

	@Override
	protected void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException {
		JSONArray generatedObjects = new JSONArray(results);
		for(int i = 0; i < generatedObjects.length(); i++)
		{
			reusableOutputKey.set(generatedObjects.getJSONObject(i).getString("@id"));
			reusableOutputValue.set(generatedObjects.getJSONObject(i).toString());
			context.write(reusableOutputKey, new Text(reusableOutputValue));
		}
	}
}
