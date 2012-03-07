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
package edu.isi.karma.rep.hierarchicalheadings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;

public class HHTree {
	private List<HHTNode> rootNodes = new ArrayList<HHTNode>();
	private int colIndex;
	private int maxDepth;

	public static int getFrontier(Map<String, Integer> map) {
		return 0;

	}

	public void constructHHTree(TForest forest) {
		/*** Create HHTNode for each TNode with same set of children ***/
		populateHHTreeFromRootTNodes(forest.getRoots());

		/*** Calculate the startCol and endCol for each HHTNode ***/
		calculateStartColAndEndCol();

		/*** Calculate depth for each HHTNode ***/
		calculateDepth();

		/*** Calculate left and right border ***/
		calculateLeftAndRightStroke(rootNodes);

		/*** Calculate the complete list of left and right strokes ***/
		calculateLeftAndRightStrokes();

		// Debug
		// printHHTree();
	}
	
	public int countRootNodes() {
		return colIndex;
	}

	private void calculateLeftAndRightStrokes() {
		for(HHTNode root:rootNodes){
			// Add the left and right stroke to the list
			root.getLeftStrokes().add(root.getLeftStroke());
			root.getRightStrokes().add(root.getRightStroke());
			
			// Add the stroke to children (if any)
			if(!root.isLeaf())
				calculateLeftAndRightStrokes(root);
		}
	}

	private void calculateLeftAndRightStrokes(HHTNode node) {
		List<HHTNode> children = node.getChildren();
		List<Stroke> leftStrokes = node.getLeftStrokes();
		List<Stroke> rightStrokes = node.getRightStrokes();
		
		for(int i = 0; i<children.size();i++){
			HHTNode child = children.get(i);
			// Add the stroke of its own to the list
			child.getLeftStrokes().add(child.getLeftStroke());
			child.getRightStrokes().add(child.getRightStroke());
			
			// If its the first child, add the parents left stroke
			if(i == 0) {
				child.getLeftStrokes().addAll(leftStrokes);
				// If its the only child, also add the right stroke
				if(children.size() == 1) {
					child.getRightStrokes().addAll(rightStrokes);
				}
			} 
			// If its the last child, add the right stroke
			else if (i == children.size()-1) {
				child.getRightStrokes().addAll(rightStrokes);
			}

			if(!child.isLeaf()) {
				calculateLeftAndRightStrokes(child);
			}
		}
	}

	private void calculateLeftAndRightStroke(List<HHTNode> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			HHTNode child = nodes.get(i);
			String id = child.gettNode().getId();
			int depth = child.getDepth();

			// If its the first child
			if (i == 0) {
				Stroke nodeOuterStroke = new Stroke(StrokeStyle.outer, id,
						depth);
				child.setLeftStroke(nodeOuterStroke);

				// If its the only child
				if (nodes.size() == 1) {
					child.setRightStroke(nodeOuterStroke);
				} else {
					Stroke nodeNoneStroke = new Stroke(StrokeStyle.none, id,
							depth);
					child.setRightStroke(nodeNoneStroke);
				}
			} else {
				Stroke nodeInnerStroke = new Stroke(StrokeStyle.inner, id,
						depth);
				child.setLeftStroke(nodeInnerStroke);
				// If its the last child in the list
				if (i == nodes.size() - 1) {
					Stroke nodeOuterStroke = new Stroke(StrokeStyle.outer, id,
							depth);
					child.setRightStroke(nodeOuterStroke);
				} else {
					Stroke nodeNoneStroke = new Stroke(StrokeStyle.none, id,
							depth);
					child.setRightStroke(nodeNoneStroke);
				}
			}
			if (!child.isLeaf()) {
				calculateLeftAndRightStroke(child.getChildren());
			}
		}
	}

	private void calculateDepth() {
		for (HHTNode root : rootNodes) {
			calculateDepth(root, 0);
		}
	}

	private void calculateDepth(HHTNode node, int depth) {
		node.setDepth(depth);
		// Keep account of the maximum depth in the HHTree
		if(depth > maxDepth)
			maxDepth = depth;
		
		if (!node.isLeaf()) {
			for (HHTNode child : node.getChildren()) {
				calculateDepth(child, depth + 1);
			}
		}
	}

	private void populateHHTreeFromRootTNodes(List<TNode> roots) {
		for (TNode root : roots) {
			HHTNode rootNode = new HHTNode(root);
			if (!isTNodeLeaf(root)) {
				populateChildren(root, rootNode);
			}
			rootNodes.add(rootNode);
		}
	}

	private void calculateStartColAndEndCol() {
		for (HHTNode root : rootNodes) {
			calculateStartColAndEndCol(root);
		}
	}

	private void calculateStartColAndEndCol(HHTNode node) {
		if (node.isLeaf()) {
			node.setStartCol(colIndex);
			node.setEndCol(colIndex++);
		} else {
			List<HHTNode> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				HHTNode child = children.get(i);
				calculateStartColAndEndCol(child);
			}
			node.setStartCol(children.get(0).getStartCol());
			node.setEndCol(children.get(children.size() - 1).getEndCol());
		}
	}

//	private void printHHTree() {
//		for (HHTNode root : rootNodes) {
//			root.prettyprint("");
//		}
//	}

	private void populateChildren(TNode node, HHTNode rootNode) {
		List<TNode> children = node.getChildren();
		List<HHTNode> childrenList = new ArrayList<HHTNode>();

		for (TNode child : children) {
			HHTNode childNode = new HHTNode(child);
			childrenList.add(childNode);
			if (!isTNodeLeaf(child))
				populateChildren(child, childNode);
		}
		rootNode.setChildren(childrenList);
	}

	private boolean isTNodeLeaf(TNode node) {
		if (node.getChildren() == null || node.getChildren().size() == 0)
			return true;
		else
			return false;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public List<HHTNode> getRootNodes() {
		return rootNodes;
	}
}
