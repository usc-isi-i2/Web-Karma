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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class TripleStoreUtil {

	private static Logger logger = LoggerFactory
			.getLogger(TripleStoreUtil.class);

	public static final String defaultServerUrl;
	public static final String defaultModelsRepoUrl;
	public static final String defaultDataRepoUrl;
	public static final String defaultWorkbenchUrl;
	public static final String karma_model_repo = "karma_models";
	public static final String karma_data_repo = "karma_data";
	static {
		String host = ContextParametersRegistry.getInstance().getDefault()
				.getParameterValue(ServletContextParameterMap.ContextParameter.JETTY_HOST);
		String port = ContextParametersRegistry.getInstance().getDefault()
				.getParameterValue(ServletContextParameterMap.ContextParameter.JETTY_PORT);
		final String baseURL = host + ":" + port + "/openrdf-sesame";
		defaultServerUrl = baseURL + "/repositories";
		defaultModelsRepoUrl = defaultServerUrl + "/karma_models";
		defaultDataRepoUrl = defaultServerUrl + "/karma_data";
		defaultWorkbenchUrl = host + ":" + port
				+ "/openrdf-workbench/repositories";
	}

	protected static HashMap<String, String> mime_types;

	public enum RDF_Types {
		TriG, BinaryRDF, TriX, N_Triples, N_Quads, N3, RDF_XML, RDF_JSON, Turtle
	}

	static {
		initialize();

		mime_types = new HashMap<>();
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
		List<String> repositories = new ArrayList<>();

		try {
			// query the list of repositories
			httpget = new HttpGet(defaultServerUrl);
			httpget.setHeader("Accept",
					"application/sparql-results+json, */*;q=0.5");
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				String s = buf.readLine();
				StringBuffer line = new StringBuffer();
				while (s != null) {
					line.append(s);
					s = buf.readLine();
				}
				JSONObject data = new JSONObject(line.toString());
				JSONArray repoList = data.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < repoList.length()) {
					JSONObject obj = repoList.getJSONObject(count++);
					repositories
					.add(obj.optJSONObject("id").optString("value"));
				}
				// check for karama_models repo
				if (!repositories.contains(karma_model_repo)) {
					logger.info("karma_models not found");
					if (create_repo(karma_model_repo,
							"Karma default model repository", "native")) {
						retVal = true;
					} else {
						logger.error("Could not create repository : "
								+ karma_model_repo);
						retVal = false;
					}
				}
				// check for karama_data repo
				if (!repositories.contains(karma_data_repo)) {
					logger.info("karma_data not found");
					if (create_repo(karma_data_repo,
							"Karma default data repository", "native")) {
						retVal = true;
					} else {
						logger.error("Could not create repository : "
								+ karma_data_repo);
						retVal = false;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return retVal;
	}

	public static boolean checkConnection(String url) {
		boolean retval = false;
		try {
			if (url.charAt(url.length() - 1) != '/') {
				url = url + "/";
			}
			url = url + "size";
			logger.info(url);
			String response = HTTPUtil.executeHTTPGetRequest(url, null);
			try {
				int i = Integer.parseInt(response.trim());
				logger.debug("Connnection to repo : " + url
						+ " Successful.\t Size : " + i);
				retval = true;
			} catch (Exception e) {
				logger.error("Could not parse size of repository query result.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	public boolean testURIExists(String tripleStoreURL, String context, String uri)
	{
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		try {
			testTripleStoreConnection(tripleStoreURL);
		} catch (KarmaException e1) {
			return false;
		}

		try {

			StringBuilder query = new StringBuilder();

			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#> ASK ");
			injectContext(context, query);
			query.append(" { { ");
			formatURI(uri,query);
			query.append(" ?y ?z .} union { ?x ?y ");
			formatURI(uri,query);
			query.append(" } } ");
			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, "application/sparql-results+json",
					formparams);

			if (responseString != null) {
				JSONObject askResult = new JSONObject(responseString);
				return askResult.getBoolean("boolean");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return false;
	}

	public Map<String, List<String>> getObjectsForSubjectsAndPredicates(String tripleStoreURL, String context, List<String> subjects, List<String> predicates, List<String> otherClasses, String sameAsProperty) throws KarmaException
	{

		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);
		Map<String, List<String>> results = new HashMap<>();
		List<String> resultSubjects = new LinkedList<>();
		List<String> resultPredicates = new LinkedList<>();
		List<String> resultObjects = new LinkedList<>();
		List<String> resultClasses = new LinkedList<>();
		results.put("resultSubjects", resultSubjects);
		results.put("resultPredicates", resultPredicates);
		results.put("resultObjects", resultObjects);
		results.put("resultClasses", resultClasses);
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			query.append("SELECT ?s ?p ?o ?filteredtype\n");			
			injectContext(context, query);
			query.append("{\n");
			query.append("VALUES ?s { ");
			for(String subject : subjects)
			{
				formatURI(subject, query);
				query.append(" ");
			}
			query.append("}\n");
			Iterator<String> predicateIterator = predicates.iterator();
			Iterator<String> otherClassIterator = otherClasses.iterator();


			String predicate;
			String otherClass;
			while(predicateIterator.hasNext() && otherClassIterator.hasNext())
			{
				query.append("{\n");
				predicate = predicateIterator.next();
				otherClass = otherClassIterator.next();

				if(!otherClass.trim().isEmpty())
				{
					query.append("BIND ( ");
					formatURI(otherClass, query);
					query.append(" AS ?filteredtype )\n");

				}
				query.append("BIND ( ");
				formatURI(predicate, query);
				query.append(" AS ?p )\n");
				query.append("{\n");
				query.append("?s ");
				formatURI(predicate, query);
				query.append(" ?o .\n");

				query.append("}\n");
				if(sameAsProperty != null && !sameAsProperty.isEmpty())
				{
					query.append("UNION\n");
					query.append("{\n");
					query.append("?s ");
					formatURI(sameAsProperty, query);
					query.append("?sprime .\n");
					query.append("?sprime ");
					formatURI(predicate, query);
					query.append(" ?o .\n");
					query.append("}\n");
					query.append("UNION\n");
					query.append("{\n");
					query.append("?sprime ");
					formatURI(sameAsProperty, query);
					query.append("?s .\n");
					query.append("?sprime ");
					formatURI(predicate, query);
					query.append(" ?o .\n");
					query.append("}\n");
				}
				if(!otherClass.trim().isEmpty())
				{
					query.append("?o a ?filteredtype .\n");
				}
				query.append("}\n");
				if(predicateIterator.hasNext() && otherClassIterator.hasNext())
				{
					query.append("UNION \n");
				}
			}

			query.append("}\n");

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, "application/sparql-results+json",
					formparams);
			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					resultSubjects.add(o.getJSONObject("s").getString("value"));
					resultPredicates.add(o.getJSONObject("p").getString("value"));
					resultObjects.add(o.getJSONObject("o").getString("value"));
					if(o.has("filteredtype"))
					{
						resultClasses.add(o.getJSONObject("filteredtype").getString("value"));
					}
					else
					{
						resultClasses.add("");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}	
		return results;
	}

	public Map<String, List<String>> getSubjectsForPredicatesAndObjects(String tripleStoreURL, String context, List<String> subjects, List<String> predicates, List<String> otherClasses, String sameAsPredicate) throws KarmaException
	{

		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);
		Map<String, List<String>> results = new HashMap<>();
		List<String> resultSubjects = new LinkedList<>();
		List<String> resultPredicates = new LinkedList<>();
		List<String> resultObjects = new LinkedList<>();
		List<String> resultClasses = new LinkedList<>();
		results.put("resultSubjects", resultSubjects);
		results.put("resultPredicates", resultPredicates);
		results.put("resultObjects", resultObjects);
		results.put("resultClasses", resultClasses);
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			query.append("SELECT ?s ?p ?o ?filteredtype\n");			
			injectContext(context, query);
			query.append("{\n");
			query.append("VALUES ?o { ");
			for(String subject : subjects)
			{
				formatURI(subject, query);
				query.append(" ");
			}
			query.append("}\n");
			Iterator<String> predicateIterator = predicates.iterator();
			Iterator<String> otherClassIterator = otherClasses.iterator();


			String predicate;
			String otherClass;
			while(predicateIterator.hasNext() && otherClassIterator.hasNext())
			{
				query.append("{\n");
				predicate = predicateIterator.next();
				otherClass = otherClassIterator.next();
				query.append("BIND ( ");
				formatURI(predicate, query);
				query.append(" AS ?p )\n");
				if(!otherClass.trim().isEmpty())
				{
					query.append("BIND ( ");
					formatURI(otherClass, query);
					query.append(" AS ?filteredtype )\n");

				}
				query.append("{\n");
				query.append("?s ?p ?o .\n");
				query.append("}\n");
				if(sameAsPredicate != null && !sameAsPredicate.isEmpty())
				{
					query.append("UNION \n");
					query.append("{\n");
					query.append("?oprime ");
					query.append(sameAsPredicate);
					query.append(" ?o .\n");
					query.append("?s ?p ?oprime .\n");
					query.append("}\n");
					query.append("UNION \n");
					query.append("{\n");
					query.append("?o ");
					query.append(sameAsPredicate);
					query.append(" ?oprime .\n");
					query.append("?s ?p ?oprime .\n");
					query.append("}\n");
				}
				if(!otherClass.trim().isEmpty())
				{
					query.append("?s a ?filteredtype .\n");

				}

				query.append("}\n");
				if(predicateIterator.hasNext() && otherClassIterator.hasNext())
				{
					query.append("UNION \n");
				}
			}

			query.append("}\n");

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, "application/sparql-results+json",
					formparams);

			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					resultSubjects.add(o.getJSONObject("s").getString("value"));
					resultPredicates.add(o.getJSONObject("p").getString("value"));
					resultObjects.add(o.getJSONObject("o").getString("value"));
					if(o.has("filteredtype"))
					{
						resultClasses.add(o.getJSONObject("filteredtype").getString("value"));
					}
					else
					{
						resultClasses.add("");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}	
		return results;
	}

	public void deleteMappingFromTripleStore(String tripleStoreURL, String context, String mappingURI) throws KarmaException
	{
		testTripleStoreConnection(tripleStoreURL);
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL) + "/statements";

		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			if (null != context && !context.trim().isEmpty())
			{
				query.append("WITH ");
				formatURI(context, query);
				query.append("\n");
			}
			query.append("DELETE { ?s ?p ?o } \n");
			query.append("WHERE\n");
			injectMapping(mappingURI, query);

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("update", queryString);

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, mime_types.get(RDF_Types.N3.name()),
					formparams);
			System.out.println(responseString);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	protected void injectMapping(String mappingURI, StringBuilder query) {
		query.append("{\n");
		injectType(mappingURI, query, Uris.RR_TRIPLESMAP_CLASS_URI, Uris.KM_HAS_TRIPLES_MAP_URI);
		injectType(mappingURI, query, Uris.RR_SUBJECTMAP_CLASS_URI, Uris.KM_HAS_SUBJECT_MAP_URI);
		injectType(mappingURI, query, Uris.RR_PREDICATEOBJECTMAP_CLASS_URI, Uris.KM_HAS_PREDICATE_OBJECT_MAP_URI);
		injectType(mappingURI, query, Uris.RR_OBJECTMAP_CLASS_URI, Uris.KM_HAS_OBJECT_MAP_URI);
		injectType(mappingURI, query, Uris.RR_LOGICAL_TABLE_CLASS_URI, Uris.KM_HAS_LOGICAL_TABLE_URI);
		injectType(mappingURI, query, Uris.RR_TEMPLATE_URI, Uris.RR_CLASS_URI, Uris.KM_HAS_SUBJECT_MAP_URI);
		query.append("{\n");
		query.append("?s ?p ?o .\n");
		query.append("?s owl:sameAs ");
		formatURI(mappingURI, query);
		query.append(" . \n"); 

		query.append("}\n");
		query.append("}\n");
	}

	protected void injectType(String mappingURI, StringBuilder query,
			String type, String hasType) {
		query.append("{\n");
		query.append("?s a <");
		query.append(type);
		query.append("> . \n");
		query.append("?mapping <");
		query.append(hasType);
		query.append("> ?s . \n");
		query.append("?mapping owl:sameAs ");
		formatURI(mappingURI, query);
		query.append(" .\n");
		query.append("?s ?p ?o . \n");
		query.append("}\n");
		query.append("UNION\n");
	}

	protected void injectType(String mappingURI, StringBuilder query,
			String type, String hasType, String hasType2) {
		query.append("{\n");
		query.append("?s <");
		query.append(type);
		query.append("> ?a . \n");
		query.append("?mapping2 <");
		query.append(hasType);
		query.append("> ?s . \n");
		query.append("?mapping <");
		query.append(hasType2);
		query.append("> ?mapping2 . \n");
		query.append("?mapping owl:sameAs ");
		formatURI(mappingURI, query);
		query.append(" .\n");
		query.append("?s ?p ?o . \n");
		query.append("}\n");
		query.append("UNION\n");
	}

	public String getMappingFromTripleStore(String tripleStoreURL, String context, String mappingURI) throws KarmaException
	{
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);
		context = normalizeURI(context);
		mappingURI = normalizeURI(mappingURI);
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			query.append("CONSTRUCT { ?s ?p ?o }\n");

			injectContext(context, query);
			injectMapping(mappingURI, query);

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, mime_types.get(RDF_Types.N3.name()),
					formparams);

			if (responseString != null) {
				return responseString;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "";
	}

	protected void injectContext(String context, StringBuilder query) {
		if (null != context && !context.trim().isEmpty())
		{
			query.append("FROM ");
			formatURI(context, query);
			query.append("\n");
		}
	}

	protected void formatURI(String uri, StringBuilder query) {
		uri = uri.trim();
		if(!uri.startsWith("<"))
		{
			query.append("<");
		}
		query.append(uri);
		if(!uri.endsWith(">"))
		{
			query.append(">");
		}
	}

	public HashMap<String, List<String>> getPredicatesForParentTriplesMapsWithSameClass(String tripleStoreURL, String context, Collection<String> classesToMatch) throws KarmaException
	{
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);

		List<String> predicates = new LinkedList<>();
		List<String> matchingRefObjMaps = new LinkedList<>();
		List<String> otherClasses = new LinkedList<>();
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			query.append("SELECT ?predicate ?otherClass (group_concat(?refObjMap; separator = \",\") as ?refObjMaps )\n");			
			injectContext(context, query);
			query.append("{\n");
			query.append("?mapping owl:sameAs ?mappingURI . \n"); 
			//			query.append("?mappingURI km-dev:hasData \"true\" .\n"); 
			query.append("?mapping km-dev:hasTriplesMap ?triplesMap .\n");
			query.append("?triplesMap rr:subjectMap ?subjectMap .\n");
			Iterator<String> itr = classesToMatch.iterator();
			while(itr.hasNext()) {
				String classToMatch = itr.next();
				query.append("{?subjectMap rr:class ");
				query.append("<").append(classToMatch.trim()).append(">");
				query.append("}");
				if (itr.hasNext())
					query.append(" UNION  \n");
				else
					query.append(". \n");
			}
			query.append("?refObjMap rr:parentTriplesMap ?triplesMap .\n");
			query.append("?pom rr:objectMap ?refObjMap .\n");
			query.append("?otherTriplesMap rr:predicateObjectMap ?pom .\n"); 
			query.append("?otherTriplesMap rr:subjectMap ?otherSubjectMap .\n");
			query.append("?otherSubjectMap rr:class ?otherClass .\n");
			query.append("FILTER NOT EXISTS { ?otherClass rr:template ?z }\n");
			query.append("?pom rr:predicate ?predicate .\n");
			query.append("}\n");
			query.append("GROUP BY ?predicate ?otherClass\n");

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, "application/sparql-results+json",
					formparams);

			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					predicates.add(o.getJSONObject("predicate").getString("value"));
					matchingRefObjMaps.add(o.getJSONObject("refObjMaps").getString("value"));
					if(o.has("otherClass"))
					{
						otherClasses.add(o.getJSONObject("otherClass").getString("value"));
					}
					else
					{
						otherClasses.add("");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		HashMap<String, List<String>> values = new HashMap<>();
		values.put("predicates", predicates);
		values.put("refObjectMaps", matchingRefObjMaps);
		values.put("otherClasses", otherClasses);
		return values;
	}


	public HashMap<String, List<String>> getPredicatesForTriplesMapsWithSameClass(String tripleStoreURL, String context, Collection<String> classesToMatch) throws KarmaException
	{
		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);

		List<String> predicates = new LinkedList<>();
		List<String> matchingPOMs = new LinkedList<>();
		List<String> otherClasses = new LinkedList<>();
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("PREFIX rr:<http://www.w3.org/ns/r2rml#>\n");
			query.append("SELECT ?predicate ?otherClass (group_concat(?pom; separator = \",\") as ?poms )\n");			
			injectContext(context, query);
			query.append("{\n");
			query.append("?mapping owl:sameAs ?mappingURI . \n"); 
			//			query.append("?mappingURI km-dev:hasData \"true\" .\n"); 
			query.append("?mapping km-dev:hasTriplesMap ?triplesMap .\n");
			query.append("?triplesMap rr:subjectMap ?subjectMap .\n");
			Iterator<String> itr = classesToMatch.iterator();
			while (itr.hasNext()) {
				String classToMatch = itr.next();
				query.append("{?subjectMap rr:class ");
				query.append("<").append(classToMatch.trim()).append(">");
				query.append("}");
				if (itr.hasNext())
					query.append(" UNION  \n");
				else
					query.append(". \n");
			}		
			query.append("?triplesMap rr:predicateObjectMap ?pom . \n");
			query.append("?pom rr:predicate ?predicate . \n");
			query.append("OPTIONAL \n");
			query.append("{\n");
			query.append("?pom rr:objectMap ?objectMap .\n");
			query.append("?objectMap rr:parentTriplesMap ?parentTriplesMap .\n");
			query.append("?parentTriplesMap rr:subjectMap ?otherSubjectMap .\n");
			query.append("?otherSubjectMap rr:class ?otherClass .\n");
			query.append("}\n}\n");
			query.append("GROUP BY ?predicate ?otherClass\n");

			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					tripleStoreURL, null, "application/sparql-results+json",
					formparams);

			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					predicates.add(o.getJSONObject("predicate").getString("value"));
					matchingPOMs.add(o.getJSONObject("poms").getString("value"));
					if(o.has("otherClass"))
					{
						otherClasses.add(o.getJSONObject("otherClass").getString("value"));
					}
					else
					{
						otherClasses.add("");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		HashMap<String, List<String>> values = new HashMap<>();
		values.put("predicates", predicates);
		values.put("predicateObjectMaps", matchingPOMs);
		values.put("otherClasses", otherClasses);
		return values;
	}


	public void testTripleStoreConnection(String tripleStoreURL)
			throws KarmaException {
		// check the connection first
		if (checkConnection(tripleStoreURL)) {
			logger.info("Connection Test passed");
		} else {
			logger.info("Failed connection test : " + tripleStoreURL);
			throw new KarmaException("Failed connect test : " + tripleStoreURL);
		}
	}

	protected String normalizeTripleStoreURL(String tripleStoreURL) {
		if (tripleStoreURL == null || tripleStoreURL.isEmpty()) {
			tripleStoreURL = defaultServerUrl + "/" + karma_model_repo + "/" + "statements";
		}

		if (tripleStoreURL.charAt(tripleStoreURL.length() - 1) == '/') {
			tripleStoreURL = tripleStoreURL.substring(0,
					tripleStoreURL.length() - 2);
		}
		logger.info("Repository URL : " + tripleStoreURL);
		return tripleStoreURL;
	}
	/**
	 * This method fetches all the source names of the models from the triple
	 * store
	 * 
	 * @param TripleStoreURL
	 *            : the triple store URL
	 * */
	public HashMap<String, List<String>> getMappingsWithMetadata(String TripleStoreURL, String context) throws KarmaException {

		TripleStoreURL = normalizeTripleStoreURL(TripleStoreURL);
		testTripleStoreConnection(TripleStoreURL);

		List<String> times = new ArrayList<>();
		List<String> names = new ArrayList<>();
		List<String> urls = new ArrayList<>();
		List<String> inputColumns = new ArrayList<>();
		List<String> contexts = new ArrayList<>();
		try {

			StringBuilder query = new StringBuilder();
			query.append("PREFIX km-dev:<http://isi.edu/integration/karma/dev#>\n");
			query.append("SELECT ?z ?y ?x ?src ?w ?u\n");
			injectContext(context, query);
			query.append("WHERE \n { \n");
			query.append("GRAPH ?src \n { \n");
			query.append("?a km-dev:sourceName ?u . \n");
			query.append("?a a km-dev:R2RMLMapping . \n");
			query.append("?a owl:sameAs ?z . \n");
			query.append("?a km-dev:modelPublicationTime ?x \n");
			query.append("OPTIONAL \n{?a km-dev:hasInputColumns ?w} \n");
			query.append("OPTIONAL \n{?a km-dev:hasModelLabel ?y} \n");
			query.append("\n}\n} \nORDER BY ?z ?y ?x ?w ?u ?src");
			String queryString = query.toString();
			logger.debug("query: " + queryString);


			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");

			String responseString = HTTPUtil.executeHTTPPostRequest(
					TripleStoreURL, null, "application/sparql-results+json",
					formparams);

			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					times.add(o.getJSONObject("x").getString("value"));
					urls.add(o.getJSONObject("z").getString("value"));
					if (o.has("w"))
							inputColumns.add(o.getJSONObject("w").getString("value"));
					else
						inputColumns.add("");
					if (o.has("y"))
						names.add(o.getJSONObject("y").getString("value"));
					else
						names.add(o.getJSONObject("u").getString("value"));
					contexts.add(o.getJSONObject("src").getString("value"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		HashMap<String, List<String>> values = new HashMap<>();
		values.put("model_publishtimes", times);
		values.put("model_names", names);
		values.put("model_urls", urls);
		values.put("model_contexts", contexts);
		values.put("model_inputcolumns", inputColumns);
		return values;
	}
	/**
	 * This method fetches all the source names of the models from the triple
	 * store
	 * 
	 * @param TripleStoreURL
	 *            : the triple store URL
	 * */
	public HashMap<String, List<String>> fetchModelNames(String TripleStoreURL) throws KarmaException {

		List<String> names = new ArrayList<>();
		List<String> urls = new ArrayList<>();

		TripleStoreURL = normalizeTripleStoreURL(TripleStoreURL);
		testTripleStoreConnection(TripleStoreURL);

		try {
			String queryString = "PREFIX km-dev:<http://isi.edu/integration/karma/dev#> SELECT ?y ?z where { ?x km-dev:sourceName ?y . ?x km-dev:serviceUrl ?z . } ORDER BY ?y ?z";
			logger.debug("query: " + queryString);

			Map<String, String> formparams = new HashMap<>();
			formparams.put("query", queryString);
			formparams.put("queryLn", "SPARQL");
			String responseString = HTTPUtil.executeHTTPPostRequest(
					TripleStoreURL, null, "application/sparql-results+json",
					formparams);

			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
					JSONObject o = values.getJSONObject(count++);
					names.add(o.getJSONObject("y").getString("value"));
					urls.add(o.getJSONObject("z").getString("value"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		HashMap<String, List<String>> values = new HashMap<>();
		values.put("model_names", names);
		values.put("model_urls", urls);
		return values;
	}

	/**
	 * @param filePath
	 *            : the url of the file from where the RDF is read
	 * @param tripleStoreURL
	 *            : the triple store URL
	 * @param context
	 *            : The graph context for the RDF
	 * @param replaceFlag
	 *            : Whether to replace the contents of the graph
	 * @param deleteSrcFile
	 *            : Whether to delete the source R2RML file or not
	 * @param rdfType
	 *            : The RDF type based on which the headers for the request are
	 *            set
	 * @param baseURL
	 *            : Specifies the base URI to resolve any relative URIs found in uploaded data against
	 * 
	 * */
	protected boolean saveToStore(HttpEntity entity, String tripleStoreURL,
			String context, boolean replaceFlag, 
			String rdfType, String baseURL) throws KarmaException {
		boolean retVal = false;
		HttpResponse response;

		tripleStoreURL = normalizeTripleStoreURL(tripleStoreURL);
		testTripleStoreConnection(tripleStoreURL);

		if (tripleStoreURL.charAt(tripleStoreURL.length() - 1) != '/') {
			tripleStoreURL += "/";
		}
		tripleStoreURL += "statements";
		try {
			URIBuilder builder = new URIBuilder(tripleStoreURL);

			// initialize the http entity
			HttpClient httpclient = new DefaultHttpClient();
			//			File file = new File(filePath);
			if (mime_types.get(rdfType) == null) {
				throw new Exception("Could not find spefied rdf type: "
						+ rdfType);
			}

			// preparing the context for the rdf
			if (context == null || context.isEmpty()) {
				logger.info("Empty context");
				context = "null";
			} else {
				context = context.trim();
				context.replaceAll(">", "");
				context.replaceAll("<", "");
				builder.setParameter("context", "<" + context + ">");
			}

			// preapring the base URL
			if (baseURL != null && !baseURL.trim().isEmpty()) {
				baseURL = baseURL.trim();
				baseURL.replaceAll(">", "");
				baseURL.replaceAll("<", "");
				builder.setParameter("baseURI", "<" + baseURL + ">");
			} else {
				logger.info("Empty baseURL");
			}


			// check if we need to specify the context
			if (!replaceFlag) {
				// we use HttpPost over HttpPut, for put will replace the entire
				// repo with an empty graph
				logger.info("Using POST to save rdf to triple store");
				HttpPost httpPost = new HttpPost(builder.build());
				httpPost.setEntity(entity);

				// executing the http request
				response = httpclient.execute(httpPost);
			} else {

				// we use HttpPut to replace the context
				logger.info("Using PUT to save rdf to triple store");
				HttpPut httpput = new HttpPut(builder.build());
				httpput.setEntity(entity);

				// executing the http request
				response = httpclient.execute(httpput);
			}

			logger.info("request url : " + builder.build().toString());
			logger.info("StatusCode: "
					+ response.getStatusLine().getStatusCode());
			logger.info(response.toString());
			int code = response.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				retVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getClass().getName() + " : " + e.getMessage());
		}
		return retVal;

	}

	/**
	 * @param filePath
	 *            : the url of the file from where the RDF is read
	 * @param tripleStoreURL
	 *            : the triple store URL
	 * @param context
	 *            : The graph context for the RDF
	 * @param replaceFlag
	 *            : Whether to replace the contents of the graph deleteSrcFile
	 *            default : false rdfType default: Turtle
	 * @param baseUri
	 *            : The graph context for the RDF
	 * 
	 * */
	public boolean saveToStoreFromFile(String filePath, String tripleStoreURL,
			String context, boolean replaceFlag, String baseUri) throws KarmaException{
		File file = new File(filePath);
		FileEntity entity = new FileEntity(file, ContentType.create(
				mime_types.get(RDF_Types.Turtle.name()), "UTF-8"));
		return saveToStore(entity, tripleStoreURL, context, replaceFlag,
				RDF_Types.Turtle.name(), baseUri);
	}

	public boolean saveToStoreFromString(String input, String tripleStoreURL,
			String context, Boolean replaceFlag, String baseUri)  throws KarmaException{
		StringEntity entity = new StringEntity(input, ContentType.create(mime_types.get(RDF_Types.Turtle.name())));
		return saveToStore(entity, tripleStoreURL, context, replaceFlag,
				RDF_Types.Turtle.name(), baseUri);
	}

	/**
	 * Invokes a SPARQL query on the given Triple Store URL and returns the JSON
	 * object containing the result. The content type of the result is
	 * application/sparql-results+json.
	 * 
	 * @param query
	 *            : SPARQL query
	 * @param tripleStoreUrl
	 *            : SPARQL endpoint address of the triple store
	 * @param acceptContentType
	 *            : The accept context type in the header
	 * @param contextType
	 *            :
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static String invokeSparqlQuery(String query, String tripleStoreUrl,
			String acceptContentType, String contextType)
					throws ClientProtocolException, IOException, JSONException {

		Map<String, String> formParams = new HashMap<>();
		formParams.put("query", query);
		formParams.put("queryLn", "SPARQL");

		String response = HTTPUtil.executeHTTPPostRequest(tripleStoreUrl,
				contextType, acceptContentType, formParams);

		if (response == null || response.isEmpty())
			return null;

		return response;
	}

	public static boolean create_repo(String repo_name, String repo_desc,
			String type) {
		// TODO : Take the repository type as an enum - native, memory, etc
		boolean retVal = false;
		if (repo_name == null || repo_name.isEmpty()) {
			logger.error("Invalid repo name : " + repo_name);
			return retVal;
		}
		if (repo_desc == null || repo_desc.isEmpty()) {
			repo_desc = repo_name;
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			HttpPost httppost = new HttpPost(defaultWorkbenchUrl
					+ "/NONE/create");
			List<NameValuePair> formparams = new ArrayList<>();
			formparams.add(new BasicNameValuePair("Repository ID", repo_name));
			formparams
			.add(new BasicNameValuePair("Repository title", repo_name));
			formparams
			.add(new BasicNameValuePair("Triple indexes", "spoc,posc"));
			formparams.add(new BasicNameValuePair("type", "native"));
			httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			httppost.setHeader("Content-Type",
					"application/x-www-form-urlencoded");
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				StringBuffer out = new StringBuffer();
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				String line = buf.readLine();
				while (line != null) {
					logger.info(line);
					out.append(line);
					line = buf.readLine();
				}
				logger.info(out.toString());
			}
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 || status < 300) {
				logger.info("Created repository : " + repo_name);
				retVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}


	/**
	 * This method fetches all the context from the given triplestore Url
	 * */
	public List<String> getContexts(String url) {
		if (url == null || url.isEmpty()) {
			url = defaultModelsRepoUrl;
		}
		url += "/contexts";
		List<String> graphs = new ArrayList<>();

		String responseString;
		try {
			responseString = HTTPUtil.executeHTTPGetRequest(url,
					"application/sparql-results+json");
			if (responseString != null) {
				JSONObject models = new JSONObject(responseString);
				JSONArray values = models.getJSONObject("results")
						.getJSONArray("bindings");
				int count = 0;
				while (count < values.length()) {
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
		logger.info("Checking for unique graphUri for url : " + graphUrl
				+ " at endPoint : " + tripleStoreUrl);
		boolean retVal = true;
		List<String> urls = this.getContexts(tripleStoreUrl);
		if (urls == null) {
			return false;
		}
		// need to compare each url in case-insensitive form
		for (String url : urls) {
			if (url.equalsIgnoreCase(graphUrl)) {
				retVal = false;
				break;
			}
		}
		return retVal;
	}
	

	/**
	 * This method clears all the statements from the given context
	 * */
	public static boolean clearContexts(String tripleStoreUrl, String graphUri) {
		if (tripleStoreUrl == null || tripleStoreUrl.isEmpty()
				|| graphUri == null || graphUri.isEmpty()) {
			logger.error("Missing graphUri or tripleStoreUrl");
			return false;
		}

		String responseString;
		try {
			String url = tripleStoreUrl + "/statements?context="
					+ URLEncoder.encode(graphUri, "UTF-8");
			logger.info("Deleting from uri : " + url);
			responseString = HTTPUtil.executeHTTPDeleteRequest(url);
			logger.info("Response=" + responseString);
			return true;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
	
	public String normalizeURI(String uri) {
		UriBuilder builder = UriBuilder.fromPath(uri);
		return builder.build().toString();
	}

}
