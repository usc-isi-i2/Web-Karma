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
package edu.isi.karma.modeling.semantictypes;

import java.util.HashMap;

public class FullCRFModel {
	/**
	 * Keeps a map between the column HNodeIds and the corresponding column
	 * model
	 */
	HashMap<String, CRFColumnModel> columnModelMap = new HashMap<String, CRFColumnModel>();

	public void addColumnModel(String nodeId, CRFColumnModel columnModel) {
		columnModelMap.put(nodeId, columnModel);
	}

	public CRFColumnModel getModelByColumnName(String name) {
		// TODO Yet to be implemented
		return null;

	}

	public CRFColumnModel getModelByHNodeId(String hNodeId) {
		return columnModelMap.get(hNodeId);
	}
}
