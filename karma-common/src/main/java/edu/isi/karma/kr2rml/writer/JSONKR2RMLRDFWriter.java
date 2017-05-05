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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.IteratorUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.modeling.Uris;

public class JSONKR2RMLRDFWriter extends SFKR2RMLRDFWriter<JSONObject> {

	private Map<String, String> contextInverseAtIdMapping = new HashMap<>();
	private Map<String, Boolean> contextInverseAtContainerMapping = new HashMap<>();
	private URL location;
	private JSONObject context;
	private String atType = "@type";
	private String atId = "@id";
	public JSONKR2RMLRDFWriter (PrintWriter outWriter) {
		super(outWriter);
	}

	public JSONKR2RMLRDFWriter (PrintWriter outWriter, String baseURI) {
		super(outWriter, baseURI);
	}
	
	public JSONKR2RMLRDFWriter (PrintWriter outWriter, String baseURI, boolean disableNesting) {
		super(outWriter, baseURI, disableNesting);
	}

	public void setGlobalContext(JSONObject context, ContextIdentifier contextId) {
		if (context.has("@context")) {
			if (contextId != null) {
				location = contextId.getLocation();
			}
			JSONObject c = context.getJSONObject("@context");
			this.context = c;
			@SuppressWarnings("rawtypes")
			Iterator itr = c.keys();
			while (itr.hasNext()) {
				String key = itr.next().toString();
				try {
					if (c.get(key).toString().equals("@id")) {
						atId = key;
					}
					if (c.get(key).toString().equals("@type")) {
						atType = key;
					}
					if (c.getJSONObject(key).has("@id")) {
						contextInverseAtIdMapping.put(c.getJSONObject(key).getString("@id"), key);
					}
					if (c.getJSONObject(key).has("@container") && c.getJSONObject(key).get("@container").equals("@set")) {
						contextInverseAtContainerMapping.put(key, true);
					}
				}catch(Exception e) 
				{

				}
			}
		}
	}

	@Override
	protected void addValue(PredicateObjectMap pom, JSONObject subject, String predicateUri, Object object) {
		String shortHandPredicateURI = generateShortHandURIFromContext(predicateUri);
		if (subject.has(shortHandPredicateURI)
				|| predicateUri.contains(Uris.RDF_TYPE_URI)
				|| Objects.equals(contextInverseAtContainerMapping.get(shortHandPredicateURI), true)) {
			addValueToArray(pom, subject, object,
					shortHandPredicateURI);
		}
		else
		{
			if (object instanceof String) {
				object = normalizeURI((String)object);
			}
			else if(object instanceof JSONObject && disableNesting)
			{
				object = getNewObject(null,((JSONObject)object).get(atId).toString());
			}
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
			object = normalizeURI((String)object);
		}
		else if(object instanceof JSONObject && disableNesting)
		{
			object = getNewObject(null,((JSONObject)object).get(atId).toString());
		}			
		
		if (shortHandPredicateURI.equalsIgnoreCase("rdf:type") && subject.has(atType)) 
			array = subject.getJSONArray(atType);
				
		array.put(object);
		if (shortHandPredicateURI.equalsIgnoreCase("rdf:type")) {
			int size = array.length();
			JSONArray newTypeArray = new JSONArray();
			for (int i = 0; i < size; i++) {
				newTypeArray.put(generateShortHandURIFromContext(array.get(i).toString()));
			}
			subject.put(atType, newTypeArray);
		}
		else {
			subject.put(shortHandPredicateURI, array);
		}
	}

	@Override
	protected Object generateLanguageLiteral(Object literal, String language) {
		//Generate expanded form JSON for the language
		JSONObject literalJSON = new JSONObject();
		literalJSON.put("@value", literal);
		literalJSON.put("@language", language);
		return literalJSON;
	}
	
	@Override
	public void finishRow() {
		for(ConcurrentHashMap<String, JSONObject> records : this.rootObjectsByTriplesMapId.values())
		{
			for(JSONObject value : records.values())
			{
				if (value.has(atId)) {
					String Id = value.get(atId).toString();
					if (!isValidURI(Id)) {
						if(!disableNesting)
						{
							value.remove(atId);
						}
						else if(!isValidBlankNode(Id))
						{
							value.remove(atId);
						}
					}
					
					
				}
				collapseSameType(value);
				if (!firstObject) {
					outWriter.println(",");
				}
				firstObject = false;
				if (location != null) {
					value.put("@context", location.toString());
				}
				else if (context != null) {
					value.put("@context", context);
				}
				outWriter.print(value.toString(4));
			}
		}
		for(Entry<String, ConcurrentHashMap<String, JSONObject>> entry : this.rootObjectsByTriplesMapId.entrySet())
		{
			entry.getValue().clear();
		}
		for(Entry<String, ConcurrentHashMap<String, JSONObject>> entry : this.generatedObjectsByTriplesMapId.entrySet())
		{
			entry.getValue().clear();
		}
		this.generatedObjectsWithoutTriplesMap.clear();
	}

