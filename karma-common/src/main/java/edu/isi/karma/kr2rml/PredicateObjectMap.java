/*******************************************************************************
 * Copyright 2012 University of Southern California
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

import java.util.UUID;

import edu.isi.karma.kr2rml.planning.TriplesMap;

public class PredicateObjectMap {
	
	// Parent TriplesMap. Useful for traversal
	private final TriplesMap triplesMap;
	private final String id;
	private Predicate predicate;
	private ObjectMap object;
	private final static String PREDICATE_OBJECT_MAP_PREFIX = "PredicateObjectMap";
	
	
	public PredicateObjectMap(String id, TriplesMap triplesMap) {
		this.id = id;
		this.triplesMap = triplesMap;
	}

	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}
	
	public ObjectMap getObject() {
		return object;
	}
	
	public TriplesMap getTriplesMap() {
		return triplesMap;
	}

	public void setObject(ObjectMap object) {
		this.object = object;
	}

	public String getId()
	{
		return id;
	}
	
	@Override
	public String toString() {
		return "PredicateObjectMap [" + predicate + ", "
				+ object + "]";
	}

	public static String getNewId()
	{
		return PREDICATE_OBJECT_MAP_PREFIX + "_" + UUID.randomUUID();
	}
}
