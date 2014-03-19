package edu.isi.karma.kr2rml;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

public class N3KR2RMLRDFWriter implements KR2RMLRDFWriter {

	protected URIFormatter uriFormatter;
	protected PrintWriter outWriter;
	protected Set<String> generatedTriples;
	
	public N3KR2RMLRDFWriter(URIFormatter uriFormatter, OutputStream outputStream)
	{
		this.outWriter = new PrintWriter(outputStream);
		this.uriFormatter = uriFormatter;
		generatedTriples = new HashSet<String>();
	}
	public N3KR2RMLRDFWriter(URIFormatter uriFormatter, PrintWriter writer)
	{
		this.outWriter =writer;
		this.uriFormatter = uriFormatter;
		generatedTriples = new HashSet<String>();
	}
	
	private void outputTriple(String triple)
	{
		if(generatedTriples.add(triple))
		{
			outWriter.println(triple);
		}
	}
	@Override
	public void outputTripleWithURIObject(String subjUri, String predicateUri, String objectUri)
	{
		outputTriple(constructTripleWithURIObject(subjUri, predicateUri, objectUri));
		
	}
	
	private String constructTripleWithURIObject(String subjUri, String predicateUri, String objectUri) {
		return subjUri + " " 
				+ uriFormatter.getExpandedAndNormalizedUri(predicateUri) + " " 
				+ objectUri + " .";
	}
	
	@Override
	public void outputTripleWithLiteralObject(String subjUri, String predicateUri, String value, 
			String literalType) {
		outputTriple(constructTripleWithLiteralObject(subjUri, predicateUri, value, literalType));
	}
	
	private String constructTripleWithLiteralObject(String subjUri, String predicateUri, String value, 
			String literalType) {
		// Use Apache Commons to escape the value
		value = StringEscapeUtils.escapeJava(value);
		
		// Add the RDF literal type to the literal if present
		if (literalType != null && !literalType.equals("")) {
			return subjUri + " " + uriFormatter.getExpandedAndNormalizedUri(predicateUri) + " \"" + value + 
					"\"" + "^^<" + literalType + "> .";
		}
		return subjUri + " " + uriFormatter.getExpandedAndNormalizedUri(predicateUri) + " \"" + value + "\" .";
	}
	
	@Override
	public void outputQuadWithLiteralObject(String subjUri, String predicateUri, 
			String value, String literalType, String graph) {
		outputTriple(constructQuadWithLiteralObject(subjUri, predicateUri, value, literalType, graph));
	}
	private String constructQuadWithLiteralObject(String subjUri, String predicateUri, 
			String value, String literalType, String graph) {
		String triple = constructTripleWithLiteralObject(subjUri, predicateUri, value, literalType);
		if (triple.length() > 2)
			return triple.substring(0, triple.length()-1) + "<" + graph + "> ." ;
		else
			return "";
	}
	
	@Override
	public void finishRow()
	{
		outWriter.println("");
		generatedTriples = new HashSet<String>();
	}
	@Override
	public void flush() {
		outWriter.flush();
		
	}
	@Override
	public void close() {
		outWriter.close();
		
	}

}
