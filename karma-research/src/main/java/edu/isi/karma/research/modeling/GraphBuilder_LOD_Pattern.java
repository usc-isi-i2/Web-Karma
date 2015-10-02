package edu.isi.karma.research.modeling;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class GraphBuilder_LOD_Pattern {

	private static Logger logger = LoggerFactory.getLogger(GraphBuilder_LOD_Pattern.class);
	private ModelLearningGraph modelLearningGraph;
	private int maxPatternSize;

	public GraphBuilder_LOD_Pattern(OntologyManager ontologyManager, String patternsDir, int maxPatternSize) {
		
		modelLearningGraph = (ModelLearningGraphCompact)
				ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
		
		this.maxPatternSize = maxPatternSize;
		buildGraph(ontologyManager, patternsDir);

	}
	
	public GraphBuilder getGraphBuilder() {
		return this.modelLearningGraph.getGraphBuilder();
	}
	
	public Set<InternalNode> addPatterns(Map<String, Pattern> patterns) {
		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		Set<InternalNode> temp = new HashSet<InternalNode>();
		for (Pattern p : patterns.values()) {
			SemanticModel sm = new SemanticModel(p.getId(), p.getGraph(), false);
			temp = modelLearningGraph.addModel(sm, true);
			if (temp != null) addedNodes.addAll(temp);
		}
		return addedNodes;
	}

	public void buildGraph(OntologyManager ontologyManager, String patternsDir) {

		Map<String, Pattern> patterns = PatternReader.importPatterns(patternsDir);
		
		Map<String, Pattern> patternsSize2 = new HashMap<String, Pattern>(); 
		Map<String, Pattern> patternsSize3 = new HashMap<String, Pattern>(); 
		Map<String, Pattern> patternsSize4 = new HashMap<String, Pattern>(); 

		for (Pattern p : patterns.values()) {
			if (p != null && p.getGraph() != null) {
				if (p.getSize() == 2) patternsSize2.put(p.getId(), p);
				else if (p.getSize() == 3) patternsSize3.put(p.getId(), p);
				else if (p.getSize() == 4) patternsSize4.put(p.getId(), p);
			}
		}

		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		Set<InternalNode> temp;
		
		if (this.maxPatternSize >= 4) {
			logger.info("adding patterns of size 4 ...");
			temp = this.addPatterns(patternsSize4);
			if (temp != null) addedNodes.addAll(temp);
		}

		if (this.maxPatternSize >= 3) {
			logger.info("adding patterns of size 3 ...");
			temp = this.addPatterns(patternsSize3);
			if (temp != null) addedNodes.addAll(temp);
		}
		
		if (this.maxPatternSize >= 2) {
			logger.info("adding patterns of size 2 ...");
			temp = this.addPatterns(patternsSize2);
			if (temp != null) addedNodes.addAll(temp);
		}

		try {
			GraphVizUtil.exportJGraphToGraphviz(this.getGraphBuilder().getGraph(), 
					"LOD Graph", 
					false, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true, 
					true, 
					Params.GRAPHS_DIR + 
					"lod.graph.small.dot");
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}

		this.modelLearningGraph.updateGraphUsingOntology(addedNodes);

		try {
			GraphVizUtil.exportJGraphToGraphviz(this.getGraphBuilder().getGraph(), 
					"LOD Graph", 
					false, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true, 
					true, 
					Params.GRAPHS_DIR + "lod.graph.dot");
			GraphUtil.exportJson(this.getGraphBuilder().getGraph(), 
					Params.GRAPHS_DIR + "lod" + Params.GRAPH_FILE_EXT, true, true);
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}
		
	}
	
	public static void main(String[] args) throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		
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
		

//		GraphBuilder_LOD_Pattern lodPatternGraphBuilder = 
				new GraphBuilder_LOD_Pattern(ontologyManager, Params.PATTERNS_DIR, 2);

		
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
