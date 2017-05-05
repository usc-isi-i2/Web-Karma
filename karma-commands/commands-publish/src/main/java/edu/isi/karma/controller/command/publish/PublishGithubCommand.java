package edu.isi.karma.controller.command.publish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;

/**
 * Created by alse on 11/21/16.
 * This command publishes dot, model, model json and report to github
 */
public class PublishGithubCommand extends Command {
    private static Logger logger = LoggerFactory.getLogger(PublishGithubCommand.class);
    private String worksheetId;
    private String repo;
    private String auth;
    private String branch;
    private HashMap<String, String> fileSHAMap = new HashMap<>();
    private String graphvizServer;
    private String originalGithubUrl;
    private enum JsonKeys {
		updateType, url, worksheetId
	}
    
    /*
    worksheetId - is the id of the worksheet that has to be published
    repo - is the github url of the repo where the files have to be published
    branch - is the branch of the repo where the files have to be published
    auth - is the base64 encoded string of username:password required for authentication
     */
    public PublishGithubCommand(String id, String model, String worksheetId, String githubUrl, String auth) {
        super(id, model);
        this.worksheetId = worksheetId;
        this.originalGithubUrl = githubUrl;
        
        String repoDetails = githubUrl.split("github\\.com")[1];
        int treeIdx = repoDetails.indexOf("/tree/");
        if(treeIdx != -1) {
        	int endIdx = repoDetails.indexOf("/", treeIdx+6);
        	String rest = "";
        	if(endIdx != -1) {
        		this.branch = repoDetails.substring(treeIdx+6, endIdx);
        		rest = repoDetails.substring(endIdx);
        	} else {
        		this.branch = repoDetails.substring(treeIdx+6);
        	}
        	repoDetails = repoDetails.substring(0, treeIdx) + 
        			"/contents" +  rest + "/";
        } else {
        	this.branch = "master";
        	repoDetails = repoDetails + "/contents/";
        }
        
        this.repo = "https://api.github.com/repos" + repoDetails;
        this.auth = auth;
    }
    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Publish Github Command";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.notInHistory;
    }
    
    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        UpdateContainer uc = new UpdateContainer();
        try{
        	try {
        		this.buildFileSHAMap();
        	} catch (FileNotFoundException fe) {
        		logger.warn("Github URL does not exist. Will try to see if it can be created.");
        	}
        	ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance()
        			.getModelingConfiguration(WorkspaceKarmaHomeRegistry.getInstance().getKarmaHome(workspace.getId()));
            this.graphvizServer = modelingConfiguration.getGraphvizServer();
            		
            Worksheet worksheet = workspace.getWorksheet(this.worksheetId);

            WorksheetProperties props = worksheet.getMetadataContainer().getWorksheetProperties();
            String modelName = props.getPropertyValue(Property.graphLabel);
            String worksheetTitle = worksheet.getTitle();
            
            String dotFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.GRAPHVIZ_MODELS_DIR)
                    + worksheetTitle + ".model.dot";

            String modelFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.R2RML_PUBLISH_DIR)
                    + modelName + "-model.ttl";

            String reportFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.REPORT_PUBLISH_DIR)
                    + worksheetTitle + ".md";

            String modelJsonFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.JSON_MODELS_DIR)
                    + worksheetTitle + ".model.json";

            if (fileExists(dotFile)) {
                String contents = getFileContents(dotFile);
                push(modelName + "-model.dot", contents);
                try {
                	//Use hosted graphviz server to convert dot to pdf
                	//https://github.com/omerio/graphviz-server
                	contents = contents.replace("digraph n0", "digraph G");
                	InputStream pdfStream = this.getGraphizPdf(contents);
                	push(modelName + "-model.pdf", pdfStream);
                } catch(Exception e) {
                	logger.error("Error generating png for the dot file", e);
                }
            }

            if (fileExists(modelFile)) {
            	String contents = getFileContents(modelFile);
                push(modelName + "-model.ttl", contents);
            }

            if (fileExists(reportFile)) {
                String contents = getFileContents(reportFile);
                push(modelName + "-model.md", contents);
            }

            if (fileExists(modelJsonFile)) {
                String contents = getFileContents(modelJsonFile);
                push(modelName + "-model.json", contents);
            }

            uc.add(new AbstractUpdate() {
                @Override
                public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
                    try {
                    	JSONObject outputObject = new JSONObject();
    					outputObject.put(JsonKeys.updateType.name(), "PublishGithubUpdate");
    					outputObject.put(JsonKeys.url.name(), originalGithubUrl);
    					outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
    					pw.println(outputObject.toString(4));
    					pw.println(",");
    					new InfoUpdate("Succesfully pushed model to Github").generateJson(prefix, pw, vWorkspace);
                    } catch (Exception e) {
                        logger.error("Error unable to set Github", e);
                    }
                }
            });
        } catch ( FileNotFoundException fe) {
        	logger.error("Error pushing to Github:" , fe);
        	uc.add(new ErrorUpdate("Error pushing to Github. <BR> Github URL is invalid"));
        }  catch (Exception e) {
            logger.error("Error pushing to Github:" , e);
            uc.add(new ErrorUpdate("Error pushing to Github: <BR>" + e.getMessage()));
        }
        return uc;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }

    /*
    This function does the push operation to github
    */
    private Integer push(String fileName, String fileContents) throws IOException{
    	return push(fileName, fileContents.getBytes());
    }
    
    private Integer push(String fileName, InputStream fileStream) throws IOException {
    	byte[] bytes = IOUtils.toByteArray(fileStream);
    	return push(fileName, bytes);
    }
    
    private Integer push(String fileName, byte[] bytes) throws IOException{
    	URL url = new URL(this.repo + fileName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty ("Authorization", "Basic " + this.auth);
        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());

        String b64FileContent = new String(Base64.encodeBase64(bytes));
        String fileSHA = getFileSHA(fileName);
        if (fileSHA == null) {
            osw.write("{\"message\": \"Create file " + fileName + 
            		"\", \"branch\":\"" + this.branch + 
            		"\", \"content\": \"" + 
            		b64FileContent + "\"}");
        } else {
            osw.write("{\"message\": \"Update file " + fileName + 
            		"\", \"branch\":\"" + this.branch + 
            		"\", \"content\": \"" + 
            		b64FileContent + "\", \"sha\": \"" + fileSHA + "\"}");
        }
        osw.flush();
        osw.close();
        return connection.getResponseCode();
    }

    private Boolean fileExists(String path) {
        File f = new File(path);
        return f.exists() && !f.isDirectory();
    }

    private String getFileContents(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }

    private InputStream getGraphizPdf(String dotContents) throws ClientProtocolException, IOException {
    	HttpClient httpClient = new DefaultHttpClient();
    	String url = this.graphvizServer + "pdf";
    	logger.info("Generating PDF for graphviz:" + url);
    	HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(dotContents));
		HttpResponse response = httpClient.execute(httpPost);
		
		// Parse the response and store it in a String
		HttpEntity entity = response.getEntity();
		return entity.getContent();
    }
    
    // Whenever we are doing an update instead of create, we need the blob sha of the file which exists on github.
    private void buildFileSHAMap() throws IOException {
    	this.fileSHAMap.clear();
    	String repo;	//Need to remove the extra / if present, else API does not use the ref parameter
    	if(this.repo.endsWith("/"))
    		repo = this.repo.substring(0, this.repo.length()-1);
    	else
    		repo = this.repo;
    	String urlStr = repo + "?ref=" + this.branch;
    	URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + this.auth);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONArray fileTree = new JSONArray(response.toString());
        for(int i=0; i<fileTree.length(); i++) {
        	JSONObject fileObj = fileTree.getJSONObject(i);
            if (fileObj.getString("type").equals("file"))
            	this.fileSHAMap.put(fileObj.getString("name"), fileObj.getString("sha"));
        }
    }
    
    private String getFileSHA(String filename) {
    	return this.fileSHAMap.get(filename);
    }
}
