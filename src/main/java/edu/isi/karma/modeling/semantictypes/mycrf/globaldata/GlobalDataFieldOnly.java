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
