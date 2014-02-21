package edu.isi.karma.kr2rml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TriplesMapForest {

	List<TriplesMapGraph> graphs;
	
	public TriplesMapForest()
	{
		graphs = new LinkedList<TriplesMapGraph>();
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