	private boolean isValidBlankNode(String id) {
		
		return id.startsWith("_:");
	}

	@Override
	public void flush() {
		outWriter.flush();
	}

	@Override
	public void close() {

		outWriter.println("");
		outWriter.println("]");
		outWriter.close();
	}

	@Override
	protected void collapseSameType(JSONObject obj) {
		for (Object key : IteratorUtils.toList(obj.keys())) {
			Object value = obj.get((String)key);
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray)value;
				JSONArray newArray = new JSONArray();
				Map<String, Object> types = new HashMap<>();
				int length = array.length();
				for (int i = 0; i < length; i++) {
					Object o = array.get(i);
					if (o instanceof JSONObject) {
						JSONObject jsonObjectValue = (JSONObject)o;
						if (jsonObjectValue.has(atId)) {
							String Id = jsonObjectValue.get(atId).toString();
							if (!isValidURI(Id)) {
								jsonObjectValue.remove(atId);
							}
						}
						if(isJustIdAndType(jsonObjectValue))
						{
							types.put(jsonObjectValue.getString(atId), jsonObjectValue.get(atId));
						}
						else
						{
							JSONObject tmp = (JSONObject)o;
							if (tmp.has(atId)) {
								types.put(tmp.getString(atId), o);
							}
							else {
								types.put(tmp.toString(), o);
							}
							collapseSameType((JSONObject)o);

						}

					}			
					else
					{
						types.put(o.toString(), o);
					}
				}
				//Let atType always be arrays
				if (types.size() > 1 || key.equals(atType) || Objects.equals(contextInverseAtContainerMapping.get(key), true)) {
					for (Entry<String, Object> type : types.entrySet()) {
						newArray.put(type.getValue());
					}
					obj.put((String)key, newArray);
				}
				else if (types.values().iterator().hasNext()){
					Object o = types.values().iterator().next();
					obj.put((String)key, o);
				}
				else
				{
					obj.put((String)key, newArray);
				}
			}
			if (value instanceof JSONObject)
			{
				JSONObject jsonObjectValue = (JSONObject)value;
				if (jsonObjectValue.has(atId)) {
					String Id = jsonObjectValue.get(atId).toString();
					if (!isValidURI(Id)) {
						jsonObjectValue.remove(atId);
					}
				}
				if(isJustIdAndType(jsonObjectValue))
				{
					obj.put((String)key, jsonObjectValue.get(atId));
				}
				else
				{
					collapseSameType((JSONObject)value);
				}
			}
		}
	}
	
	private boolean isValidURI(String URI) {
		try {
			@SuppressWarnings("unused")
			URI uri = new URI(URI);
		}catch(Exception e) {
			return false;
		}
		return true;
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
		object.put(atId, subjUri);
		return object;
	}
	
	public String getAtId() {
		return atId;
	}
	
	public String getAtType() {
		return atType;
	}

	private String generateShortHandURIFromContext(String uri) {
		if (uri.startsWith("<") && uri.endsWith(">")) { 
			uri = uri.substring(1, uri.length() - 1);		
		}
		String shortHandPredicateURI = contextInverseAtIdMapping.get(uri);
		if (shortHandPredicateURI == null) {
			shortHandPredicateURI = shortHandURIGenerator.getShortHand(uri).toString();
		}
		return shortHandPredicateURI;
	}
	
	private String normalizeURI(String URI) {
		if (URI.startsWith("<") && URI.endsWith(">")) {
			URI = URI.substring(1, URI.length() - 1);
			try {
				URI uri = new URI(URI);
				if (!uri.isAbsolute())
					URI = baseURI + URI;
			}catch(Exception e) {

			}
		}
		return URI;
	}
	
	@Override
	public void setR2RMLMappingIdentifier(
			R2RMLMappingIdentifier mappingIdentifer) {
		
	}
	
}
