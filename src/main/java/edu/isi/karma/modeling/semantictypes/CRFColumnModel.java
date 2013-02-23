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
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
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

	public JSONObject getAsJSONObject(OntologyManager ontMgr, Alignment alignment) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		
		/** Create a set of node ids of internal nodes of Steiner Tree **/
		Set<String> steinerTreeNodeIds = new HashSet<String>();
		if (alignment != null && !alignment.isEmpty()) {
			for (Node node: alignment.getSteinerTree().vertexSet()) {
				if (node.getType() == NodeType.InternalNode) {
					steinerTreeNodeIds.add(node.getId());
				}
			}
		}

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
				
				StringBuilder domainId = new StringBuilder();
				StringBuilder domainDisplayLabel = new StringBuilder();
				String clazzLocalNameWithPrefix = domainURI.getLocalNameWithPrefix();
				
				getDomainDisplayLabelAndId(domainURI, domainId, domainDisplayLabel, clazzLocalNameWithPrefix, alignment, steinerTreeNodeIds);
				
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayDomainLabel.name(), domainDisplayLabel.toString());
				oj.put(SemanticTypesUpdate.JsonKeys.Domain.name(), domainId.toString());
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayLabel.name(), typeURI.getLocalNameWithPrefix());
				oj.put(SemanticTypesUpdate.JsonKeys.FullType.name(), label.split("\\|")[1]);
			} else {
				Label typeURI = ontMgr.getUriLabel(label);
				if(typeURI == null)
					continue;
				
				StringBuilder domainId = new StringBuilder();
				StringBuilder domainDisplayLabel = new StringBuilder();
				String clazzLocalNameWithPrefix = typeURI.getLocalNameWithPrefix();
				
				getDomainDisplayLabelAndId(typeURI, domainId, domainDisplayLabel, clazzLocalNameWithPrefix, alignment, steinerTreeNodeIds);
				oj.put(SemanticTypesUpdate.JsonKeys.FullType.name(), domainId.toString());
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayLabel.name(), domainDisplayLabel.toString());
				oj.put(SemanticTypesUpdate.JsonKeys.DisplayDomainLabel.name(), "");
				oj.put(SemanticTypesUpdate.JsonKeys.Domain.name(), "");
			}
			
			oj.put("Probability", scoreMap.get(label));
			arr.put(oj);
		}
		obj.put("Labels", arr);
		return obj;
	}
	
	private void getDomainDisplayLabelAndId(Label domainURI, StringBuilder domainId, StringBuilder domainDisplayLabel, 
			String clazzLocalNameWithPrefix, Alignment alignment, Set<String> steinerTreeNodeIds) {
		int graphLastIndex = alignment.getLastIndexOfNodeUri(domainURI.getUri());
		if (graphLastIndex == -1) { // No instance present in the graph
			domainDisplayLabel.append(clazzLocalNameWithPrefix + "1 (add)");
			domainId.append(domainURI.getUri());
		} else {
			// Check if already present in the steiner tree
			if (steinerTreeNodeIds.contains(domainURI.getUri() + (graphLastIndex))) {
				domainDisplayLabel.append(clazzLocalNameWithPrefix + (graphLastIndex));
				domainId.append(domainURI.getUri() + (graphLastIndex));
			} else {
				// Check if present in graph and not tree
				Node graphNode = alignment.getNodeById(domainURI.getUri() + (graphLastIndex));
				if (graphNode != null) {
					domainDisplayLabel.append(clazzLocalNameWithPrefix + (graphLastIndex) + " (add)");
					domainId.append(graphNode.getId());
				} else {
					domainDisplayLabel.append(clazzLocalNameWithPrefix + (graphLastIndex+1) + " (add)");
					domainId.append(domainURI.getUri());
				}
			}
		}
	}
}
