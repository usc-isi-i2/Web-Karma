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

package edu.isi.karma.modeling.research.approach1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.rep.alignment.Node;

public class RankedSteinerSet implements Comparable<RankedSteinerSet>{

	private Set<Node> nodes;
	private List<Integer> cohesion;

	public RankedSteinerSet(Set<Node> nodes) {
		this.nodes = nodes;
		this.cohesion = this.computeCohesion();
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public String getCohesionString() {
		String s = "";
		for (Integer i : this.cohesion)
			s += String.valueOf(i);
		return s;
	}
	
	private List<Integer> computeCohesion() {
		
		if (nodes == null)
			return null;
		  
		List<String> patternIds = new ArrayList<String>();
		
		for (Node n : nodes) 
			for (String s : n.getPatternIds())
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
	
//	@Override
//	public int compareTo(RankedSteinerSet s) {
//		
//		int size1 = this.nodes == null ? 0 : this.nodes.size();
//		int size2 = s.getNodes() == null ? 0 : s.getNodes().size();
//		
//		if (size1 < size2)
//			return -1;
//		else if (size1 > size2)
//			return 1;
//		else
//			return -compareCohesions(this.cohesion, s.cohesion);
//	}

	@Override
	public int compareTo(RankedSteinerSet s) {
		
		int size1 = this.nodes == null ? 0 : this.nodes.size();
		int size2 = s.getNodes() == null ? 0 : s.getNodes().size();
		
		int k = compareCohesions(this.cohesion, s.cohesion);
		if (k > 0)
			return -1;
		else if (k < 0)
			return 1;
		else if (size1 < size2)
			return -1;
		else 
			return 1;
	}

}
