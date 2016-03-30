package edu.isi.karma.semantictypes.numeric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabelComparator;

public class KSTest {
	
	public List<SemanticTypeLabel> predictLabelsForColumn(int numPredictions, Map<String, List<Double>> trainingLabelToExamplesMap,
			List<Double> testExamples) {

		List<SemanticTypeLabel> sortedPredictions = new ArrayList<>();	// descending order of p-Value
		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
	  	double pValue;
	    
	  	double[] sample1 = new double[testExamples.size()];
	  	for(int i = 0; i < testExamples.size(); i++){
	      sample1[i] = testExamples.get(i);
	  	}
	    
	    for (Entry<String, List<Double>> entry : trainingLabelToExamplesMap.entrySet()) {
	    	String label = entry.getKey();
	    	List<Double> trainExamples = entry.getValue();
	    	double[] sample2 = new double[trainExamples.size()];
	    	for(int i = 0; i < trainExamples.size(); i++){
	        sample2[i] = trainExamples.get(i);
	    	} 	
	    	if (sample1.length > 1 && sample2.length > 1) {
		    	pValue = test.kolmogorovSmirnovTest(sample1, sample2);
		    	SemanticTypeLabel pred = new SemanticTypeLabel(label, (float)pValue);
		    	sortedPredictions.add(pred);
	    	}
	    }
	    
		// sorting based on p-Value
		Collections.sort(sortedPredictions, new SemanticTypeLabelComparator());		
		
		return sortedPredictions;
	}
	
}
