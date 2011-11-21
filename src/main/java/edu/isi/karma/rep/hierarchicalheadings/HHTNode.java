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
		if(isLeaf())
			return 1;
		else {
			int span = 0;
			for(HHTNode child:children){
				span += child.getHTMLColSpan();
				if(!child.isLeaf())
					span += 2;
			}
			return span;
		}
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
