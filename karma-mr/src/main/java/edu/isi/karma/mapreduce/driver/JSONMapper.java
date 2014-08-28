package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.hadoop.io.Text;
import org.json.JSONArray;

import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

public class JSONMapper extends BaseRDFMapper {
	@Override
	protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
		PrintWriter pw = new PrintWriter(sw);
		KR2RMLRDFWriter outWriter = new JSONKR2RMLRDFWriter(pw, baseURI);
		return outWriter;
	}

	@Override
	protected void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException {
		JSONArray generatedObjects = new JSONArray(results);
		for(int i = 0; i < generatedObjects.length(); i++)
		{
			context.write(new Text(generatedObjects.getJSONObject(i).getString("@id")), new Text(generatedObjects.getJSONObject(i).toString()));
		}
	}
}
