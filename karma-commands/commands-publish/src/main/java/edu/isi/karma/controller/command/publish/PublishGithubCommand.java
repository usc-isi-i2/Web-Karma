package edu.isi.karma.controller.command.publish;

import com.sun.org.apache.xpath.internal.operations.Bool;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by alse on 11/21/16.
 */
public class PublishGithubCommand extends Command {
    private static Logger logger = LoggerFactory.getLogger(PublishGithubCommand.class);
    private String worksheetId;
    private String repo;
    private String branch;
    private String auth;

    public PublishGithubCommand(String id, String model, String worksheetId, String repo, String branch, String auth) {
        super(id, model);
        this.worksheetId = worksheetId;
        this.repo = repo;
        this.branch = branch;
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

            Worksheet worksheet = workspace.getWorksheet(this.worksheetId);

            String modelName = worksheet.getTitle();

            String dotFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.GRAPHVIZ_MODELS_DIR)
                    + modelName + ".model.dot";

            String modelFile = getHistoryFilepath(workspace);

            String reportFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.REPORT_PUBLISH_DIR)
                    + modelName + ".md";

            String modelJsonFile = ContextParametersRegistry.getInstance()
                    .getContextParameters(ContextParametersRegistry.getInstance().getDefault().getId())
                    .getParameterValue(ServletContextParameterMap.ContextParameter.JSON_MODELS_DIR)
                    + modelName + ".model.json";

            if (fileExists(dotFile)) {
                String contents = getFileContents(dotFile);
                // if you cant create a file then update it
                if (push(modelName + ".model.dot", contents, true) != 201) {
                    push(modelName + ".model.dot", contents, false);
                }
            }

            if (fileExists(modelFile)) {
                String contents = getFileContents(modelFile);
                String modelFilename = workspace.getCommandPreferencesId() + worksheetId + "-" +
                        worksheet.getTitle() +  "-auto-model.ttl";
                if (push(modelFilename, contents, true) != 201) {
                    push(modelFilename, contents, false);
                }
            }

            if (fileExists(reportFile)) {
                String contents = getFileContents(reportFile);
                // if you cant create a file then update it
                if (push(modelName + ".md", contents, true) != 201) {
                    push(modelName + ".md", contents, false);
                }
            }

            if (fileExists(modelJsonFile)) {
                String contents = getFileContents(modelJsonFile);
                // if you cant create a file then update it
                if (push(modelName + ".model.json", contents, true) != 201) {
                    push(modelName + ".model.json", contents, false);
                }
            }

            uc.add(new AbstractUpdate() {
                @Override
                public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
                    try {
                        JSONWriter writer = new JSONStringer().object();
                        writer.key("updateType").value(this.getClass().getName());
                        pw.print(writer.toString());
                        writer.endObject();
                    } catch (Exception e) {
                        logger.error("Error unable to set Github", e);
                    }
                }
            });
        }  catch (Exception e) {
            logger.error("Error unable to set Github" , e);
            uc.add(new ErrorUpdate("Error unable to set Github"));
        }
        return uc;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }

    public Integer push(String fileName, String fileContents, Boolean isCreate) throws IOException{
        String repoUser = this.repo.split("github\\.com")[1].split("/")[1];
        String repoName = this.repo.split("github\\.com")[1].split("/")[2];
        URL url = new URL("https://api.github.com/repos/" + repoUser + "/" + repoName + "/contents/" + fileName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty ("Authorization", "Basic " + this.auth);
        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());

        String b64FileContent = new String(Base64.encodeBase64(fileContents.getBytes()));
        if (isCreate) {
            osw.write("{\"message\": \"Create file " + fileName + "\", \"branch\":\"" + this.branch + "\", \"committer\": {\"name\": \"Karma\", \"email\": \"user@karma.com\"}, \"content\": \"" + b64FileContent + "\"}");
        } else {
            String shaFileContent = getBlobSHA(fileName);
            osw.write("{\"message\": \"Update file " + fileName + "\", \"branch\":\"" + this.branch + "\", \"committer\": {\"name\": \"Karma\", \"email\": \"user@karma.com\"}, \"content\": \"" + b64FileContent + "\", \"sha\": \"" + shaFileContent + "\"}");
        }
        osw.flush();
        osw.close();
        return connection.getResponseCode();
    }

    public Boolean fileExists(String path) {
        File f = new File(path);
        return f.exists() && !f.isDirectory();
    }

    public String getFileContents(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }

    public String getBlobSHA(String fileName) throws IOException{
        String repoUser = this.repo.split("github\\.com")[1].split("/")[1];
        String repoName = this.repo.split("github\\.com")[1].split("/")[2];
        URL url = new URL("https://api.github.com/repos/" + repoUser + "/" + repoName + "/git/trees/" + this.branch);
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

        JSONArray fileTree = new JSONObject(response.toString()).getJSONArray("tree");
        for(int i=0; i<fileTree.length(); i++) {
            if (fileTree.getJSONObject(i).getString("path").equals(fileName))
                return fileTree.getJSONObject(i).getString("sha");
        }
        return "";
    }

    public String getHistoryFilepath(Workspace workspace) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		String modelFilename = workspace.getCommandPreferencesId() + worksheetId + "-" +
				worksheet.getTitle() +  "-auto-model.ttl";
		return contextParameters.getParameterValue(
				ServletContextParameterMap.ContextParameter.R2RML_USER_DIR) +  modelFilename;
	}
}
