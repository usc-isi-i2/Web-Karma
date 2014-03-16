package edu.isi.karma.kr2rml.planning;

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

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.rep.Row;

public class TriplesMapPlanGenerator {
	private static Logger LOG = LoggerFactory.getLogger(TriplesMapPlanGenerator.class);
	
	private Map<TriplesMap, TriplesMapWorkerPlan> triplesMapToWorkerPlan;
	private Set<TriplesMap> visitedMaps = new HashSet<TriplesMap>();
	private Row r;
	private KR2RMLRDFWriter outWriter;
	
	public TriplesMapPlanGenerator(Map<TriplesMap, TriplesMapWorkerPlan> triplesMapToWorkerPlan, Row r, KR2RMLRDFWriter outWriter) {
		
		this.triplesMapToWorkerPlan = triplesMapToWorkerPlan;
		this.r = r;
		this.outWriter = outWriter;
	}

	public TriplesMapPlan generatePlan(TriplesMapGraphMerger tmf)
	{
		List<TriplesMapWorker> workers = new LinkedList<TriplesMapWorker>();
		Map<String, List<PopulatedTemplateTermSet>>triplesMapSubjects = new ConcurrentHashMap<String, List<PopulatedTemplateTermSet>>();
		TriplesMapPlan plan = new TriplesMapPlan(workers, r, triplesMapSubjects);
		
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
		Map<String, List<PopulatedTemplateTermSet>>triplesMapSubjects = new ConcurrentHashMap<String, List<PopulatedTemplateTermSet>>();
		TriplesMapPlan plan = new TriplesMapPlan(workers, r, triplesMapSubjects);
		workers.addAll(generatePlan(graph, plan));
		return plan;
	}
	private Collection<TriplesMapWorker> generatePlan(TriplesMapGraph graph, TriplesMapPlan plan)
	{
		//add strategy
		Map<TriplesMap, TriplesMapWorker> mapToWorker = new HashMap<TriplesMap, TriplesMapWorker>();
		String triplesMapId = graph.findRoot(new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy()));
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
		TriplesMapWorker newWorker = new TriplesMapWorker(map, new CountDownLatch(workersDependentOn.size()), r, triplesMapToWorkerPlan.get(map), outWriter);
		mapToWorker.put(map, newWorker);
		
		for(TriplesMapWorker worker : workersDependentOn)
		{
			worker.addDependentTriplesMapLatch(newWorker.getLatch());
		}
	}
}
