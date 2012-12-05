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

package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.URI;
import edu.isi.karma.modeling.ontology.OntologyTreeNode;
import edu.isi.karma.view.VWorkspace;

public class OntologyHierarchyUpdate extends AbstractUpdate {
	private OntologyTreeNode rootNode;
	private String updateType;
	
	private static Logger logger = LoggerFactory.getLogger(OntologyHierarchyUpdate.class.getSimpleName());
	
	private enum JsonKeys {
		data, URI, metadata, children
	}
	
	public OntologyHierarchyUpdate(OntologyTreeNode rootNode, String updateType) {
		this.rootNode = rootNode;
		this.updateType = updateType;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		List<OntologyTreeNode> rootNodes = rootNode.getChildren();
		
		try {
			JSONArray dataArray = new JSONArray();
			for (OntologyTreeNode node: rootNodes) {
				JSONObject resourceObject = new JSONObject();
				
				if (node.hasChildren()) {
					// Add the children
					JSONArray childrenArray = new JSONArray();
					addChildren(node, childrenArray);
					resourceObject.put(JsonKeys.children.name(), childrenArray);
				}
				
				// Add the data
				URI resourceUri = node.getUri();
				resourceObject.put(JsonKeys.data.name(), resourceUri.getLocalNameWithPrefixIfAvailable());
				JSONObject metadataObject = new JSONObject();
				metadataObject.put(JsonKeys.URI.name(), resourceUri.getUriString());
				resourceObject.put(JsonKeys.metadata.name(), metadataObject);
				dataArray.put(resourceObject);
			}
			
			// Prepare the output JSON
			JSONObject outputObject = new JSONObject();
			outputObject.put(GenericJsonKeys.updateType.name(), updateType);
			outputObject.put(JsonKeys.data.name(), dataArray);
			pw.println(outputObject.toString());
		} catch (JSONException e) {
			logger.error("Error occured while creating ontology hierarchy JSON!", e);
		}
	}

	private void addChildren(OntologyTreeNode parentNode, JSONArray childrenArray) throws JSONException {
		List<OntologyTreeNode> children = parentNode.getChildren();
		for (OntologyTreeNode childNode : children) {
			JSONObject resourceObject = new JSONObject();
			
			if (childNode.hasChildren()) {
				// Add the children
				JSONArray childrenArraySubProp = new JSONArray();
				addChildren(childNode, childrenArraySubProp);
				resourceObject.put(JsonKeys.children.name(), childrenArraySubProp);
			}
			
			// Add the data
			URI resourceUri = childNode.getUri();
			resourceObject.put(JsonKeys.data.name(), resourceUri.getLocalNameWithPrefixIfAvailable());
			JSONObject metadataObject = new JSONObject();
			metadataObject.put(JsonKeys.URI.name(), resourceUri.getUriString());
			resourceObject.put(JsonKeys.metadata.name(), metadataObject);
			childrenArray.put(resourceObject);
		}
	}
}
