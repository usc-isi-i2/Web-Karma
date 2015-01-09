package edu.isi.karma.research.lod.pattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;

public class LODGreedyModelLearner {

	static Logger logger = LoggerFactory.getLogger(LODGreedyModelLearner.class);
	Map<String, Pattern> patterns; // patternId to pattern map
	Map<String, Set<String>> patternIndex; // type to pattern map
	
	public LODGreedyModelLearner(String patternDirectoryPath) {
		logger.info("importing patterns ...");
		long timeStart = System.currentTimeMillis();
		patterns = PatternReader.importPatterns(patternDirectoryPath);
		long timeImportPatterns = System.currentTimeMillis();
		float elapsedTimeSec = (timeImportPatterns - timeStart)/1000F;
		logger.info("time to import the patterns: " + elapsedTimeSec + "s");

		if (patterns == null) {
			logger.error("no pattern imported.");
			return;
		}
		
		logger.info("creating index over patterns ...");
		patternIndex = PatternReader.createPatternIndex(patterns.values());
		long timeIndexPatterns = System.currentTimeMillis();
		elapsedTimeSec = (timeIndexPatterns - timeImportPatterns)/1000F;
		logger.info("time to create index for the patterns: " + elapsedTimeSec + "s");
	}
	
	private Set<Pattern> findRelatedPatterns(List<String> types, Map<String, Pattern> patterns, Map<String, Set<String>> patternIndex) {
		HashSet<Pattern> relatedPatterns = new HashSet<Pattern>();
		if (types == null || patternIndex == null) {
			logger.info("either list of the semantic types or the pattern index is empty");
			return relatedPatterns;
		}
		
		for (String t : types) {
			Set<String> patternIds = patternIndex.get(t);
			if (patternIds != null) {
				for (String patternId : patternIds) {
					relatedPatterns.add(patterns.get(patternId));
				}
			}
		}
		return relatedPatterns;
	}
	
	private List<Pattern> sortPatterns(Set<Pattern> patterns, List<String> types) {
		List<Pattern> patternsList = new ArrayList<Pattern>(patterns);
		Collections.sort(patternsList, new PatternComparator(types));
		return patternsList;
	}
	
	
	private List<Pattern> findMinimalCoveringSet(List<Pattern> sortedPatterns, List<String> types) {

		List<Pattern> minimalSet = new LinkedList<Pattern>();
		
		Multiset<String> sourceTypes = HashMultiset.create(types);
		Multiset<String> coveredTypes = HashMultiset.create();
		
		for (Pattern p : sortedPatterns) {
			Multiset<String> patternTypes = HashMultiset.create(p.getTypes());
			Multiset<String> patternCommonTypes = Multisets.intersection(patternTypes, sourceTypes);
			if (Multisets.containsOccurrences(coveredTypes, patternCommonTypes)) // this pattern does not cover any new source type
				continue;
			else {
				minimalSet.add(p);
				coveredTypes.addAll(patternCommonTypes);
			}
			if (Multisets.containsOccurrences(coveredTypes, sourceTypes))
				break;
		}
		return minimalSet;
	}
	
	private DirectedWeightedMultigraph<Node, LabeledLink> combinePatterns(List<Pattern> patterns) {
		
		DirectedWeightedMultigraph<Node, LabeledLink> graph = 
				new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);
		
		Set<String> nodeIds = new HashSet<String>();
		Set<String> edgeIds = new HashSet<String>();
		
		Node source, target;
		
		HashMap<String, Integer> linkIdMap = 
			new HashMap<String, Integer>();
		
		for (Pattern p : patterns) {
			DirectedWeightedMultigraph<Node, LabeledLink> g = p.getGraph();
			for (LabeledLink l : g.edgeSet()) {
				source = l.getSource();
				target = l.getTarget();
				
				if (!nodeIds.contains(source.getId())) {
					graph.addVertex(source);
					nodeIds.add(source.getId());
				} 
				if (!nodeIds.contains(target.getId())) {
					graph.addVertex(target);
					nodeIds.add(target.getId());
				}
				if (!edgeIds.contains(l.getId())) {
					graph.addEdge(source, target, l);
					graph.setEdgeWeight(l, l.getWeight());
					linkIdMap.put(l.getId(), 1);
				} else {
					Integer count = linkIdMap.get(l.getId());
					linkIdMap.put(l.getId(), ++count);
					LabeledLink newLink = l.copy(l.getId() + count);
					graph.addEdge(source, target, newLink);
					graph.setEdgeWeight(newLink, newLink.getWeight());
				}
			}
		}
		
