package edu.isi.karma.cleaning.Correctness;

import java.util.ArrayList;
import java.util.Arrays;

import libsvm.svm_parameter;
import edu.isi.karma.cleaning.features.RecordClassifier;
import edu.isi.karma.cleaning.features.RecordFeatureSet;

public class OutlierDetector {
	RecordClassifier clf;
	public OutlierDetector()
	{
		RecordFeatureSet rfs1 = new RecordFeatureSet();
		clf = new RecordClassifier(rfs1, svm_parameter.ONE_CLASS);
	}
	
	public void train(ArrayList<String> tdata)
	{
		for(String line:tdata)
		{
			clf.addTrainingData(line, "1");
		}
		clf.learnClassifer();
	}
	public String getLabel(String input)
	{
		String label = clf.getLabel(input);
		return label;
	}
	public static void main(String[] args)
	{
		OutlierDetector outDet = new OutlierDetector();
		String[] dat = {"A", "AA","B", "BB"};
		String[] tst = {"B", "b", "AAAAAAAAAAAA","."};
		ArrayList<String> data = new ArrayList<String>(Arrays.asList(dat));
		outDet.train(data);
		for(String l:tst)
		{
			String out = outDet.getLabel(l);
			System.out.println(l+": "+out);
		}
	}

}
