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

package edu.isi.karma.cleaning.Research;

import java.util.Vector;

//used in alignment
public class ANode {
	public Vector<Integer> orgPos = new Vector<Integer>();
	public Vector<Integer> tarPos = new Vector<Integer>();
	public Vector<Integer> length = new Vector<Integer>();
	public Vector<String[]> exps = new Vector<String[]>();
	public Vector<ANode> children = new Vector<ANode>();

	public ANode(Vector<Integer> orgPos, Vector<Integer> tarPos,
			Vector<Integer> length, Vector<String[]> exps) {
		this.orgPos = orgPos;
		this.tarPos = tarPos;
		this.length = length;
		this.exps = exps;
	}

	public void addChild(ANode a) {
		this.children.add(a);
	}

	// check whether current node valid based on
	// all copy node
	// all constant node and same content
	public boolean isvalid() {
		if (orgPos.size() <= 1)
			return true;
		for (int i = 1; i < orgPos.size(); i++) {
			int value = orgPos.get(i) * orgPos.get(i - 1);
			if (value < 0)
				return false;
			if (orgPos.get(i) < 0) {
				String sub = this.exps.get(i)[1].substring(tarPos.get(i),
						tarPos.get(i) + length.get(i));
				String presub = this.exps.get(i - 1)[1].substring(
						tarPos.get(i - 1),
						tarPos.get(i - 1) + length.get(i - 1));
				if (sub.compareTo(presub) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public void reorderChildren() {
		// reorder the children to accelerate the search.
	}
}
