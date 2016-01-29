package edu.isi.karma.util;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class SavedModelURLs {

	private static final String modelFile = "model-urls.json";
	private static int maxNumUrls = 20;
	
	public void saveModelUrl(String url, String contextId) throws JSONException, IOException {
		File file = getModelsFile(contextId);
		JSONObject json = new JSONObject(FileUtil.readFileContentsToString(file, "UTF-8"));
		JSONArray models = ((JSONArray)json.get("models"));
		JSONArray newModels = new JSONArray();
		
		int modelIndex = modelExists(models, url);
		
		for(int i = Math.max(0,  models.length() - maxNumUrls); i < models.length(); i ++)
		{
			if(modelIndex != i)
			{
				newModels.put(models.get(i));
			}
		}
		newModels.put(url);
		json.put("models", newModels);
		FileUtil.writePrettyPrintedJSONObjectToFile(json, file);
	}
	
	public JSONObject getSavedModels(String contextId) throws IOException {
		File file = getModelsFile(contextId);
		JSONObject json = new JSONObject(FileUtil.readFileContentsToString(file, "UTF-8"));
		return json;
	}
	
	private File getModelsFile(String contextId) throws IOException {
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		File file = new File(contextParameters.getParameterValue(ContextParameter.USER_PREFERENCES_DIRECTORY) + 
				  "/" + modelFile);
		if(!file.exists()) {
			JSONObject json = new JSONObject("{\"models\":[]}");
			file.createNewFile();
			FileUtil.writePrettyPrintedJSONObjectToFile(json, file);
		}
		return file;
	}
	
	private int modelExists(JSONArray models, String url) {
		for(int i=0; i<models.length(); i++) {
			String modelUrl = models.getString(i);
			if(modelUrl.equals(url))
				return i;
		}
		return -1;
	}
}
