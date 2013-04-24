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

package edu.isi.karma.cleaning.features;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

import edu.isi.karma.cleaning.PartitionClassifierType;

import weka.classifiers.functions.SimpleLogistic;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class RecordClassifier implements PartitionClassifierType{
	SimpleLogistic cf;
	String[] labels;
	Attribute category;
	RecordFeatureSet rf = new RecordFeatureSet();
	HashMap<String, Vector<String>> trainData = new HashMap<String, Vector<String>>();

	public RecordClassifier() {
	}

	public SimpleLogistic train(HashMap<String, Vector<String>> traindata)
			throws Exception {
		String trainFile = "./target/tmp/train.arff";
		String csvTrainFile = "./target/tmp/csvtrain.csv";
		Data2Features.Traindata2CSV(traindata, csvTrainFile,rf);
		Data2Features.Data2arff(csvTrainFile, trainFile);
		BufferedReader fileReader = new BufferedReader(
				new FileReader(trainFile));

		Instances instances = new Instances(fileReader);
		instances.setClassIndex(instances.numAttributes() - 1); // set the
																// category
																// attribute
		category = instances.attribute(instances.classIndex());
		SimpleLogistic logreg = new SimpleLogistic(10, true, true);
		logreg.buildClassifier(instances);
		return logreg;
	}

	public Vector<String> Classify(Vector<String> tests) throws Exception {
		String testFile = "./target/tmp/test.arff";
		String csvTestFile = "./target/tmp/csvtest.csv";
		Data2Features.Testdata2CSV(tests, csvTestFile,rf);
		Data2Features.Data2arff(csvTestFile, testFile);
		BufferedReader fileReader = new BufferedReader(new FileReader(testFile));

		Instances instances = new Instances(fileReader);
		instances.setClassIndex(instances.numAttributes() - 1);
		Vector<String> res = new Vector<String>();
		for (int i = 0; i < instances.size(); i++) {
			Instance instance = instances.get(i);
			double[] dist = cf.distributionForInstance(instance);
			Double max = -1.0;
			int index = 0;
			for (int pos = 0; pos < dist.length; pos++) {
				if (dist[pos] >max) {
					max = dist[pos];
					index = pos;
				}
			}
			res.add(category.value(index));
			
		}
		fileReader.close();
		return res;
	}

	public static void main(String[] args) {
		try {
			HashMap<String, Vector<String>> trainData = new HashMap<String, Vector<String>>();
			Vector<String> test = new Vector<String>();
			Vector<String> par1 = new Vector<String>();
			par1.add("Jan");
			//par1.add("11 w 37th pl, los angeles");
			Vector<String> par2 = new Vector<String>();
			par2.add("Feb");
			//par2.add("1 jefferson st");
			Vector<String> par3 = new Vector<String>();
			par3.add("Mar");
			//par3.add("1 jefferson st#ni bangongshi");
			trainData.put("c1", par1);
			trainData.put("c2", par2);
			trainData.put("c3", par3);
			test.add("Jan");

			RecordClassifier rc = new RecordClassifier();
			for(String key:trainData.keySet())
			{
				for(String value:trainData.get(key))
				{
					rc.addTrainingData(value, key);
				}
			}
			rc.learnClassifer();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addTrainingData(String value, String label) {
		if(trainData.containsKey(label))
		{
			trainData.get(label).add(value);
		}
		else {
			Vector<String> vsStrings = new Vector<String>();
			vsStrings.add(value);
			trainData.put(label, vsStrings);
		}
	}

	@Override
	public String learnClassifer() {
		try
		{
			this.cf = this.train(trainData);
		}
		catch (Exception e) {
			System.out.println(""+e.toString());
		}
		return this.cf.toString();
	}

	@Override
	public String getLabel(String value) {
		Vector<String> tests = new Vector<String>();
		tests.add(value);
		try
		{
			Vector<String> labels = this.Classify(tests);
			if(labels.size()>0)
				return labels.get(0);
			else
			{
				return "null_in_classification";
			}
		}
		catch (Exception e) {
			return "null_in_classification";
			// TODO: handle exception
		}
	}
}
