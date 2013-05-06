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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.collect.Sets;

import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class PatternContainment {

	private DirectedWeightedMultigraph<Node, Link> mainGraph;
	private DirectedWeightedMultigraph<Node, Link> newGraph;
	
	public PatternContainment(DirectedWeightedMultigraph<Node, Link> mainGraph, 
			DirectedWeightedMultigraph<Node, Link> newGraph) {
		this.mainGraph = mainGraph;
		this.newGraph = newGraph;
	}
	
	public boolean containedIn(Set<String> mappedNodes, Set<String> mappedLinks) {
		
		HashMap<String, List<Link>> mainLinks = new HashMap<String, List<Link>>();
		HashMap<String, List<Link>> newLinks = new HashMap<String, List<Link>>();
		
		for (Link e : mainGraph.edgeSet()) {
			String target = (e.getTarget() instanceof InternalNode)?e.getTarget().getLabel().getUri():"";
			String key = e.getSource().getLabel().getUri() +
					target + 
					e.getLabel().getUri();
			List<Link> links = mainLinks.get(key);
			if (links == null) {
				links = new ArrayList<Link>();
				mainLinks.put(key, links);
			}
			links.add(e);
		}

		for (Link e : newGraph.edgeSet()) {
			String target = (e.getTarget() instanceof InternalNode)?e.getTarget().getLabel().getUri():"";
			String key = e.getSource().getLabel().getUri() +
					target + 
					e.getLabel().getUri();
			List<Link> links = newLinks.get(key);
			if (links == null) {
				links = new ArrayList<Link>();
				newLinks.put(key, links);
			}
			links.add(e);
		}
		
		Set<String> sharedLinks = Sets.intersection(mainLinks.keySet(), newLinks.keySet());
		for (String key : sharedLinks) {
			if (mainLinks.get(key).size() != newLinks.get(key).size())
				return false;
		}
		
		if (sharedLinks.size() == newLinks.keySet().size()) {
			for (String key : sharedLinks) {
				List<Link> links = mainLinks.get(key);
				for (Link l : links) {
					mappedNodes.add(l.getSource().getId());
					mappedNodes.add(l.getTarget().getId());
					mappedLinks.add(l.getId());
				}
			}
			return true;
		} 

		return false;

	}
}
