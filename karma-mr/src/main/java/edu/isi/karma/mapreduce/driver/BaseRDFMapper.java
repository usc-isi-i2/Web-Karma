package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.StringWriter;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.BaseKarma;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.rdf.RDFGeneratorRequest;

public abstract class BaseRDFMapper extends Mapper<Writable, Text, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);

	protected BaseKarma karma;
	protected String header = null;
	protected String delimiter = null;
	protected boolean hasHeader = false;
	boolean isModelInSource = false;
	boolean isRootInSource = false;
	@Override
	public void setup(Context context) {

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
		
		
		isModelInSource = Boolean.parseBoolean(context.getConfiguration().get("is.model.in.json"));
		isRootInSource = Boolean.parseBoolean(context.getConfiguration().get("is.root.in.json"));
	
	}

	@Override
	public void map(Writable key, Text value, Context context) throws IOException,
			InterruptedException {

		String contents = value.toString();
		
		//TODO key should be url, match it with regex here instead of doing it in landmark-extraction
		
		//String modelName="model";
		
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
		
		if(karma.getInputType() != null && karma.getInputType().equals(InputType.JSON)){
			
			JSON json = JSONSerializer.toJSON(contents);
			
			if(json instanceof JSONObject){
				
				JSONObject jObj = (JSONObject) json;

				String modelName=null;
					
				if (jObj.containsKey("model_uri")){
					
					//add a new model with uri as name. This will prevent hitting github million times(literally)
					
					String modelURL = jObj.getString("model_uri");
					
					int index = modelURL.lastIndexOf("/");
					
					modelName = modelURL.substring(index+1);
					
					modelName = modelName.substring(0, modelName.length()-4);
		
					karma.addModel(modelName,null, jObj.getString("model_uri"));
						
					LOG.error("Added Model from SOURCE JSON:" + jObj.toString());
				}
				else {
					LOG.error("NO MODEL URI found in source: " + jObj.toString());
				}
			
				
				if(jObj.containsKey("roots")){
					
					JSONArray jArrayRoots = jObj.getJSONArray("roots");
					
					for (int i=0;i<jArrayRoots.size();i++){
						
						JSONObject jObjRoots = jArrayRoots.getJSONObject(i);
						
						if(jObjRoots.containsKey("root")){
							
							karma.setRdfGenerationRoot(jObjRoots.getString("root"),modelName);
							
							LOG.error("ROOT SELECTED:" + karma.getRdfGenerationRoot());
							LOG.error("MODEL ROOTS: " + modelName);
							String results = generateJSONLD(key, value,modelName);
							LOG.error("JSONLD ROOTS: " + results);
							if (!results.equals("[\n\n]\n") && results != null) {
								
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
					LOG.error("NO ROOTS MODEL:" +  modelName);
					String results = generateJSONLD(key, value,modelName);
					LOG.error("JSONLD ROOTS: " + results);
					if (results != null && !results.equals("[\n\n]\n")) {
						LOG.error("JSON-LD PRODUCED: " + results);
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
			LOG.error("JSON-LD PRODUCED: " + results);
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
				LOG.error("ROOT FROM FUNCTION GENERATEJSONLD:" + karma.getRdfGenerationRoot());
				request.setStrategy(new UserSpecifiedRootStrategy(karma.getRdfGenerationRoot()));
			}
			if (karma.getContextId() != null) {
				request.setContextName(karma.getContextId().getName());
			}
			
			karma.getGenerator().generateRDF(request);

			results = sw.toString();
			LOG.error("GENERATED JSON:" + results);
			

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Unable to generate RDF: " + e.getMessage());
			//throw new IOException();
		}
		
		return results;
	}
	
	protected abstract KR2RMLRDFWriter configureRDFWriter(StringWriter sw);

	protected abstract void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException;

}