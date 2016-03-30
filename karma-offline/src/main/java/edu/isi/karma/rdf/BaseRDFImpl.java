package edu.isi.karma.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

/**
 * Created by chengyey on 12/6/15.
 */
public abstract class BaseRDFImpl implements Serializable {
   private static final long serialVersionUID = 6611527418523710154L;

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFImpl.class);

    protected BaseKarma karma;
    protected String header = null;
    protected String delimiter = null;
    protected boolean hasHeader = false;
    boolean readKarmaConfig = false;
    protected JSONArray jKarmaConfig = null;
    protected Map<String, Pattern> urlPatterns=null;
    protected boolean disableNesting = false;
    public BaseRDFImpl(String propertyPath) {
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(propertyPath));
            setup(properties);
        } catch (Exception e) {
            LOG.error("Can't setup", e);
        }
    }

    public BaseRDFImpl(Properties properties) {
    	try {
			setup(properties);
		} catch (IOException e) {
			 LOG.error("Can't setup", e);
		}
    }
    
    public BaseRDFImpl() {

    }

	public void setup(Properties configuration) throws IOException {
        urlPatterns = new HashMap<>();
        karma = new BaseKarma();
        String inputTypeString = (String) configuration.get(
                "karma.input.type");
        
        String modelUri = (String) configuration.get("model.uri");
        String modelFile = (String) configuration.get("model.file");
        String model = (String) configuration.get("model.content");
         
        String contextURI = (String) configuration.get("context.uri");
        String context = (String) configuration.get("context.content");
        
        String baseURI = (String) configuration.get("base.uri"); 
        String rdfGenerationRoot = (String)configuration.get("rdf.generation.root");
        String rdfSelection = (String)configuration.get("rdf.generation.selection");
        delimiter = (String)configuration.get("karma.input.delimiter");
        hasHeader = Boolean.parseBoolean((String)configuration.get("karma.input.header"));
        disableNesting = Boolean.parseBoolean((String)configuration.getProperty("rdf.generation.disable.nesting", "false"));
        karma.setup("./karma.zip/karma", inputTypeString, modelUri, modelFile, model,
                baseURI, contextURI, context, rdfGenerationRoot, rdfSelection);


        readKarmaConfig = Boolean.parseBoolean((String)configuration.get("read.karma.config"));

        if(readKarmaConfig)
        {
            String configFilePath = (String)configuration.get("karma.config.file");
            File configFile = new File(configFilePath);
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

                jKarmaConfig = new JSONArray(sbConfig.toString());

                br.close();
            }
        }
    }

    public String mapResult(String key, String value) throws IOException,
            InterruptedException {

        String contents = value;

        JSONObject jMatchedKarmaConfig = matchKeyToKarmaConfig(key);
        String results = "";
        if (contents.trim() != ""){

            LOG.debug(key + " started");
            if(hasHeader && header ==null)
            {
                header=contents;
                LOG.debug("found header: " + header);
                return "";
            }
            else if(hasHeader && header != null)
            {
                contents = header+"\n" + contents;
            }

            if(readKarmaConfig && jMatchedKarmaConfig != null){

                String modelName = addModelToKarmaSetup(jMatchedKarmaConfig);
                if(modelName != null){
                    if(jMatchedKarmaConfig.has("roots")){
                        JSONArray jArrayRoots = jMatchedKarmaConfig.getJSONArray("roots");
                        for (int i=0;i<jArrayRoots.length();i++){
                            JSONObject jObjRoots = jArrayRoots.getJSONObject(i);
                            if(jObjRoots.has("root")){
                                karma.setRdfGenerationRoot(jObjRoots.getString("root"),modelName);
                                results = checkResultsAndWriteToContext(key,value,modelName);
                            }
                        }
                    }
                    else{
                        results = checkResultsAndWriteToContext(key,value,modelName);
                    }
                }
                else{
                    LOG.info("Model uri missing from karma config:" + jMatchedKarmaConfig.toString());
                }
            }
            else{
                results = checkResultsAndWriteToContext(key,value,"model");
            }
            LOG.debug(key + " finished");
        }
        return results;
    }



    protected String addModelToKarmaSetup(JSONObject jMatchedKarmaConfig) throws MalformedURLException {

        String modelName=null;
        if (jMatchedKarmaConfig.has("model-uri")){

            //add the new model and cache it
            String modelURL = jMatchedKarmaConfig.getString("model-uri");
            modelName=extractModelName(modelURL);
            karma.addModel(modelName,null, jMatchedKarmaConfig.getString("model-uri"), null);
        }else if(jMatchedKarmaConfig.has("model-file")){

            String modelFile = jMatchedKarmaConfig.getString("model-file");
            modelName=extractModelName(modelFile);
            karma.addModel(modelName, modelFile, null, null);
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

    protected String checkResultsAndWriteToContext(String key, String value,String modelName) throws IOException, InterruptedException{
        String results = generateJSONLD(key, value,modelName);
        return results;
    }
    protected JSONObject matchKeyToKarmaConfig(String key){

        JSONObject jMatchedKarmaConfig = null;
        Pattern p = null;
        if(jKarmaConfig != null){
            for(int i=0;i<jKarmaConfig.length();i++){
                if(jKarmaConfig.getJSONObject(i).has("urls")){
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

    protected String generateJSONLD(String key, String value, String modelName)
    {
        String filename = key;
        String contents = value;
        StringWriter sw = new StringWriter();

        String results = "";

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
}
