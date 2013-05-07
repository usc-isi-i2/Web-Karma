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

import java.util.ArrayList;
import java.util.List;

public class R2RMLMapping {
	
	private List<TriplesMap> triplesMap = new ArrayList<TriplesMap>();
	private List<Prefix> prefixes = new ArrayList<Prefix>();
	
	public void addTriplesMap(TriplesMap tmap) {
		triplesMap.add(tmap);
	}
	
	public void addPrefix(Prefix prefix) {
		prefixes.add(prefix);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("R2RMLMapping [triplesMapSet=\n");
		for (TriplesMap trMap:triplesMap)
			str.append("\t" + trMap.toString() + "\n");
		str.append("]");
		return str.toString();
	}
}
