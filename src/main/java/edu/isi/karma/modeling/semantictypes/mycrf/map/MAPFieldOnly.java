package edu.isi.karma.modeling.semantictypes.mycrf.map ;

import edu.isi.karma.modeling.semantictypes.mycrf.crfmodel.CRFModelFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.fieldonly.LblFtrPair;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.math.LargeNumber;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

public class MAPFieldOnly {
	
	GlobalDataFieldOnly globalData ;
	
	public MAPFieldOnly(GlobalDataFieldOnly globalData) {
		this.globalData = globalData ;
	}
	
	public void findMap(String file) {
		CRFModelFieldOnly crfModel = globalData.crfModel ;
		GraphFieldOnly graph = new GraphFieldOnly(file, false, globalData) ;
		
		double largestExp = -1000000 ;
		int mostLikelyLabelIndex = -1 ;
		
		for(int l=0;l<globalData.labels.size();l++) {
			double exp = 0 ;
			for(int f=0;f<crfModel.ffs.size();f++) {
				LblFtrPair ff = crfModel.ffs.get(f) ;
				if (ff.labelIndex == l && graph.node.features.contains(ff.feature)) {
					exp = exp + crfModel.weights[f] ;
				}
			}
			if (exp > largestExp) {
				largestExp = exp ;
				mostLikelyLabelIndex = l ;
			}
		}
		Prnt.prn("For file : " + file + " The most likely label is " + globalData.labels.get(mostLikelyLabelIndex)) ;
	}
	
	public double[] probabilitiesForLabels(GraphFieldOnly graph) {
		double[] exps, prob;
		LargeNumber[] potentials;
		LargeNumber totalPotential ;
		exps = weightedFeatureFunctionSums(graph);
		potentials = new LargeNumber[globalData.labels.size()] ;
		totalPotential = new LargeNumber(0.0, 0) ;
		for(int i=0;i<globalData.labels.size();i++) {
			potentials[i] = LargeNumber.makeLargeNumberUsingExponent(exps[i]) ;
			totalPotential.plusEquals(potentials[i]) ;
		}
		prob = new double[globalData.labels.size()] ;
		for(int i=0;i<globalData.labels.size();i++) {
			prob[i] = LargeNumber.divide(potentials[i], totalPotential) ;
		}
		return prob ;
	}
	
	public double[] weightedFeatureFunctionSums(GraphFieldOnly graph) {
		CRFModelFieldOnly crfModel;
		double[] exps;
		crfModel = globalData.crfModel ;
		exps = new double[globalData.labels.size()] ;
		for(int ffIndex = 0 ; ffIndex < crfModel.ffs.size(); ffIndex++) {
			LblFtrPair ff;
			ff = crfModel.ffs.get(ffIndex) ;
			if (graph.node.features.contains(ff.feature)) {
				exps[ff.labelIndex]+=crfModel.weights[ffIndex] ;
			}
		}
		return exps;
	}
	
	
	
}