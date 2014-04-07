package edu.isi.karma.kr2rml;

public interface KR2RMLRDFWriter {


	void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri);

	void outputTripleWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType);

	void outputQuadWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String graph);
	
	void finishRow();
	void flush();
	void close();
}
