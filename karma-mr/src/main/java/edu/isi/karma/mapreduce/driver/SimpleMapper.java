package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.rdf.ContentDetectingRDFGenerator;
import edu.isi.karma.webserver.KarmaException;

public class SimpleMapper extends Mapper<Text, Text, Text, Text>{

	private static Logger LOG = Logger.getLogger(SimpleMapper.class);
	@Override
	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

		String filename = key.toString();
		String contents = value.toString();
		String modelUri = context.getConfiguration().get("model.uri");
		Path p = context.getWorkingDirectory();
		System.out.println(p.toString());
		
		ContentDetectingRDFGenerator generator = new ContentDetectingRDFGenerator();
		generator.addModel(new R2RMLMappingIdentifier(filename, new URL(modelUri)));
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			generator.generateRDF(filename, filename, contents, false, pw);
		} catch (JSONException | KarmaException e) {
			LOG.error("Unable to generate RDF: " + e.getMessage());
		}
		String results = sw.toString();
		context.write(new Text(filename), new Text(results));
	}
}
