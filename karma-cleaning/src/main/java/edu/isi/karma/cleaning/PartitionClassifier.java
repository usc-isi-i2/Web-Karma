package edu.isi.karma.cleaning;

import java.util.Vector;

import org.python.core.PyObject;


public class PartitionClassifier {
	@SuppressWarnings("unused")
	private PyObject interpreterClass = null;
	public String clssettingString = "";
	public String[] vocabs;

	public PartitionClassifier() {
	}
	public PartitionClassifierType create2(Vector<Partition> pars,PartitionClassifierType ele,DataPreProcessor dpp)
	{
		ele.init();
		for(int i = 0; i<pars.size(); i++)
		{
			Partition partition = pars.get(i);
			for (int j = 0; j < partition.orgNodes.size(); j++) {
				String s = UtilTools.print(partition.orgNodes.get(j));
				String label = partition.label;
				ele.addTrainingData(s, label);
			}
			for(int j = 0; j < partition.orgUnlabeledData.size(); j++)
			{
				String label = partition.label;
				String s = partition.orgUnlabeledData.get(j);
				ele.addTrainingData(s, label);
			}
		}
		this.clssettingString = ele.learnClassifer();
		return ele;
	}
}
