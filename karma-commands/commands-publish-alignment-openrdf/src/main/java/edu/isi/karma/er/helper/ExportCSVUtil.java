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

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.common.OSUtils;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand.PreferencesKeys;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

/**
 * @author shri
 * <br /> The ExportCSV utility class
 * */
public class ExportCSVUtil {

	private static Logger logger = LoggerFactory.getLogger(ExportCSVUtil.class);

	private ExportCSVUtil() {
	}

	/**
	 * @author shri
	 * 
	 * @param workspace The Workspace object used to generate the KR2RML model
	 * @param worksheetId
	 * @param rootNodeId The root Node Id from where the csv is to be generated
	 * @param columnList An ArrayList<HashMap<String, String>> that has the list of columns to be fetched. These columns are identified by their complete URL as defined in the ontology. <br /> If null then all the columns are fetched
	 * @param graphUrl The context/graph from where the columns of the models are to be fetched
	 * 
	 * @return HashMap<String, String> of the format 'Query: <the sparql query' or 'Error: <The error message>' 
	 * */
	
	public static HashMap<String, String> generateCSVQuery(Workspace workspace, final String worksheetId, final String rootNodeId,
			final ArrayList<HashMap<String, String>> columnList, final String graphUrl) {
	
		HashMap<String, String> retVal = new HashMap<>();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		// Get the alignment for this Worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
				Instance().constructAlignmentId(workspace.getId(), worksheetId));

		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			retVal.put("Error", "Please align the worksheet before generating CSV!");
			return retVal;
		}
		// Get the namespace and prefix from the preferences
		String namespace = "";
		String prefix = "";
		try {
			JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
					PublishRDFCommand.class.getSimpleName()+"Preferences");
			if (prefObject != null) {
				namespace = prefObject.getString(PreferencesKeys.rdfNamespace.name());
				prefix = prefObject.getString(PreferencesKeys.rdfPrefix.name());
				namespace = (namespace == null || namespace.equals("")) ? 
						Namespaces.KARMA_DEV : namespace;
				prefix = (prefix == null || prefix.equals("")) ? 
						Prefixes.KARMA_DEV : prefix;
			} else {
				namespace = Namespaces.KARMA_DEV;
				prefix = Prefixes.KARMA_DEV;
			}
	
	
			// Generate the KR2RML data structures for the RDF generation
			final ErrorReport errorReport = new ErrorReport();
			KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(workspace, worksheet, alignment, 
					worksheet.getSemanticTypes(), prefix, namespace, true);
	
			// Generate the Spqrql query for the columns starting from the given root node
			SPARQLGeneratorUtil genObj = new SPARQLGeneratorUtil();
			String query = null;
			if(rootNodeId != null) {
	
				List<TriplesMap> triples = mappingGen.getKR2RMLMapping().getTriplesMapList();
				TriplesMap root_node = null;
				// get the root node first
				for (TriplesMap row : triples) {
					if (row.getSubject().getId().equalsIgnoreCase(rootNodeId) ) {
						root_node = row;
						break;
					}
				}
				query = genObj.get_query(root_node, columnList, true);
			} else {
				query = genObj.get_query(mappingGen.getKR2RMLMapping(), graphUrl);
			}
			retVal.put("Query", query);
			logger.info("Generated sparql query : " + query);
		} catch (Exception e) {
			logger.error("Error while generating query for csv export.",e);
			retVal.put("Error", "Error while generating query for csv export. : " + e.getMessage());
		}
		
		return retVal;
	}
	
	
	
	/**
	 * @author shri
	 * 
	 * @param workspace The Workspace object used to generate the KR2RML model
	 * @param worksheetId
	 * @param rootNodeId The root Node Id from where the csv is to be generated
	 * @param columnList An ArrayList of strings that has the list of columns to be fetched. These columns are identified by their complete URL as defined in the ontology. <br /> If null then all the columns are fetched
	 * @param graphUrl The context/graph from where the columns of the models are to be fetched
	 * @param tripleStoreUrl The sparql end point for data to be fetched
	 * 
	 * @return HashMap<String, String> of the format 'File: <the csv file path' or 'Error: <The error message>' 
	 * */
	
	public static HashMap<String, String> generateCSVFile(Workspace workspace, final String worksheetId, final String rootNodeId,
			final ArrayList<HashMap<String, String>> columnList, final String graphUrl, final String tripleStoreUrl, final String csvFilePath) {
		HashMap<String, String> retVal = new HashMap<>();
		HashMap<String, String> query = generateCSVQuery(workspace, worksheetId, rootNodeId, columnList, graphUrl);
		try {
			if(query.containsKey("Error")) {
				logger.error("Error while generating csv");
				return query;
			}
			// execute the query on the triple store
			// TODO need to fix the encoding type for the data returned in csv form
			// the new line char is not properly encoded
			String data = TripleStoreUtil.invokeSparqlQuery(query.get("Query"), tripleStoreUrl, "application/sparql-results+json", null);
			
			File f = new File(csvFilePath);
			File parentDir = f.getParentFile();
			parentDir.mkdirs();
			PrintWriter writer = new PrintWriter(f, "UTF-8");
			JSONObject jsonData = new JSONObject(data);
			JSONArray headers = jsonData.getJSONObject("head").getJSONArray("vars");
			JSONArray rows = jsonData.getJSONObject("results").getJSONArray("bindings");
			// write the header
			for(int j=0;j<headers.length();j++) {
				writer.write(headers.getString(j).replaceAll("\"", "\\\""));
				if(j < headers.length()-1) {
					writer.write(",");
				}
			}
			for(int i=0;i<rows.length();i++) {
//				JSONObject obj = rows.getJSONObject(i);
				writer.write("\n");
				for(int j=0;j<headers.length();j++) {
					try {
//						writer.write("\"" + rows.optJSONObject(i).optJSONObject(headers.getString(j)).optString("value").replaceAll("\"", "\\\"") + "\"");
						writer.write(rows.optJSONObject(i).optJSONObject(headers.getString(j)).optString("value").replaceAll("\"", "\\\""));
						if(j < headers.length()-1) {
							writer.write(",");
						}
					}catch(Exception e1) {
						logger.error("Could not find key in json object");
					}
				}
			}
			
			logger.info("Fetching data from triple store in csv format : " + tripleStoreUrl);
//			String data = TripleStoreUtil.invokeSparqlQuery(query.get("Query"), tripleStoreUrl, "text/csv", null);
			
			writer.write("\n");
			writer.flush();
			writer.close();
			if(OSUtils.isWindows())
				System.gc();  //Invoke gc for windows, else it gives error: The requested operation cannot be performed on a file with a user-mapped section open
			//when the model is republished, and the original model is earlier open
			
			logger.info("Writing CSV file :" + csvFilePath);
			
		} catch(Exception e) {
			logger.error("Error while generating csv : ",e);
			retVal.put("Error", e.getMessage());
		}
		return retVal;
		
		
	}
	
}
