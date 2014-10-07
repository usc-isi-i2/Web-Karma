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
package edu.isi.karma.kr2rml.writer;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.modeling.Uris;

public class JSONKR2RMLRDFWriter extends SFKR2RMLRDFWriter<JSONObject> {

	private Map<String, String> contextInverseMapping = new HashMap<String, String>();
	private URL location;
	public JSONKR2RMLRDFWriter (PrintWriter outWriter) {
		super(outWriter);
	}

	public JSONKR2RMLRDFWriter (PrintWriter outWriter, String baseURI) {
		super(outWriter, baseURI);
	}

	public void setGlobalContext(JSONObject context, ContextIdentifier contextId) {
		if (context.has("@context")) {
			location = contextId.getLocation();
			JSONObject c = context.getJSONObject("@context");
			@SuppressWarnings("rawtypes")
			Iterator itr = c.keys();
			while (itr.hasNext()) {
				String key = itr.next().toString();
				try {
					contextInverseMapping.put(c.getJSONObject(key).getString("@id"), key);
				}catch(Exception e) 
				{

				}
			}
		}
	}

	@Override
	protected void addValue(PredicateObjectMap pom, JSONObject subject, String predicateUri, Object object) {
		if (subject.has(shortHandURIGenerator.getShortHand(predicateUri).toString()) || predicateUri.contains(Uris.RDF_TYPE_URI)) {
			String shortHandPredicateURI = generateShortHandURIFromContext(predicateUri);
			addValueToArray(pom, subject, object,
					shortHandPredicateURI);
		}
		else
		{
			String shortHandPredicateURI = generateShortHandURIFromContext(predicateUri);
			subject.put(shortHandPredicateURI, object);
		}
	}

	@Override
	protected void addValueToArray(PredicateObjectMap pom, JSONObject subject, Object object,
			String shortHandPredicateURI) {
		JSONArray array = null;
		if(subject.has(shortHandPredicateURI))
		{
			Object obj = subject.get(shortHandPredicateURI);
			if(obj != null)
			{
				if (obj instanceof JSONArray) {
					array = (JSONArray) obj;
				}
				else{
					array = new JSONArray();
					array.put(obj);
				}
			}
			else
			{
				array = new JSONArray();
			}
		}
		else
		{
			array = new JSONArray();
		}
		if (object instanceof String) {
			String t = ((String)object).trim();
			if (t.startsWith("<") && t.endsWith(">")) {
				t = t.substring(1, t.length() - 1);
				try {
					URI uri = new URI(t);
					if (!uri.isAbsolute())
						t = baseURI + t;
				}catch(Exception e) {

				}
			}
			object = t;
		}
		array.put(object);
		if (shortHandPredicateURI.equalsIgnoreCase("rdf:type")) {
			int size = array.length();
			for (int i = 0; i < size; i++) {
				String t = generateShortHandURIFromContext(array.remove(0).toString());
				array.put(t);
			}
			subject.put("@type", array);
		}
		else {
			subject.put(shortHandPredicateURI, array);
		}
	}

	@Override
	public void finishRow() {

	}

	@Override
	public void flush() {
		outWriter.flush();
	}

	@Override
	public void close() {
		for(ConcurrentHashMap<String, JSONObject> records : this.rootObjectsByTriplesMapId.values())
		{
			for(JSONObject value : records.values())
			{
				collapseSameType(value);
				if (!firstObject) {
					outWriter.println(",");
				}
				firstObject = false;
				if (location != null) {
					value.put("@context", location.toString());
				}
				outWriter.print(value.toString(4));
			}
		}
		outWriter.println("");
		outWriter.println("]");
		outWriter.close();
	}

	@Override
	protected void collapseSameType(JSONObject obj) {
		for (Object key : obj.keySet()) {
			Object value = obj.get((String)key);
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray)value;
				TreeMap<String, Object> types = new TreeMap<String, Object>();
				int length = array.length();
				for (int i = 0; i < length; i++) {
					Object o = array.remove(0);
					if (o instanceof JSONObject) {
						JSONObject jsonObjectValue = (JSONObject)o;
						if(isJustIdAndType(jsonObjectValue))
						{
							types.put(jsonObjectValue.getString("@id"), jsonObjectValue.get("@id"));
						}
						else
						{
							collapseSameType((JSONObject)o);
							types.put(((JSONObject)o).getString("@id"), o);

						}

					}			
					else
					{
						types.put((String)o, o);
					}
				}
				if (types.size() > 1) {
					for (Entry<String, Object> type : types.entrySet()) {
						array.put(type.getValue());
					}
				}
				else if (types.values().iterator().hasNext()){
					Object o = types.values().iterator().next();
					obj.put((String)key, o);
				}
			}
			if (value instanceof JSONObject)
			{
				JSONObject jsonObjectValue = (JSONObject)value;
				if(isJustIdAndType(jsonObjectValue))
				{
					obj.put((String)key, jsonObjectValue.get("@id"));
				}
				else
				{
					collapseSameType((JSONObject)value);
				}
			}
		}
	}

	protected boolean isJustIdAndType(JSONObject object)
	{
		//return object.keySet().size() <= 4;
		return false;
	}
	@Override
	protected void initializeOutput() {
		outWriter.println("[");

	}


	@Override
	public JSONObject getNewObject(String triplesMapId, String subjUri) {
		JSONObject object = new JSONObject();
		subjUri.trim();
		if (subjUri.startsWith("<") && subjUri.endsWith(">")) {
			subjUri = subjUri.substring(1, subjUri.length() - 1);
			try {
				URI uri = new URI(subjUri);
				if (!uri.isAbsolute())
					subjUri = baseURI + subjUri;
			}catch(Exception e) {

			}
		}
		object.put("@id", subjUri);
		return object;
	}

	private String generateShortHandURIFromContext(String uri) {
		if (uri.startsWith("<") && uri.endsWith(">")) { 
			uri = uri.substring(1, uri.length() - 1);		
		}
		String shortHandPredicateURI = contextInverseMapping.get(uri);
		if (shortHandPredicateURI == null) {
			shortHandPredicateURI = shortHandURIGenerator.getShortHand(uri).toString();
		}
		return shortHandPredicateURI;
	}

}
