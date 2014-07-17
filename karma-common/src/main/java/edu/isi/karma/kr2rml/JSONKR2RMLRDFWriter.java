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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.kr2rml.planning.TriplesMap;

public class JSONKR2RMLRDFWriter implements KR2RMLRDFWriter{
	private class ShortHand {
		private final String prefix;
		private final String uri;
		public ShortHand(String prefix, String uri) {
			this.prefix = prefix;
			this.uri = uri;
		}

		public String toString() {
			if (prefix != null) 
				return prefix + ":" + uri;
			else
				return uri;
		}

	}

	protected boolean firstObject = true;
	protected PrintWriter outWriter;
	protected Map<String, JSONObject> generatedObjects;
	protected Map<String, JSONObject> rootObjects = new ConcurrentHashMap<String, JSONObject>();
	private Set<Prefix> prefixes = new HashSet<Prefix>();
	private Map<String, Prefix> prefixMapping = new TreeMap<String, Prefix>();
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
		if (getPrefix(predicateUri).toString().equals("rdf:type")) {
			value = getPrefix(value).toString();
		}
		if (object.has(getPrefix(predicateUri).toString())) {
			JSONArray array = new JSONArray();
			Object obj = object.get(getPrefix(predicateUri).toString());
			if (obj instanceof String) {
				array.put(new JSONObject().put("values", value));
				array.put(new JSONObject().put("values", obj));
			}
			else if (obj instanceof JSONArray){
				JSONArray oldArray = (JSONArray) obj;
				for (int i = 0; i < oldArray.length(); i++)
					array.put(oldArray.get(i));
				array.put(new JSONObject().put("values", value));
			}
			object.put(getPrefix(predicateUri).toString(), array);
		}
		else
			object.put(getPrefix(predicateUri).toString(), value);
		generatedObjects.put(subjUri, object);
	}

	private void addURIObject(String subjUri, String predicateUri, String objectUri) {
		if (generatedObjects.containsKey(objectUri)) {
			JSONObject object1 = generatedObjects.get(subjUri);
			JSONObject object2 = generatedObjects.get(objectUri);
			if (object1.has(getPrefix(predicateUri).toString())) {
				Object obj = object1.get(getPrefix(predicateUri).toString());
				JSONArray array = new JSONArray();
				if (obj instanceof JSONObject) {
					array.put(new JSONObject().put("objects", object2));
					array.put(new JSONObject().put("objects", obj));
				}
				else if (obj instanceof JSONArray) {
					JSONArray oldArray = (JSONArray) obj;
					for (int i = 0; i < oldArray.length(); i++)
						array.put(oldArray.get(i));
					array.put(new JSONObject().put("objects", object2));
				}
				object1.put(getPrefix(predicateUri).toString(), array);
			}
			else
				object1.put(getPrefix(predicateUri).toString(), object2);
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
	public void outputTripleWithURIObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri,
			String objectUri) {
		outputTripleWithURIObject(subjUri, predicateUri, objectUri);

	}

	@Override
	public void outputTripleWithURIObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri, TriplesMap objTriplesMapId,
			String objectUri) {
		outputTripleWithURIObject(subjUri, predicateUri, objectUri);

	}


	@Override
	public void outputTripleWithLiteralObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri, String value,
			String literalType) {
		outputTripleWithLiteralObject(subjUri, predicateUri, value, value);

	}

	@Override
	public void outputQuadWithLiteralObject(TriplesMap subjTriplesMap,
			String subjUri, PredicateObjectMap predicateObjectMap, String predicateUri, String value,
			String literalType, String graph) {
		outputQuadWithLiteralObject(subjUri, predicateUri, value, literalType, graph);

	}

	public void addPrefixes(Collection<Prefix> prefixes) {
		this.prefixes.addAll(prefixes);
	}

	private ShortHand getPrefix(String URI) {
		Prefix p = prefixMapping.get(URI);
		if (p != null) {
			URI = URI.replace("<", "");
			URI = URI.replace(">", "");
			return new ShortHand(p.getNamespace(), URI.replace(p.getPrefix(), ""));
		}
		for (Prefix prefix : prefixes) {
			if (URI.indexOf(prefix.getPrefix()) >= 0) {
				prefixMapping.put(URI, prefix); {
					URI = URI.replace("<", "");
					URI = URI.replace(">", "");
					return new ShortHand(prefix.getNamespace(), URI.replace(prefix.getPrefix(), ""));
				}
			}
		}
		return new ShortHand(null, URI);
	}

}
