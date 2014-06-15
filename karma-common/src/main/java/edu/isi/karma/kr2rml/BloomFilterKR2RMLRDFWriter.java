package edu.isi.karma.kr2rml;

import java.io.PrintWriter;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.modeling.Uris;


public class BloomFilterKR2RMLRDFWriter implements KR2RMLRDFWriter {

	protected KR2RMLBloomFilterManager bloomFilterManager;
	protected PrintWriter output;
	private final static String formattedTypeURI = "<" + Uris.RDF_TYPE_URI +">";
	public BloomFilterKR2RMLRDFWriter(PrintWriter output, R2RMLMappingIdentifier mappingIdentifer)
	{
		bloomFilterManager = new KR2RMLBloomFilterManager(mappingIdentifer);
		this.output = output;
	}
	
	@Override
	public void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri) {
		return;
	}

	@Override
	public void outputTripleWithURIObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri,
			String objectUri) {

		bloomFilterManager.addUriToBloomFilter(subjTriplesMap.getId(), subjUri);
		if(predicateUri.equalsIgnoreCase(formattedTypeURI))
		{
			return;
		}
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getId(), subjUri);

	}
	
	@Override
	public void outputTripleWithURIObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri, TriplesMap objTriplesMap,
			String objectUri) {

		bloomFilterManager.addUriToBloomFilter(subjTriplesMap.getId(), subjUri);
		if(predicateUri.equalsIgnoreCase(formattedTypeURI))
		{
			return;
		}
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getId(), subjUri);
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getObject().getRefObjectMap().getId(), objectUri);

	}



	@Override
	public void outputTripleWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType) {
		return;

	}

	@Override
	public void outputTripleWithLiteralObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri, String value,
			String literalType) {

		bloomFilterManager.addUriToBloomFilter(subjTriplesMap.getId(), subjUri);
		if(predicateUri.equalsIgnoreCase(formattedTypeURI))
		{
			return;
		}
		bloomFilterManager.addUriToBloomFilter(predicateObjectMap.getId(), subjUri);

	}

	@Override
	public void outputQuadWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String graph) {
		return;

	}

	@Override
	public void outputQuadWithLiteralObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri, String value,
			String literalType, String graph) {

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
		output.write(bloomFilterManager.toJSON().toString(4));
		output.flush();
		output.close();
	}

}
