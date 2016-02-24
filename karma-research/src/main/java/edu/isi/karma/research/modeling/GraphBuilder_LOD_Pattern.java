package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphCompact;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.modeling.alignment.learner.PatternWeightSystem;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GraphBuilder_LOD_Pattern {

	private static Logger logger = LoggerFactory.getLogger(GraphBuilder_LOD_Pattern.class);
	private ModelLearningGraph modelLearningGraph;
	private int maxPatternLength;

	public GraphBuilder_LOD_Pattern(OntologyManager ontologyManager, String patternsDir, int maxPatternLength) {
		
		modelLearningGraph = (ModelLearningGraphCompact)
				ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
		
		this.maxPatternLength = maxPatternLength;
		buildGraph(ontologyManager, patternsDir);

	}
	
	public GraphBuilder getGraphBuilder() {
		return this.modelLearningGraph.getGraphBuilder();
	}
	
	public Set<InternalNode> addPatterns(List<Pattern> patterns) {
		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		Set<InternalNode> temp = new HashSet<InternalNode>();
		int count = 0; int length = 0;
		if (patterns == null) return addedNodes;
		if (patterns.size() > 0) length = patterns.get(0).getLength();
		for (Pattern p : patterns) {
			if (modelLearningGraph.contains(p.getGraph())) {
				count++; 
				continue;
			}
			SemanticModel sm = new SemanticModel(p.getId(), p.getGraph(), false);
//			temp = modelLearningGraph.addModel(sm, PatternWeightSystem.Default);
			temp = ((ModelLearningGraphCompact)modelLearningGraph).addLodPattern(sm);
			if (temp != null) addedNodes.addAll(temp);
		}
//		System.out.println("total patterns with length " + length + ": " + patterns.size());
//		System.out.println("duplicate patterns with length " + length + ": " + count);
		return addedNodes;
	}
	
	public void buildGraph(OntologyManager ontologyManager, String patternsDir) {

		@SuppressWarnings("unchecked")
		List<Pattern>[] patternArray = new LinkedList[this.maxPatternLength]; 
		
		for (int i = 1; i <= this.maxPatternLength; i++) {

			List<Pattern> patterns = new LinkedList<Pattern>();
			File f = new File(patternsDir);
			f = new File(f.getAbsoluteFile() + "/" + i);
			File[] files = f.listFiles();
			if (files != null) {
				for (File file : files) {
					Pattern p;
					try {
						p = Pattern.readJson(file.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					patterns.add(p);
				}
			}
			patternArray[i-1] = patterns;
		}
		
		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		Set<InternalNode> temp;
		
		for (int i = this.maxPatternLength; i >= 1; i--) {
			logger.info("adding patterns of length " + i + " ...");
			//TODO: don't add a pattern if it is already in the graph
			temp = this.addPatterns(patternArray[i-1]);
			if (temp != null) addedNodes.addAll(temp);
		}
		
		HashMap<String, Integer> linkFrequency = new HashMap<String, Integer>();
		
		// fetching the frequency of patterns with length 1
		int sumFrequency = 0;
		if (patternArray[0] != null) {
			for (Pattern p : patternArray[0]) {
				if (p == null || p.getGraph() == null) continue;
				if (p.getGraph().edgeSet().size() != 1) continue;
				LabeledLink l = p.getGraph().edgeSet().iterator().next();
				String key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
				linkFrequency.put(key, p.getFrequency());
				sumFrequency += p.getFrequency();
			}
	
		}

		if (this.getGraphBuilder().getGraph() != null) {
			Integer frequency;
			for (DefaultLink l : this.getGraphBuilder().getGraph().edgeSet()) {
				if (l instanceof LabeledLink) {
					String key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
					frequency = linkFrequency.get(key);
					if (frequency == null)
						frequency = 1;
					this.getGraphBuilder().changeLinkWeight(l, 1.0 - ((double) frequency / (double) sumFrequency));
				}
			}
		}
		
//		try {
//			GraphVizUtil.exportJGraphToGraphviz(this.getGraphBuilder().getGraph(), 
//					"LOD Graph", 
//					false, 
//					GraphVizLabelType.LocalId,
//					GraphVizLabelType.LocalUri,
//					true, 
//					true, 
//					Params.GRAPHS_DIR + 
//					"lod.graph.small.dot");
//		} catch (Exception e) {
//			logger.error("error in exporting the alignment graph to graphviz!");
//		}
		
		this.modelLearningGraph.updateGraphUsingOntology(addedNodes);

	}
	
	public void serialize(String graphPath) {

		try {
			GraphVizUtil.exportJGraphToGraphviz(this.getGraphBuilder().getGraph(), 
					"LOD Graph", 
					false, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true, 
					true, 
					graphPath + Params.GRAPH_GRAPHVIZ_FILE_EXT);
			
			GraphUtil.exportJson(this.getGraphBuilder().getGraph(), 
					graphPath, true, true);
			
		} catch (Exception e) {
			logger.error("error in serializing the alignment graph to " + graphPath);
		}
		
	}
	
	public static void main(String[] args) throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().registerByKarmaHome("/Users/mohsen/karma/");
		contextParameters.setParameterValue(ContextParameter.USER_DIRECTORY_PATH, "/Users/mohsen/karma/");
		contextParameters.setParameterValue(ContextParameter.USER_CONFIG_DIRECTORY, "/Users/mohsen/karma/config");
		
		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		if (files == null) {
			logger.error("no ontology to import at " + ff.getAbsolutePath());
			return;
		}

		for (File f : files) {
			if (f.getName().endsWith(".owl") || 
					f.getName().endsWith(".rdf") || 
					f.getName().endsWith(".rdfs") || 
					f.getName().endsWith(".n3") || 
					f.getName().endsWith(".ttl") || 
					f.getName().endsWith(".xml")) {
				System.out.println("Loading ontology file: " + f.getAbsolutePath());
				logger.info("Loading ontology file: " + f.getAbsolutePath());
				ontologyManager.doImport(f, "UTF-8");
			}
		}
		ontologyManager.updateCache(); 
		

		String patternPath = Params.LOD_DIR + "s09-s-18-artists" + "/" + Params.PATTERNS_OUTPUT_DIR;
		GraphBuilder_LOD_Pattern lodPatternGraphBuilder = 
				new GraphBuilder_LOD_Pattern(ontologyManager, patternPath, 3);
		lodPatternGraphBuilder.serialize("test");
		
//		HashMap<String, Integer> opFrequency = new HashMap<String, Integer>();
//		HashMap<String, Integer> dpFrequency = new HashMap<String, Integer>();
//		
//		String key;
//		for (Pattern p : patternsSize2.values()) {
//			for (LabeledLink l : p.getGraph().edgeSet()) {
//				if (l.getTarget() instanceof InternalNode) {
//					key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
//					opFrequency.put(key, p.getFrequency());
//				} else {
//					key = l.getSource().getUri() + l.getUri();
//					dpFrequency.put(key, p.getFrequency());
//				}
//			}
//		}
		
//		Integer frequency;
//		double w;
//		if (modelLearningGraph.getGraphBuilder().getGraph() != null) {
//			for (DefaultLink l : modelLearningGraph.getGraphBuilder().getGraph().edgeSet()) {
//				if (l.getTarget() instanceof InternalNode) {
//					key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
//					frequency = opFrequency.get(key);
//				} else {
//					key = l.getSource().getUri() + l.getUri();
//					frequency = dpFrequency.get(key);
//				}
//				if (frequency != null) {
//					w = 0.0;
//					modelLearningGraph.getGraphBuilder().changeLinkWeight(l, w);
//				} else {
//					logger.info("this should not happen because all the ");
//				}
//			}
//		}
				


		logger.info("finished.");
		
	}
	
}
