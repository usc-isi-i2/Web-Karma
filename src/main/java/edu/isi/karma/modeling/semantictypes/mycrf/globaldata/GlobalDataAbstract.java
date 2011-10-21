package edu.isi.karma.modeling.semantictypes.mycrf.globaldata ;

import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;


public abstract class GlobalDataAbstract {
	
	public ArrayList<GraphInterface> trainingGraphs ;
	
	public abstract double errorValue() ;
	
	public abstract void errorGradient(double[] gradient) ;
	
}