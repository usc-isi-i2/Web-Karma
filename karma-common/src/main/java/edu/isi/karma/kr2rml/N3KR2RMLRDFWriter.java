/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.kr2rml;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class N3KR2RMLRDFWriter implements KR2RMLRDFWriter {

	private static final Logger LOG = LoggerFactory.getLogger(N3KR2RMLRDFWriter.class);
	protected URIFormatter uriFormatter;
	protected PrintWriter outWriter;
	protected Map<String,String> generatedTriples;
	
	public N3KR2RMLRDFWriter(URIFormatter uriFormatter, OutputStream outputStream)
	{
		this.outWriter = new PrintWriter(outputStream);
		this.uriFormatter = uriFormatter;
		generatedTriples = new ConcurrentHashMap<String, String>();
	}
	public N3KR2RMLRDFWriter(URIFormatter uriFormatter, PrintWriter writer)
	{
		this.outWriter =writer;
		this.uriFormatter = uriFormatter;
		generatedTriples = new ConcurrentHashMap<String, String>();
	}
	
	private void outputTriple(String triple)
	{
		generatedTriples.put(triple, "");
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
		for(String value : generatedTriples.keySet())
		{
			outWriter.println(value);
		}
		outWriter.println("");
		generatedTriples = new ConcurrentHashMap<String, String>();
	}
	@Override
	public void flush() {
		LOG.debug("Flushing writer");
		for(String value : generatedTriples.keySet())
		{
			outWriter.println(value);
		}
		outWriter.flush();
		LOG.debug("Flushed writer");
		
	}
	@Override
	public void close() {
		outWriter.close();
		
	}

}
