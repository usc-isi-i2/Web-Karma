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

package edu.isi.karma.modeling.alignment.learner;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.webserver.ContextParametersRegistry;

public class SteinerNodes implements Comparable<SteinerNodes> {

//	private static Logger logger = LoggerFactory.getLogger(SteinerNodes.class);
	private static final double MIN_CONFIDENCE = 1E-6;
	
	private Set<Node> nodes;
	private Map<ColumnNode, ColumnNode> mappingToSourceColumns;
	private Confidence confidence;
	private NodeCoherence nodeCoherence;
//	private int frequency;
	private double score;
	private int semanticTypesCount;
	private int nonModelNodesCount; // nodes that do not belong to any pattern
	private Map<ColumnNode, SemanticTypeMapping> columnNodeInfo;
	private String contextId;
	public SteinerNodes(String contextId) {
		this.contextId = contextId;
		this.nodes = new HashSet<>();
		this.mappingToSourceColumns = new HashMap<>();
		this.columnNodeInfo = new HashMap<>();
		this.semanticTypesCount = 0;
		this.confidence = new Confidence();
		this.nodeCoherence = new NodeCoherence();
		this.nonModelNodesCount = 0;
//		this.frequency = 0;
		this.score = 0.0;
	}
	
	public SteinerNodes(SteinerNodes steinerNodes,String contextId) {
		this.contextId = contextId;
		this.nodes = new HashSet<>(steinerNodes.getNodes());
		this.mappingToSourceColumns = new HashMap<>(steinerNodes.getMappingToSourceColumns());
		this.columnNodeInfo = new HashMap<>(steinerNodes.getColumnNodeInfo());
		this.confidence = new Confidence(steinerNodes.getConfidence());
		this.nodeCoherence = new NodeCoherence(steinerNodes.getCoherence());
//		this.frequency = steinerNodes.getFrequency();
		this.semanticTypesCount = steinerNodes.getSemanticTypesCount();
		this.nonModelNodesCount = steinerNodes.getNonModelNodesCount();
		this.score = steinerNodes.getScore();
	}

	
	public Set<Node> getNodes() {
		return Collections.unmodifiableSet(this.nodes);
	}
	
	public Map<ColumnNode, ColumnNode> getMappingToSourceColumns() {
		return mappingToSourceColumns;
	}

	public int getSemanticTypesCount() {
		return semanticTypesCount;
	}

	public Map<ColumnNode, SemanticTypeMapping> getColumnNodeInfo() {
		return columnNodeInfo;
	}

//	public boolean addNodes(ColumnNode sourceColumn, InternalNode n1, ColumnNode n2, double confidence) {
	public boolean addNodes(SemanticTypeMapping stm) {
		
		ColumnNode sourceColumn = stm.getSourceColumn();
		InternalNode n1 = stm.getSource();
		ColumnNode n2 = stm.getTarget();
		double confidence = stm.getConfidence();
		
		if (this.nodes.contains(n1) && this.nodes.contains(n2))
			return false;
		
		this.semanticTypesCount ++;
		
		if (!this.nodes.contains(n1)) {
			this.nodes.add(n1);
			this.nodeCoherence.updateCoherence(n1);
			if (n1.getModelIds() == null || n1.getModelIds().isEmpty())
				this.nonModelNodesCount ++;
		}
		if (!this.nodes.contains(n2)) {
			this.nodes.add(n2);
			this.nodeCoherence.updateCoherence(n2);
			this.columnNodeInfo.put(n2, stm);
			this.mappingToSourceColumns.put(n2, sourceColumn);
			if (n2.getModelIds() == null || n2.getModelIds().isEmpty())
				this.nonModelNodesCount ++;
		}

		if (confidence <= 0 || confidence > 1)
			confidence = MIN_CONFIDENCE;
		
		this.confidence.addValue(confidence);
		
		this.computeScore();
		
		return true;
		
	}
	
	public boolean addNode(InternalNode n) {
		
		if (this.nodes.contains(n))
			return false;
		
		this.nodes.add(n);
		this.nodeCoherence.updateCoherence(n);
		if (n.getModelIds() == null || n.getModelIds().isEmpty())
				this.nonModelNodesCount ++;

		this.computeScore();
		
		return true;
		
	}
	
	public Confidence getConfidence() {
		return this.confidence;
	}
	
	public int getNodesCount() {
		return this.nodes.size();
	}
	
	public int getNonModelNodesCount() {
		return this.nonModelNodesCount;
	}
	
	public double getScore() {
		return this.score;
	}
	
