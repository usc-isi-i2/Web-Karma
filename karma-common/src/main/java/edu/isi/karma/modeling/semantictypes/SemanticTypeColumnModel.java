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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public class SemanticTypeColumnModel implements Jsonizable {

	private final HashMap<String, Double> scoreMap = new HashMap<>();

	public SemanticTypeColumnModel(List<SemanticTypeLabel> labels) {
		for (int i = 0; i < labels.size(); i++) {
			SemanticTypeLabel label = labels.get(i);
			scoreMap.put(label.getLabel(), (double)label.getScore());
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
		for (Map.Entry<String, Double> stringDoubleEntry : scoreMap.entrySet()) {
			writer.object();
			writer.key(SemanticTypesUpdate.JsonKeys.FullType.name()).value(stringDoubleEntry.getKey());
			writer.key("probability").value(stringDoubleEntry.getValue());
			writer.endObject();
		}
		writer.endArray();
		writer.endObject();
	}

	public JSONObject getAsJSONObject(String classUri, OntologyManager ontMgr, int maxLabels) throws JSONException {
		JSONArray arr = new JSONArray();
		
		// Need to sort
		HashMap<String, Double> sortedMap = Util.sortHashMap(scoreMap);
		for (String label : sortedMap.keySet()) {
			double probability = scoreMap.get(label);
			// Check if the type contains domain
			if(label.contains("|")){
				String domainURIStr = label.split("\\|")[0];
				String typeURIStr = label.split("\\|")[1];
				
				Label domainURI = ontMgr.getUriLabel(domainURIStr);
				Label typeURI = ontMgr.getUriLabel(typeURIStr);
				if(domainURI == null || typeURI == null)
					continue;
				
				if(domainURIStr.equals(classUri)) {
					String clazzLocalNameWithPrefix = domainURI.getDisplayName();
					if(domainURI.getPrefix() == null) 
						clazzLocalNameWithPrefix = domainURI.getUri() + "/" + domainURI.getLocalName();
					insertSemanticTypeSuggestion(arr, clazzLocalNameWithPrefix, domainURI.getRdfsLabel(), domainURIStr, domainURIStr, 
							typeURI.getDisplayName(), typeURI.getRdfsLabel(), typeURIStr, probability);
					if(arr.length() == maxLabels)
						break;
				}
			}
		}
		JSONObject obj = new JSONObject();
		obj.put("Labels", arr);
		return obj;
	}
	
	public JSONObject getAsJSONObject(OntologyManager ontMgr, Alignment alignment) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		
		/** Create a set of node ids of internal nodes of Steiner Tree **/
		Set<String> steinerTreeNodeIds = new HashSet<>();
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
			double probability = scoreMap.get(label);
			// Check if the type contains domain
			if(label.contains("|")){
				Label domainURI = ontMgr.getUriLabel(label.split("\\|")[0]);
				Label typeURI = ontMgr.getUriLabel(label.split("\\|")[1]);
				if(domainURI == null || typeURI == null)
					continue;
				
				String clazzLocalNameWithPrefix = domainURI.getDisplayName();
				if(domainURI.getPrefix() == null) 
					clazzLocalNameWithPrefix = domainURI.getUri() + "/" + domainURI.getLocalName();
					
				int graphLastIndex = alignment.getLastIndexOfNodeUri(domainURI.getUri());
				if (graphLastIndex == -1) { // No instance present in the graph
					insertSemanticTypeSuggestion(arr, clazzLocalNameWithPrefix + "1 (add)", 
							domainURI.getRdfsLabel(), domainURI.getUri(), domainURI.getUri() + "1",
							typeURI.getDisplayName(), typeURI.getRdfsLabel(), label.split("\\|")[1], probability);
				} else {
					boolean hasLastNodeFromSteinerTree = false;
					for (int i=1; i<= graphLastIndex; i++) {
						
						if (steinerTreeNodeIds.contains(domainURI.getUri() + i)) {
							insertSemanticTypeSuggestion(arr, clazzLocalNameWithPrefix + i, 
									domainURI.getRdfsLabel(),
									domainURI.getUri(),
									domainURI.getUri() + i, 
									typeURI.getDisplayName(),
									typeURI.getRdfsLabel(),
									label.split("\\|")[1], probability);
							if (i == graphLastIndex)
								hasLastNodeFromSteinerTree = true;
						} else {
							Node graphNode = alignment.getNodeById(domainURI.getUri() + i);
							if (graphNode != null)
								insertSemanticTypeSuggestion(arr, clazzLocalNameWithPrefix + i + " (add)", 
									graphNode.getLabel().getRdfsLabel(),
									graphNode.getUri(),
									graphNode.getId(), 
									typeURI.getDisplayName(), 
									typeURI.getRdfsLabel(),
									label.split("\\|")[1], probability);
						}
					}
					// Add an option to add one more node for the domain
					if (hasLastNodeFromSteinerTree)
						insertSemanticTypeSuggestion(arr, clazzLocalNameWithPrefix + (graphLastIndex+1) + " (add)", 
								domainURI.getRdfsLabel(),
								domainURI.getUri(), 
								domainURI.getUri() + (graphLastIndex+1),
								typeURI.getDisplayName(), 
								typeURI.getRdfsLabel(),
								label.split("\\|")[1], probability);
				}
			} else {
				Label typeURI = ontMgr.getUriLabel(label);
				if(typeURI == null)
					continue;
				
				String clazzLocalNameWithPrefix = typeURI.getDisplayName();
				
				int graphLastIndex = alignment.getLastIndexOfNodeUri(typeURI.getUri());
				if (graphLastIndex == -1) { // No instance present in the graph
					insertSemanticTypeSuggestion(arr, "", "", "", clazzLocalNameWithPrefix + "1 (add)", 
							typeURI.getRdfsLabel(),
							typeURI.getUri(), typeURI.getUri() + "1", probability);
				} else {
					boolean hasLastNodeFromSteinerTree = false;
					for (int i=1; i<= graphLastIndex; i++) {
						if (steinerTreeNodeIds.contains(typeURI.getUri() + (graphLastIndex))) {
							insertSemanticTypeSuggestion(arr, "", "", "", clazzLocalNameWithPrefix + i, 
									typeURI.getRdfsLabel(),
									typeURI.getUri(), typeURI.getUri() + i, probability);
						} else {
							Node graphNode = alignment.getNodeById(typeURI.getUri() + i);
							if (graphNode != null)
								insertSemanticTypeSuggestion(arr, "", "", "", clazzLocalNameWithPrefix + i + " (add)", 
										graphNode.getLabel().getRdfsLabel(),
										graphNode.getUri(), graphNode.getId(), probability);
						}
					}
					// Add an option to add one more node for the domain
					if (hasLastNodeFromSteinerTree)
						insertSemanticTypeSuggestion(arr, "", "", "", clazzLocalNameWithPrefix + (graphLastIndex+1) + " (add)", 
								typeURI.getRdfsLabel(),
								typeURI.getUri(), typeURI.getUri() + (graphLastIndex+1), probability);
				}
			}
		}
		obj.put("Labels", arr);
		return obj;
	}
	
	private void insertSemanticTypeSuggestion(JSONArray arr, 
			String domainDisplayLabel, String domainRDFSLabel, String domainUri, String domainId, 
			String typeDisplayLabel, String typeRDFSLabel, String type, 
			double probability) throws JSONException {
		JSONObject typeObj = new JSONObject();
		typeObj.put(SemanticTypesUpdate.JsonKeys.DisplayDomainLabel.name(), domainDisplayLabel);
		typeObj.put(SemanticTypesUpdate.JsonKeys.DomainRDFSLabel.name(), domainRDFSLabel);
		typeObj.put(SemanticTypesUpdate.JsonKeys.DomainUri.name(), domainUri);
		typeObj.put(SemanticTypesUpdate.JsonKeys.DomainId.name(), domainId);
		typeObj.put(SemanticTypesUpdate.JsonKeys.DisplayLabel.name(), typeDisplayLabel);
		typeObj.put(SemanticTypesUpdate.JsonKeys.DisplayRDFSLabel.name(), typeRDFSLabel);
		typeObj.put(SemanticTypesUpdate.JsonKeys.FullType.name(), type);
		typeObj.put("Probability", probability);
		arr.put(typeObj);
	}

}
