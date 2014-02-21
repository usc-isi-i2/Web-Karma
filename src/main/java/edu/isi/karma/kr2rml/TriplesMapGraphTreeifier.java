package edu.isi.karma.kr2rml;

import java.util.List;

public interface TriplesMapGraphTreeifier {

	public List<String> treeify(TriplesMapGraph graph, RootStrategy rootStrategy);
	
}
