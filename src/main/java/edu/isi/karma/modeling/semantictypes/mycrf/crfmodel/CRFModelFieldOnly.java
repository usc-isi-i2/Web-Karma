package edu.isi.karma.modeling.semantictypes.mycrf.crfmodel ;

import java.util.ArrayList;
import java.util.HashSet;

import edu.isi.karma.modeling.semantictypes.mycrf.fieldonly.LblFtrPair;
import edu.isi.karma.modeling.semantictypes.mycrf.globaldata.GlobalDataFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphFieldOnly;
import edu.isi.karma.modeling.semantictypes.mycrf.graph.GraphInterface;



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