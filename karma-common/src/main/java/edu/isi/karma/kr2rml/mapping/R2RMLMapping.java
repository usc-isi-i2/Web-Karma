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

package edu.isi.karma.kr2rml.mapping;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.kr2rml.Prefix;
import edu.isi.karma.kr2rml.planning.TriplesMap;

public class R2RMLMapping {
	
	private List<TriplesMap> triplesMapList = new ArrayList<>();
	private List<Prefix> prefixes = new ArrayList<>();
	private R2RMLMappingIdentifier id;
	
	public R2RMLMapping(R2RMLMappingIdentifier id) {
		this.id = id;
	}

	public List<TriplesMap> getTriplesMapList() {
		return triplesMapList;
	}

	public void addTriplesMap(TriplesMap tmap) {
		triplesMapList.add(tmap);
	}
	
	public void addPrefix(Prefix prefix) {
		prefixes.add(prefix);
	}
	
	public List<Prefix> getPrefixes()
	{
		return prefixes;
	}
	public R2RMLMappingIdentifier getId() {
		return id;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("R2RMLMapping [triplesMapSet=\n");
		for (TriplesMap trMap:triplesMapList)
			str.append("\t" + trMap.toString() + "\n");
		str.append("]");
		return str.toString();
	}
}
