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
import edu.isi.karma.modeling.semantictypes.mycrf.crfmodel.CRFModelAbstract;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataAbstract;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;
import edu.isi.karma.modeling.semantictypes.mycrf.math.Matrix;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

/**
 * This class performs the task of finding the step size for the gradient during the optimization process.
 * 
 * @author amangoel
 *
 */
public class BacktrackingLineSearch {

	GlobalDataAbstract globalData ;
	CRFModelAbstract crfModel ;
	int dim ;
	
	public BacktrackingLineSearch(CRFModelAbstract crfModel, GlobalDataAbstract globalData) {
		this.crfModel = crfModel ;
		this.globalData = globalData ;
		dim = crfModel.weights.length ;
	}

	double findStep(double[] searchDir, double[] gradient, double currError) {
		double[] currWeights = new double[dim] ;
		double lam1=0.0, lam2=0.0, tmplam=0.0, lammin=0.0;
		double f1=0.0, f2=0.0 ;
		double slope = 0.0 ;

		System.arraycopy(crfModel.weights, 0, currWeights, 0, dim) ;

		double searchDirNorm = Matrix.norm(searchDir) ;
		if(searchDirNorm > Constants.BACKTRACKINGLINESEARCH_MAX_STEP) {
			double ratio = Constants.BACKTRACKINGLINESEARCH_MAX_STEP / searchDirNorm ;
			for(int i=0; i<dim; i++)
				searchDir[i]*=ratio ;
		}
		
		slope = Matrix.dotProduct(gradient, searchDir) ;

		double test = 0.0 ;
		double tmp = 0.0 ;
		for(int i=0; i<dim; i++) {
			tmp = Math.abs(searchDir[i]/Math.max(Math.abs(currWeights[i]), 1.0)) ;
			if(tmp > test)
				test = tmp ;
		}
		lammin = Constants.TOLX/test ;

		int iteration = 1 ;
		lam1 = 1.0 ;
		while(true) {
			Prnt.prn("BacktrackingLineSearch iteration #" + iteration + " and lambda = " + lam1) ;
			
			for(int i=0;i<dim;i++) 
				crfModel.weights[i] = currWeights[i] + lam1 * searchDir[i] ; 

			for(GraphInterface graph : globalData.trainingGraphs) {
				graph.computeGraphPotentialAndZ() ;
			}
			f1 = globalData.errorValue() ; 
			
			if(f1 < currError + Constants.ALPHA * lam1 * slope) {
				return lam1 ;
			}
			
			if(iteration == 1) {
				tmplam = -slope / (2 * (f1 - currError - slope)) ;
			}
			else {
				double rhs1 = f1 - currError - slope*lam1 ;
				double rhs2 = f2 - currError - slope*lam2 ;
				double a = (rhs1/(lam1 * lam1) - rhs2/(lam2 * lam2)) / (lam1 - lam2) ;
				double b = ((-rhs1 * lam2 / (lam1 * lam1)) + rhs2 * lam1 / (lam2 * lam2)) / (lam1 - lam2) ;
				if(a == 0.0)
					tmplam = -slope / (2.0 * b) ;
				else {
					double disc = b*b - 3*a*slope ;
					if(disc < 0) {
						Prnt.prn("Returning from Backtracking line search because discriminant is negative") ;
						Prnt.endIt("in backtrackinglinesearch, disc is less than 0. exiting.") ;
					}
					else
						tmplam = (-b + Math.sqrt(disc))/(3*a) ;
				}
				if(tmplam > 0.5 * lam1)
					tmplam = 0.5 * lam1 ;
			}

			if(tmplam < lammin) {       // lambda too small. can't move forward
				System.arraycopy(currWeights, 0, crfModel.weights, 0, dim) ;
				Prnt.prn("Returning because tmplam = " + tmplam + " < lammin = "+ lammin) ;
				return 0.0 ; 
			}

			f2 = f1 ;
			lam2 = lam1 ;
			lam1 = Math.max(tmplam, 0.1*lam1) ;
			iteration++ ;
		}
	}




}
