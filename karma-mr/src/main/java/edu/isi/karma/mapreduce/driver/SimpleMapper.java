package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.isi.karma.controller.command.transformation.PythonRepository;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.rdf.GenericRDFGenerator;
import edu.isi.karma.webserver.KarmaException;

public class SimpleMapper extends Mapper<Text, Text, Text, Text>{

	private static Logger LOG = Logger.getLogger(SimpleMapper.class);

	GenericRDFGenerator generator;
	
	@Override
	public void setup(Context context)
	{
		
		try {
			//TODO dynamically discover the archive
			File karmaUserHome = new File("./karma.zip/karma");
			if(!karmaUserHome.exists())
			{
				LOG.info("No Karma user home provided.  Creating default Karma configuration");
			}
			System.setProperty("KARMA_USER_HOME", karmaUserHome.getAbsolutePath());
	        KarmaMetadataManager userMetadataManager;
			userMetadataManager = new KarmaMetadataManager();
			UpdateContainer uc = new UpdateContainer();
	        userMetadataManager.register(new UserPreferencesMetadata(), uc);
	        userMetadataManager.register(new UserConfigMetadata(), uc);
	        userMetadataManager.register(new PythonTransformationMetadata(), uc);
	        PythonRepository.disableReloadingLibrary();
	        String modelUri = context.getConfiguration().get("model.uri");
	        generator = new GenericRDFGenerator();
	        URL modelURL = new URL(modelUri);
	        generator.addModel(new R2RMLMappingIdentifier("model", modelURL));
		} catch (KarmaException | IOException  e) {
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
		URIFormatter uriFormatter = new URIFormatter();
		KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
		try {
			generator.generateRDF("model", filename, contents, null, false, outWriter);
		} catch (JSONException | KarmaException e) {
			LOG.error("Unable to generate RDF: " + e.getMessage());
		}
		String results = sw.toString();
		context.write(new Text(filename), new Text(results));
	}
}
