package edu.isi.karma.kr2rml.planning;

import java.util.List;

public interface TriplesMapGraphDAGifier {

	public List<String> dagify(TriplesMapGraph graph, RootStrategy rootStrategy);
	
}
