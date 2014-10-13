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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TriplesMapGraphMerger {

	protected List<TriplesMapGraph> graphs;
	
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
			TriplesMap source = graph.getTriplesMap(link.getSourceMap().getId());
			TriplesMap target = graph.getTriplesMap(link.getTargetMap().getId());
			if(source != null || target != null)
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
