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
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Node;



public class CandidateSteinerSets {
	
	private static Logger logger = LoggerFactory.getLogger(CandidateSteinerSets.class);
	private List<SteinerNodes> steinerSets;
	private String contextId;
	public CandidateSteinerSets(int maxNumberOfSteinerNodes, String contextId) {
		this.contextId = contextId;
		this.steinerSets = new ArrayList<>();
	}
	
	public int numberOfCandidateSets() {
		return this.steinerSets.size();
	}
	
	public List<SteinerNodes> getSteinerSets() {
		return this.steinerSets;
	}
	
	public void updateSteinerSets(Set<SemanticTypeMapping> mappings) {
		
		List<SteinerNodes> newSteinerNodes = new ArrayList<>();
		if (mappings == null || mappings.isEmpty()) 
			return;

		List<SemanticTypeMapping> sortedMappings = new ArrayList<>(mappings);
		Collections.sort(sortedMappings);

		
		if (this.steinerSets.isEmpty()) {
			for (SemanticTypeMapping stm : sortedMappings) {
				SteinerNodes sn = new SteinerNodes(contextId);
				if (sn.addNodes(stm))
					this.steinerSets.add(sn);
			}			
		} else {
			int numOfNewSets = 0;
			for (SteinerNodes nodeSet : this.steinerSets) {
				for (SemanticTypeMapping stm : sortedMappings) {
					
					if (nodeSet.getNodes().contains(stm.getSource()) &&
							nodeSet.getNodes().contains(stm.getTarget()))
						continue;

					SteinerNodes sn = new SteinerNodes(nodeSet,contextId);
					if (sn.addNodes(stm)) {
						newSteinerNodes.add(sn);
						numOfNewSets ++;
					}
				}
			}
			if (numOfNewSets == 0) {
				for (SteinerNodes nodeSet : this.steinerSets) {
					newSteinerNodes.add(nodeSet);
				}
			}
			
//			this.steinerSets = getTopKSteinerSets(newSteinerNodes, ModelingConfiguration.getMaxQueuedMappigs());
			
			// sort Steiner nodes based on their score
			Collections.sort(newSteinerNodes);
			
			this.steinerSets.clear();
			
//			for (int i = 0; i < newSteinerNodes.size(); i++) // do not cut off

			for (int i = 0; i < ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextId).getMappingBranchingFactor() && i < newSteinerNodes.size(); i++)
				this.steinerSets.add(newSteinerNodes.get(i));

		}
		for (SteinerNodes sn : this.steinerSets) {
			logger.debug(sn.getScoreDetailsString());
		}
		logger.debug("***************************************************************");
	}
	
	public void updateSteinerSets(InternalNode n) {
		
		List<SteinerNodes> newSteinerNodes = new ArrayList<>();
		if (n == null) 
			return;
		
		if (this.steinerSets.isEmpty()) {
			SteinerNodes sn = new SteinerNodes(contextId);
			if (sn.addNode(n))
				this.steinerSets.add(sn);
		} else {
			int numOfNewSets = 0;
			for (SteinerNodes nodeSet : this.steinerSets) {

				if (nodeSet.getNodes().contains(n))
					continue;

				SteinerNodes sn = new SteinerNodes(nodeSet,contextId);
				
				if (sn.addNode(n)) {
					newSteinerNodes.add(sn);
					numOfNewSets ++;
				}
			}
			if (numOfNewSets == 0) {
				for (SteinerNodes nodeSet : this.steinerSets) {
					newSteinerNodes.add(nodeSet);
				}
			}
			
			Collections.sort(newSteinerNodes);
			
			this.steinerSets.clear();
			
//			for (int i = 0; i < newSteinerNodes.size(); i++) // do not cut off
			for (int i = 0; i < ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextId).getMappingBranchingFactor() && i < newSteinerNodes.size(); i++)
				this.steinerSets.add(newSteinerNodes.get(i));

		}
		for (SteinerNodes sn : this.steinerSets) {
			logger.debug(sn.getScoreDetailsString());
		}
		logger.debug("***************************************************************");
	}
	
	@SuppressWarnings("unused")
	private List<SteinerNodes> getTopKSteinerSets(List<SteinerNodes> steinerSets, int k) {
		
//		If the points are already sorted by one of the coordinates (say the x-coordinate), this can be done in O(n) as follows:
//
//			Process the points from the largest x-coordinate.
//			As you go through them, keep track of the largest y-coordinate.
//			If the current point's y-coordinate is smaller than the largest y-coordinate thus far, it's dominated by another point. Otherwise, it's not dominated, so add it to the output.
		
		logger.info("number of steiner sets before computing pareto optimal: " + steinerSets.size());
		
		List<SteinerNodes> paretoFrontierSteinerSets =
				new ArrayList<>();
		
		if (steinerSets == null || steinerSets.isEmpty() || k <= 0)
			return paretoFrontierSteinerSets;
		
		Collections.sort(steinerSets, Collections.reverseOrder(new SteinerNodesCoherenceComparator()));
		
		SteinerNodes sn = steinerSets.get(0);
		paretoFrontierSteinerSets.add(sn);
		double largestObservedConfidence = sn.getConfidence().getConfidenceValue();
		
		for (int i = 1; i < steinerSets.size(); i++) {
			sn = steinerSets.get(i);
//			System.out.println(sn.getScoreDetailsString());
			if (sn.getConfidence().getConfidenceValue() < largestObservedConfidence)
				continue;
			
			largestObservedConfidence = sn.getConfidence().getConfidenceValue();
			
			paretoFrontierSteinerSets.add(sn);
		}
		
		logger.info("number of steiner sets after computing pareto optimal: " + paretoFrontierSteinerSets.size());

		List<SteinerNodes> results = new ArrayList<>();
		for (int i = 0; i < ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextId).getMappingBranchingFactor() && i < paretoFrontierSteinerSets.size(); i++) {
			if (i == 0) {
				System.out.println(paretoFrontierSteinerSets.get(i).getScoreDetailsString());
				for (Node n : paretoFrontierSteinerSets.get(i).getNodes())
					System.out.println(n.getId());
			}
			results.add(paretoFrontierSteinerSets.get(i));
		}

		return results;
	}
}
