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


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.rep.alignment.LabeledLink;

public class SortableSemanticModel2 extends SemanticModel
	implements Comparable<SortableSemanticModel2>{
	
	private double cost;
	private SteinerNodes steinerNodes;
	
	public SortableSemanticModel2(SemanticModel semanticModel, SteinerNodes steinerNodes) {
		
		super(semanticModel);
		
		this.steinerNodes = steinerNodes;
		
		if (this.graph != null && this.graph.edgeSet().size() > 0) {
			this.cost = this.computeCost();
		}
	}
	
	public SortableSemanticModel2(SemanticModel semanticModel) {
		
		super(semanticModel);
		
		if (this.graph != null && this.graph.edgeSet().size() > 0) {
			this.cost = this.computeCost();
		}
	}

	public SemanticModel getBaseModel() {
		return new SemanticModel(this);
	}
	
	public double getCost() {
		return cost;
	}

	public double getScore() {
		return this.steinerNodes.getScore();
	}
	
	public SteinerNodes getSteinerNodes() {
		return this.steinerNodes;
	}
	
	private double computeCost() {
		double cost = 0.0;
		for (LabeledLink e : this.graph.edgeSet()) {
			cost += e.getWeight();
		}
		return cost;
	}
	
	private List<Integer> computeCoherence() {
		
		if (this.getGraph().edgeSet().size() == 0)
			return null;
		  
		List<String> patternIds = new ArrayList<String>();
		
		for (LabeledLink e : this.getGraph().edgeSet()) 
			for (String s : e.getModelIds())
				patternIds.add(s);
		
		Function<String, String> stringEqualiy = new Function<String, String>() {
		  @Override public String apply(final String s) {
		    return s;
		  }
		};

		Multimap<String, String> index =
				   Multimaps.index(patternIds, stringEqualiy);
		
		List<Integer> frequencies = new LinkedList<Integer>();
		for (String s : index.keySet()) {
			frequencies.add(index.get(s).size());
		}

		Collections.sort(frequencies);
		frequencies = Lists.reverse(frequencies);
		return frequencies;
	}

	private static int compareCoherence(List<Integer> c1, List<Integer> c2) {
		if (c1 == null || c2 == null)
			return 0;
		
		for (int i = 0; i < c1.size(); i++) {
			if (i < c2.size()) {
				if (c1.get(i) > c2.get(i)) return 1;
				else if (c1.get(i) < c2.get(i)) return -1;
			}
		}
		if (c1.size() < c2.size())
			return 1;
		else if (c2.size() < c1.size())
			return -1;
		else
			return 0;
	}
	
	@Override
	public int compareTo(SortableSemanticModel2 m) {
		
		int lessThan = 1;
		int greaterThan = -1;
		
		double score1 = this.getScore();
		double score2 = m.getScore();

		List<Integer> c1 = this.computeCoherence();
		List<Integer> c2 = m.computeCoherence();
		
		int coherence = SortableSemanticModel2.compareCoherence(c1, c2);

		if (this.cost < m.cost)
			return greaterThan;
		else if (m.cost < this.cost)
			return lessThan;
		else if (coherence > 0)
			return greaterThan;
		else if (coherence < 0)
			return lessThan;
		else if (score1 > score2)
			return greaterThan;
		else if (score1 < score2)
			return lessThan;
		else {
			return 0;
		}
	}

}
