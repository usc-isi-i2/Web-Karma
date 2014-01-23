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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.publish.PublishRDFCommandPreferencesKeys;
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class InvokeDataMiningServiceCommand extends Command {
	private static Logger logger = LoggerFactory.getLogger(InvokeDataMiningServiceCommand.class);
	private final String worksheetId;
	
	private String tripleStoreUrl;
	private String modelContext;
	private String dataMiningURL;
	
	public String getDataMiningURL() {
		return dataMiningURL;
	}

	public void setDataMiningURL(String dataMiningURL) {
		this.dataMiningURL = dataMiningURL;
	}

	public String getModelContext() {
		return modelContext;
	}

	public void setModelContext(String modelContext) {
		this.modelContext = modelContext;
	}

	public String getTripleStoreUrl() {
		return tripleStoreUrl;
	}

	public void setTripleStoreUrl(String tripleStoreUrl) {
		this.tripleStoreUrl = tripleStoreUrl;
	}

	protected InvokeDataMiningServiceCommand(String id, String worksheetId, String url, String graph, String miningUrl) {
		super(id);
		this.worksheetId = worksheetId;
		if (url == null || url.isEmpty()) {
			url = TripleStoreUtil.defaultDataRepoUrl;
		}
		this.tripleStoreUrl = url;
		this.modelContext = graph;
		this.dataMiningURL = miningUrl;
	}

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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
//		String jsonString = fetch_data_temp();
		try {
			
			// Get the alignment for this Worksheet
			Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
					Instance().constructAlignmentId(workspace.getId(), worksheetId));
			
			if (alignment == null) {
				logger.info("Alignment is NULL for " + worksheetId);
				return new UpdateContainer(new ErrorUpdate(
						"Please align the worksheet before generating R2RML Model!"));
			}
			// Get the namespace and prefix from the preferences
			String namespace = "";
			String prefix = "";
			JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
					"PublishRDFCommand"+"Preferences");
			if (prefObject != null) {
				namespace = prefObject.getString(PublishRDFCommandPreferencesKeys.rdfNamespace.name());
				prefix = prefObject.getString(PublishRDFCommandPreferencesKeys.rdfPrefix.name());
				namespace = ((namespace == null) || (namespace.equals(""))) ? 
						Namespaces.KARMA_DEV : namespace;
				prefix = ((prefix == null) || (prefix.equals(""))) ? 
						Prefixes.KARMA_DEV : prefix;
			} else {
				namespace = Namespaces.KARMA_DEV;
				prefix = Prefixes.KARMA_DEV;
			}

			Worksheet worksheet = workspace.getWorksheet(worksheetId);
			// Generate the KR2RML data structures for the RDF generation
			final ErrorReport errorReport = new ErrorReport();
			KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(workspace, alignment, 
					worksheet.getSemanticTypes(), prefix, namespace, true, errorReport);
			
			SPARQLGeneratorUtil genObj = new SPARQLGeneratorUtil();
			String query = genObj.get_query(mappingGen.getKR2RMLMapping(), this.modelContext);
			
			// execute the query on the triple store
			String data = TripleStoreUtil.invokeSparqlQuery(query, tripleStoreUrl, "application/sparql-results+json", null);

			// prepare the input for the data mining service
//			int row_num = 0;
			
			logger.debug(data);
			
			// post the results 
			//TODO : integrate the service with karma
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("data", data);
			String response = HTTPUtil.executeHTTPPostRequest("http://localhost:1234/consumejson", null, null, formParameters);
			return new UpdateContainer(new InvokeDataMiningServiceUpdate(new JSONObject().put("data", response), 
					InvokeDataMiningServiceUpdate.DataPrcessingFormats.testFormat.name()));
			
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
