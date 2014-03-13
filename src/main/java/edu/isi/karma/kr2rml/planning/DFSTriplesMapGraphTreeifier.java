package edu.isi.karma.kr2rml.planning;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFSTriplesMapGraphTreeifier implements TriplesMapGraphTreeifier {
	
	private static Logger logger = LoggerFactory.getLogger(DFSTriplesMapGraphTreeifier.class);
	@Override
	public List<String> treeify(TriplesMapGraph graph, RootStrategy rootStrategy) {
	
		HashSet<String> triplesMapsIds = new HashSet<String>();

		TriplesMapGraph newGraph = copyGraph(graph, triplesMapsIds);
		
		String rootTriplesMapId = newGraph.findRoot(rootStrategy);
		
		List<String> spilledNodes = cleanGraph(triplesMapsIds, newGraph, rootTriplesMapId);
		
		if(triplesMapsIds.isEmpty())
		{
			return spilledNodes;
		}
		
		dfs(newGraph, rootTriplesMapId);
		spilledNodes.addAll(cleanGraph(triplesMapsIds, newGraph, rootTriplesMapId));
		return spilledNodes;
	}

	private List<String> cleanGraph(HashSet<String> triplesMapsIds,
			TriplesMapGraph newGraph, String rootTriplesMapId) {
		boolean modifications = true;
		List<String> spilledTriplesMaps = new LinkedList<String>();
		
		while(modifications)
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
					continue;
				}
				if(links.size() <= 1 || allLinksAreIncoming(triplesMapId, links))
				{
					List<TriplesMapLink> tempLinks = new LinkedList<TriplesMapLink>();
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
						newGraph.removeLink(link);
						logger.debug("Removing " + link.getPredicateObjectMapLink());
						if(link.getSourceMap().getId().compareTo(triplesMapId) == 0 && (triplesMapId.compareTo(rootTriplesMapId) != 0))
						{
							link.setIsFlipped(true);
							logger.debug("Flipping " + link.getPredicateObjectMapLink());
						}
						//what should we do with this?
					}
					if(links.isEmpty())
					{
						logger.debug("Spilling " + tm.getSubject().getRdfsType()  + " " +triplesMapId);
						modifications = true;
						spilledTriplesMaps.add(triplesMapId);
						ids.remove();
						newGraph.removeTriplesMap(triplesMapId);
					}
				}
			}
		}
		return spilledTriplesMaps;
	}

	private boolean allLinksAreIncoming(String triplesMapId, List<TriplesMapLink> links) {
		for(TriplesMapLink link : links)
		{
			if(link.getSourceMap().getId().compareTo(triplesMapId) != 0 || link.isFlipped())
			{
				return false;
			}
		}
		logger.debug("all links are in coming " + triplesMapId);
		return true;
	}

	private void dfs(TriplesMapGraph graph, String rootTriplesMapId, Set<String> visited, String triplesMapId)
	{
		visited.add(triplesMapId);
		List<TriplesMapLink> links = graph.getAllNeighboringTriplesMap(triplesMapId);
		List<String> nodesToVisit = new LinkedList<String>();
		for(TriplesMapLink link : links)
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
		Set<String> visited = new HashSet<String>();
		dfs(graph, rootTriplesMapId, visited, rootTriplesMapId);
	}
	private TriplesMapGraph copyGraph(TriplesMapGraph graph, 
			HashSet<String> triplesMapsIds) {
		TriplesMapGraph newGraph = new TriplesMapGraph();
		for(TriplesMapLink link : graph.getLinks())
		{
			triplesMapsIds.add(link.getSourceMap().getId());
			triplesMapsIds.add(link.getTargetMap().getId());
			newGraph.addLink(link);
		}
		return newGraph;
	}

}
