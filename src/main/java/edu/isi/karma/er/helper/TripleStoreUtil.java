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

package edu.isi.karma.er.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class TripleStoreUtil {
	
	private static Logger logger = LoggerFactory.getLogger(TripleStoreUtil.class);
	
	
	public static final String defaultServerUrl;
	public static final String defaultModelsRepoUrl;
	public static final String defaultDataRepoUrl;
	public static final String defaultWorkbenchUrl;
	public static final String karma_model_repo = "karma_models";
	public static final String karma_data_repo = "karma_data";
	
	static 
	{
		String host = ServletContextParameterMap.getParameterValue(ServletContextParameterMap.ContextParameter.JETTY_HOST);
		String port = ServletContextParameterMap.getParameterValue(ServletContextParameterMap.ContextParameter.JETTY_PORT);
		final String baseURL = host+":"+port+"/openrdf-sesame";
		defaultServerUrl = baseURL + "/repositories";
		defaultModelsRepoUrl = defaultServerUrl + "/karma_models";
		defaultDataRepoUrl = defaultServerUrl + "/karma_data";
		defaultWorkbenchUrl = host+":"+port + "/openrdf-workbench/repositories";
	}
	
	private static HashMap<String, String> mime_types;
	
	public enum RDF_Types {
		TriG,
		BinaryRDF,
		TriX,
		N_Triples,
		N_Quads,
		N3,
		RDF_XML,
		RDF_JSON,
		Turtle
	}
	
	static {
		initialize();
		
		mime_types = new HashMap<String, String>();
		mime_types.put(RDF_Types.TriG.name(), "application/x-trig");
		mime_types.put(RDF_Types.BinaryRDF.name(), "application/x-binary-rdf");
		mime_types.put(RDF_Types.TriX.name(), "application/trix");
		mime_types.put(RDF_Types.N_Triples.name(), "text/plain");
		mime_types.put(RDF_Types.N_Quads.name(), "text/x-nquads");
		mime_types.put(RDF_Types.N3.name(), "text/rdf+n3");
		mime_types.put(RDF_Types.Turtle.name(), "application/x-turtle");
		mime_types.put(RDF_Types.RDF_XML.name(), "application/rdf+xml");
		mime_types.put(RDF_Types.RDF_JSON.name(), "application/rdf+json");
		
	}
	
	/**
	 * This method check for the default karma repositories in the local server 
	 * If not, it creates them
	 * */
	public static boolean initialize() {
		
		
		boolean retVal = false;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget;
		HttpResponse response;
		HttpEntity entity;
		ArrayList<String> repositories = new ArrayList<String>();
		
		try {
			// query the list of repositories
			httpget = new HttpGet(defaultServerUrl);
			httpget.setHeader("Accept", "application/sparql-results+json, */*;q=0.5");
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
				String s = buf.readLine();
				StringBuffer line = new StringBuffer(); 
				while(s != null) {
					line.append(s);
					s = buf.readLine();
				}
				JSONObject data = new  JSONObject(line.toString());
				JSONArray repoList = data.getJSONObject("results").getJSONArray("bindings");
				int count  = 0;
				while(count < repoList.length()) {
					JSONObject obj = repoList.getJSONObject(count++);
					repositories.add(obj.optJSONObject("id").optString("value"));
				}
				// check for karama_models repo
				if (!repositories.contains(karma_model_repo)) {
					logger.info("karma_models not found");
					if (create_repo(karma_model_repo, "Karma default model repository", "native")) {
						retVal = true;
					} else {
						logger.error("Could not create repository : " + karma_model_repo);
						retVal = false;
					}
				}
				// check for karama_data repo
				if (!repositories.contains(karma_data_repo)) {
					logger.info("karma_data not found");
					if (create_repo(karma_data_repo, "Karma default data repository", "native")) {
						retVal = true;
					} else {
						logger.error("Could not create repository : " + karma_data_repo);
						retVal = false;
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return retVal;
	}
	
	public static boolean checkConnection(String url) {
		boolean retval = false;
		try {
			if(url.charAt(url.length()-1) != '/') {
				url = url + "/";
			}
			url = url +  "size";
			logger.info(url);
			String response = HTTPUtil.executeHTTPGetRequest(url, null);
				try {
					int i = Integer.parseInt(response);
					logger.debug("Connnection to repo : " + url + " Successful.\t Size : " + i);
					 retval = true;
				} catch (Exception e) {
					logger.error("Could not parse size of repository query result.");
				}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		return retval;
	}
	
	/**
	 * This method fetches all the source names of the models from the triple store
	 * @param TripleStoreURL : the triple store URL
	 * */
	public HashMap<String, ArrayList<String>> fetchModelNames(String TripleStoreURL) { 
		if(TripleStoreURL == null || TripleStoreURL.isEmpty()) {
			TripleStoreURL = defaultServerUrl + "/"  +karma_model_repo;
		}
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> urls = new ArrayList<String>();
		
		if(TripleStoreURL.charAt(TripleStoreURL.length() - 1) == '/') {
			TripleStoreURL = TripleStoreURL.substring(0, TripleStoreURL.length()-2);
		}
		logger.info("Repositoty URL : " + TripleStoreURL);
		
		// check the connection first
		if (checkConnection(TripleStoreURL)) {
			logger.info("Connection Test passed");
		} else {
			logger.info("Failed connection test : " + TripleStoreURL);
			return null;
		}
		
		try {
			String queryString = "PREFIX km-dev:<http://isi.edu/integration/karma/dev#> SELECT ?y ?z where { ?x km-dev:sourceName ?y . ?x km-dev:serviceUrl ?z . } ORDER BY ?y ?z";
			logger.debug("query: " + queryString);
			
			Map<String, String> formparams = new HashMap<String, String>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");
			String responseString =  HTTPUtil.executeHTTPPostRequest(TripleStoreURL, null, "application/sparql-results+json", formparams);
			
			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results").getJSONArray("bindings");
				int count = 0;
				while(count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					names.add(o.getJSONObject("y").getString("value"));
					urls.add(o.getJSONObject("z").getString("value"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		HashMap<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>();
		values.put("model_names", names);
		values.put("model_urls", urls);
		return values;
	}

	/**
	 * @param fileUrl : the url of the file from where the RDF is read
	 * @param tripleStoreURL : the triple store URL
	 * @param context : The graph context for the RDF
	 * @param replaceFlag : Whether to replace the contents of the graph
	 * @param deleteSrcFile : Whether to delete the source R2RML file or not
	 * @param rdfType : The RDF type based on which the headers for the request are set
	 * 
	 * */
	private boolean saveToStore(String filePath, String tripleStoreURL, String context, boolean replaceFlag, boolean deleteSrcFile, String rdfType) {
		boolean retVal = false;
		URI uri = null;
		HttpResponse response = null;
		// check the connection first
		if (checkConnection(tripleStoreURL)) {
			logger.info("Connection Test passed");
		} else {
			logger.info("Failed connection test url : " + tripleStoreURL);
			return retVal;
		}
		
		if(tripleStoreURL.charAt(tripleStoreURL.length()-1) != '/') {
			tripleStoreURL += "/";
		}
		tripleStoreURL += "statements";
		try {
			URIBuilder builder = new URIBuilder(tripleStoreURL);
			
			// initialize the http entity
			HttpClient httpclient = new DefaultHttpClient();
			File file = new File(filePath);
			if (mime_types.get(rdfType) == null) {
				throw new Exception("Could not find spefied rdf type: " + rdfType);
			}
			FileEntity entity = new FileEntity(file, ContentType.create(mime_types.get(rdfType), "UTF-8"));
			
			// check if we need to specify the context
			if (!replaceFlag) {
				if (context == null || context.isEmpty()) {
					context = "null";
				} else {
					context = "<" + context + ">";
					builder.setParameter("baseURI", "<"+context+">");
				}
				builder.setParameter("context", context);
				
				// as we dont have the context or we want to append to context 
				// we use HttpPost over HttpPut, for put will replace the entire repo with an empty graph
				logger.info("Using POST to save rdf to triple store");
				uri = builder.build();
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setEntity(entity);
				
				// executing the http request
				response = httpclient.execute(httpPost);
			} 
			else 
			{
				builder.setParameter("context", "<"+context+">");
				builder.setParameter("baseURI", "<"+context+">");
				uri = builder.build();
				
				// we use HttpPut to replace the context
				logger.info("Using PUT to save rdf to triple store");
				HttpPut httpput = new HttpPut(uri);
				httpput.setEntity(entity);
				
				// executing the http request
				response = httpclient.execute(httpput);
			}
			
			logger.info("request url : " + uri.toString());
			logger.info("StatusCode: " + response.getStatusLine().getStatusCode());
			int code = response.getStatusLine().getStatusCode();
			if(code >= 200 && code < 300) {
				retVal = true;
			}
			if(deleteSrcFile) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getClass().getName() + " : " + e.getMessage());
		}
		return retVal;
	
	}
	
	
	/**
	 * @param fileUrl : the url of the file from where the RDF is read
	 * @param tripleStoreURL : the triple store URL
	 * @param context : The graph context for the RDF
	 * @param replaceFlag : Whether to replace the contents of the graph
	 * deleteSrcFile default : false
	 * rdfType default: Turtle
	 * 
	 * */
	public boolean saveToStore(String filePath, String tripleStoreURL, String context, boolean replaceFlag) {
		return saveToStore(filePath, tripleStoreURL, context, replaceFlag, false);
	}
	
	
	/**
	 * @param fileUrl : the url of the file from where the RDF is read
	 * 
	 * Default_Parameters <br />
	 * tripleStoreURL : the local triple store URL
	 * context : null
	 * rdfType : Turtle
	 * deleteSrcFile : False (will retain the source file)
	 * replaceFlag : true
	 * 
	 * */
	public boolean saveToStore(String fileUrl) {
		return saveToStore(fileUrl,defaultServerUrl + "/" + karma_model_repo, null, true);
	}
	
	
	/**
	 * @param fileUrl : the url of the file from where the RDF is read
	 * @param tripleStoreURL : the triple store URL
	 * @param context : The graph context for the RDF
	 * @param replaceFlag : Whether to replace the contents of the graph
	 * @param deleteSrcFile : Whether to delete the source R2RML file or not
	 * 
	 * rdfType default : Turtle
	 * 
	 * */
	public boolean saveToStore(String filePath, String tripleStoreURL, String context, boolean replaceFlag, boolean deleteSrcFile) {
		return saveToStore(filePath, tripleStoreURL, context, replaceFlag, deleteSrcFile, RDF_Types.Turtle.name());
	}
	
	
	
	/**
	 * Invokes a SPARQL query on the given Triple Store URL and returns the JSON object
	 * containing the result. The content type of the result is application/sparql-results+json. 
	 * 
	 * @param query: SPARQL query
	 * @param tripleStoreUrl: SPARQL endpoint address of the triple store
	 * @param acceptContentType: The accept context type in the header
	 * @param contextType: 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static String invokeSparqlQuery(String query, String tripleStoreUrl,
			 String acceptContentType, String contextType) throws ClientProtocolException, IOException, JSONException {
		
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("query", query);
		formParams.put("queryLn", "SPARQL");
		
		String response = HTTPUtil.executeHTTPPostRequest(tripleStoreUrl,
				contextType, acceptContentType, formParams);
		
		if (response == null || response.isEmpty()) return null;
		
		return response;
	}
	
	
	public static boolean create_repo(String repo_name, String repo_desc, String type ) {
		// TODO : Take the repository type as an enum - native, memory, etc
		boolean retVal = false;
		if (repo_name == null || repo_name.isEmpty() ) {
			logger.error("Invalid repo name : " + repo_name);
			return retVal;
		}
		if (repo_desc == null || repo_desc.isEmpty()) {
			repo_desc = repo_name;
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			HttpPost httppost = new HttpPost(defaultWorkbenchUrl+"/NONE/create");
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("Repository ID",repo_name));
			formparams.add(new BasicNameValuePair("Repository title",repo_name));
			formparams.add(new BasicNameValuePair("Triple indexes","spoc,posc"));
			formparams.add(new BasicNameValuePair("type","native"));
			httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				StringBuffer out = new StringBuffer();
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
				String line = buf.readLine();
				while(line != null) {
					logger.info(line);
					out.append(line);
					line = buf.readLine();
				}
				logger.info(out.toString());
			}
			int status = response.getStatusLine().getStatusCode();
			if ( status >= 200 || status < 300) {
				logger.info("Created repository : " + repo_name);
				retVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}
	
	
//	public org.json.JSONObject fetch_data(String graph, String tripleStoreUrl) throws ClientProtocolException, IOException, JSONException {
//		if (tripleStoreUrl == null || tripleStoreUrl.isEmpty()) {
//			tripleStoreUrl = defaultDataRepoUrl;
//		}
//		//JSONObject retVal = new JSONObject();
//		StringBuffer queryString = new StringBuffer();
//		queryString.append("SELECT ?x ?z ")
//			.append("WHERE { GRAPH <").append(graph.trim()).append("> { ")
//			.append("?x  ?p <http://isi.edu/integration/karma/ontologies/model/current/Input> . "
//				+ "?x  <http://isi.edu/integration/karma/ontologies/model/current/hasValue> ?z . } }");
//		
//		String sData = invokeSparqlQuery(queryString.toString(), tripleStoreUrl, "application/sparql-results+json", null);
//		if (sData == null | sData.isEmpty()) {
//			logger.error("Enpty response object from query : " + queryString.toString());
//		}
//		JSONObject data = new JSONObject(sData);
//		JSONArray d1 = data.getJSONObject("results").getJSONArray("bindings");
//		int count = 0;
//		HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
//		while(count < d1.length()) {
//			JSONObject obj = d1.getJSONObject(count++);
//			String key = obj.getJSONObject("x").getString("value");
//			String val = obj.getJSONObject("z").getString("value");
//			
//			if (!results.keySet().contains(key)) {
//				results.put(key, new ArrayList<String>());
//			} 
//			results.get(key).add(val);
//		}
//		return new JSONObject(results);
//	}
	
	/**
	 * This method fetches all the context from the given triplestore Url
	 * */
	public ArrayList<String> getContexts(String url) {
		if(url==null || url.isEmpty()) {
			url = defaultModelsRepoUrl;
		} 
		url += "/contexts";
		ArrayList<String> graphs = new ArrayList<String>();
		
		String responseString;
		try {
			responseString = HTTPUtil.executeHTTPGetRequest(url, "application/sparql-results+json");
			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results").getJSONArray("bindings");
				int count = 0;
				while(count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					graphs.add(o.getJSONObject("contextID").getString("value"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			graphs = null;
		}
		
		return graphs;
	}
	
	public boolean isUniqueGraphUri(String tripleStoreUrl, String graphUrl) {
		logger.info("Checking for unique graphUri for url : " + graphUrl + " at endPoint : " + tripleStoreUrl);
		boolean retVal = true;
		ArrayList<String> urls = this.getContexts(tripleStoreUrl);
		if(urls == null ){
			return false;
		}
		// need to compare each url in case-insensitive form
		for(String url : urls) {
			if (url.equalsIgnoreCase(graphUrl)) {
				retVal = false;
				break;
			}
		}
		return retVal;
	}
	
}
