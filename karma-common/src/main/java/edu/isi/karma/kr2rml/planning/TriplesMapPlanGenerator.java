package edu.isi.karma.kr2rml.planning;

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
	private Set<String> unprocessedTriplesMapsIds = new HashSet<String>();
	private Row r;
	private List<KR2RMLRDFWriter> outWriters;
	
	public TriplesMapPlanGenerator(Map<TriplesMap, TriplesMapWorkerPlan> triplesMapToWorkerPlan, Row r, List<KR2RMLRDFWriter> outWriters) {
		
		this.triplesMapToWorkerPlan = triplesMapToWorkerPlan;
		this.r = r;
		this.outWriters = outWriters;
	}

	public TriplesMapPlan generatePlan(TriplesMapGraphMerger tmf)
	{
		List<TriplesMapWorker> workers = new LinkedList<TriplesMapWorker>();
		Map<String, List<PopulatedTemplateTermSet>>triplesMapSubjects = new ConcurrentHashMap<String, List<PopulatedTemplateTermSet>>();
		TriplesMapPlan plan = new TriplesMapPlan(workers, r, triplesMapSubjects);
		
		List<TriplesMapGraph> graphs = tmf.getGraphs();
		for(TriplesMapGraph graph : graphs)
		{
			//This can end up in deadlock.
			workers.addAll(generatePlan(graph, plan).values());
		}
		return plan;
	}

	public TriplesMapPlan generatePlan(TriplesMapGraph graph, List<String> triplesMapProcessingOrder)
	{
		List<TriplesMapWorker> workers = new LinkedList<TriplesMapWorker>();
		Map<String, List<PopulatedTemplateTermSet>>triplesMapSubjects = new ConcurrentHashMap<String, List<PopulatedTemplateTermSet>>();
		TriplesMapPlan plan = new TriplesMapPlan(workers, r, triplesMapSubjects);
		Map<TriplesMap, TriplesMapWorker> mapToWorker = generatePlan(graph, plan);
		for(String triplesMapId : triplesMapProcessingOrder)
		{
			TriplesMap map = graph.getTriplesMap(triplesMapId);
			TriplesMapWorker worker = mapToWorker.get(map);
			if(worker != null)
			{
				workers.add(worker);
			}
			else
			{
				LOG.error("Graph is disconnected from " + triplesMapId );
			}
			
			
		}
		
		return plan;
	}
	private Map<TriplesMap, TriplesMapWorker> generatePlan(TriplesMapGraph graph, TriplesMapPlan plan)
	{
		unprocessedTriplesMapsIds.addAll(graph.getTriplesMapIds());
		//add strategy
		Map<TriplesMap, TriplesMapWorker> mapToWorker = new HashMap<TriplesMap, TriplesMapWorker>();
		String triplesMapId = graph.findRoot(new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy()));
		
		do
		{
			if(triplesMapId == null)
			{
				triplesMapId = unprocessedTriplesMapsIds.iterator().next();
			}
			TriplesMap map = graph.getTriplesMap(triplesMapId);
			generateTriplesMapWorker(mapToWorker, graph, map, plan);	
			triplesMapId = null;
		}
		while(!unprocessedTriplesMapsIds.isEmpty());

		
		return mapToWorker;
	}
	private void generateTriplesMapWorker(
			Map<TriplesMap, TriplesMapWorker> mapToWorker,
			TriplesMapGraph graph, TriplesMap map, TriplesMapPlan plan) {
		
		if(!unprocessedTriplesMapsIds.remove(map.getId()))
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
		TriplesMapWorker newWorker = new TriplesMapWorker(map, new CountDownLatch(workersDependentOn.size()), r, triplesMapToWorkerPlan.get(map), outWriters);
		mapToWorker.put(map, newWorker);
		
		for(TriplesMapWorker worker : workersDependentOn)
		{
			worker.addDependentTriplesMapLatch(newWorker.getLatch());
		}
	}
}
