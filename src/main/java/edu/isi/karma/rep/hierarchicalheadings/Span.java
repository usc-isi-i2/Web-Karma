package edu.isi.karma.rep.hierarchicalheadings;

public class Span {
	private int startIndex;
	private int endIndex;

	public Span(int startCol, int endCol) {
		this.startIndex = startCol;
		this.endIndex = endCol;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	@Override
	public String toString() {
		return "Span [startIndex=" + startIndex + ", endIndex=" + endIndex
				+ "]";
	}
}
