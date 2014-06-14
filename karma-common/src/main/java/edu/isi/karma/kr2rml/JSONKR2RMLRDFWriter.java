package edu.isi.karma.kr2rml;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

public class JSONKR2RMLRDFWriter implements KR2RMLRDFWriter{

	protected boolean firstObject = true;
	protected PrintWriter outWriter;
	protected Map<String, JSONObject> generatedObjects;
	protected Map<String, JSONObject> rootObjects = new ConcurrentHashMap<String, JSONObject>();

	public JSONKR2RMLRDFWriter (PrintWriter outWriter) {
		this.outWriter = outWriter;
		generatedObjects = new ConcurrentHashMap<String, JSONObject>();
		
		outWriter.println("[");
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
		outputTripleWithLiteralObject(subjUri, predicateUri, value, literalType);
	}

	private void checkAndAddsubjUri(String subjUri) {
		if (!generatedObjects.containsKey(subjUri)) {
			JSONObject object = new JSONObject();
			object.put("uri", subjUri);
			generatedObjects.put(subjUri, object);
			rootObjects.put(subjUri, object);
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
			//			generatedObjects.put(subjUri, object1);
			rootObjects.remove(objectUri);
		} else {
			addLiteralObject(subjUri, predicateUri, objectUri);
		}
	}

	@Override
	public void finishRow() {
		for(JSONObject value : rootObjects.values())
		{
			if (!firstObject) {
				outWriter.println(",");
			}
			firstObject = false;
			outWriter.print(value.toString(4));
		}
		outWriter.println("");
		generatedObjects = new ConcurrentHashMap<String, JSONObject>();
		rootObjects = new ConcurrentHashMap<String, JSONObject>();		
	}

	@Override
	public void flush() {
		finishRow();
		outWriter.flush();
	}

	@Override
	public void close() {
		outWriter.print("]");
		outWriter.close();
	}

	@Override
	public void outputTripleWithURIObject(String subjTriplesMapId,
			String subjUri, String predicateObjectMapId, String predicateUri,
			String objectUri) {
		outputTripleWithURIObject(subjUri, predicateUri, objectUri);
		
	}
	
	@Override
	public void outputTripleWithURIObject(String subjTriplesMapId,
			String subjUri, String predicateObjectMapId, String predicateUri, String objTriplesMapId,
			String objectUri) {
		outputTripleWithURIObject(subjUri, predicateUri, objectUri);
		
	}

	
	@Override
	public void outputTripleWithLiteralObject(String subjTriplesMapId,
			String subjUri, String predicateObjectMapId, String predicateUri, String value,
			String literalType) {
		outputTripleWithLiteralObject(subjUri, predicateUri, value, value);
		
	}

	@Override
	public void outputQuadWithLiteralObject(String subjTriplesMapId,
			String subjUri, String predicateObjectMapId, String predicateUri, String value,
			String literalType, String graph) {
		outputQuadWithLiteralObject(subjUri, predicateUri, value, literalType, graph);
		
	}

}
