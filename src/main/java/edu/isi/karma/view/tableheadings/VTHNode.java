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
package edu.isi.karma.view.tableheadings;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class VTHNode implements TNode {
	private final String id;
	private final String label;
	List<TNode> children;
	
	private enum JsonKeys {
		id, label
	}

	public VTHNode (String id, String label) {
		this.id = id;
		this.label = label;
		children = new ArrayList<TNode>();
	}
	
	public List<TNode> getChildren() {
		return children;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void generateContentJson(PrintWriter pw) {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(), label);
			pw.print(obj.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getColorKey() {
		return null;
	}

	public JSONObject generateJsonObject() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put(JsonKeys.id.name(), id);
			obj.put(JsonKeys.label.name(), label);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	void addChild(VTHNode childNode) {
		if(!children.contains(childNode))
			children.add(childNode);
	}
}
