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
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import edu.isi.karma.cleaning.PartitionClassifierType;

public class RecordClassifier2 implements PartitionClassifierType {
	HashMap<String, Vector<String>> trainData = new HashMap<String, Vector<String>>();
	RecordFeatureSet rf = new RecordFeatureSet();
	OnlineLogisticRegression cf;
	List<String> labels = new ArrayList<String>();
	LogisticModelParameters lmp;
	public RecordClassifier2() {

	}
	public OnlineLogisticRegression train(
			HashMap<String, Vector<String>> traindata) throws Exception {
		String csvTrainFile = "./target/tmp/csvtrain.csv";
		Data2Features.Traindata2CSV(traindata, csvTrainFile, rf);
		lmp = new LogisticModelParameters();
		lmp.setTargetVariable("label");
		lmp.setMaxTargetCategories(rf.labels.size());
		lmp.setNumFeatures(rf.getFeatureNames().size());
		List<String> typeList = Lists.newArrayList();
		typeList.add("numeric");
		List<String> predictorList = Lists.newArrayList();
		for (String attr : rf.getFeatureNames()) {
			if (attr.compareTo("lable") != 0) {
				predictorList.add(attr);
			}
		}
		lmp.setTypeMap(predictorList, typeList);
		// lmp.setUseBias(!getBooleanArgument(cmdLine, noBias));
		// lmp.setTypeMap(predictorList, typeList);
		lmp.setLambda(1e-4);
		lmp.setLearningRate(50);
		int passes = 100;
		CsvRecordFactory csv = lmp.getCsvRecordFactory();
		OnlineLogisticRegression lr = lmp.createRegression();
		for (int pass = 0; pass < passes; pass++) {
			BufferedReader in = new BufferedReader(new FileReader(new File(
					csvTrainFile)));
			;
			try {
				// read variable names
				csv.firstLine(in.readLine());
				String line = in.readLine();
				while (line != null) {
					// for each new line, get target and predictors
					RandomAccessSparseVector input = new RandomAccessSparseVector(
							lmp.getNumFeatures());
					int targetValue = csv.processLine(line, input);
					//String label = csv.getTargetCategories().get(lr.classifyFull(input).maxValueIndex());
					// now update model
					lr.train(targetValue, input);
					line = in.readLine();
				}
			} finally {
				Closeables.closeQuietly(in);
			}
		}
		labels = csv.getTargetCategories();
		return lr;

	}

	private static double predictorWeight(OnlineLogisticRegression lr, int row,
			RecordFactory csv, String predictor) {
		double weight = 0;
		for (Integer column : csv.getTraceDictionary().get(predictor)) {
			weight += lr.getBeta().get(row, column);
		}
		return weight;
	}

	public String Classify(String instance) {
		Collection<Feature> cfeat = rf.computeFeatures(instance, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		// row.add(f.getName());
		RandomAccessSparseVector row = new RandomAccessSparseVector(x.length);
		String line = "";
		for (int k = 0; k < cfeat.size(); k++) {
			line += x[k].getScore()+",";
		}
		line +="label"; // dummy class label for testing
		CsvRecordFactory csv = lmp.getCsvRecordFactory();
		csv.processLine(line, row);
		DenseVector dvec = (DenseVector) this.cf.classifyFull(row);
		String label = labels.get(dvec.maxValueIndex());
		return label;
	}

	@Override
	public void addTrainingData(String value, String label) {
		if (trainData.containsKey(label)) {
			trainData.get(label).add(value);
		} else {
			Vector<String> vsStrings = new Vector<String>();
			vsStrings.add(value);
			trainData.put(label, vsStrings);
		}
	}

	@Override
	public String learnClassifer() {
		try {
			this.cf = this.train(trainData);
		} catch (Exception e) {
			System.out.println("" );
		}
		if(cf ==null)
		{
			return "";
		}
		else
		{
			return this.cf.toString();
		}
	}

	@Override
	public String getLabel(String value) {
		try {
			String label = this.Classify(value);
			if (label.length() > 0)
				return label;
			else {
				return "null_in_classification";
			}
		} catch (Exception e) {
			return "null_in_classification";
			// TODO: handle exception
		}
	}

	public static void main(String[] args) {
		try {
			HashMap<String, Vector<String>> trainData = new HashMap<String, Vector<String>>();
			Vector<String> test = new Vector<String>();
			Vector<String> par1 = new Vector<String>();
			par1.add("L: 11.5 in, Diam: .875 in");
			par1.add("L: 9.5 in, W: 4.5 in");
			Vector<String> par2 = new Vector<String>();
			par2.add("H: 40 in, W: 31 in");
			par2.add("Neg Type: 120.00 mm");
			trainData.put("c1", par1);
			trainData.put("c2", par2);
			//trainData.put("c3", par3);
			//trainData.put("c4", par4);
			//trainData.put("c5", par5);
			test.add("L: 13 in, W: 13.5 in");

			RecordClassifier2 rc = new RecordClassifier2();
			for (String key : trainData.keySet()) {
				for (String value : trainData.get(key)) {
					rc.addTrainingData(value, key);
				}
			}
			rc.learnClassifer();
			System.out.println(rc.Classify(test.get(0)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
