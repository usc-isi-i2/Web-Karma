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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextParameterMap {
	private static HashMap<ContextParameter, String> valuesMap = new HashMap<ContextParameter, String>();

	private static Logger logger = LoggerFactory
			.getLogger(ServletContextParameterMap.class);

	public enum ContextParameter {
		PUBLIC_RDF_ADDRESS,PUBLIC_KML_ADDRESS, 
		KML_TRANSFER_SERVICE, WGS84_LAT_PROPERTY, 
		WGS84_LNG_PROPERTY, POINT_POS_PROPERTY, 
		POS_LIST_PROPERTY, POINT_CLASS, LINE_CLASS, 
		TRAINING_EXAMPLE_MAX_COUNT, USER_DIRECTORY_PATH,MSFT,
		PRELOADED_ONTOLOGY_DIRECTORY, POLYGON_CLASS, SRID_PROPERTY, SRID_CLASS, AUTO_MODEL_URI
	}

	public static void setParameterValue(ContextParameter param, String value) {
		valuesMap.put(param, value);
	}

	public static String getParameterValue(ContextParameter param) {
		if (valuesMap.containsKey(param))
			return valuesMap.get(param);
		else
			logger.error("Parameter value does not exist!");

		return "";
	}
}
