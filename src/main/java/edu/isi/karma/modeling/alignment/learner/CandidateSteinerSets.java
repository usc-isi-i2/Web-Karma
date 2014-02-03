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

import edu.isi.karma.modeling.ModelingConfiguration;



public class CandidateSteinerSets {
	
	private static Logger logger = LoggerFactory.getLogger(CandidateSteinerSets.class);
	private List<SteinerNodes> steinerSets;
	
	public CandidateSteinerSets(int maxNumberOfSteinerNodes) {
		this.steinerSets = new ArrayList<SteinerNodes>();
	}
	
	public int numberOfCandidateSets() {
		return this.steinerSets.size();
	}
	
	public List<SteinerNodes> getSteinerSets() {
		return this.steinerSets;
	}
	
	public void updateSteinerSets(Set<SemanticTypeMapping> mappings) {
		
		List<SteinerNodes> newSteinerNodes = new ArrayList<SteinerNodes>();
		if (mappings == null || mappings.isEmpty()) 
			return;
		
		if (this.steinerSets.size() == 0) {
			for (SemanticTypeMapping stm : mappings) {
				SteinerNodes sn = new SteinerNodes();
				sn.addNodes(stm.getSourceColumn(), stm.getSource(), stm.getTarget(), stm.getConfidence());
				this.steinerSets.add(sn);
			}			
		} else {
			for (SteinerNodes nodeSet : this.steinerSets) {
				for (SemanticTypeMapping stm : mappings) {
					SteinerNodes sn = new SteinerNodes(nodeSet);
					
					if (nodeSet.getNodes().contains(stm.getSource()) &&
							nodeSet.getNodes().contains(stm.getTarget()))
						continue;
					sn.addNodes(stm.getSourceColumn(), stm.getSource(), stm.getTarget(), stm.getConfidence());
					newSteinerNodes.add(sn);
				}
			}
			
			// sort Steiner nodes based on their score
			Collections.sort(newSteinerNodes);
			
			this.steinerSets.clear();
			
			for (int i = 0; i < ModelingConfiguration.getMaxQueuedMappigs() && i < newSteinerNodes.size(); i++)
				this.steinerSets.add(newSteinerNodes.get(i));

		}
		
		for (SteinerNodes sn : this.steinerSets) {
			logger.debug(sn.getScoreDetailsString());
		}
		logger.debug("***************************************************************");
	}
}
