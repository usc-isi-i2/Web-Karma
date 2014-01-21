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

package edu.isi.karma.rep.metadata;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class WorksheetProperties {
	private Map<Property, String> propertyValueMap;
	private boolean hasServiceProperties;
	
	public static String DEFAULT_GRAPH_NAME_PREFIX = "http://localhost/worksheets/";
	
	public enum Property {
		serviceRequestMethod, serviceDataPostMethod, graphName, serviceUrl, hasServiceProperties
	}
	
	public WorksheetProperties() {
		propertyValueMap = new HashMap<Property, String>();
	}

	public void setPropertyValue(Property property, String value) {
		propertyValueMap.put(property, value);
	}
	
	public String getPropertyValue(Property property) {
		return propertyValueMap.get(property);
	}
	
	public void setHasServiceProperties(boolean flag) {
		this.hasServiceProperties = flag;
	}
	
	public boolean hasServiceProperties() {
		return hasServiceProperties;
	}
	
	public JSONObject getJSONRepresentation() throws JSONException {
		JSONObject obj = new JSONObject();
		for (Property prop:Property.values()) {
			if (propertyValueMap.containsKey(prop)) {
				String val = propertyValueMap.get(prop);
				if (val != null && !val.equals("")) {
					obj.put(prop.name(), val);
				}
			}
		}
		if (hasServiceProperties()) {
			obj.put(Property.hasServiceProperties.name(), true);
		} else {
			obj.put(Property.hasServiceProperties.name(), false);
		}
		return obj;
	}

	public static String createDefaultGraphName(String worksheetTitle) throws URIException {
		return DEFAULT_GRAPH_NAME_PREFIX + URIUtil.encodePath(worksheetTitle);
	}
}
