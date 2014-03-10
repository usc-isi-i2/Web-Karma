/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.controller.command.alignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommand.PreferencesKeys;
import edu.isi.karma.controller.command.worksheet.ExportCSVCommand.JsonKeys;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InvokeDataMiningServiceUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.SPARQLGeneratorUtil;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.util.HTTPUtil.HTTP_HEADERS;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class InvokeDataMiningServiceCommand extends Command {
	private static Logger logger = LoggerFactory.getLogger(InvokeDataMiningServiceCommand.class);
	private final String worksheetId;
	
//	private String tripleStoreUrl;
	private final String csvFileName;
//	private String graphUrl;
	private String dataMiningURL;
//	private String generatedRDFFileName = null;
//	private final ArrayList<String> columnList;
//	private final String rootNodeId;
	
	public String getDataMiningURL() {
		return dataMiningURL;
	}

	public void setDataMiningURL(String dataMiningURL) {
		this.dataMiningURL = dataMiningURL;
	}

//	public String getTripleStoreUrl() {
//		return tripleStoreUrl;
//	}
//
//	public void setTripleStoreUrl(String tripleStoreUrl) {
//		this.tripleStoreUrl = tripleStoreUrl;
//	}

	/**
	 * @param id
	 * @param worksheetId
	 * @param miningUrl
	 * @param csvFileName
	 * */
	protected InvokeDataMiningServiceCommand(String id, String worksheetId, String miningUrl, String fileName) {
		super(id);
		this.worksheetId = worksheetId;
		this.dataMiningURL = miningUrl;
		this.csvFileName = fileName;
	}
	/**
	 * @param id
	 * @param worksheetId
	 * @param rootNode
	 * @param sparqlUrl
	 * @param graph
	 * @param miningUrl
	 * @param nodes
	 * */
//	protected InvokeDataMiningServiceCommand(String id, String worksheetId, String rootNode, String sparqlUrl, String graph, String miningUrl, ArrayList<String> nodes) {
//		super(id);
//		this.worksheetId = worksheetId;
//		if (sparqlUrl == null || sparqlUrl.isEmpty()) {
//			sparqlUrl = TripleStoreUtil.defaultDataRepoUrl;
//		}
//		this.tripleStoreUrl = sparqlUrl;
//		this.graphUrl = graph;
//		this.dataMiningURL = miningUrl;
//		this.rootNodeId = rootNode;
//		this.columnList = nodes;
//	}

	@Override
	public String getCommandName() {
		return InvokeDataMiningServiceCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Invoke Data Mining Service";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}
	
	// Pedro: this is not being used. Candidate for deletion.
	// Is this from Shrikanth?
//	private String fetch_data_temp() 
//	{
//		HttpClient httpclient = new DefaultHttpClient();
//		TripleStoreUtil utilObj = new TripleStoreUtil();
//		StringBuffer jsonString = new StringBuffer();
//		try {
//
//			JSONObject result = utilObj.fetch_data(this.modelContext, null);
//			logger.debug(result.toString());
//			
//			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//			formparams = new ArrayList<NameValuePair>();
//			formparams.add(new BasicNameValuePair("data", result.toString()));
//			
//			HttpPost httppost = new HttpPost("http://localhost:1234/consumejson");
//			httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
//			HttpResponse response = httpclient.execute(httppost);
//
//			for(Header h : response.getAllHeaders()) {
//				logger.debug(h.getName() +  " : " + h.getValue());
//			}
//			HttpEntity entity = response.getEntity();
//			if (entity != null) {
//				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
//				String line = buf.readLine();
//				while(line != null) {
//					logger.debug(line);
//					jsonString.append(line);
//					line = buf.readLine();
//				}
//
//			}
//			return jsonString.toString();
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//		}
//		return "";
//	}
	
	@Override
	public UpdateContainer doIt(Workspace workspace) {
		
		final String csvFileLocalPath = ServletContextParameterMap.getParameterValue(
				ContextParameter.USER_DIRECTORY_PATH) +  "publish/CSV/" + this.csvFileName;
		
//		String jsonString = fetch_data_temp();
		try {
			
			// Get the alignment for this Worksheet
//			Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
//					Instance().constructAlignmentId(workspace.getId(), worksheetId));
//			
//			if (alignment == null) {
//				logger.info("Alignment is NULL for " + worksheetId);
//				return new UpdateContainer(new ErrorUpdate(
//						"Please align the worksheet before generating R2RML Model!"));
//			}
//			// Get the namespace and prefix from the preferences
//			String namespace = "";
//			String prefix = "";
//			JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
//					PublishRDFCommand.class.getSimpleName()+"Preferences");
//			if (prefObject != null) {
//				namespace = prefObject.getString(PreferencesKeys.rdfNamespace.name());
//				prefix = prefObject.getString(PreferencesKeys.rdfPrefix.name());
//				namespace = ((namespace == null) || (namespace.equals(""))) ? 
//						Namespaces.KARMA_DEV : namespace;
//				prefix = ((prefix == null) || (prefix.equals(""))) ? 
//						Prefixes.KARMA_DEV : prefix;
//			} else {
//				namespace = Namespaces.KARMA_DEV;
//				prefix = Prefixes.KARMA_DEV;
//			}
//
//			Worksheet worksheet = workspace.getWorksheet(worksheetId);
//			// Generate the KR2RML data structures for the RDF generation
//			final ErrorReport errorReport = new ErrorReport();
//			KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(workspace, worksheet, alignment, 
//					worksheet.getSemanticTypes(), prefix, namespace, true, errorReport);
//			
//			SPARQLGeneratorUtil genObj = new SPARQLGeneratorUtil();
//			String query = genObj.get_query(mappingGen.getKR2RMLMapping(), this.modelContext);
//			
//			// execute the query on the triple store
//			String data = TripleStoreUtil.invokeSparqlQuery(query, tripleStoreUrl, "application/sparql-results+json", null);
//
//			// prepare the input for the data mining service
////			int row_num = 0;
//			
//			logger.debug(data);
//			
			// post the results 
			//TODO : integrate the service with karma
			
			// Prepare the headers
			HttpPost httpPost = new HttpPost(this.dataMiningURL);
	
			FileEntity file = new FileEntity(new File(csvFileLocalPath));
			httpPost.setEntity(file);
			HttpClient httpClient = new DefaultHttpClient();
			httpPost.setHeader("Content-Type", "text/csv");
			
			// Execute the request
			HttpResponse response = httpClient.execute(httpPost);
			
			// Parse the response and store it in a String
			HttpEntity entity = response.getEntity();
			StringBuilder responseString = new StringBuilder();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
				
				String line = buf.readLine();
				while(line != null) {
					responseString.append(line);
					line = buf.readLine();
				}
			}
			logger.info(responseString.toString());
			final String modelFileName = responseString.toString(); 
			UpdateContainer uc = new UpdateContainer();
			uc.add(new AbstractUpdate() {
				public void generateJson(String prefix, PrintWriter pw,	
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put("updateType", "InvokeDataMiningServiceUpdate");
						outputObject.put("model_name", modelFileName);
						pw.println(outputObject.toString());
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
			return uc;
					
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new UpdateContainer(new ErrorUpdate("Error !"));
		}

	}
	

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
