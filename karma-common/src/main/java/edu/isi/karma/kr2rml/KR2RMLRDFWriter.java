package edu.isi.karma.kr2rml;

public interface KR2RMLRDFWriter {


	void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri);
	
	void outputTripleWithURIObject(String subjTriplesMapId, String subjUri, String predicateObjectMapId, String predicateUri,
			String objectUri);
	
	void outputTripleWithURIObject(String subjTriplesMapId, String subjUri, String predicateObjectMapId, String predicateUri,
			String objTriplesMapId, String objectUri);
	
	void outputTripleWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType);

	void outputTripleWithLiteralObject(String subjTriplesMapId, String subjUri, String predicateObjectMapId, String predicateUri,
			String value, String literalType);
	
	void outputQuadWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String graph);
	
	void outputQuadWithLiteralObject(String subjTriplesMapId, String subjUri, String predicateObjectMapId, String predicateUri,
			String value, String literalType, String graph);
	
	void finishRow();
	void flush();
	void close();
}
