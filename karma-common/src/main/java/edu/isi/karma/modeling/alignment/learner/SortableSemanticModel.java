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

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.rep.alignment.LabeledLink;

public class SortableSemanticModel extends SemanticModel
	implements Comparable<SortableSemanticModel>{
	
	private double cost;
	private SteinerNodes steinerNodes;
	private LinkCoherence linkCoherence;
	
	public SortableSemanticModel(SemanticModel semanticModel, SteinerNodes steinerNodes, boolean interactiveMode) {
		
		super(semanticModel);
		
		this.steinerNodes = steinerNodes;
		this.linkCoherence = new LinkCoherence(interactiveMode);
		
		if (this.graph != null && !this.graph.edgeSet().isEmpty()) {
			this.cost = this.computeCost();
			this.computeCoherence();
		}
	}
	
	public SortableSemanticModel(SemanticModel semanticModel, boolean interactiveMode) {
		
		super(semanticModel);
		this.linkCoherence = new LinkCoherence(interactiveMode);

		if (this.graph != null && !this.graph.edgeSet().isEmpty()) {
			this.cost = this.computeCost();
			this.computeCoherence();
		}
	}

	public SemanticModel getBaseModel() {
		return new SemanticModel(this);
	}
	
	public double getCost() {
		return cost;
	}

	public double getConfidenceScore() {
		return (this.steinerNodes == null || this.steinerNodes.getConfidence() == null) ? 
				0.0 : this.steinerNodes.getConfidence().getConfidenceValue();
	}
	
	public SteinerNodes getSteinerNodes() {
		return this.steinerNodes;
	}
	
	public Coherence getLinkCoherence() {
		return this.linkCoherence;
	}

	private double computeCost() {
		double cost = 0.0;
		for (LabeledLink e : this.graph.edgeSet()) {
			cost += e.getWeight();
		}
		return cost;
	}
	
	private void computeCoherence() {
		for (LabeledLink l : this.graph.edgeSet()) {
			linkCoherence.updateCoherence(this.getGraph(), l);
		}
	}

	public double getScore() {
		if (this.steinerNodes != null)
			return this.steinerNodes.getScore();
		else
			return 0.0;
	}
	
//	private List<Integer> computeCoherence() {
//		
//		if (this.getGraph().edgeSet().size() == 0)
//			return null;
//		  
//		List<String> patternIds = new ArrayList<String>();
//		
//		for (LabeledLink e : this.getGraph().edgeSet()) 
//			for (String s : e.getModelIds())
//				patternIds.add(s);
//		
//		Function<String, String> stringEqualiy = new Function<String, String>() {
//		  @Override public String apply(final String s) {
//		    return s;
//		  }
//		};
//
//		Multimap<String, String> index =
//				   Multimaps.index(patternIds, stringEqualiy);
//		
//		List<Integer> frequencies = new LinkedList<Integer>();
//		for (String s : index.keySet()) {
//			frequencies.add(index.get(s).size());
//		}
//
//		Collections.sort(frequencies);
//		frequencies = Lists.reverse(frequencies);
//		return frequencies;
//	}
//
//	private static int compareCoherence(List<Integer> c1, List<Integer> c2) {
//		if (c1 == null || c2 == null)
//			return 0;
//		
//		for (int i = 0; i < c1.size(); i++) {
//			if (i < c2.size()) {
//				if (c1.get(i) > c2.get(i)) return 1;
//				else if (c1.get(i) < c2.get(i)) return -1;
//			}
//		}
//		if (c1.size() < c2.size())
//			return 1;
//		else if (c2.size() < c1.size())
//			return -1;
//		else
//			return 0;
//	}

	private int compareCoherencePair(Integer[] c1, Integer[] c2) {
		if (c1 == null || c2 == null)
			return 0;
		
		int items1 = c1[1];
		int items2 = c2[1];
		
		int nonPatternLinks1 = c1[1] - c1[0];
		int nonPatternLinks2 = c2[1] - c2[0];
		
		// item count
		if (items1 == 0 && items2 == 0)
			return 0;
		else if (items1 == 0)
			return -1;
		else if (items2 == 0)
			return 1;
		
		if (nonPatternLinks1 < nonPatternLinks2)
			return -1;
		else if (nonPatternLinks1 > nonPatternLinks2)
			return 1;
		else {
			
			if (nonPatternLinks1 == 0 && nonPatternLinks2 == 0) 
				return Integer.compare(items2, items1);
			else
				return Integer.compare(items1, items2);
		}
	}
	
	@Override
	public int compareTo(SortableSemanticModel m) {
		
		int lessThan = 1;
		int greaterThan = -1;
		
		double confidenceScore1 = this.getConfidenceScore();
		double confidenceScore2 = m.getConfidenceScore();
//		double score1 = this.getScore();
//		double score2 = m.getScore();

//		double linkCoherence1 = this.linkCoherence.getCoherenceValue();
//		double linkCoherence2 = m.linkCoherence.getCoherenceValue();
//		
//		if (linkCoherence1 > linkCoherence2)
//			return greaterThan;
//		else if (linkCoherence1 < linkCoherence2)
//			return lessThan;
		
		Integer[] linkCoherencePair1 = this.linkCoherence.getCoherencePair();
		Integer[] linkCoherencePair2 = m.linkCoherence.getCoherencePair();
		int linkCoherence = this.compareCoherencePair(linkCoherencePair1, linkCoherencePair2);
		if (linkCoherence < 0)
			return greaterThan;
		else if (linkCoherence > 0)
			return lessThan;

//		if (score1 > score2)
//			return greaterThan;
//		else if (score1 < score2)
//			return lessThan;	

		if (this.cost < m.cost)
			return greaterThan;
		else if (m.cost < this.cost)
			return lessThan;
		
		
		if (confidenceScore1 > confidenceScore2)
			return greaterThan;
		else if (confidenceScore1 < confidenceScore2)
			return lessThan;	
		
		return 0;
		
	}

	private static double roundDecimals(double d, int k) {
		String format = "";
		for (int i = 0; i < k; i++) format += "#";
        DecimalFormat DForm = new DecimalFormat("#." + format);
        return Double.valueOf(DForm.format(d));
	}
	
	public String getCoherenceString() {
		String c = "";
		c += "[";
		c += this.linkCoherence.getCoherencePair()[0];
		c += ",";
		c += this.linkCoherence.getCoherencePair()[1];
		c += "]";
		return c;
	}
	
	public String getRankingDetails() {
		
		String label = "";
		label +=
//				(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
//				"link coherence:" + (this.getLinkCoherence() == null ? "" : this.getLinkCoherence().getCoherenceValue()) + "\n";
				"link coherence:" + (this.getLinkCoherence() == null ? "" : this.getCoherenceString()) + "\n";
		label += (this.getSteinerNodes() == null || this.getSteinerNodes().getCoherence() == null) ? 
				"" : "node coherence:" + this.getSteinerNodes().getCoherence().getCoherenceValue() + "\n";
		label += "confidence:" + this.getConfidenceScore() + "\n";
		label += this.getSteinerNodes() == null ? "" : "mapping score:" + this.getSteinerNodes().getScore() + "\n";
		label +=
				"cost:" + roundDecimals(this.getCost(), 6) + "\n";
		return label;

	}
}
