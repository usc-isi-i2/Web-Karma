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
package edu.isi.karma.kr2rml.planning;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFSTriplesMapGraphDAGifier implements TriplesMapGraphDAGifier {
	
	private static Logger logger = LoggerFactory.getLogger(DFSTriplesMapGraphDAGifier.class);
	@Override
	public List<String> dagify(TriplesMapGraph graph, RootStrategy rootStrategy) {
	
		HashSet<String> triplesMapsIds = new HashSet<>();

		if(graph.getTriplesMapIds().isEmpty())
		{
			return new LinkedList<>();
		}
		if(graph.getTriplesMapIds().size() == 1)
		{
			List<String> results = new LinkedList<>();
			results.addAll(graph.getTriplesMapIds());
			return results;
		}
		TriplesMapGraph newGraph = graph.copyGraph(triplesMapsIds);
		
		String rootTriplesMapId = newGraph.findRoot(rootStrategy);
		
		List<String> spilledNodes = cleanGraph(triplesMapsIds, newGraph, rootTriplesMapId);
		
		if(triplesMapsIds.isEmpty())
		{
			return spilledNodes;
		}
		
		dfs(newGraph, rootTriplesMapId);
		spilledNodes.addAll(cleanGraph(triplesMapsIds, newGraph, rootTriplesMapId));
		if(!triplesMapsIds.isEmpty())
		{
			logger.error("Unable to create DAG for the following nodes: "+ triplesMapsIds.toString());
		}
		return spilledNodes;
	}

	private List<String> cleanGraph(Set<String> triplesMapsIds,
			TriplesMapGraph newGraph, String rootTriplesMapId) {
		boolean modifications = true;
		List<String> spilledTriplesMaps = new LinkedList<>();
		
		while(!triplesMapsIds.isEmpty() && modifications)
		{
			logger.trace("starting a cleaning cycle");
			modifications = false;
			Iterator<String> ids = triplesMapsIds.iterator();
			while(ids.hasNext())
			{
				String triplesMapId = ids.next();
				List<TriplesMapLink> links = newGraph.getAllNeighboringTriplesMap(triplesMapId);
				TriplesMap tm = newGraph.getTriplesMap(triplesMapId);
				if(tm == null)
				{
					logger.debug(triplesMapId + " was already spilled");
					spilledTriplesMaps.add(triplesMapId);
					ids.remove();
					modifications = true;
					continue;
				}
				if(links.size() <= 1 || allLinksAreIncoming(triplesMapId, links))
				{
					List<TriplesMapLink> tempLinks = new LinkedList<>();
					tempLinks.addAll(links);
					// leave the root alone unless it's empty!
					if(triplesMapId.compareTo(rootTriplesMapId) == 0)
					{
						logger.debug("working on root");
						if(!links.isEmpty())
							continue;
						logger.debug("root is being spilled");
					}
					
					for(TriplesMapLink link : tempLinks)
					{
						logger.debug("Removing " + link.getPredicateObjectMapLink());
						newGraph.removeLink(link);
						
						if(link.getSourceMap().getId().compareTo(triplesMapId) == 0 && (triplesMapId.compareTo(rootTriplesMapId) != 0))
						{
							link.setIsFlipped(true);
							logger.debug("Flipping " + link.getPredicateObjectMapLink());
						}
					}
					if(links.isEmpty())
					{
						logger.debug("Spilling " + tm.getSubject().getRdfsType()  + " " +triplesMapId);
						modifications = true;
						List<String> removedTriplesMaps = newGraph.removeTriplesMap(triplesMapId);
						
						spilledTriplesMaps.addAll(removedTriplesMaps);
						ids.remove();
					}
					modifications = true;
				}
			}
		}
		return spilledTriplesMaps;
	}

	private boolean allLinksAreIncoming(String triplesMapId, List<TriplesMapLink> links) {
		for(TriplesMapLink link : links)
		{
			if(link.getSourceMap().getId().compareTo(triplesMapId) == 0 && !link.isFlipped())
			{
				return false;
			}
		}
		logger.debug("all links are in coming " + triplesMapId);
		return true;
	}

	private void dfs(TriplesMapGraph graph, String rootTriplesMapId, Set<String> visited, String triplesMapId)
	{
		final String localTriplesMapId = triplesMapId;
		visited.add(triplesMapId);
		List<TriplesMapLink> links = graph.getAllNeighboringTriplesMap(triplesMapId);
		List<String> nodesToVisit = new LinkedList<>();
		List<TriplesMapLink> sortedLinks = new LinkedList<>();
		sortedLinks.addAll(links);
		Collections.sort(sortedLinks, new Comparator<TriplesMapLink>(){

			@Override
			public int compare(TriplesMapLink o1, TriplesMapLink o2) {
				boolean o1IsSource = o1.getSourceMap().getId().compareTo(localTriplesMapId) == 0;
				boolean o2IsSource = o2.getSourceMap().getId().compareTo(localTriplesMapId) == 0;
				if((o1IsSource && o2IsSource)||
						(!o1IsSource && !o2IsSource))
				{
					return 0;
				}
				else if(o1IsSource && !o2IsSource)
				{
					return -1;
				}
				else 
				{
					return 1;
				}
			}
			
		});
		for(TriplesMapLink link : sortedLinks)
		{
			String nextNode = null;
			if(link.getSourceMap().getId().compareTo(triplesMapId) == 0)
			{
				nextNode = link.getTargetMap().getId();
			}
			else
			{
				
				nextNode = link.getSourceMap().getId();
				
				if(!visited.contains(nextNode))
				{
					link.setIsFlipped(true);
					logger.debug("Flipping " + link.getPredicateObjectMapLink());
				}
			}
			nodesToVisit.add(nextNode);

			
		}
		for(String nodeToVisit : nodesToVisit)
		{
			if(!visited.contains(nodeToVisit))
			{
				dfs(graph, rootTriplesMapId, visited, nodeToVisit);
			}
		}
	}
	private void dfs(TriplesMapGraph graph, String rootTriplesMapId)
	{
		Set<String> visited = new HashSet<>();
		dfs(graph, rootTriplesMapId, visited, rootTriplesMapId);
	}

}
