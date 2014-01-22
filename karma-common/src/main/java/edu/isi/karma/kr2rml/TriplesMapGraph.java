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

import java.util.*;

public class TriplesMapGraph {
	private Set<TriplesMapLink> links;
	private Map<String, List<TriplesMapLink>> neighboringTriplesMapCache;
	
	public TriplesMapGraph() {
		this.links = new HashSet<TriplesMapLink>();
		this.neighboringTriplesMapCache = new HashMap<String, List<TriplesMapLink>>();
	}

	public Set<TriplesMapLink> getLinks() {
		return links;
	}

	public void addLink(TriplesMapLink link) {
		links.add(link);
		updateCache(link);
	}
	
	private void updateCache(TriplesMapLink link) {
		// Add source neighboring links to the cache
		List<TriplesMapLink> sourceNeighbouringLinks = neighboringTriplesMapCache.get(link.getSourceMap().getId());
		if (sourceNeighbouringLinks == null) {
			sourceNeighbouringLinks = new ArrayList<TriplesMapLink>();
		}
		sourceNeighbouringLinks.add(link);
		neighboringTriplesMapCache.put(link.getSourceMap().getId(), sourceNeighbouringLinks);

		// Add target neighboring links to the cache
		List<TriplesMapLink> targetNeighbouringLinks = neighboringTriplesMapCache.get(link.getTargetMap().getId());
		if (targetNeighbouringLinks == null) {
			targetNeighbouringLinks = new ArrayList<TriplesMapLink>();
		}
		targetNeighbouringLinks.add(link);
		neighboringTriplesMapCache.put(link.getTargetMap().getId(), targetNeighbouringLinks);
	}
	
	public List<TriplesMapLink> getAllNeighboringTriplesMap(String triplesMapId) {
		if (neighboringTriplesMapCache.get(triplesMapId) == null)
			return new ArrayList<TriplesMapLink>();
		
		return neighboringTriplesMapCache.get(triplesMapId);
	}
}