	public NodeCoherence getCoherence() {
		return this.nodeCoherence;
	}
	
//	public int getFrequency() {
//		return frequency;
//	}
	
//	private int computeFrequency() {
//		int frequency = 0;
//		for (Node n : this.nodes)
//			frequency += n.getPatternIds().size();
//		return frequency;
//	}

//	private double computeConfidenceValue() {
//		
//		if (this.confidenceList.size() == 1)
//			return 1e-10;
//		
//		double confidence = 1.0;
//		
//		for (double d : this.confidenceList) {
//			if (d == 0)
//				confidence *= 1e-10;
//			else
//				confidence *= d;
//		}
//		
//		return confidence;
//	}

	private double getSizeReduction() {
		
		int minSize = this.semanticTypesCount;
		int maxSize = this.semanticTypesCount * 2;
		
		if (maxSize - minSize == 0)
			return 0.0;
		
		//feature scaling: (x - min) / (max - min)
		// here: x: reduction in size --- min reduction: 0 --- max reduction: maxSize - minSize 
		return (double)(maxSize - this.getNodesCount()) / 
				(double)(maxSize - minSize);
		
	}

	
//	private double getHarmonicMean(double[] input) {
//		
//		double result = 0.0;
//		if (input == null)
//			return result;
//		
//		double min = 1E-6;
//		double sum = 0.0;
//		for (double d : input) {
//			if (d <= 0.0) d = min;
//			sum += 1.0 / d;
//		}
//		
//		if (sum == 0.0)
//			return result;
//		
//		result = (double) input.length / sum;
//		return result;
//		
//	}
	
	private double getArithmeticMean(double[] input) {
		
		double result;
		if (input == null)
			return 0.0;
		
		double sum = 0.0;
		for (double d : input) {
			if (d < 0.0) d = 0.0;
			sum += d;
		}
		
		result = sum / (double)input.length;
		return result;
		
	}
	
	private void computeScore() {
		
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(contextId).getKarmaHome());
		
		double confidence = this.confidence.getConfidenceValue();
		double sizeReduction = this.getSizeReduction();
		double coherence = this.nodeCoherence.getCoherenceValue();
		//int frequency = this.getFrequency();
		
		double alpha = modelingConfiguration.getScoringConfidenceCoefficient();
		double beta = modelingConfiguration.getScoringCoherenceSCoefficient();
		double gamma = modelingConfiguration.getScoringSizeCoefficient();
//		
//		this.score = alpha * coherence + 
//				beta * distanceToMaxSize + 
//				gamma * confidence;
		
		double[] measures = new double[3];
		measures[0] = alpha * confidence;
		measures[1] = beta * coherence;
		measures[2] = gamma * sizeReduction;
//		this.score = sizeReduction;
//		this.score = coherence;
//		this.score = confidence;
//		this.score = getHarmonicMean(measures);
		this.score = getArithmeticMean(measures);
	}

	@Override
	public int compareTo(SteinerNodes target) {
		
		double score1 = this.getScore();
		double score2 = target.getScore();
		
		if (score1 < score2)
			return 1;
		else if (score1 > score2)
			return -1;
		else return 0;
	}
	
	
	private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}	

	public String getScoreDetailsString() {
//		this.computeCoherenceList();
		StringBuffer sb = new StringBuffer();
		
//		if (this.nodes != null)
//		for (Node n : this.nodes) {
//			if (n instanceof InternalNode)
//				sb.append(n.getLocalId());
//			else {
//				if (mappingToSourceColumns.containsKey((ColumnNode)n))
//					sb.append(mappingToSourceColumns.get((ColumnNode)n).getColumnName() );
//				else
//					sb.append( ((ColumnNode)n).getColumnName() );
//			}
//			sb.append("|");
//		}
//		sb.append("\n");

		sb.append("\n");
		sb.append("node coherence: ");
		sb.append(nodeCoherence.printCoherenceList());
//		sb.append("\n");
		sb.append("--- value: " + this.nodeCoherence.getCoherenceValue());
		sb.append("\n");
		sb.append("size: " + this.getNodesCount() + ", max size: " + (this.semanticTypesCount * 2) + "---" + 
				"size reduction: " +  roundTwoDecimals(this.getSizeReduction()) );
		sb.append("\n");
		sb.append("confidence list: (");
		for (Double cf : this.confidence.getValues()) {
			if (cf != null)
				sb.append( roundTwoDecimals(cf.doubleValue()) + ",");
		}
		sb.append(") --- ");
		sb.append("confidence: " + roundTwoDecimals(this.confidence.getConfidenceValue()));
		sb.append("\n");
//		sb.append("total number of patterns: " + this.frequency);
//		sb.append("\n");
		sb.append("final score: " + roundTwoDecimals(this.getScore()) + " - [arithmetic mean]");
		sb.append("\n");
		return sb.toString();
	}
		
}
