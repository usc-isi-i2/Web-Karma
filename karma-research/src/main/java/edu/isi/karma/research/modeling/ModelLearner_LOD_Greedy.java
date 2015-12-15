package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.alignment.TreePostProcess;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class ModelLearner_LOD_Greedy {

	static Logger logger = LoggerFactory.getLogger(ModelLearner_LOD_Greedy.class);
	private Map<String, Pattern> patterns; // patternId to pattern map
	private Map<String, Set<String>> patternIndex; // type to pattern map
	private OntologyManager ontologyManager;
	
	public ModelLearner_LOD_Greedy(String patternDirectoryPath, OntologyManager ontologyManager) {
		
		if (ontologyManager == null) {
			logger.warn("ontology manager is null.");
		}
		this.ontologyManager = ontologyManager;
		
		logger.info("importing patterns ...");
		long timeStart = System.currentTimeMillis();
		patterns = PatternReader.importPatterns(patternDirectoryPath, null);
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
//		Multiset<String> coveredTypes = HashMultiset.create();
		
		for (Pattern p : sortedPatterns) {
			Multiset<String> patternTypes = HashMultiset.create(p.getTypes());
			Multiset<String> patternCommonTypes = Multisets.intersection(patternTypes, sourceTypes);
//			if (Multisets.containsOccurrences(coveredTypes, patternCommonTypes)) // this pattern does not cover any new source type
			if (patternCommonTypes.size() == 0) // this pattern does not cover any new source type
				continue;
			else {
				minimalSet.add(p);
//				coveredTypes.addAll(patternCommonTypes);
				Multisets.removeOccurrences(sourceTypes, patternCommonTypes);
			}
//			if (Multisets.containsOccurrences(coveredTypes, sourceTypes))
			if (sourceTypes.size() == 0)
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
	
	public GraphBuilder addOntologyPaths(DirectedWeightedMultigraph<Node, LabeledLink> graph) {
		
		logger.info("graph nodes before using ontology: " + graph.vertexSet().size());
		logger.info("graph links before using ontology: " + graph.edgeSet().size());

		GraphBuilder graphBuilder = new GraphBuilder(ontologyManager, false);
		for (Node n : graph.vertexSet()) {
			graphBuilder.addNodeAndUpdate(n);
		}
		for (DefaultLink l : graph.edgeSet()) {
			graphBuilder.addLink(l.getSource(), l.getTarget(), l, l.getWeight());
		}

		logger.info("graph nodes after using ontology: " + graphBuilder.getGraph().vertexSet().size());
		logger.info("graph links after using ontology: " + graphBuilder.getGraph().edgeSet().size());

		return graphBuilder;
	}
	
	public DirectedWeightedMultigraph<Node, LabeledLink> getSteinerTree(GraphBuilder graphBuilder, List<Node> steinerNodes) {
		SteinerTree steinerTree = new SteinerTree(
				new AsUndirectedGraph<Node, DefaultLink>(graphBuilder.getGraph()), steinerNodes);
		WeightedMultigraph<Node, DefaultLink> t = steinerTree.getDefaultSteinerTree();
		TreePostProcess treePostProcess = new TreePostProcess(graphBuilder, t);
		return treePostProcess.getTree();
	}
	
	public SemanticModel hypothesize(List<ColumnNode> columnNodes) {
		
		if (patterns == null || patternIndex == null) {
			logger.error("no pattern/patternIndex found.");
			return null; 
		}

		if (columnNodes == null || columnNodes.isEmpty()) {
			logger.error("column nodes list is empty.");
			return null; 
		}
		
		HashMultimap<String, ColumnNode> typeColumnNodes = HashMultimap.create();
		List<String> types = new LinkedList<String>();
		for (ColumnNode cn : columnNodes) {
			if (cn.getSemanticTypeStatus() != ColumnSemanticTypeStatus.UserAssigned) 
				continue;
			SemanticType st = cn.getUserSemanticTypes().get(0);
			String domain = st.getDomain() == null ? null : st.getDomain().getUri();
			if (domain == null) {
				logger.warn("the domain is null for the semantic type: " + st.getModelLabelString());
				continue;
			}
			types.add(domain);
			typeColumnNodes.put(domain, cn);
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

		if (this.ontologyManager == null) {
			SemanticModel sm = new SemanticModel("", graph);
			return sm;
		}

		logger.info("add the paths from ontology ...");
		GraphBuilder graphBuilder = addOntologyPaths(graph);
		long timeAddOntologyPaths = System.currentTimeMillis();
		elapsedTimeSec = (timeAddOntologyPaths - timeCombinePatterns)/1000F;
		logger.info("time to combine patterns: " + elapsedTimeSec + "s");

		logger.info("compute steiner tree ...");
		List<Node> steinerNodes = new LinkedList<Node>(); 
		for (Node n : graph.vertexSet()) {
			if (n instanceof InternalNode) {
				if (typeColumnNodes.containsKey(n.getUri()) &&
						typeColumnNodes.get(n.getUri()).iterator().hasNext()) {
					ColumnNode cn = typeColumnNodes.get(n.getUri()).iterator().next();
					typeColumnNodes.remove(n.getUri(), cn);
					steinerNodes.add(n);
				}
			}
		}
		DirectedWeightedMultigraph<Node, LabeledLink> tree = getSteinerTree(graphBuilder, steinerNodes);
		long timeComputeSteinerNodes = System.currentTimeMillis();
		elapsedTimeSec = (timeComputeSteinerNodes - timeAddOntologyPaths)/1000F;
		logger.info("time to combine patterns: " + elapsedTimeSec + "s");

		// add the column nodes
//		String nodeUri, propertyUri;
//		Node source, target;
//		List<Node> graphNodes = new LinkedList<Node>(); 
//		for (Node n : graph.vertexSet()) {
//			graphNodes.add(n);
//		}
//		for (Node n : graphNodes) {
//			if (n instanceof InternalNode) {
//				nodeUri = n.getUri();
//				if (typeColumnNodes.containsKey(nodeUri) &&
//						typeColumnNodes.get(nodeUri).iterator().hasNext()) {
//					ColumnNode cn = typeColumnNodes.get(nodeUri).iterator().next();
//					typeColumnNodes.remove(nodeUri, cn);
//					
//					graph.addVertex(cn);
//					source = n;
//					target = cn;
//					propertyUri = cn.getUserSemanticTypes().get(0).getType().getUri();
//					LabeledLink l = new DataPropertyLink(
//							LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId()), 
//							new Label(propertyUri));
//					graph.addEdge(source, target, l);
//				}
//			}
//		}
		
		SemanticModel sm = new SemanticModel("", tree);
		return sm;

	}
	
	private static double roundDecimals(double d, int k) {
		String format = "";
		for (int i = 0; i < k; i++) format += "#";
		DecimalFormat DForm = new DecimalFormat("#." + format);
		return Double.valueOf(DForm.format(d));
	}
	
	public static void simpleTest() {
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

		List<ColumnNode> columnNodes = new LinkedList<ColumnNode>();
		for (SemanticType st : types) {
			ColumnNode cn = new ColumnNode(null, null, null, null);
			cn.assignUserType(st);
			columnNodes.add(cn);
		}
		
		ModelLearner_LOD_Greedy ml = new ModelLearner_LOD_Greedy(Params.PATTERNS_INPUT_DIR, null);
		SemanticModel sm = ml.hypothesize(columnNodes);
		String output = Params.RESULTS_DIR + "out.dot";

		if (sm == null) {
			logger.info("could not learn any model for the source");
			return ;
		}
		
		// sm.print();
		try {
			sm.writeGraphviz(output, true, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("result is ready at: " + output);

	}
	
	public static void main(String[] args) throws Exception {
		
//		simpleTest();
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
					f.getName().endsWith(".n3") || 
					f.getName().endsWith(".ttl") || 
					f.getName().endsWith(".xml")) {
				logger.info("Loading ontology file: " + f.getAbsolutePath());
				ontologyManager.doImport(f, "UTF-8");
			}
		}
		ontologyManager.updateCache(); 
		
//		LODGreedyModelLearner modelLearner = new LODGreedyModelLearner(Params.PATTERNS_DIR, ontologyManager);
		ModelLearner_LOD_Greedy modelLearner = new ModelLearner_LOD_Greedy(Params.PATTERNS_INPUT_DIR, null);

		String outputPath = Params.OUTPUT_DIR;

		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		boolean randomModel = false;
		boolean useCorrectType = true;
		int numberOfCRFCandidates = 1;
		String filePath = Params.RESULTS_DIR;
		String filename = "";
		filename += "results,k=" + numberOfCRFCandidates;
		filename += useCorrectType ? "-correct types":"";
		filename += randomModel ? "-random":"";
		filename += ".csv"; 
		PrintWriter resultFile = new PrintWriter(new File(filePath + filename));

		resultFile.println("source \t p \t r \t t \n");

		for (int i = 0; i < semanticModels.size(); i++) {
//		for (int i = 0; i <= 10; i++) {
//		int i = 1; {

			int newSourceIndex = i;
			SemanticModel newSource = semanticModels.get(newSourceIndex);

			logger.info("======================================================");
			logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			logger.info("======================================================");


			SemanticModel correctModel = newSource;
			List<ColumnNode> columnNodes = correctModel.getColumnNodes();

			long start = System.currentTimeMillis();

			SemanticModel sm = modelLearner.hypothesize(columnNodes);

			long elapsedTimeMillis = System.currentTimeMillis() - start;
			float elapsedTimeSec = elapsedTimeMillis/1000F;

			List<SortableSemanticModel> topHypotheses = new LinkedList<SortableSemanticModel>();
			if (sm != null) topHypotheses.add(new SortableSemanticModel(sm, false));

			Map<String, SemanticModel> models = 
					new TreeMap<String, SemanticModel>();

			ModelEvaluation me;
			models.put("1-correct model", correctModel);
			if (topHypotheses != null)
				for (int k = 0; k < topHypotheses.size(); k++) {

					SortableSemanticModel m = topHypotheses.get(k);

					me = m.evaluate(correctModel);

					String label = "candidate" + k + 
							(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
							"cost:" + roundDecimals(m.getCost(), 6) + 
							//								"-distance:" + me.getDistance() + 
							"-precision:" + me.getPrecision() + 
							"-recall:" + me.getRecall();

					models.put(label, m);

					if (k == 0) { // first rank model
						System.out.println("precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec);
						logger.info("precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec);
						String s = newSource.getName() + "\t" + me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec;
						resultFile.println(s);

					}
				}

			String outName = outputPath + newSource.getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT;

			GraphVizUtil.exportSemanticModelsToGraphviz(
					models, 
					newSource.getName(),
					outName,
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true,
					true);

		}

		resultFile.close();

	}
		
}
