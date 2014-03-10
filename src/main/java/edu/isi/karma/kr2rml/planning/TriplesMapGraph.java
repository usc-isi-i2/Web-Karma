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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TriplesMapGraph {
	private Set<TriplesMapLink> links;
	private Map<String, List<TriplesMapLink>> neighboringTriplesMapCache;
	private Map<String, TriplesMap> triplesMapIndex;
	
	public TriplesMapGraph() {
		this.links = new HashSet<TriplesMapLink>();
		this.neighboringTriplesMapCache = new HashMap<String, List<TriplesMapLink>>();
		this.triplesMapIndex = new HashMap<String, TriplesMap>();
	}

	public Set<TriplesMapLink> getLinks() {
		return links;
	}

	public void addTriplesMap(TriplesMap triplesMap)
	{
		triplesMapIndex.put(triplesMap.getId(), triplesMap);
		List<TriplesMapLink> neighbouringLinks = neighboringTriplesMapCache.get(triplesMap.getId());
		if (neighbouringLinks == null) {
			neighbouringLinks = new ArrayList<TriplesMapLink>();
			neighboringTriplesMapCache.put(triplesMap.getId(), neighbouringLinks);
		}
	}
	
	public void addLink(TriplesMapLink link) {
		links.add(link);
		updateCache(link);
	}
	
	public static TriplesMapGraph mergeGraphs(TriplesMapGraph graphToMerge1, TriplesMapGraph graphToMerge2)
	{
		TriplesMapGraph newGraph = new TriplesMapGraph();
		
		for(TriplesMapLink link : graphToMerge1.getLinks())
		{
			newGraph.addLink(link);
		}
		for(TriplesMapLink link : graphToMerge2.getLinks())
		{
			newGraph.addLink(link);
		}
		return newGraph;
	}
	private void updateCache(TriplesMapLink link) {
		// Add source neighboring links to the cache
		List<TriplesMapLink> sourceNeighbouringLinks = neighboringTriplesMapCache.get(link.getSourceMap().getId());
		if (sourceNeighbouringLinks == null) {
			sourceNeighbouringLinks = new ArrayList<TriplesMapLink>();
		}
		sourceNeighbouringLinks.add(link);
		triplesMapIndex.put(link.getSourceMap().getId(),link.getSourceMap());
		neighboringTriplesMapCache.put(link.getSourceMap().getId(), sourceNeighbouringLinks);

		// Add target neighboring links to the cache
		List<TriplesMapLink> targetNeighbouringLinks = neighboringTriplesMapCache.get(link.getTargetMap().getId());
		if (targetNeighbouringLinks == null) {
			targetNeighbouringLinks = new ArrayList<TriplesMapLink>();
		}
		targetNeighbouringLinks.add(link);
		triplesMapIndex.put(link.getTargetMap().getId(),link.getTargetMap());
		neighboringTriplesMapCache.put(link.getTargetMap().getId(), targetNeighbouringLinks);
	}
	
	public List<TriplesMapLink> getAllNeighboringTriplesMap(String triplesMapId) {
		if (neighboringTriplesMapCache.get(triplesMapId) == null)
			return new ArrayList<TriplesMapLink>();
		
		return neighboringTriplesMapCache.get(triplesMapId);
	}

	public void removeLink(TriplesMapLink link) {
		links.remove(link);
		removeLinkFromCache(link, link.getTargetMap().getId());
		removeLinkFromCache(link, link.getSourceMap().getId());
	}

	private void removeLinkFromCache(TriplesMapLink link, String mapId) {
		List<TriplesMapLink> targetLinks = neighboringTriplesMapCache.get(mapId);
		targetLinks.remove(link);
		if(targetLinks.isEmpty())
		{
			neighboringTriplesMapCache.remove(mapId);
			triplesMapIndex.remove(mapId);
		}
	}
	
	public Set<String> getTriplesMapIds()
	{
		return triplesMapIndex.keySet();
	}
	public String findRoot(RootStrategy strategy)
	{
		return strategy.findRoot(this);
	}
	
	public TriplesMap getTriplesMap(String triplesMapId)
	{
		return this.triplesMapIndex.get(triplesMapId);
	}

	public void removeTriplesMap(String triplesMapId) {
		
		List<TriplesMapLink> targetLinks = neighboringTriplesMapCache.get(triplesMapId);
		if(targetLinks != null)
		{
			for(TriplesMapLink link : targetLinks)
			{
				removeLink(link);
			}	
		}
		neighboringTriplesMapCache.remove(triplesMapId);
		triplesMapIndex.remove(triplesMapId);
		
		
	}
	
	
}
