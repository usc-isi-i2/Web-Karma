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
package edu.isi.karma.modeling.semantictypes;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.util.Jsonizable;
import edu.isi.karma.util.Util;

public class CRFColumnModel implements Jsonizable {

	private final HashMap<String, Double> scoreMap = new HashMap<String, Double>();

	public CRFColumnModel(ArrayList<String> labels, ArrayList<Double> scores) {
		for (int i = 0; i < labels.size(); i++) {
			scoreMap.put(labels.get(i), scores.get(i));
		}
	}

	public HashMap<String, Double> getScoreMap() {
		return scoreMap;
	}

	public Double getScoreForLabel(String label) {
		return scoreMap.get(label);
	}

	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		writer.array();
		for (String label : scoreMap.keySet()) {
			writer.object();
			writer.key(SemanticTypesUpdate.JsonKeys.FullType.name()).value(label);
			writer.key("probability").value(scoreMap.get(label));
			writer.endObject();
		}
		writer.endArray();
		writer.endObject();
	}

	public JSONObject getAsJSONObject(OntologyManager ontMgr) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();

		// Need to sort
		HashMap<String, Double> sortedMap = Util.sortHashMap(scoreMap);

		for (String label : sortedMap.keySet()) {
			JSONObject oj = new JSONObject();
			
			// Check if the type contains domain
			if(label.contains("|")){
				Label domainURI = ontMgr.getUriLabel(label.split("\\|")[0]);
				Label typeURI = ontMgr.getUriLabel(label.split("\\|")[1]);
				if(domainURI == null || typeURI == null)
					continue;
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayDomainLabel.name(), domainURI.getLocalNameWithPrefix());
				oj.put(SemanticTypesUpdate.JsonKeys.Domain.name(), label.split("\\|")[0]);
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayLabel.name(), typeURI.getLocalNameWithPrefix());
				oj.put(SemanticTypesUpdate.JsonKeys.FullType.name(), label.split("\\|")[1]);
			} else {
				Label typeURI = ontMgr.getUriLabel(label);
				if(typeURI == null)
					continue;
				oj.put(SemanticTypesUpdate.JsonKeys.FullType.name(), label);
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayLabel.name(), typeURI.getLocalNameWithPrefix());
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayDomainLabel.name(), "");
				oj.put(SemanticTypesUpdate.JsonKeys.Domain.name(), "");
			}
			
			oj.put("Probability", scoreMap.get(label));
			arr.put(oj);
		}
		obj.put("Labels", arr);
		return obj;
	}
}
