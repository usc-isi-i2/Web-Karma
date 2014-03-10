package edu.isi.karma.kr2rml.planning;

import java.util.List;

public interface TriplesMapGraphTreeifier {

	public List<String> treeify(TriplesMapGraph graph, RootStrategy rootStrategy);
	
}
