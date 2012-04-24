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
package edu.isi.karma.modeling.semantictypes.mycrf.globaldata ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.mycrf.common.Constants;
import edu.isi.karma.modeling.semantictypes.mycrf.crfmodel.CRFModelFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;
import edu.isi.karma.modeling.semantictypes.mycrf.math.Matrix;
import edu.isi.karma.modeling.semantictypes.myutils.Prnt;


/**
 * This class represents global information used while creating graphs or training the CRF model
 * 
 * @author amangoel
 *
 */
public class GlobalDataFieldOnly extends GlobalDataAbstract {

	public ArrayList<String> labels ;
	public CRFModelFieldOnly crfModel ;
	
	
	public GlobalDataFieldOnly() {
		labels = new ArrayList<String>() ;	
	}

	public void collectAllLabels(ArrayList<String> files) {
		// This method collects all labels and puts them into an ArrayList
		BufferedReader br = null ;
		String line = null ;

		for(String file : files) {
			try {
				br = new BufferedReader(new FileReader(file)) ;
				line = br.readLine() ;
				br.close() ;
			}
			catch(Exception e) {
				e.printStackTrace() ;
				Prnt.endIt("Error, quiting.") ;
			}
			String[] tokens = line.split("\\s+");
			String label = tokens[tokens.length-1] ;
			if (!labels.contains(label)) {
				labels.add(label) ;
			}
		}

	}
	
	public void errorGradient(double[] gradient) {
		
		double invSD = 1.0 / (Constants.STANDARD_DEVIATION * Constants.STANDARD_DEVIATION) ;
		
		for(int i=0;i<gradient.length;i++) {
			gradient[i] = 0.0 ;
		}
		
		double[] tmpGradient = new double[gradient.length] ;
		
		for(GraphInterface graphI : trainingGraphs) {
			GraphFieldOnly graph = (GraphFieldOnly) graphI ;
			graph.logLikelihoodGradient(tmpGradient) ;
			Matrix.plusEquals(gradient, tmpGradient, 1) ;
		}
		
		for(int i=0;i<gradient.length;i++) {
			gradient[i] = -gradient[i] ;
		}
		
		Matrix.plusEquals(gradient, crfModel.weights, invSD) ;
	}
	
	public double errorValue() {
		double error = 0 ;
		
		for(GraphInterface graphI : trainingGraphs) {
			GraphFieldOnly graph = (GraphFieldOnly) graphI ;
			error+=graph.logLikelihood() ;
		}
		
		error = - error + Matrix.dotProduct(crfModel.weights, crfModel.weights) / (2 * Constants.STANDARD_DEVIATION * Constants.STANDARD_DEVIATION) ; 
		
		return error ;
	}
	
	
	
	
	
}
