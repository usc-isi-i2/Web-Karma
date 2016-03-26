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

package edu.isi.karma.modeling.alignment;

import java.util.HashMap;

public class NodeIdFactory implements Cloneable {

	private HashMap<String, Integer> nodeUris = new HashMap<>();
	

	public String getNodeId(String uri) {
		
		int index;
		String id;
		
		if (nodeUris.containsKey(uri)) {
			
			index = nodeUris.get(uri).intValue();
			nodeUris.put(uri, ++index);
			id = uri + "" + index;
			
		} else {
			index = 1;
			nodeUris.put(uri, index);
			id = uri + "" + index;
//			id = uriString;
		}
		return id;
	}
	
	public void addNodeId(String id, String uri) {
		String newIndexStr = id.replace(uri, "");
		int newIndex = -1;
		try {
			newIndex = Integer.parseInt(newIndexStr);
		} catch (Exception e) {
			return;
		}
		int index;
		if (nodeUris.containsKey(uri)) {
			index = nodeUris.get(uri).intValue();
			if (newIndex > index)
				nodeUris.put(uri, newIndex);			
		} else {
			nodeUris.put(uri, newIndex);
		}		
	}

	public boolean duplicateUri(String uriString) {
		return this.nodeUris.containsKey(uriString);
	}
	
	public int lastIndexOf(String uri) {
		if (nodeUris.containsKey(uri))
			return nodeUris.get(uri).intValue();
		else
			return -1;
	}
	
	public NodeIdFactory clone() {
		NodeIdFactory clone = new NodeIdFactory();
		clone.nodeUris.putAll(this.nodeUris);
		return clone;
	}
}
