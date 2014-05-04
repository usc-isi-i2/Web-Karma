package edu.isi.karma.kr2rml;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

public class JSONKR2RMLRDFWriter implements KR2RMLRDFWriter{
	
	protected PrintWriter outWriter;
	protected Map<String, JSONObject> generatedObjects;

	public JSONKR2RMLRDFWriter (PrintWriter outWriter) {
		this.outWriter = outWriter;
		generatedObjects = new ConcurrentHashMap<String, JSONObject>();
	}
	
	@Override
	public void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri) {
		checkAndAddsubjUri(subjUri);
		addURIObject(subjUri, predicateUri, objectUri);
	}

	@Override
	public void outputTripleWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType) {
		checkAndAddsubjUri(subjUri);
		addLiteralObject(subjUri, predicateUri, value);
	}

	@Override
	public void outputQuadWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String graph) {
		// TODO Auto-generated method stub
		
	}

	private void checkAndAddsubjUri(String subjUri) {
		if (!generatedObjects.containsKey(subjUri)) {
			JSONObject object = new JSONObject();
			object.put("uri", subjUri);
			generatedObjects.put(subjUri, object);
		}
	}

	private void addLiteralObject(String subjUri, String predicateUri, String value) {
		JSONObject object = generatedObjects.get(subjUri);
		object.put(predicateUri, value);
		generatedObjects.put(subjUri, object);
	}

	private void addURIObject(String subjUri, String predicateUri, String objectUri) {
		if (generatedObjects.containsKey(objectUri)) {
			JSONObject object1 = generatedObjects.get(subjUri);
			JSONObject object2 = generatedObjects.get(objectUri);
			object1.put(predicateUri, object2);
			generatedObjects.put(subjUri, object1);
			generatedObjects.remove(objectUri);
		} else {
			addLiteralObject(subjUri, predicateUri, objectUri);
		}
	}

	@Override
	public void finishRow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
