package edu.isi.karma.modeling.semantictypes.mycrf.graph ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.mycrf.common.Constants;
import edu.isi.karma.modeling.semantictypes.mycrf.common.Node;
import edu.isi.karma.modeling.semantictypes.mycrf.fieldonly.LblFtrPair;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.math.LargeNumber;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;


public class GraphFieldOnly implements GraphInterface {

	GlobalDataFieldOnly globalData ;
	public Node node ;
	LargeNumber Z ;
	LargeNumber graphPotential ;
	double[] nodeMarginals ;

	public GraphFieldOnly(String file, boolean labeled, GlobalDataFieldOnly globalData) {
		this.globalData = globalData ;
		BufferedReader br = null ;
		String line = null ;

		try {
			br = new BufferedReader(new FileReader(file)) ;
			line = br.readLine() ;
			br.close() ;
		}
		catch(Exception e) {
			e.printStackTrace() ;
			Prnt.endIt("Quiting") ;
		}

		String[] tokens = line.split("\\s+") ;
		node = new Node(Constants.FIELD_TYPE, 0, 0) ;
		node.string = tokens[0] ;

		for(int i=1;i<tokens.length-(labeled?1:0);i++) {
			node.features.add(tokens[i]) ;
		}

		if(labeled){
			node.labelIndex = globalData.labels.indexOf(tokens[tokens.length-1]) ;
		}
		else {
			node.labelIndex = -1 ;
		}
		nodeMarginals = new double[globalData.labels.size()] ;
	}
	
	public GraphFieldOnly(String fieldString, String label, ArrayList<String> features, GlobalDataFieldOnly globalData) {
		if (features == null || features.size() == 0) {
			Prnt.endIt("GraphFieldOnly constructor called with empty feature list.") ;
		}
		this.globalData = globalData ;
		this.node = new Node(Constants.FIELD_TYPE, 0, 0) ;
		node.string = fieldString ;
		if (label == null) {
			node.labelIndex = -1 ;
		}
		else {
			label = label.trim() ;
			node.labelIndex = globalData.labels.indexOf(label) ;
			if (node.labelIndex == -1) {
				Prnt.endIt("Label not found in globalData.labels") ;
			}
		}
		node.features = new ArrayList<String>(features) ;
		nodeMarginals = new double[globalData.labels.size()] ;
	}

	public void compute_Z() {
		LargeNumber tmp = new LargeNumber(0.0, 0) ;
		for(int i=0;i<globalData.labels.size();i++) {	
			tmp.plusEquals(LargeNumber.makeLargeNumberUsingExponent(potentialExpForLabelIndex(i))) ;
		}
		Z = tmp ;
	}

	public void compute_graphPotential() {
		this.graphPotential = LargeNumber.makeLargeNumberUsingExponent(potentialExpForLabelIndex(node.labelIndex)) ;
	}
	
	public void computeNodeMarginals() {
		if (globalData.labels.size() != nodeMarginals.length) {
			nodeMarginals = new double[globalData.labels.size()] ;
		}
		for(int i=0;i<globalData.labels.size();i++) {
			nodeMarginals[i] = LargeNumber.divide(LargeNumber.makeLargeNumberUsingExponent(potentialExpForLabelIndex(i)), this.Z) ;
		}
	}

	public double potentialExpForLabelIndex(int labelIndex) {
		double exp = 0.0 ;
		for(int f=0;f<globalData.crfModel.ffs.size();f++) {
			LblFtrPair ff = globalData.crfModel.ffs.get(f) ;
			if (ff.labelIndex == labelIndex && this.node.features.contains(ff.feature)) {
				exp+=globalData.crfModel.weights[f] ;
			}
		}
		return exp ;
	}
	
	public double logLikelihood() {
		double likelihood = LargeNumber.divide(graphPotential, Z) ;
		return Math.log(likelihood);
	}
	
	public void logLikelihoodGradient(double[] gradient) {
		double lhs = 0, rhs = 0 ;
		for(int f=0;f<globalData.crfModel.ffs.size();f++) {
			LblFtrPair ff = globalData.crfModel.ffs.get(f) ;
			lhs = (this.node.labelIndex == ff.labelIndex && this.node.features.contains(ff.feature)) ? 1.0 : 0.0 ;
			rhs = (node.features.contains(ff.feature)) ? this.nodeMarginals[ff.labelIndex] : 0.0 ;
			gradient[f] = lhs - rhs ;
		}
	}
	
	public void computeGraphPotentialAndZ() {
		compute_graphPotential() ;
		compute_Z() ;
	}
	
	public void computeGraphPotentialAndZAndMarginals() {
		computeGraphPotentialAndZ() ;
		computeNodeMarginals() ;
	}

	
	
}


