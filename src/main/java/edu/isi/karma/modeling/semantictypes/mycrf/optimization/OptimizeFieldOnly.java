package edu.isi.karma.modeling.semantictypes.mycrf.optimization ;

import edu.isi.karma.modeling.semantictypes.mycrf.common.Constants;
import edu.isi.karma.modeling.semantictypes.mycrf.crfmodel.CRFModelFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;
import edu.isi.karma.modeling.semantictypes.mycrf.math.Matrix;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

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
			Prnt.prn("Optimization iteration = " + iter) ;
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