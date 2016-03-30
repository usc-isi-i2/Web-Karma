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

package edu.isi.karma.kr2rml.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.SubjectMap;

public class TriplesMap {
	
	private final String id;
	private SubjectMap subject;
	private List<PredicateObjectMap> predicateObjectMaps = new ArrayList<>();
	private final static String TRIPLES_MAP_PREFIX = "TriplesMap";
	
	public TriplesMap(String id, SubjectMap subject) {
		this.id = id;
		this.subject = subject;
	}
	
	public TriplesMap(String id, SubjectMap subject,
			List<PredicateObjectMap> predicateObjectMaps) {
		this.id = id;
		this.subject = subject;
		this.predicateObjectMaps = predicateObjectMaps;
	}
	
	public SubjectMap getSubject() {
		return subject;
	}
	
	public void setSubject(SubjectMap subject) {
		this.subject = subject;
	}
	
	public List<PredicateObjectMap> getPredicateObjectMaps() {
		return predicateObjectMaps;
	}
	
	public void setPredicateObjectMaps(List<PredicateObjectMap> predicateObjectMaps) {
		this.predicateObjectMaps = predicateObjectMaps;
	}
	
	public void addPredicateObjectMap(PredicateObjectMap poMap) {
		this.predicateObjectMaps.add(poMap);
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder predStr = new StringBuilder();
		for (PredicateObjectMap poMap:predicateObjectMaps) {
			predStr.append("\t\t\t" + poMap.toString() + "\n");
		}
		
		return "TriplesMap [\n" +
				"\t\tid=" + id + ",\n" +
				"\t\tsubject=" + subject + ",\n" +
				"\t\tpredicateObjectMaps=\n" + predStr.toString() + "\n\t]";
	}

	public static String getNewId() {
		return TRIPLES_MAP_PREFIX + "_" + UUID.randomUUID();
	}

}
