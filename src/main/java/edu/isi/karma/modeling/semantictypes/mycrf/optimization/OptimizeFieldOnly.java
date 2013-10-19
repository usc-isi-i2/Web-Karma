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
package edu.isi.karma.modeling.semantictypes.mycrf.optimization ;

import edu.isi.karma.modeling.semantictypes.mycrf.common.Constants;
import edu.isi.karma.modeling.semantictypes.mycrf.crfmodel.CRFModelFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;
import edu.isi.karma.modeling.semantictypes.mycrf.math.Matrix;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

/**
 * This is the class that performs the optimization of the CRF model.
 * It uses other classes in this package to guess the gradient to follow 
 * and the step size to use.
 * It also have several stopping criteria, such as,
 * gradient being too small, and change in the error value being too small.
 * 
 * @author amangoel
 *
 */
public class OptimizeFieldOnly {
	
	CRFModelFieldOnly crfModel ;
	GlobalDataFieldOnly globalData ;
	
	public OptimizeFieldOnly(CRFModelFieldOnly crfModel, GlobalDataFieldOnly globalData) {
		this.crfModel = crfModel ;
		this.globalData = globalData ;
	}
	
	public void optimize(int maxIters) {
		int dim = crfModel.ffs.size() ;
		LBFGS lfbgs = new LBFGS(dim) ;
		BacktrackingLineSearch lineSearch = new BacktrackingLineSearch(crfModel, globalData) ;
		double[] gradient = new double[dim] ;
		double[] searchDir = new double[dim] ;
		double errorValue = 0.0 ;
		
		for(GraphInterface graphI : globalData.trainingGraphs) {
			GraphFieldOnly graph = (GraphFieldOnly) graphI ;
			graph.computeGraphPotentialAndZ() ;
		}
		for(int iter=1;iter<=maxIters;iter++) {
//			Prnt.prn("Optimization iteration = " + iter) ;
			errorValue = globalData.errorValue() ;
			for(GraphInterface graphI : globalData.trainingGraphs) {
				GraphFieldOnly graph = (GraphFieldOnly) graphI ;
				graph.computeNodeMarginals() ;
			}
			globalData.errorGradient(gradient) ;
			if (Matrix.norm(gradient) < Constants.EPSILON_GRADIENT) {
				break ;
			}
			lfbgs.searchDir(crfModel.weights, gradient, searchDir) ;
			double step = lineSearch.findStep(searchDir, gradient, errorValue) ;
			if(step == 0) {
				break ;
			}
		}
		
	}
	
	
	
	
	
}
