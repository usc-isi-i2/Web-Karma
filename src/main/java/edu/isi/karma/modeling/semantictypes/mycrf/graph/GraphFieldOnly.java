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


/**
 * This class represents a single node graph.
 * It also stores its computed partition function value,
 * potential, and
 * marginals.
 * 
 * @author amangoel
 *
 */
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


