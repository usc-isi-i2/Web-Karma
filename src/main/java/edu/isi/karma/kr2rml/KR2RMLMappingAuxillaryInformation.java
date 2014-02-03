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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KR2RMLMappingAuxillaryInformation {
	private TriplesMapGraph triplesMapGraph;
	private Map<String, List<String>> blankNodesColumnCoverage;
	private Map<String, String> blankNodesUriPrefixMap;
	private Map<String, List<PredicateObjectMap>> columnNameToPredObjMLinks;
	
	public KR2RMLMappingAuxillaryInformation() {
		this.triplesMapGraph = new TriplesMapGraph();
		this.blankNodesUriPrefixMap = new HashMap<String, String>();
		this.blankNodesColumnCoverage = new HashMap<String, List<String>>();
		this.columnNameToPredObjMLinks = new HashMap<String, List<PredicateObjectMap>>();
	}

	public TriplesMapGraph getTriplesMapGraph() {
		return triplesMapGraph;
	}

	public Map<String, List<String>> getBlankNodesColumnCoverage() {
		return blankNodesColumnCoverage;
	}

	public Map<String, String> getBlankNodesUriPrefixMap() {
		return blankNodesUriPrefixMap;
	}
	
	public Map<String, List<PredicateObjectMap>> getColumnNameToPredObjLinks() {
		return this.columnNameToPredObjMLinks;
	}
}
