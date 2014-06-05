package edu.isi.karma.kr2rml;

import java.io.PrintWriter;


public class BloomFilterKR2RMLRDFWriter implements KR2RMLRDFWriter {

	TriplesMapBloomFilterManager bloomFilterManager;
	PrintWriter output;
	public BloomFilterKR2RMLRDFWriter(PrintWriter output)
	{
		bloomFilterManager = new TriplesMapBloomFilterManager();
		this.output = output;
	}
	
	@Override
	public void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri) {
		return;
	}

	@Override
	public void outputTripleWithURIObject(String subjTriplesMapId,
			String subjUri, String predicateUri,
			String objectUri) {
		bloomFilterManager.addUriToBloomFilter(subjTriplesMapId, subjUri);

	}
	
	@Override
	public void outputTripleWithURIObject(String subjTriplesMapId,
			String subjUri, String predicateUri, String objTriplesMapId,
			String objectUri) {
		bloomFilterManager.addUriToBloomFilter(subjTriplesMapId, subjUri);

	}



	@Override
	public void outputTripleWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType) {
		return;

	}

	@Override
	public void outputTripleWithLiteralObject(String subjTriplesMapId,
			String subjUri, String predicateUri, String value,
			String literalType) {
		bloomFilterManager.addUriToBloomFilter(subjTriplesMapId, subjUri);

	}

	@Override
	public void outputQuadWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String graph) {
		return;

	}

	@Override
	public void outputQuadWithLiteralObject(String subjTriplesMapId,
			String subjUri, String predicateUri, String value,
			String literalType, String graph) {
		bloomFilterManager.addUriToBloomFilter(subjTriplesMapId, subjUri);

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
