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
package edu.isi.karma.modeling.semantictypes.mycrf.crfmodel ;

import edu.isi.karma.modeling.semantictypes.mycrf.fieldonly.LblFtrPair;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;

import java.util.ArrayList;
import java.util.HashSet;



/**
 * This class represents the CRF model that is trained on single node graphs.
 * These single node graphs represent a field (without considering its neighbors and without splitting it into tokens)
 * Its main properties are a list of feature functions and a double[] array to store their weights.
 * 
 * @author amangoel
 *
 */
public class CRFModelFieldOnly extends CRFModelAbstract {
	
	GlobalDataFieldOnly globalData ;
	public ArrayList<LblFtrPair> ffs ;
	
	public CRFModelFieldOnly(GlobalDataFieldOnly globalData) {
		this.globalData = globalData ;
	}
	
	public void createFFsFromGraphs() {
		ffs = new ArrayList<LblFtrPair>();
		for(int i=0;i<globalData.labels.size();i++) {
			HashSet<String> ftrs = new HashSet<String>() ;
			for(GraphInterface graphI : globalData.trainingGraphs) {
				GraphFieldOnly graph = (GraphFieldOnly) graphI ;
				if (graph.node.labelIndex == i) {
					ftrs.addAll(graph.node.features) ;
				}
			}
			for(String ftr : ftrs) {
				ffs.add(new LblFtrPair(i,ftr)) ;
			}
		}
		weights = new double[ffs.size()] ;
	}
	
	

	
	
	
}
