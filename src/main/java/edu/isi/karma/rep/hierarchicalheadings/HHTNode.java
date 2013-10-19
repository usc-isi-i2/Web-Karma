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

import edu.isi.karma.view.Stroke;

public class HHTNode {
	private int startCol;
	private int endCol;
	private int depth;

	private Stroke leftStroke;
	private Stroke rightStroke;

	private List<Stroke> leftStrokes = new ArrayList<Stroke>();
	private List<Stroke> rightStrokes = new ArrayList<Stroke>();

	private List<HHTNode> children;
	
	private final TNode tNode;
	
	private int htmlColSpan;

	public HHTNode(TNode tNode) {
		super();
		this.tNode = tNode;
	}

	public boolean isLeaf() {
		if(children == null || children.size() == 0)
			return true;
		else
			return false;
	}

	public int getStartCol() {
		return startCol;
	}

	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}

	public int getEndCol() {
		return endCol;
	}

	public void setEndCol(int endCol) {
		this.endCol = endCol;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public Stroke getLeftStroke() {
		return leftStroke;
	}

	public void setLeftStroke(Stroke leftStroke) {
		this.leftStroke = leftStroke;
	}

	public Stroke getRightStroke() {
		return rightStroke;
	}

	public void setRightStroke(Stroke rightStroke) {
		this.rightStroke = rightStroke;
	}

	public List<Stroke> getLeftStrokes() {
		return leftStrokes;
	}

	public void setLeftStrokes(List<Stroke> leftStrokes) {
		this.leftStrokes = leftStrokes;
	}

	public List<Stroke> getRightStrokes() {
		return rightStrokes;
	}

	public void setRightStrokes(List<Stroke> rightStrokes) {
		this.rightStrokes = rightStrokes;
	}

	public List<HHTNode> getChildren() {
		return children;
	}

	public void setChildren(List<HHTNode> children) {
		this.children = children;
	}

	public TNode gettNode() {
		return tNode;
	}
	
	public int getHTMLColSpan() {
//		int corr = 0;
//		if(isLeaf())
//			corr = 1;
//		else {
//			int span = 0;
//			for(HHTNode child:children){
//				span += child.getHTMLColSpan();
//				if(!child.isLeaf())
//					span += 2;
//			}
//			corr = span;
////			return span;
//		}
//		System.out.println("Correct HTML Col span: " + corr);
//		System.out.println("Coordinate based: " + htmlColSpan);
		//return corr;
		return htmlColSpan;
	}
	
	public int getHtmlColSpan() {
		return htmlColSpan;
	}

	public void setHtmlColSpan(int htmlColSpan) {
		this.htmlColSpan = htmlColSpan;
	}

	public void prettyprint(String prefix) {
		System.out.print(prefix + "HHTNode [startCol=" + startCol + ", endCol=" + endCol
				+ ", depth=" + depth + ", leftStroke=" + leftStroke
				+ ", rightStroke=" + rightStroke + ", leftStrokes="
				+ leftStrokes + ", rightStrokes=" + rightStrokes
				+ ", tNode=" + tNode.getId() + "]");
		
		if(!isLeaf()) {
			System.out.println(" Children: ");
			for(HHTNode node: children) {
				node.prettyprint(prefix +"    ");
			}
		} else
			System.out.println();
	}
}
