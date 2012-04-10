package edu.isi.karma.rep.hierarchicalheadings;

import java.util.HashMap;

public class ColspanMap {
	final private HashMap<HHTNode, Span> spanMap = new HashMap<HHTNode, Span>();

	public HashMap<HHTNode, Span> getSpanMap() {
		return spanMap;
	}

	public ColspanMap(HHTree tree) {
		for (HHTNode root : tree.getRootNodes()) {
			populateColSpan(root);
		}
	}

	private void populateColSpan(HHTNode node) {
		Span span = new Span(node.getStartCol(), node.getEndCol());
		spanMap.put(node, span);

		if (!node.isLeaf()) {
			for (HHTNode child : node.getChildren())
				populateColSpan(child);
		}
	}
	
	public Span getSpan(HHTNode node) {
		return spanMap.get(node);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Colspan Map: \n");
		for (HHTNode node : spanMap.keySet()) {
			str.append("Node: " + node.gettNode().getId() + ", "
					+ spanMap.get(node) + "\n");
		}
		return str.toString();
	}
}
