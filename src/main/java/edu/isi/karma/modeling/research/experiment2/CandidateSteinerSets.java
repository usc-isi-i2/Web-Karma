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

public class CandidateSteinerSets {
	
	private static final int MAX_SIZE = 1000;
	private int maxNumberOfSteinerNodes;
	private List<SteinerNodes> steinerSets;
	
	public CandidateSteinerSets(int maxNumberOfSteinerNodes) {
		this.maxNumberOfSteinerNodes = maxNumberOfSteinerNodes;
		this.steinerSets = new ArrayList<SteinerNodes>();
	}
	
	public int numberOfCandidateSets() {
		return this.steinerSets.size();
	}
	
	public List<SteinerNodes> getSteinerSets() {
		return this.steinerSets;
	}
	
	public void updateSteinerSets(SemanticTypeMapping mapping) {
		
		List<SteinerNodes> newSteinerNodes = new ArrayList<SteinerNodes>();
		
		if (this.steinerSets.size() == 0) {
			for (MappingStruct ms : mapping.getMappingStructs()) {
				SteinerNodes sn = new SteinerNodes(maxNumberOfSteinerNodes);
				sn.addNode(ms.getSource(), 1.0);
				if (mapping.getType() == MappingType.DataNode) {
					sn.addNode(ms.getTarget(), sn.getConfidence());
				}
				this.steinerSets.add(sn);
			}			
		} else {
			for (SteinerNodes nodeSet : this.steinerSets) {
				for (MappingStruct ms : mapping.getMappingStructs()) {
					SteinerNodes sn = new SteinerNodes(nodeSet);
					
					if (mapping.getType() == MappingType.ClassNode) {
						if (nodeSet.getNodes().contains(ms.getSource()))
							continue;
						sn.addNode(ms.getSource(), sn.getConfidence());
					}
					else if (mapping.getType() == MappingType.DataNode) {
						if (nodeSet.getNodes().contains(ms.getSource()) &&
								nodeSet.getNodes().contains(ms.getTarget()))
							continue;
						sn.addNode(ms.getSource(), sn.getConfidence());
						sn.addNode(ms.getTarget(), sn.getConfidence());
					}
					newSteinerNodes.add(sn);
				}
			}
			
			// sort Steiner nodes based on their score
			Collections.sort(newSteinerNodes);
			
			this.steinerSets.clear();
			
			for (int i = 0; i < MAX_SIZE && i < newSteinerNodes.size(); i++)
				this.steinerSets.add(newSteinerNodes.get(i));

		}
		
	}
}
