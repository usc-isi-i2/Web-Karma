package edu.isi.karma.kr2rml.planning;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TriplesMapGraphMerger {

	List<TriplesMapGraph> graphs;
	
	public TriplesMapGraphMerger()
	{
		graphs = new LinkedList<TriplesMapGraph>();
	}
	
	public TriplesMapGraph getTriplesMapGraph(String triplesMapId)
	{
		for(TriplesMapGraph graph : graphs)
		{
			if(graph.getTriplesMapIds().contains(triplesMapId))
			{
				return graph;
			}
		}
		return null;
	}
	public void addTriplesMap(TriplesMap triplesMap)
	{
		TriplesMapGraph graph = new TriplesMapGraph();
		graph.addTriplesMap(triplesMap);
		graphs.add(graph);
		return;
	}
	public void addLink(TriplesMapLink link)
	{
		if(graphs.isEmpty())
		{
			TriplesMapGraph graph = new TriplesMapGraph();
			graph.addLink(link);
			graphs.add(graph);
			return;
		}
		List<TriplesMapGraph> graphsForMerging = new LinkedList<TriplesMapGraph>();
		Iterator<TriplesMapGraph> iter = graphs.iterator();
		while(iter.hasNext())
		{
			TriplesMapGraph graph = iter.next();
			List<TriplesMapLink> sourceLinks = graph.getAllNeighboringTriplesMap(link.getSourceMap().getId());
			List<TriplesMapLink> targetLinks = graph.getAllNeighboringTriplesMap(link.getTargetMap().getId());
			if(sourceLinks != null && targetLinks != null)
			{
				graphsForMerging.add(graph);
				iter.remove();
			}
		}
		iter = graphsForMerging.iterator();
		TriplesMapGraph mergedGraph = iter.next();
		TriplesMapGraph graphToMerge = null;
		while(iter.hasNext())
		{
			graphToMerge = iter.next();
			mergedGraph = TriplesMapGraph.mergeGraphs(mergedGraph, graphToMerge);
		}
		mergedGraph.addLink(link);
		graphs.add(mergedGraph);
				
	}
	
	public List<TriplesMapGraph> getGraphs()
	{
		return graphs;
	}
}
