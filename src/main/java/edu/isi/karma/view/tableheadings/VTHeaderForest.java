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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.hierarchicalheadings.TForest;
import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class VTHeaderForest implements TForest {
	private List<TNode> roots = new ArrayList<TNode>();
	private Map<String, VTHNode> nodeMap = new HashMap<String, VTHNode>();

	@Override
	public List<TNode> getRoots() {
		return roots;
	}

	public void constructFromHNodePaths(List<HNodePath> paths) {
		for (HNodePath path : paths) {

			// Add the root
			HNode root = path.getFirst();
			String rootId = root.getId();
			VTHNode rootNode;
			if (nodeMap.containsKey(rootId)) {
				rootNode = nodeMap.get(rootId);

			} else {
				rootNode = new VTHNode(rootId, root.getColumnName());
				nodeMap.put(rootId, rootNode);
			}
			if (!roots.contains(rootNode))
				roots.add(rootNode);

			// Add the children
			HNodePath rest = path.getRest();
			VTHNode parentNode = rootNode;
			while (!rest.isEmpty()) {
				HNode child = rest.getFirst();
				String childId = child.getId();
				
				VTHNode childNode;
				if (nodeMap.containsKey(childId)) {
					childNode = nodeMap.get(childId);
				} else {
					childNode = new VTHNode(childId, child.getColumnName());
					nodeMap.put(childId, childNode);
				}
				parentNode.addChild(childNode);
				rest = rest.getRest();
				parentNode = childNode;
			}
		}
	}

}
