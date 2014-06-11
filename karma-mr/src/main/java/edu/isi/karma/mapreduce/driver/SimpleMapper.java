package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.rdf.ContentDetectingRDFGenerator;
import edu.isi.karma.webserver.KarmaException;

public class SimpleMapper extends Mapper<Text, Text, Text, Text>{

	private static Logger LOG = Logger.getLogger(SimpleMapper.class);

	ContentDetectingRDFGenerator generator;
	
	@Override
	public void setup(Context context)
	{
		
		try {
			Configuration conf = context.getConfiguration();
			String karmaUserHome = conf.get("KARMA_USER_HOME") != null? conf.get("KARMA_USER_HOME"): context.getWorkingDirectory().toString() + java.io.File.separator + "karma";
			System.setProperty("KARMA_USER_HOME", karmaUserHome);
			
	        KarmaMetadataManager userMetadataManager;
			userMetadataManager = new KarmaMetadataManager();
			UpdateContainer uc = new UpdateContainer();
	        userMetadataManager.register(new UserPreferencesMetadata(), uc);
	        userMetadataManager.register(new UserConfigMetadata(), uc);
	        userMetadataManager.register(new PythonTransformationMetadata(), uc);
	        
	        String modelUri = context.getConfiguration().get("model.uri");
	        generator = new ContentDetectingRDFGenerator();
	        URL modelURL = new URL(modelUri);
	        generator.addModel(new R2RMLMappingIdentifier("model", modelURL));
		} catch (KarmaException | IOException e) {
			LOG.error("Unable to complete Karma set up: " + e.getMessage());
			throw new RuntimeException("Unable to complete Karma set up: " + e .getMessage());
		}
	}

	@Override
	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

		String filename = key.toString();
		String contents = value.toString();
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			generator.generateRDF("model", filename, contents, false, pw);
		} catch (JSONException | KarmaException e) {
			LOG.error("Unable to generate RDF: " + e.getMessage());
		}
		String results = sw.toString();
		context.write(new Text(filename), new Text(results));
	}
}
