package edu.isi.karma.mapreduce.driver;

import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.BaseKarma;
import edu.isi.karma.rdf.RDFGeneratorRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseRDFMapper extends Mapper<Writable, Text, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);

	protected BaseKarma karma;
	protected String header = null;
	protected String delimiter = null;
	protected boolean hasHeader = false;
	boolean readKarmaConfig = false;
	protected JSONArray jKarmaConfig = null;
	protected HashMap<String, Pattern> urlPatterns=null;
	@Override
	public void setup(Context context) throws IOException {

		urlPatterns = new HashMap<String, Pattern>();
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
		
		File configFile = new File(configFilePath);
		
		if(readKarmaConfig)
		{
			if(!configFile.exists()){
				throw new FileNotFoundException("File at :" + configFilePath + " doesn't exist.");
			}
			else{
				StringBuilder sbConfig = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(configFile));
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
		
		JSONObject jMatchedKarmaConfig = matchKeyToKarmaConfig(key.toString());
				
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
				
				String modelName = addModelToKarmaSetup(jMatchedKarmaConfig);
				if(modelName != null){
					if(jMatchedKarmaConfig.containsKey("roots")){
						JSONArray jArrayRoots = jMatchedKarmaConfig.getJSONArray("roots");
						for (int i=0;i<jArrayRoots.size();i++){
							JSONObject jObjRoots = jArrayRoots.getJSONObject(i);
							if(jObjRoots.containsKey("root")){
								karma.setRdfGenerationRoot(jObjRoots.getString("root"),modelName);
								checkResultsAndWriteToContext(key,value,modelName,context);
							}
						}
					}
					else{
						checkResultsAndWriteToContext(key,value,modelName,context);
					}
				}
				else{
					LOG.info("Model uri missing from karma config:" + jMatchedKarmaConfig.toString());
				}
			}
			else{
				checkResultsAndWriteToContext(key,value,"model",context);
			}
			LOG.debug(key.toString() + " finished");
		}
		
	}
	
	protected String addModelToKarmaSetup(JSONObject jMatchedKarmaConfig) throws MalformedURLException{
		
		String modelName=null;
		if (jMatchedKarmaConfig.containsKey("model-uri")){
			
			//add the new model and cache it
			String modelURL = jMatchedKarmaConfig.getString("model-uri");
			modelName=extractModelName(modelURL);
			karma.addModel(modelName,null, jMatchedKarmaConfig.getString("model-uri"));
		}else if(jMatchedKarmaConfig.containsKey("model-file")){
			
			String modelFile = jMatchedKarmaConfig.getString("model-file");
			modelName=extractModelName(modelFile);
			karma.addModel(modelName, modelFile, null);
		}
		return modelName;
	}
	
	
	protected String extractModelName(String model){
		String modelName=null;
		int index = model.lastIndexOf("/");
		if(index != -1){
			modelName = model.substring(index+1);
			modelName = modelName.substring(0, modelName.length()-4);
		}else{
			modelName = model.substring(0, model.length()-4);
		}
		
		return modelName;
	}
	
	protected Pattern getMatchedURLPattern(String pattern){
		
		if(urlPatterns.containsKey(pattern.trim())){
			return urlPatterns.get(pattern.trim());
		}
		Pattern p = Pattern.compile(pattern.trim());
		urlPatterns.put(pattern.trim(), p);
		return p;
	}

	protected void checkResultsAndWriteToContext(Writable key, Text value,String modelName,Context context) throws IOException, InterruptedException{
		String results = generateJSONLD(key, value,modelName);
		if (results != null && !results.equals("[\n\n]\n")) {
			
			writeRDFToContext(context, results);
			
		}
		else
		{
			LOG.info("RDF is empty! ");
		}
	}
	protected JSONObject matchKeyToKarmaConfig(String key){
		
		JSONObject jMatchedKarmaConfig = null;
		Pattern p = null;
		if(jKarmaConfig != null){
			for(int i=0;i<jKarmaConfig.size();i++){
				if(jKarmaConfig.getJSONObject(i).containsKey("urls")){
					p = getMatchedURLPattern(jKarmaConfig.getJSONObject(i).getString("urls").trim());
					Matcher m = p.matcher(key.trim());
					if(m.find()){
						jMatchedKarmaConfig = jKarmaConfig.getJSONObject(i);
						break;
					}
				}
			}
		}
		return jMatchedKarmaConfig;
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