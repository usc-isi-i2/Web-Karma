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

package edu.isi.karma.modeling.research.experiment2;

import java.util.HashMap;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.collect.Sets;

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class RankedModel implements Comparable<RankedModel>{

	private DirectedWeightedMultigraph<Node, Link> model;
	// sum of the cost of all the links
	private double cost;
	// the number of patterns shared among all the links 
	private int frequency;
	// the maximum number of links sharing the same pattern 
	private double cohesion;
	
	public RankedModel(DirectedWeightedMultigraph<Node, Link> model) {
		
		this.model = model;
		
		if (this.model != null && this.model.edgeSet().size() > 0) {
			this.frequency = this.computeFrequency();
			this.cost = this.computeCost();
			this.cohesion = this.computeCohesion();
		}
	}

	public double getCost() {
		return cost;
	}

	public int getFrequency() {
		return frequency;
	}

	public double getCohesion() {
		return cohesion;
	}

	public DirectedWeightedMultigraph<Node, Link> getModel() {
		return model;
	}
	
	private double computeCost() {
		double cost = 0.0;
		for (Link e : model.edgeSet()) {
			cost += e.getWeight();
		}
		return cost;
	}

	private int computeFrequency() {
		
		Set<String> commonPatterns = null;
		
		if (model == null || model.edgeSet().size() == 0)
			return 0;
		   
		boolean firstVisit = true;
		
		for (Link e : model.edgeSet()) {

			if (e.getPatternIds() == null)
				return 0;
			
			if (firstVisit) {
				commonPatterns = e.getPatternIds();
				firstVisit = false;
			}
			else
				commonPatterns = Sets.intersection(e.getPatternIds(), commonPatterns);
			
			if (commonPatterns.size() == 0)
				return 0;
		}
		
		return commonPatterns.size();
	}

	private double computeCohesion() {
		
		if (model == null || model.edgeSet().size() == 0)
			return 0;
		   
		HashMap<String, Integer> patternToLinkCount = 
				new HashMap<String, Integer>();
		
		for (Link e : model.edgeSet()) {

			if (e.getPatternIds() == null)
				return 0;
			
			for (String s : e.getPatternIds()) {
				Integer i = patternToLinkCount.get(s);
				if (i == null)
					patternToLinkCount.put(s, 0);
				else
					patternToLinkCount.put(s, i.intValue() + 1);
			}
		}
		
		int max = 1;
		for (String s : patternToLinkCount.keySet())
			if (patternToLinkCount.get(s).intValue() > max)
				max = patternToLinkCount.get(s).intValue();
		
		return (double)max;
	}

	@Override
	public int compareTo(RankedModel m) {
		
		if (this.frequency > m.getFrequency())
			return -1;
		else if (m.getFrequency() > this.frequency)
			return 1;
		else if (this.cost < m.cost)
			return -1;
		else if (m.cost < this.cost)
			return 1;
		else if (this.cohesion > m.cohesion)
			return -1;
		else if (m.cohesion > this.cohesion)
			return 1;
		else
			return 0;
	}
	
}
