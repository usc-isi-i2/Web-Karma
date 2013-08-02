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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleStoreUtil {
	
	private static Logger logger = LoggerFactory.getLogger(TripleStoreUtil.class);
	private static final String defaultServerUrl = "http://localhost:8080/openrdf-sesame/repositories";
	private static final String defaultWorkbenchUrl = "http://localhost:8080/openrdf-workbench/repositories";
	private static final String karma_model_repo = "karma_models";
	private static final String karma_data_repo = "karma_data";
	
	static {
		initialize();
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
				JSONObject data = JSONObject.fromObject(line.toString());
				JSONArray repoList = data.getJSONObject("results").getJSONArray("bindings");
				Iterator<JSONObject> itr = repoList.iterator();
				while(itr.hasNext()) {
					JSONObject obj = itr.next();
					repositories.add(obj.optJSONObject("id").optString("value"));
				}
				// check for karama_models repo
				if (!repositories.contains(karma_model_repo)) {
					System.out.println("karma_models not found");
					if (create_repo(karma_model_repo, "Karma default model repository", "native")) {
						retVal = true;
					} else {
						logger.error("Could not create repository : " + karma_model_repo);
						retVal = false;
					}
				}
				// check for karama_data repo
				if (!repositories.contains(karma_data_repo)) {
					System.out.println("karma_data not found");
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
	
	private static boolean checkConnection(String url) {
//		initialize();
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget;
		HttpResponse response;
		HttpEntity entity;
		StringBuffer out = new StringBuffer();
		boolean retval = false;
		try {
			if(url.charAt(url.length()-1) != '/') {
				url = url + "/";
			}
			url = url +  "size";
			logger.info(url);
			httpget = new HttpGet(url);
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
				String line = buf.readLine();
				while(line != null) {
					out.append(line);
					line = buf.readLine();
				}
				try {
					int i = Integer.parseInt(out.toString());
					logger.debug("Connnection to repo : " + url + " Successful.\t Size : " + i);
					 retval = true;
				} catch (Exception e) {
					logger.error("Could not parse size of repository query result.");
				}
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
	public ArrayList<String> fetchModelNames(String TripleStoreURL) { 
		if(TripleStoreURL == null || TripleStoreURL.isEmpty()) {
			TripleStoreURL = defaultServerUrl + "/"  +karma_model_repo;
		}
		ArrayList<String> list = new ArrayList<String>();
		HttpClient httpclient = new DefaultHttpClient();
		
		if(TripleStoreURL.charAt(TripleStoreURL.length() - 1) == '/') {
			TripleStoreURL = TripleStoreURL.substring(0, TripleStoreURL.length()-2);
		}
		logger.info("Repositoty URL : " + TripleStoreURL);
		
		// check the connection first
		if (checkConnection(TripleStoreURL)) {
			logger.info("Connection Test passed");
		} else {
			logger.info("Failed connection test : " + TripleStoreURL);
			return new ArrayList<String>();
		}
		
		try {
			String queryString = "PREFIX km-dev:<http://isi.edu/integration/karma/dev#> SELECT ?y WHERE { ?x km-dev:sourceName ?y . }";
			logger.debug("query: " + queryString);
			
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("query",queryString));
			formparams.add(new BasicNameValuePair("queryLn","SPARQL"));
			
			HttpPost httppost = new HttpPost(TripleStoreURL);
			httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			httppost.setHeader("Accept", "application/sparql-results+json");
			HttpResponse response = httpclient.execute(httppost);
			
			for(Header h : response.getAllHeaders()) {
				logger.debug(h.getName() +  " : " + h.getValue());
			}
			logger.info("StatusCode: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
				StringBuffer jsonString = new StringBuffer();
				String line = buf.readLine();
				while(line != null) {
					System.out.println(line);
					jsonString.append(line);
					line = buf.readLine();
				}
				
				JSONObject data = JSONObject.fromObject(jsonString.toString());
				JSONArray values = data.getJSONObject("results").getJSONArray("bindings");
				Iterator<JSONObject> itr = values.iterator();
				while(itr.hasNext()) {
					JSONObject o = itr.next();
					list.add(o.getJSONObject("y").getString("value"));
				}
			}
			logger.debug("repositories fetched: " + list.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param fileUrl : the url of the file from where the RDF is read
	 * @param tripleStoreURL : the triple store URL
	 * @param context : The graph context for the RDF
	 * @param replaceFlag : Whether to replace the contents of the graph
	 * 
	 * */
	public boolean saveModel(String filePath, String tripleStoreURL, String context, boolean replaceFlag) {
		boolean retVal = false;
		
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
			
			// check if we need to specify the context
			if (context == null || context.isEmpty()) {
				builder.setParameter("context", "null");
			} else {
				builder.setParameter("context", context);
				// if the context is given, then only we consider the option of replacing the old contents
				if (replaceFlag) {
					builder.setParameter("baseURI", context);
				}
			}
			
			// build the POST params
			URI uri = builder.build();
			HttpClient httpclient = new DefaultHttpClient();
			File file = new File(filePath);
			FileEntity entity = new FileEntity(file, ContentType.create("application/x-turtle", "UTF-8"));        
			HttpPost httppost = new HttpPost(uri);
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			logger.info("StatusCode: " + response.getStatusLine().getStatusCode());
			for(Header h : response.getAllHeaders()) {
				logger.info(h.getName() +  " : " + h.getValue());
			}
			int code = response.getStatusLine().getStatusCode();
			if(code >= 200 && code < 300) {
				retVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getClass().getName() + " : " + e.getMessage());
		}
		return retVal;
	
	}
	
	public boolean saveModel(String fileUrl) {
		return saveModel(fileUrl,defaultServerUrl + "/" + karma_model_repo, null, true);
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
					System.out.println(line);
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

}