		return graph;
	}
	
	public SemanticModel learn(List<SemanticType> semanticTypes) {
		
		if (patterns == null || patternIndex == null) {
			logger.error("no pattern/patternIndex found.");
			return null; 
		}

		if (semanticTypes == null || semanticTypes.isEmpty()) {
			logger.error("semantic type list is empty.");
			return null; 
		}

		List<String> types = new LinkedList<String>(); 
		for (SemanticType st : semanticTypes) {
			String domain = st.getDomain() == null ? null : st.getDomain().getUri();
			if (domain == null) {
				logger.warn("the domain is null for the semantic type: " + st.getModelLabelString());
				continue;
			}
			types.add(domain);
		}
		if (types.isEmpty()) {
			logger.error("semantic type list is empty.");
			return null;
		}

		logger.info("finding related patterns ...");
		long start = System.currentTimeMillis();
		Set<Pattern> relatedPatterns = findRelatedPatterns(types, patterns, patternIndex);
		long timeFindRelatedPatterns = System.currentTimeMillis();
		float elapsedTimeSec = (timeFindRelatedPatterns - start)/1000F;
		logger.info("time to find related patterns: " + elapsedTimeSec + "s");

		if (relatedPatterns == null || relatedPatterns.isEmpty()) {
			logger.info("no related pattern found for the source.");
			return null;
		}
		
		logger.info("sorting related patterns ...");
		List<Pattern> sortedPatterns = sortPatterns(relatedPatterns, types);
		long timeSortPatterns = System.currentTimeMillis();
		elapsedTimeSec = (timeSortPatterns - timeFindRelatedPatterns)/1000F;
		logger.info("time to sort related patterns: " + elapsedTimeSec + "s");

		logger.info("finding minimal set of patterns with maximum covering of source types ...");
		List<Pattern> minimalSet = findMinimalCoveringSet(sortedPatterns, types);
		long timeFindMinimalCover = System.currentTimeMillis();
		elapsedTimeSec = (timeFindMinimalCover - timeSortPatterns)/1000F;
		logger.info("time to find minimal set: " + elapsedTimeSec + "s");

		if (minimalSet == null || minimalSet.isEmpty()) {
			logger.error("cannot find a pattern set that covers at least one source type, this should not conceptually happen if we have even one related pattern");
			return null;
		}
		
		logger.info("combining patterns ...");
		DirectedWeightedMultigraph<Node, LabeledLink> graph = combinePatterns(minimalSet);
		long timeCombinePatterns = System.currentTimeMillis();
		elapsedTimeSec = (timeCombinePatterns - timeFindMinimalCover)/1000F;
		logger.info("time to combine patterns: " + elapsedTimeSec + "s");

		if (graph == null || graph.edgeSet().isEmpty()) {
			logger.error("graph is null, this should not conceptually happen if we have even one related pattern");
			return null;
		}

		SemanticModel sm = new SemanticModel("", graph);
		return sm;

	}
	
	public static void main(String[] args) {
		
		String patternDirectoryPath = "/Users/mohsen/Dropbox/Source Modeling/datasets/lod-bm-sample/patterns/";
		String resultsPath = "/Users/mohsen/Dropbox/Source Modeling/datasets/lod-bm-sample/results/";
		LODGreedyModelLearner ml = new LODGreedyModelLearner(patternDirectoryPath);
		
		// get list of semantic types for a source
		List<SemanticType> types = new ArrayList<SemanticType>();
		SemanticType st1 = new SemanticType("", null, new Label("http://erlangen-crm.org/current/E39_Actor"), Origin.User, 1.0);
		SemanticType st2 = new SemanticType("", null, new Label("http://erlangen-crm.org/current/E22_Man-Made_Object"), Origin.User, 1.0);
		SemanticType st3 = new SemanticType("", null, new Label("http://erlangen-crm.org/current/E21_Person"), Origin.User, 1.0);
		SemanticType st4 = new SemanticType("", null, new Label("http://erlangen-crm.org/current/E55_Type"), Origin.User, 1.0);
		SemanticType st5 = new SemanticType("", null, new Label("http://www.w3.org/2004/02/skos/core#Concept"), Origin.User, 1.0);
		types.add(st1); 
		types.add(st2);
		types.add(st3);
		types.add(st4);
		types.add(st5);

		SemanticModel sm = ml.learn(types);
		String output = resultsPath + "out.dot";
		logger.info("result is ready at: " + output);

		// sm.print();
		try {
			sm.writeGraphviz(output, true, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
