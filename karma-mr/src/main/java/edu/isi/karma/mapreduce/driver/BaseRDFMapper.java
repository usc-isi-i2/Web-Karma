package edu.isi.karma.mapreduce.driver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.BaseKarma;
import edu.isi.karma.rdf.RDFGeneratorRequest;

public abstract class BaseRDFMapper extends Mapper<Writable, Text, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);

	protected BaseKarma karma;
	protected String header = null;
	protected String delimiter = null;
	protected boolean hasHeader = false;
	boolean readKarmaConfig = false;
	JSONArray jKarmaConfig = null;
	@Override
	public void setup(Context context) throws IOException {

		karma = new BaseKarma();
		String inputTypeString = context.getConfiguration().get(
				"karma.input.type");
		String modelUri = context.getConfiguration().get("model.uri");
		String modelFile = context.getConfiguration().get("model.file");
		String baseURI = context.getConfiguration().get("base.uri");
		String contextURI = context.getConfiguration().get("context.uri");
		String rdfGenerationRoot = context.getConfiguration().get("rdf.generation.root");
		String rdfSelection = context.getConfiguration().get("rdf.generation.selection");
		delimiter = context.getConfiguration().get("karma.input.delimiter");
		hasHeader = context.getConfiguration().getBoolean("karma.input.header", false);
		karma.setup("./karma.zip/karma", inputTypeString, modelUri, modelFile, 
				baseURI, contextURI, rdfGenerationRoot, rdfSelection);
		
		
		readKarmaConfig = Boolean.parseBoolean(context.getConfiguration().get("read.karma.config"));
		
		String configFilePath = context.getConfiguration().get("karma.config.file");
		
		FileSystem fs = FileSystem.get(context.getConfiguration());
		
		if(readKarmaConfig)
		{
			if(!fs.exists(new Path(configFilePath))){
				throw new FileNotFoundException("File at :" + configFilePath + " doesn't exist.");
			}
			else{
				StringBuilder sbConfig = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(new Path(configFilePath))));
				String line=null;
				
				while((line = br.readLine()) != null){
					sbConfig.append(line.trim());
				}
				
				jKarmaConfig = (JSONArray) JSONSerializer.toJSON(sbConfig.toString());
			}
		}
	
	}

	@Override
	public void map(Writable key, Text value, Context context) throws IOException,
			InterruptedException {

		String contents = value.toString();
		
		JSONObject jMatchedKarmaConfig = null;
				
		for(int i=0;i<jKarmaConfig.size();i++){
			
			if(key.toString().matches(jKarmaConfig.getJSONObject(i).getString("urls"))){
				jMatchedKarmaConfig = jKarmaConfig.getJSONObject(i);
				break;
			}
			
		}
		
		
		
		if (contents.trim() != ""){
			LOG.debug(key.toString() + " started");
			if(hasHeader && header ==null)
			{
				header=contents;
				LOG.debug("found header: " + header);
				return;
			}
			else if(hasHeader && header != null)
			{
				contents = header+"\n" + contents;
			}
			
			if(readKarmaConfig && jMatchedKarmaConfig != null){
				
				if(jMatchedKarmaConfig instanceof JSONObject){
					
					String modelName=null;
						
					if (jMatchedKarmaConfig.containsKey("model-uri")){
						
						//add a new model with uri as name. This will prevent hitting github million times(literally)
						
						String modelURL = jMatchedKarmaConfig.getString("model-uri");
						
						int index = modelURL.lastIndexOf("/");
						
						modelName = modelURL.substring(index+1);
						
						modelName = modelName.substring(0, modelName.length()-4);
			
						karma.addModel(modelName,null, jMatchedKarmaConfig.getString("model-uri"));
					}
					else {
					}
				
					
					if(jMatchedKarmaConfig.containsKey("roots")){
						
						JSONArray jArrayRoots = jMatchedKarmaConfig.getJSONArray("roots");
						
						for (int i=0;i<jArrayRoots.size();i++){
							
							JSONObject jObjRoots = jArrayRoots.getJSONObject(i);
							
							if(jObjRoots.containsKey("root")){
								
								karma.setRdfGenerationRoot(jObjRoots.getString("root"),modelName);
								String results = generateJSONLD(key, value,modelName);
								if (results != null && !results.equals("[\n\n]\n")) {
									
									writeRDFToContext(context, results);
									
								}
								else
								{
									LOG.info("RDF is empty! ");
								}
							}
						}
					}
					else{
						String results = generateJSONLD(key, value,modelName);
						if (results != null && !results.equals("[\n\n]\n")) {
							writeRDFToContext(context, results);
							
						}
						else
						{
							LOG.info("RDF is empty! ");
						}
					}
				}
			}
			else{
				
				String results = generateJSONLD(key, value,"model");
				if (!results.equals("[\n\n]\n") && results != null) {
					
					writeRDFToContext(context, results);
					
				}
				else
				{
					LOG.info("RDF is empty! ");
				}
			}
		
			LOG.debug(key.toString() + " finished");
		}
		
	}

	
	protected String generateJSONLD(Writable key, Text value, String modelName)
	{
		String filename = key.toString();
		String contents = value.toString();
		StringWriter sw = new StringWriter();
		
		String results = null;
		
		KR2RMLRDFWriter outWriter = configureRDFWriter(sw);
		try {
			RDFGeneratorRequest request = new RDFGeneratorRequest(modelName, filename);
			request.setDataType(karma.getInputType());
			request.setInputData(contents);
			request.setAddProvenance(false);
			request.addWriter(outWriter);
			request.setMaxNumLines(0);
			request.setStrategy(new UserSpecifiedRootStrategy(""));
			if(delimiter != null)
			{
				request.setDelimiter(delimiter);
			}
			if(karma.getContextId() != null)
			{
				request.setContextName(karma.getContextId().getName());
			}
			if(karma.getRdfGenerationRoot() != null)
			{
				request.setStrategy(new UserSpecifiedRootStrategy(karma.getRdfGenerationRoot()));
			}
			if (karma.getContextId() != null) {
				request.setContextName(karma.getContextId().getName());
			}
			
			karma.getGenerator().generateRDF(request);

			results = sw.toString();
			

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Unable to generate RDF: " + e.getMessage());
		}
		
		return results;
	}
	
	protected abstract KR2RMLRDFWriter configureRDFWriter(StringWriter sw);

	protected abstract void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException;

}