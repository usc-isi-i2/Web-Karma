package edu.isi.karma.rep.alignment;

import java.util.Comparator;

public class NodeSupportingModelsComparator implements Comparator<Node> {

	@Override
	public int compare(Node n1, Node n2) {
		if ((n1.getModelIds() == null || n1.getModelIds().isEmpty()) &&
				(n2.getModelIds() == null || n2.getModelIds().isEmpty()))
			return 0;
		else if (n1.getModelIds() == null || n1.getModelIds().isEmpty())
			return 1;
		else if (n2.getModelIds() == null || n2.getModelIds().isEmpty())
			return -1;
		else {
			return Integer.compare(n2.getModelIds().size(), n1.getModelIds().size());
		}
	}

}
