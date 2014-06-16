package edu.isi.karma.kr2rml;

import edu.isi.karma.kr2rml.planning.TriplesMap;

public interface KR2RMLRDFWriter {


	void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri);
	
	void outputTripleWithURIObject(TriplesMap subjTriplesMap, String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri,
			String objectUri);
	
	void outputTripleWithURIObject(TriplesMap subjTriplesMap, String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri,
			TriplesMap objTriplesMap, String objectUri);
	
	void outputTripleWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType);

	void outputTripleWithLiteralObject(TriplesMap subjTriplesMap, String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri,
			String value, String literalType);
	
	void outputQuadWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String graph);
	
	void outputQuadWithLiteralObject(TriplesMap subjTriplesMap, String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri,
			String value, String literalType, String graph);
	
	void finishRow();
	void flush();
	void close();
}
