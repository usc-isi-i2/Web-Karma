package edu.isi.karma.modeling.alignment;

import java.util.Comparator;

public class VertexComparatorByID implements Comparator<Vertex> {

	public int compare(Vertex o1, Vertex o2) {
		return o1.getID().compareTo(o2.getID());
	}

}
