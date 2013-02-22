/*******************************************************************************
 * Copyright 2012 University of Southern California
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

package edu.isi.karma.modeling.research;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;

public class Approach1 {

	private static Logger logger = Logger.getLogger(Approach1.class);
	private static String ontologyDir = "/Users/mohsen/Dropbox/Service Modeling/ontologies/";
	private static String outputPath = "/Users/mohsen/Dropbox/Service Modeling/output/output.dot";
	private HashMap<String, Integer> sourceTargetLinkCounterMap;

	private List<ServiceModel> trainingData;
	private OntologyManager ontologyManager;
	
	public Approach1(List<ServiceModel> trainingData, OntologyManager ontologyManager) {
		this.trainingData = trainingData;
		this.ontologyManager = ontologyManager;
		this.sourceTargetLinkCounterMap = new HashMap<String, Integer>();
	}
	
	public DirectedWeightedMultigraph<Node, Link> hypothesize(
			DirectedWeightedMultigraph<Node, Link> serviceModel) {
		
		List<SemanticLabel> inSemanticLabels = getModelSemanticLabels(serviceModel);
		if (inSemanticLabels == null || inSemanticLabels.size() == 0) {
			logger.info("The input model does not have any semantic label.");
			return null;
		}
		
		logger.info("=====================================================================");
		logger.info("Input Semantic Labels: ");
		for (SemanticLabel sl: inSemanticLabels)
			sl.print();
		logger.info("=====================================================================");
		
		List<SemanticLabel> outSemanticLabels = new ArrayList<SemanticLabel>();
		
		for (SemanticLabel sl : inSemanticLabels) {

			sl.print();
			logger.info("-------------------------------------------");

			List<SemanticLabel> matchedSemanticLabels = findMatchedSemanticLabels(sl);
			if (matchedSemanticLabels == null || matchedSemanticLabels.size() == 0) {
				logger.info("Cannot find any match for semantic label:");
				sl.print();
				return null;
			}
			
			logger.info("Matched Semantic Labels: ");
			for (SemanticLabel matched: matchedSemanticLabels)
				matched.print();
			logger.info("-------------------------------------------");

			SemanticLabel bestMatch = selectBestMatchedSemanticLabel(matchedSemanticLabels);
			outSemanticLabels.add(bestMatch);

			logger.info("Best Match: ");
			bestMatch.print();
			logger.info("-------------------------------------------");
		}
		
		if (outSemanticLabels == null || outSemanticLabels.size() == 0) {
			logger.info("The output model does not have any semantic label.");
			return null;
		}

		logger.info("=====================================================================");
		logger.info("Output Semantic Labels: ");
		for (SemanticLabel sl: outSemanticLabels)
			sl.print();
		logger.info("=====================================================================");
		
		DirectedWeightedMultigraph<Node, Link> hypothesis = this.buildAlignmentGraph(outSemanticLabels);
		this.buildSourceTargetLinkCounterMap();
		
		return hypothesis;
	}
	
	private List<SemanticLabel> getModelSemanticLabels(
			DirectedWeightedMultigraph<Node, Link> serviceModel) {
		
		List<SemanticLabel> semanticLabels = new ArrayList<SemanticLabel>();

		for (Node n : serviceModel.vertexSet()) {
			if (!(n instanceof ColumnNode) && !(n instanceof LiteralNode)) continue;
			
			Set<Link> incomingLinks = serviceModel.incomingEdgesOf(n);
			if (incomingLinks != null) { // && incomingLinks.size() == 1) {
				Link link = incomingLinks.toArray(new Link[0])[0];
				Node domain = link.getSource();
				
				SemanticLabel sl = new SemanticLabel(domain.getLabel(), link.getLabel(), n.getId());
				semanticLabels.add(sl);
			} 
		}
		return semanticLabels;
	}
	
	private List<SemanticLabel> findMatchedSemanticLabels(SemanticLabel semanticLabel) {

		List<SemanticLabel> matchedSemanticLabels = new ArrayList<SemanticLabel>();
		
		for (ServiceModel sm : trainingData) {
			//sm.computeMatchedSubGraphs(1);
			List<MatchedSubGraphs> matchedSubGraphs = sm.getMatchedSubGraphs();
			SemanticLabel sl1 = null, sl2 = null;
			List<SemanticLabel> slList1 = null, slList2 = null;
			for (MatchedSubGraphs m : matchedSubGraphs) {
				slList1 = getModelSemanticLabels(m.getSubGraph1());
				if (slList1 != null && slList1.size() == 1) sl1 = slList1.get(0); else sl1 = null;
				
				slList2 = getModelSemanticLabels(m.getSubGraph2());
				if (slList2 != null && slList2.size() == 1) sl2 = slList2.get(0); else sl2 = null;
				
				if (sl1 == null || sl2 == null) continue;
				
				if (sl1.compareTo(semanticLabel) == 0) {
					matchedSemanticLabels.add(sl2); 
					logger.info(sm.getServiceNameWithPrefix());
				} else if (sl2.compareTo(semanticLabel) == 0) {
					matchedSemanticLabels.add(sl1);
					logger.info(sm.getServiceNameWithPrefix());
				}
				
			}
		}
		return matchedSemanticLabels;
	}
	
	public SemanticLabel selectBestMatchedSemanticLabel(List<SemanticLabel> matchedSemanticLabels) {
		
		// select the most frequent semantic label from the matched list 

		if (matchedSemanticLabels == null || matchedSemanticLabels.size() == 0)
			return null;
		
		int indexOfMostFrequentSemanticLabel = 0;
		int countOfEqualSemanticLabels = 1;
		int maxNumberOfEqualSemanticLabels = 1;
		
		Collections.sort(matchedSemanticLabels);
		for (int i = 1; i < matchedSemanticLabels.size(); i++) {
			SemanticLabel prev = matchedSemanticLabels.get(i - 1);
			SemanticLabel curr = matchedSemanticLabels.get(i);
			if (curr.compareTo(prev) == 0) { // they are the same
				countOfEqualSemanticLabels ++;
			} else {
				if (countOfEqualSemanticLabels > maxNumberOfEqualSemanticLabels) {
					maxNumberOfEqualSemanticLabels = countOfEqualSemanticLabels;
					indexOfMostFrequentSemanticLabel = i - 1;
					countOfEqualSemanticLabels = 1;
				}
			}
		}
		// check the last item
		if (countOfEqualSemanticLabels > maxNumberOfEqualSemanticLabels) {
			maxNumberOfEqualSemanticLabels = countOfEqualSemanticLabels;
			indexOfMostFrequentSemanticLabel = matchedSemanticLabels.size() - 1;
		}

//		logger.debug("Count of most frequent semantic label is: " + maxNumberOfEqualSemanticLabels);
//		logger.debug("Most frequent semantic label is:");
//		matchedSemanticLabels.get(indexOfMostFrequentSemanticLabel).print();
		return matchedSemanticLabels.get(indexOfMostFrequentSemanticLabel);
	}
	
	private DirectedWeightedMultigraph<Node, Link> buildAlignmentGraph(List<SemanticLabel> semanticLabels) {
		
		Alignment alignment = new Alignment(this.ontologyManager);
		
		for (int i = 0; i < semanticLabels.size(); i++) {
			SemanticLabel sl = semanticLabels.get(i);
			InternalNode n = alignment.addInternalNodeWithoutUpdatingGraph(sl.getNodeLabel());
//			ColumnNode c = alignment.addColumnNode("H" + String.valueOf(i), sl.getLeafName());
			ColumnNode c = alignment.addColumnNodeWithoutUpdatingGraph(sl.getLeafName(), sl.getLeafName());
			DataPropertyLink link = alignment.addDataPropertyLink(n, c, sl.getLinkLabel(), false);
			break;
		}
		
		alignment.updateGraph();
		
		updateWeights(alignment.getGraph(), getInitialLinkWeight());
		alignment.align();
		DirectedWeightedMultigraph<Node, Link> steinerTree = alignment.getSteinerTree();
		return steinerTree;
	}
	
	private double getInitialLinkWeight() {
		double w = 0;
		for (ServiceModel sm : this.trainingData) {
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(1);
			w += m.edgeSet().size();
		}
		return w;
	}
	
	private void updateWeights(DirectedWeightedMultigraph<Node, Link> graph, double initialWeight) {
		String key;
		Integer count;
		double w;
		for (Link link : graph.edgeSet()) {
			
			w = initialWeight;
			
			key = link.getSource().getLabel().getUri() + 
					link.getTarget().getLabel().getUri() +
					link.getLabel().getUri();
			
			count = this.sourceTargetLinkCounterMap.get(key);
			if (count != null) w -= count;
			graph.setEdgeWeight(link, w);
		}
	}
	
	private void buildSourceTargetLinkCounterMap() {
		Integer count;
		for (ServiceModel sm : this.trainingData) {
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(1);
			for (Link link : m.edgeSet()) {
				Node source = link.getSource();
				Node target = link.getTarget();
				if (source instanceof InternalNode && target instanceof InternalNode) {
					String key = source.getLabel().getUri() +
								 target.getLabel().getUri() + 
								 link.getLabel().getUri();
					count = sourceTargetLinkCounterMap.get(key);
					if (count == null) sourceTargetLinkCounterMap.put(key, 1);
					else sourceTargetLinkCounterMap.put(key, ++count);
				}
			}
		}
	}
	
	private static void testSelectionOfBestMatch() {
		
		Label nodeLabel1 = new Label("n2"); Label linkLabel1 = new Label("e1");
		Label nodeLabel2 = new Label("n2"); Label linkLabel2 = new Label("e4");
		Label nodeLabel3 = new Label("n2"); Label linkLabel3 = new Label("e2");
		Label nodeLabel4 = new Label("n1"); Label linkLabel4 = new Label("e1");
		Label nodeLabel5 = new Label("n1"); Label linkLabel5 = new Label("e1");
		Label nodeLabel6 = new Label("n2"); Label linkLabel6 = new Label("e3");
		Label nodeLabel7 = new Label("n3"); Label linkLabel7 = new Label("e3");
		Label nodeLabel8 = new Label("n4"); Label linkLabel8 = new Label("e4");
		Label nodeLabel9 = new Label("n3"); Label linkLabel9 = new Label("e3");

		
		SemanticLabel sl1 = new SemanticLabel(nodeLabel1, linkLabel1, "1");
		SemanticLabel sl2 = new SemanticLabel(nodeLabel2, linkLabel2, "2");
		SemanticLabel sl3 = new SemanticLabel(nodeLabel3, linkLabel3, "3");
		SemanticLabel sl4 = new SemanticLabel(nodeLabel4, linkLabel4, "4");
		SemanticLabel sl5 = new SemanticLabel(nodeLabel5, linkLabel5, "5");
		SemanticLabel sl6 = new SemanticLabel(nodeLabel6, linkLabel6, "6");
		SemanticLabel sl7 = new SemanticLabel(nodeLabel7, linkLabel7, "7");
		SemanticLabel sl8 = new SemanticLabel(nodeLabel8, linkLabel8, "8");
		SemanticLabel sl9 = new SemanticLabel(nodeLabel9, linkLabel9, "9");
		
		List<SemanticLabel> semanticLabels = new ArrayList<SemanticLabel>();
		semanticLabels.add(sl1); semanticLabels.add(sl2); semanticLabels.add(sl3);
		semanticLabels.add(sl4); semanticLabels.add(sl5); semanticLabels.add(sl6);
		semanticLabels.add(sl7); semanticLabels.add(sl8); semanticLabels.add(sl9);
		
		new Approach1(null, null).selectBestMatchedSemanticLabel(semanticLabels);
	}

	private static void testApproach() throws IOException {
		
		List<ServiceModel> serviceModels = ModelReader.importServiceModels();
		List<ServiceModel> trainingData = new ArrayList<ServiceModel>();
		
		int inputModelIndex = 0;
		for (int i = 0; i < serviceModels.size(); i++) {
			serviceModels.get(i).computeMatchedSubGraphs(1);
			if (i != inputModelIndex) trainingData.add(serviceModels.get(i));
		}
		
		DirectedWeightedMultigraph<Node, Link> inputModel = serviceModels.get(inputModelIndex).getModels().get(0);
		DirectedWeightedMultigraph<Node, Link> oututModel = serviceModels.get(inputModelIndex).getModels().get(1);
		DirectedWeightedMultigraph<Node, Link> hypothesis;

		OntologyManager ontManager = new OntologyManager();
		ontManager.doImport(new File(Approach1.ontologyDir + "dbpedia_3.8.owl"));
		ontManager.doImport(new File(Approach1.ontologyDir + "foaf.rdf"));
		ontManager.doImport(new File(Approach1.ontologyDir + "geonames.rdf"));
		ontManager.doImport(new File(Approach1.ontologyDir + "wgs84_pos.xml"));
		ontManager.doImport(new File(Approach1.ontologyDir + "helper.owl"));
		ontManager.updateCache();
//		ontManager.doImport(new File(this.ontologyDir + "wgs84_pos.xml"));
//		ontManager.doImport(new File(this.ontologyDir + "schema.rdf"));
		
		Approach1 app = new Approach1(trainingData, ontManager);
		hypothesis = app.hypothesize(inputModel);
		
		GraphUtil.printGraph(hypothesis);
		GraphUtil.printGraphSimple(hypothesis);
		
		GraphVizUtil.exportJGraphToGraphvizFile(hypothesis, outputPath);
	}
	
	public static void main(String[] args) {
		
		try {
//			testSelectionOfBestMatch();
			testApproach();
		} catch (Exception e) {
			
		}
	}
}
