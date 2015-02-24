package edu.isi.karma.cleaning;

import java.util.Vector;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;


public class PartitionClassifier {
	private PyObject interpreterClass;
	public String clssettingString = "";
	public String[] vocabs;

	public PartitionClassifier() {
		String dirpathString = "";
		if (dirpathString.compareTo("") == 0) {
			dirpathString = "./src/main/scripts/Lib";
		}
		PythonInterpreter interpreter = new PythonInterpreter();
		// change the sys.path
		interpreter.exec("import sys");
		// interpreter.exec("sys.path.append('/System/Library/Frameworks/Python.framework/Versions/2.7/Extras/lib/python/')");
		// interpreter.exec("sys.path.append('/Library/Python/2.7/site-packages')");
		interpreter.exec("sys.path.append('" + dirpathString + "')");
		// /Users/bowu/projects/IDCT/src/edu/isi/karma/cleaning
		interpreter.exec("sys.path.append('" + dirpathString + "')");
		// interpreter.exec("print sys.path");
		interpreter.exec("import re");
		// interpreter.exec("print sys.path");
		interpreter.exec("from FunctionList import *");
		interpreter.exec("from FeatureFactory import *");
		interpreter.exec("from NaiveBayes import *");
		interpreter.exec("from IDCTClassifier import *");
		interpreterClass = interpreter.get("IDCTClassifier");
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
