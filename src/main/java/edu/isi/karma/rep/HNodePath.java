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
package edu.isi.karma.rep;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author szekely
 * 
 */
public class HNodePath {

	private List<HNode> hNodes = new LinkedList<HNode>();

	public HNodePath() {
	}

	public HNodePath(HNode hNode) {
		hNodes.add(hNode);
	}

	public HNodePath(List<HNode> hNodes) {
		this.hNodes = hNodes;
	}

	public HNodePath(HNodePath path) {
		for (HNode hn : path.hNodes) {
			this.hNodes.add(hn);
		}
	}

	/** 
	 * Adds an HNode to this path.
	 * @param n
	 * @author mariam
	 */
	public void addHNode(HNode n){
		hNodes.add(n);
	}
	
	/** 
	 * Adds an HNodePath to this path.
	 * @param np
	 * @author mariam
	 */
	public void addHNodePath(HNodePath np){
		hNodes.addAll(np.hNodes);
	}

	public boolean isEmpty() {
		return hNodes.isEmpty();
	}
	
	public HNode getFirst() {
		return hNodes.get(0);
	}

	public HNode getLeaf() {
		return hNodes.get(hNodes.size() - 1);
	}

	public HNodePath getRest() {
		return new HNodePath(hNodes.subList(1, hNodes.size()));
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		Iterator<HNode> it = hNodes.iterator();
		while (it.hasNext()) {
			b.append(it.next().getId());
			if (it.hasNext()){
				b.append("/");
			}
		}
		return b.toString();
	}

	//mariam
	/**
	 * Returns the HNodePath as a String containing the column name of the child node at the end.
	 * @return
	 * 		the HNodePath as a String containing the column name of the child node at the end.
	 * Example:
	 * HN1/HN2/ColumnName
	 */
	public String addColumnName() {
		String path = toString() + "/";
		//get the last node
		HNode n = hNodes.get(hNodes.size()-1);
		path += n.getColumnName();
		return path;
	}
	
	/**
	 * Returns the HNodePath as a String containing column names instead of HnodeIds
	 * @return
	 * 		the HNodePath as a String containing column names instead of HnodeIds.
	 * Example:
	 * ColumnName1/ColumnName2
	 */
	public String toColumnNamePath() {
		StringBuffer b = new StringBuffer();
		Iterator<HNode> it = hNodes.iterator();
		while (it.hasNext()) {
			b.append(it.next().getColumnName());
			if (it.hasNext()){
				b.append("/");
			}
		}
		return b.toString();
	}

	public static HNodePath concatenate(HNodePath prefix, HNodePath suffix) {
		HNodePath result = new HNodePath(prefix);
		for (HNode hn : suffix.hNodes){
			result.hNodes.add(hn);
		}
		return result;
	}
}
