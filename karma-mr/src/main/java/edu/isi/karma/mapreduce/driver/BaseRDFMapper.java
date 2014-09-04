package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.rdf.GenericRDFGenerator;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.webserver.KarmaException;

public abstract class BaseRDFMapper extends Mapper<Text, Text, Text, Text>{

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);

	protected GenericRDFGenerator generator;
	protected String baseURI;
	protected InputType inputType;
	@Override
	public void setup(Context context)
	{
		
		try {
			setupKarmaHome();
	        determineInputType(context);
	        generator = new GenericRDFGenerator(null);
	        addModel(context);
	        baseURI = context.getConfiguration().get("base.uri");
		} catch (KarmaException | IOException  e) {
			LOG.error("Unable to complete Karma set up: " + e.getMessage());
			throw new RuntimeException("Unable to complete Karma set up: " + e .getMessage());
		}
	}

	private void setupKarmaHome() throws KarmaException {
		//TODO dynamically discover the archive
		File karmaUserHome = new File("./karma.zip/karma");
		if(!karmaUserHome.exists())
		{
			LOG.info("No Karma user home provided.  Creating default Karma configuration");
		}
		else
		{
			System.setProperty("KARMA_USER_HOME", karmaUserHome.getAbsolutePath());
		}
		KarmaMetadataManager userMetadataManager;
		userMetadataManager = new KarmaMetadataManager();
		UpdateContainer uc = new UpdateContainer();
		userMetadataManager.register(new UserPreferencesMetadata(), uc);
		userMetadataManager.register(new UserConfigMetadata(), uc);
		userMetadataManager.register(new PythonTransformationMetadata(), uc);
		PythonRepository.disableReloadingLibrary();
	}

	private void addModel(Context context) throws MalformedURLException {
		URL modelURL = null;
		String modelUri = context.getConfiguration().get("model.uri");
		String modelFile = context.getConfiguration().get("model.file");
		if(modelUri != null)
		{
			modelURL = new URL(modelUri);
		}
		else if(modelFile != null)
		{
			modelURL = new File(modelFile).toURI().toURL();
		}
		generator.addModel(new R2RMLMappingIdentifier("model", modelURL));
	}

	private void determineInputType(Context context) {
		String inputTypeString = context.getConfiguration().get("karma.input.type");
		inputType = null;
		if(inputTypeString != null)
		{
			try
			{
				inputType = InputType.valueOf(inputTypeString.toUpperCase());
				LOG.info("Expecting input of type {}.", inputType.toString());
			}
			catch(Exception e)
			{
				LOG.error("Unable to recognize input type {}. Will attempt to automatically detect serialization format.", inputTypeString);
			}
		}
		else
		{
			LOG.info("No input type provided.  Will attempt to automatically detect serialization format.");
		}
	}

	@Override
	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

		String filename = key.toString();
		String contents = value.toString();
		
		StringWriter sw = new StringWriter();
		KR2RMLRDFWriter outWriter = configureRDFWriter(sw);
		try {
			generator.generateRDF("model", filename, contents, inputType, false, outWriter);
		} catch (JSONException | KarmaException e) {
			LOG.error("Unable to generate RDF: " + e.getMessage());
		}
		String results = sw.toString();
		writeRDFToContext(context, results);
	}

	protected abstract KR2RMLRDFWriter configureRDFWriter(StringWriter sw);
	
	protected abstract void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException;
	
}
