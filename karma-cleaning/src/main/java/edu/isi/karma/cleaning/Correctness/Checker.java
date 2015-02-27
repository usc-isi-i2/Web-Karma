package edu.isi.karma.cleaning.Correctness;

import java.util.ArrayList;

import edu.isi.karma.cleaning.features.RecordClassifier;
import edu.isi.karma.cleaning.features.RecordFeatureSet;

/*
 * check whether the transformed results are correct
 */
public class Checker {
	RecordClassifier clf;
	public Checker()
	{
		RecordFeatureSet rfs1 = new RecordFeatureSet();
		clf = new RecordClassifier(rfs1);

	}
	public String binds(String[] exp)
	{
		String res = "";
		if(exp.length == 2)
		{
			res = String.format("bef:%s aft:%s", exp[0],exp[1]);
		}
		else
		{
			res = "NOEXP";
		}
		return res;
	}
	public void train(ArrayList<String[]> postive, ArrayList<String[]> negative)
	{
		clf.init();
		for(String[] pos:postive)
		{
			String tmp = binds(pos);
			clf.addTrainingData(tmp, "1");
		}
		for(String[] neg:negative)
		{
			String tmp = binds(neg);
			clf.addTrainingData(tmp, "-1");
		}
		clf.learnClassifer();
	}
	
	public String test(String[] record)
	{
		String line = binds(record);
		String label = clf.getLabel(line);
		return label;
	}
}
