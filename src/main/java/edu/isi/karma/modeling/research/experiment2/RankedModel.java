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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class RankedModel implements Comparable<RankedModel>{

	private DirectedWeightedMultigraph<Node, Link> model;
	// sum of the cost of all the links
	private double cost;
	// the number of patterns shared among all the links 
	private int frequency;
	private List<Integer> cohesion;
	
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

	public String getCohesionString() {
		String s = "";
		for (Integer i : this.cohesion)
			s += String.valueOf(i);
		return s;
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
	
	private List<Integer> computeCohesion() {
		
		if (model == null || model.edgeSet().size() == 0)
			return null;
		  
		List<String> patternIds = new ArrayList<String>();
		
		for (Link e : model.edgeSet()) 
			for (String s : e.getPatternIds())
				patternIds.add(s);
		
		Function<String, String> stringEqualiy = new Function<String, String>() {
		  @Override public String apply(final String s) {
		    return s;
		  }
		};

		Multimap<String, String> index =
				   Multimaps.index(patternIds, stringEqualiy);
		
		List<Integer> frequencies = new ArrayList<Integer>();
		for (String s : index.keySet()) {
			frequencies.add(index.get(s).size());
		}

		Collections.sort(frequencies);
		frequencies = Lists.reverse(frequencies);
		return frequencies;
		
//		HashMap<String, Integer> patternToLinkCount = 
//				new HashMap<String, Integer>();
//
//		for (Link e : model.edgeSet()) {
//
//			for (String s : e.getPatternIds()) {
//				Integer i = patternToLinkCount.get(s);
//				if (i == null)
//					patternToLinkCount.put(s, 0);
//				else
//					patternToLinkCount.put(s, i.intValue() + 1);
//			}
//		}
//		
//		int max = 1;
//		for (String s : patternToLinkCount.keySet())
//			if (patternToLinkCount.get(s).intValue() > max)
//				max = patternToLinkCount.get(s).intValue();
//		
//		return (double)max;
	}

	private int compareCohesions(List<Integer> c1, List<Integer> c2) {
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
	public int compareTo(RankedModel m) {
		
		if (this.frequency > m.getFrequency())
			return -1;
		else if (m.getFrequency() > this.frequency)
			return 1;
		else {
			int k = compareCohesions(this.cohesion, m.cohesion);
			if (k > 0)
				return -1;
			else if (k < 0)
				return 1;
			else if (this.cost < m.cost)
				return -1;
			else if (m.cost < this.cost)
				return 1;
			else {
				return 0;
			}
		}
	}
	
}
