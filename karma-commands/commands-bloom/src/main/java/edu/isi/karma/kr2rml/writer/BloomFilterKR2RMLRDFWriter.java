package edu.isi.karma.kr2rml.writer;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.modeling.Uris;


public class BloomFilterKR2RMLRDFWriter extends KR2RMLRDFWriter {

	protected KR2RMLBloomFilterManager bloomFilterManager;
	protected PrintWriter output;
	protected boolean isRDF;
	private final static String formattedTypeURI = "<" + Uris.RDF_TYPE_URI +">";
	private String baseURI;
	public BloomFilterKR2RMLRDFWriter(PrintWriter output, boolean isRDF, String baseURI)
	{
		initialize(output, isRDF, baseURI);
	}

	@Override
	public void initialize(Properties p )
	{
		
	}
	private void initialize(PrintWriter output, boolean isRDF, String baseURI) {
		this.output = output;
		this.isRDF = isRDF;
		this.baseURI = baseURI;
	}
	
	public void setR2RMLMappingIdentifier(R2RMLMappingIdentifier mappingIdentifer) {
		bloomFilterManager = new KR2RMLBloomFilterManager(mappingIdentifer);
	}

	@Override
	public void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri) {
		return;
	}

	@Override
	public void outputTripleWithURIObject(PredicateObjectMap predicateObjectMap,
			String subjUri, String predicateUri,
			String objectUri) {
		if (subjUri.indexOf("<") != -1 && subjUri.indexOf(">") != -1) {
			String tmp = subjUri.substring(1, subjUri.length() - 1);
			subjUri = "<" + normalizeURI(tmp) + ">";
		}
		if (objectUri.indexOf("<") != -1 && objectUri.indexOf(">") != -1) {
			String tmp = objectUri.substring(1, objectUri.length() - 1);
			objectUri = "<" + normalizeURI(tmp) + ">";
		}
		TriplesMap subjTriplesMap = predicateObjectMap.getTriplesMap();
		bloomFilterManager.addUriToBloomFilter(subjTriplesMap.getId(), subjUri);
		if(predicateUri.equalsIgnoreCase(formattedTypeURI))
		{
			return;
		}
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getId(), subjUri);
		if(predicateObjectMap.getObject().hasRefObjectMap())
		{
			bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getObject().getRefObjectMap().getId(), objectUri);
		}
	}

	@Override
	public void outputTripleWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String language) {
		return;
	}

	@Override
	public void outputTripleWithLiteralObject( PredicateObjectMap predicateObjectMap,
			String subjUri, String predicateUri, String value,
			String literalType, String language) {
		if (subjUri.indexOf("<") != -1 && subjUri.indexOf(">") != -1) {
			String tmp = subjUri.substring(1, subjUri.length() - 1);
			subjUri = "<" + normalizeURI(tmp) + ">";
		}
		TriplesMap subjTriplesMap = predicateObjectMap.getTriplesMap();
		bloomFilterManager.addUriToBloomFilter(subjTriplesMap.getId(), subjUri);
		if(predicateUri.equalsIgnoreCase(formattedTypeURI))
		{
			return;
		}
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getId(), subjUri);

	}

	@Override
	public void outputQuadWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String language, String graph) {
		return;

	}

	@Override
	public void outputQuadWithLiteralObject( PredicateObjectMap predicateObjectMap,
			String subjUri, String predicateUri, String value,
			String literalType, String language, String graph) {
		if (subjUri.indexOf("<") != -1 && subjUri.indexOf(">") != -1) {
			String tmp = subjUri.substring(1, subjUri.length() - 1);
			subjUri = "<" + normalizeURI(tmp) + ">";
		}
		TriplesMap subjTriplesMap = predicateObjectMap.getTriplesMap();
		bloomFilterManager.addUriToBloomFilter(subjTriplesMap.getId(), subjUri);
		if(predicateUri.equalsIgnoreCase(formattedTypeURI))
		{
			return;
		}
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getId(), subjUri);

	}

	@Override
	public void finishRow() {
		return;

	}

	@Override
	public void flush() {
		return;

	}

	@Override
	public void close() {
		if (!isRDF)
			output.write(bloomFilterManager.toJSON().toString(4));
		else {
			output.write(bloomFilterManager.toRDF());
		}
		output.flush();
		output.close();
	}

	private String normalizeURI(String URI) {
		try {
			URI = URI.replace(" ", "");
			URI uri = new URI(URI);
			if (!uri.isAbsolute() && baseURI != null)
				return baseURI + URI;
		}catch(URISyntaxException e) {
			if (baseURI != null)
				return baseURI + URI;
			else
				return URI;
		}
		return URI;
	}

}
