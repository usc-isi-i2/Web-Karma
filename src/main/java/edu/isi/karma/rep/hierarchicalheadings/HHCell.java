package edu.isi.karma.rep.hierarchicalheadings;

import java.util.Arrays;

import edu.isi.karma.view.Stroke;

public class HHCell {
	private TNode tNode;
	private int colspan;
	private String colorKey;
	private Stroke[] leftBorders;
	private Stroke[] rightBorders;
	private Stroke topBorder;
	private int depth;
	private int htmlTableColSpan;
	private boolean dummy = false;

	public TNode gettNode() {
		return tNode;
	}

	public void settNode(TNode tNode) {
		this.tNode = tNode;
	}

	public int getColspan() {
		return colspan;
	}

	public void setColspan(int colspan) {
		this.colspan = colspan;
	}

	public String getColorKey() {
		return colorKey;
	}

	public void setColorKey(String colorKey) {
		this.colorKey = colorKey;
	}

	@Override
	public String toString() {
		return "HHCell [colspan=" + colspan + ", colorKey=" + colorKey
				+ ", leftBorders=" + Arrays.toString(leftBorders)
				+ ", rightBorders=" + Arrays.toString(rightBorders)
				+ ", topBorder=" + topBorder + ", depth=" + depth
				+ ", htmlTableColSpan=" + htmlTableColSpan + "]";
	}

	public Stroke getTopBorder() {
		return topBorder;
	}

	public void setTopBorder(Stroke topBorder) {
		this.topBorder = topBorder;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public Stroke[] getLeftBorders() {
		return leftBorders;
	}

	public void setLeftBorders(Stroke[] leftBorders) {
		this.leftBorders = leftBorders;
	}

	public Stroke[] getRightBorders() {
		return rightBorders;
	}

	public void setRightBorders(Stroke[] rightBorders) {
		this.rightBorders = rightBorders;
	}

	public int getHtmlTableColSpan() {
		return htmlTableColSpan;
	}

	public void setHtmlTableColSpan(int htmlTableColSpan) {
		this.htmlTableColSpan = htmlTableColSpan;
	}

	public void setDummy(boolean dummy) {
		this.dummy = dummy;
	}

	public boolean hasTNode() {
		if (tNode != null)
			return true;
		else
			return false;
	}
	
	public boolean isDummy() {
		return dummy;
	}

	public boolean hasLeafTNode() {
		if (hasTNode()) {
			if (tNode.getChildren() != null && tNode.getChildren().size() != 0)
				return false;
			else
				return true;

		} else
			return false;
	}
}
