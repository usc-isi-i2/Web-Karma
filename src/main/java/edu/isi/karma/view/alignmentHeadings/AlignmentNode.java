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
package edu.isi.karma.view.alignmentHeadings;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class AlignmentNode implements TNode {
	private final String id;
	private List<TNode> children;
	private AlignmentLink parentLink;
	private final String label;
	private SemanticType semanticType;

	private enum JsonKeys {
		id, parentLinkLabel, parentLinkId, label
	}

	public AlignmentNode(String id, List<TNode> children,
			AlignmentLink parentLink, String label, SemanticType semanticType) {
		super();
		this.id = id;
		this.children = children;
		this.parentLink = parentLink;
		this.label = label;
		this.semanticType = semanticType;
	}

	public SemanticType getType() {
		return semanticType;
	}

	public List<TNode> getChildren() {
		return children;
	}

	public String getId() {
		return id;
	}
	
	public boolean hasSemanticType() {
		if(semanticType != null)
			return true;
		return false;
	}
	
	public boolean hasChildren() {
		if(children != null && children.size() != 0)
			return true;
		return false;
	}
	
	public String getSemanticTypeHNodeId() {
		if(semanticType != null) {
			return semanticType.getHNodeId();
		} else
			return null;
	}

	public void generateContentJson(PrintWriter pw) {
		JSONObject obj = new JSONObject();

		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(),
					SemanticTypeUtil.removeNamespace(label));
			obj.put(JsonKeys.parentLinkId.name(), parentLink.getId());
			obj.put(JsonKeys.parentLinkLabel.name(),
					SemanticTypeUtil.removeNamespace(parentLink.getLabel()));

			pw.println(obj.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONObject generateJsonObject() {
		JSONObject obj = new JSONObject();

		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(),
					SemanticTypeUtil.removeNamespace(label));
			if (parentLink != null) {
				obj.put(JsonKeys.parentLinkId.name(), parentLink.getId());
				obj.put(JsonKeys.parentLinkLabel.name(),
						SemanticTypeUtil.removeNamespace(parentLink.getLabel()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public String getColorKey() {
		return null;
	}

}
