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

import java.util.HashSet;
import java.util.Set;

import edu.isi.karma.rep.metadata.TagsContainer.Color;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;

public class Tag {
	private HashSet<String> nodeIdList;
	private final TagName label;
	private final Color color;
	
	
	public Tag(TagName label, Color color) {
		super();
		this.label = label;
		this.color = color;
		nodeIdList = new HashSet<>();
	}
	
	public Color getColor() {
		return color;
	}

	public TagName getLabel() {
		return label;
	}

	public HashSet<String> getNodeIdList() {
		return nodeIdList;
	}

	public void addNodeId(String nodeId) {
		nodeIdList.add(nodeId);
	}
	
	public void addNodeIds(Set<String> nodes) {
		nodeIdList.addAll(nodes);
	}
	
	public void removeNodeId(String nodeId) {
		nodeIdList.add(nodeId);
	}
	
	public void removeNodeIds(Set<String> nodes) {
		nodeIdList.removeAll(nodes);
	}
}
