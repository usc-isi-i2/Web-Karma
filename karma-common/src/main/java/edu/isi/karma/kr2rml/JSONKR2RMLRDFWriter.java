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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONKR2RMLRDFWriter implements KR2RMLRDFWriter{

	protected boolean firstObject = true;
	protected PrintWriter outWriter;
	protected Map<String, JSONObject> generatedObjects;
	protected Map<String, JSONObject> rootObjects = new ConcurrentHashMap<String, JSONObject>();
	protected ShortHandURIGenerator shortHandURIGenerator = new ShortHandURIGenerator();
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
		if (shortHandURIGenerator.getShortHand(predicateUri).toString().equals("rdf:type")) {
			value = shortHandURIGenerator.getShortHand(value).toString();
		}
		if (object.has(shortHandURIGenerator.getShortHand(predicateUri).toString())) {
			JSONArray array = null;
			Object obj = object.get(shortHandURIGenerator.getShortHand(predicateUri).toString());
			if (obj instanceof String) {
				array = new JSONArray();
				array.put(value);
				array.put(obj);
			}
			else if (obj instanceof JSONArray){
				array = (JSONArray) obj;
				array.put(value);
			}
			object.put(shortHandURIGenerator.getShortHand(predicateUri).toString(), array);
		}
		else
			object.put(shortHandURIGenerator.getShortHand(predicateUri).toString(), value);
		generatedObjects.put(subjUri, object);
	}

	private void addURIObject(String subjUri, String predicateUri, String objectUri) {
		if (generatedObjects.containsKey(objectUri)) {
			JSONObject object1 = generatedObjects.get(subjUri);
			JSONObject object2 = generatedObjects.get(objectUri);
			if (object1.has(shortHandURIGenerator.getShortHand(predicateUri).toString())) {
				Object obj = object1.get(shortHandURIGenerator.getShortHand(predicateUri).toString());
				JSONArray array = null;
				if (obj instanceof JSONObject) {
					array = new JSONArray();
					array.put(object2);
					array.put(obj);
				}
				else if (obj instanceof JSONArray) {
					array = (JSONArray) obj;
					array.put(object2);
				}
				object1.put(shortHandURIGenerator.getShortHand(predicateUri).toString(), array);
			}
			else
			{
				object1.put(shortHandURIGenerator.getShortHand(predicateUri).toString(), object2);
			}
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
	public void outputTripleWithURIObject(PredicateObjectMap predicateObjectMap,
			String subjUri, String predicateUri,
			String objectUri) {
		outputTripleWithURIObject(subjUri, predicateUri, objectUri);
	}


	@Override
	public void outputTripleWithLiteralObject( PredicateObjectMap predicateObjectMap, 
			String subjUri, String predicateUri, String value,
			String literalType) {
		outputTripleWithLiteralObject(subjUri, predicateUri, value, value);
	}

	@Override
	public void outputQuadWithLiteralObject( PredicateObjectMap predicateObjectMap, 
			String subjUri, String predicateUri, String value,
			String literalType, String graph) {
		outputQuadWithLiteralObject(subjUri, predicateUri, value, literalType, graph);

	}

	public void addPrefixes(Collection<Prefix> prefixes) {
		shortHandURIGenerator.addPrefixes(prefixes);
	}

}
