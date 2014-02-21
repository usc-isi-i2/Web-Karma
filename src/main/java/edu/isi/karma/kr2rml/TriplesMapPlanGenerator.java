package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator.TriplesMapWorker;
import edu.isi.karma.rep.Row;

public class TriplesMapPlanGenerator {
	private static Logger LOG = LoggerFactory.getLogger(TriplesMapPlanGenerator.class);
	
	public Set<TriplesMap> visitedMaps = new HashSet<TriplesMap>();
	public KR2RMLWorksheetRDFGenerator generator;
	public Row r;
	
	public TriplesMapPlanGenerator(KR2RMLWorksheetRDFGenerator generator, Row r) {
		
		this.generator = generator;
		this.r = r;
	}

	public TriplesMapPlan generatePlan(TriplesMapForest tmf)
	{
		List<TriplesMapWorker> workers = new LinkedList<TriplesMapWorker>();
		Map<String, List<String>>triplesMapURIs = new ConcurrentHashMap<String, List<String>>();
		TriplesMapPlan plan = new TriplesMapPlan(workers, r, triplesMapURIs);
		
		List<TriplesMapGraph> graphs = tmf.getGraphs();
		for(TriplesMapGraph graph : graphs)
		{
			workers.addAll(generatePlan(graph, plan));
		}
		return plan;
	}

	public TriplesMapPlan generatePlan(TriplesMapGraph graph)
	{
		List<TriplesMapWorker> workers = new LinkedList<TriplesMapWorker>();
		Map<String, List<String>>triplesMapURIs = new ConcurrentHashMap<String, List<String>>();
		TriplesMapPlan plan = new TriplesMapPlan(workers, r, triplesMapURIs);
		workers.addAll(generatePlan(graph, plan));
		return plan;
	}
	private Collection<TriplesMapWorker> generatePlan(TriplesMapGraph graph, TriplesMapPlan plan)
	{
		//add strategy
		Map<TriplesMap, TriplesMapWorker> mapToWorker = new HashMap<TriplesMap, TriplesMapWorker>();
		String triplesMapId = graph.findRoot(new SteinerTreeRootStrategy(new WorksheetDepthTreeRootStrategy()));
		TriplesMap map = graph.getTriplesMap(triplesMapId);
		generateTriplesMapWorker(mapToWorker, graph, map, plan);
		return mapToWorker.values();
	}
	private void generateTriplesMapWorker(
			Map<TriplesMap, TriplesMapWorker> mapToWorker,
			TriplesMapGraph graph, TriplesMap map, TriplesMapPlan plan) {
		
		if(!visitedMaps.add(map))
		{
			LOG.error("already visited " + map.toString());
			return;
		}
		List<TriplesMapLink> links = graph.getAllNeighboringTriplesMap(map.getId());
		
		
		List<TriplesMapWorker> workersDependentOn = new LinkedList<TriplesMapWorker>();
		for(TriplesMapLink link : links)
		{
			if((link.getSourceMap() == map && !link.isFlipped()) || (link.getTargetMap() == map && link.isFlipped()))
			{
				TriplesMap mapDependedOn = link.getSourceMap()==map? link.getTargetMap() : link.getSourceMap(); 
				if(!mapToWorker.containsKey(mapDependedOn))
				{
					generateTriplesMapWorker(mapToWorker, graph, mapDependedOn, plan);
				}
				workersDependentOn.add(mapToWorker.get(mapDependedOn));
			}
		}
		//TODO this is garbage
		TriplesMapWorker newWorker = generator.new TriplesMapWorker(map, new CountDownLatch(workersDependentOn.size()), r, plan.triplesMapURIs);
		mapToWorker.put(map, newWorker);
		
		for(TriplesMapWorker worker : workersDependentOn)
		{
			worker.addDependentTriplesMapLatch(newWorker.getLatch());
		}
	}
}
