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
package edu.isi.karma.webserver;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextParameterMap {
	private Map<ContextParameter, String> valuesMap = new ConcurrentHashMap<>();

	protected final String id;
	
	private static Logger logger = LoggerFactory
			.getLogger(ServletContextParameterMap.class);
 
	public enum ContextParameter {
		PUBLIC_RDF_ADDRESS,PUBLIC_KML_ADDRESS, 
		KML_TRANSFER_SERVICE, WGS84_LAT_PROPERTY, 
		WGS84_LNG_PROPERTY, POINT_POS_PROPERTY, 
		POS_LIST_PROPERTY, POINT_CLASS, LINE_CLASS, 
		TRAINING_EXAMPLE_MAX_COUNT, 
		USER_DIRECTORY_PATH,
		MSFT,
		WEBAPP_PATH,
		PRELOADED_ONTOLOGY_DIRECTORY, POLYGON_CLASS, SRID_PROPERTY, 
		SRID_CLASS, AUTO_MODEL_URI, PYTHON_SCRIPTS_DIRECTORY,
		KML_CUSTOMIZATION_CLASS, KML_CATEGORY_PROPERTY,KML_LABEL_PROPERTY,
		CLEANING_SERVICE_URL, CLUSTER_SERVICE_URL,
		JETTY_PORT, JETTY_HOST, SEMTYPE_MODEL_DIRECTORY, 
		TEXTUAL_SEMTYPE_MODEL_DIRECTORY, NUMERIC_SEMTYPE_MODEL_DIRECTORY,
		ALIGNMENT_GRAPH_DIRECTORY, USER_PREFERENCES_DIRECTORY, USER_CONFIG_DIRECTORY,
		GRAPHVIZ_MODELS_DIR,
		JSON_MODELS_DIR, 
		R2RML_PUBLISH_DIR, R2RML_PUBLISH_RELATIVE_DIR,
		R2RML_USER_DIR,
		RDF_PUBLISH_DIR, RDF_PUBLISH_RELATIVE_DIR,
		CSV_PUBLISH_DIR, CSV_PUBLISH_RELATIVE_DIR, USER_PYTHON_SCRIPTS_DIRECTORY,
		JSON_PUBLISH_DIR, JSON_PUBLISH_RELATIVE_DIR,
		REPORT_PUBLISH_DIR, REPORT_PUBLISH_RELATIVE_DIR, AVRO_PUBLISH_DIR, AVRO_PUBLISH_RELATIVE_DIR, USER_UPLOADED_DIR,
		KML_PUBLISH_DIR, KML_PUBLISH_RELATIVE_DIR, EVALUATE_MRR
	}
	
	public ServletContextParameterMap(String karmaDir)
	{
		setup(karmaDir);
		id = getKarmaHome(); 
	}
	public void setup(String karmaDir) {
		
		if(karmaDir == null)
		{
			// Find a safe place to store preferences
			karmaDir = System.getenv("KARMA_USER_HOME");
		}
		if(karmaDir == null)
		{
			karmaDir = System.getProperty("KARMA_USER_HOME");
			if(karmaDir == null) {
				Preferences preferences = Preferences.userRoot().node("WebKarma");
				karmaDir = preferences.get("KARMA_USER_HOME",  null);
			}
		}
		if(karmaDir == null)
		{
			String defaultLocation = System.getProperty("user.home") + File.separator + "karma";
			logger.info("KARMA_USER_HOME not set.  Defaulting to " + defaultLocation);
			File newKarmaDir = new File(defaultLocation);
			karmaDir = newKarmaDir.getAbsolutePath() + File.separator;
			
		}
		if(!karmaDir.endsWith(File.separator))
		{
			karmaDir += File.separator;
		}
		setParameterValue(ContextParameter.USER_DIRECTORY_PATH, karmaDir);
		logger.info("Karma home: " + karmaDir);
		
	}
	public void setParameterValue(ContextParameter param, String value) {
		valuesMap.put(param, value);
	}

	public String getParameterValue(ContextParameter param) {
		if (valuesMap.containsKey(param))
			return valuesMap.get(param);

		return "";
	}

	public String getKarmaHome() {
		
		return getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
	}
	
	public String getId(){
		return id;
	}

	@Override
	public String toString(){
		
		return id + ": " + valuesMap.toString();
	}
}
