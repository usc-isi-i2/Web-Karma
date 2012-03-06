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
/**
 * 
 */
package edu.isi.karma.view.tableheadings;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.cells;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

;

/**
 * Contains the levels of headings, except for the root of the tree.
 * 
 * @author szekely
 * 
 */
public class VHTreeNodeLevel {

	private final List<VHTreeNode> elements = new LinkedList<VHTreeNode>();
	private final int depth;

	VHTreeNodeLevel(int depth) {
		super();
		this.depth = depth;
	}

	VHTreeNodeLevel(VHTreeNode root) {
		super();
		this.elements.add(root);
		this.depth = 0;
	}

	int getDepth() {
		return depth;
	}

	boolean isFinalLevel() {
		for (VHTreeNode n : elements) {
			if (n.hasChildren()) {
				return false;
			}
		}
		return true;
	}

	VHTreeNodeLevel getNextLevel() {
		VHTreeNodeLevel nextLevel = new VHTreeNodeLevel(depth + 1);
		for (VHTreeNode n : elements) {
			if (n.hasChildren()) {
				nextLevel.elements.addAll(n.getChildren());
			} else {
				nextLevel.elements.add(n);
			}
		}
		return nextLevel;
	}

	void generateJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		jw.object()//
				.key(cells.name()).array();
		for (VHTreeNode n : elements) {
			n.generateJson(jw, depth, vWorksheet, vWorkspace);
		}
		jw.endArray();
		jw.endObject();
	}
}
