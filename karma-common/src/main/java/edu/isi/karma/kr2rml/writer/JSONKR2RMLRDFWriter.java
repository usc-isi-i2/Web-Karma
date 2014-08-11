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
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.modeling.Uris;

public class JSONKR2RMLRDFWriter extends SFKR2RMLRDFWriter<JSONObject> {

	public JSONKR2RMLRDFWriter (PrintWriter outWriter) {
		super(outWriter);
	}

	@Override
	protected void addValue(PredicateObjectMap pom, JSONObject subject, String predicateUri, Object object) {
		if (subject.has(shortHandURIGenerator.getShortHand(predicateUri).toString()) || predicateUri.contains(Uris.RDF_TYPE_URI)) {
			String shortHandPredicateURI = shortHandURIGenerator.getShortHand(predicateUri).toString();
			addValueToArray(pom, subject, object,
					shortHandPredicateURI);
		}
		else
		{
			subject.put(shortHandURIGenerator.getShortHand(predicateUri).toString(), object);
		}
	}

	@Override
	protected void addValueToArray(PredicateObjectMap pom,JSONObject subject, Object object,
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
		array.put(object);
		subject.put(!shortHandPredicateURI.equalsIgnoreCase("rdf:type")?shortHandPredicateURI: "@type", array);
	}

	@Override
	public void finishRow() {

	}

	@Override
	public void flush() {
		//		finishRow();
		outWriter.flush();
	}

	@Override
	public void close() {
		for(JSONObject value : rootObjects.values())
		{
			collapseSameType(value);
			if (!firstObject) {
				outWriter.println(",");
			}
			firstObject = false;
			outWriter.print(value.toString(4));
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
				Set<Object> types = new HashSet<Object>();
				int length = array.length();
				for (int i = 0; i < length; i++) {
					Object o = array.remove(0);
					if (o instanceof JSONObject) {
						collapseSameType((JSONObject) o);
					}					
					types.add(o);	
				}
				for (Object type : types) {
					array.put(type);
				}
			}
			if (value instanceof JSONObject)
				collapseSameType((JSONObject)value);
		}
	}

	@Override
	protected void initializeOutput() {
		outWriter.println("[");

	}


	@Override
	public JSONObject getNewObject(String triplesMapId, String subjUri) {
		JSONObject object = new JSONObject();
		object.put("@id", subjUri);
		return object;
	}

}
