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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import edu.isi.karma.cleaning.PartitionClassifierType;

public class RecordClassifier2 implements PartitionClassifierType {
	private static Logger logger = LoggerFactory
			.getLogger(RecordClassifier2.class);
	HashMap<String, Vector<String>> trainData = new HashMap<String, Vector<String>>();
	RecordFeatureSet rf = new RecordFeatureSet();
	OnlineLogisticRegression cf;
	List<String> labels = new ArrayList<String>();
	LogisticModelParameters lmp;

	public RecordClassifier2() {
	}

	public RecordClassifier2(RecordFeatureSet rf) {
		this.rf = rf;
	}
	//clear the training data
	public void init()
	{
		this.trainData = new HashMap<String, Vector<String>>();
		this.rf.init();
	}
	@SuppressWarnings({ "deprecation" })
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
			if (attr.compareTo("label") != 0) {
				predictorList.add(attr);
			}
		}
		lmp.setTypeMap(predictorList, typeList);
		// lmp.setUseBias(!getBooleanArgument(cmdLine, noBias));
		// lmp.setTypeMap(predictorList, typeList);
		lmp.setLambda(0.1e-3);
		lmp.setLearningRate(20);
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
					// String label =
					// csv.getTargetCategories().get(lr.classifyFull(input).maxValueIndex());
					// now update model
					lr.train(targetValue, input);
					line = in.readLine();
				}
			}
			catch(Exception e)
			{
				logger.debug(e.toString());
			}
			finally {
				Closeables.closeQuietly(in);
			}
		}
		labels = csv.getTargetCategories();
		return lr;

	}
	public boolean selfVerify()
	{
		boolean res = true;
		for (String label: this.trainData.keySet())
		{
			Vector<String> tdata = trainData.get(label);
			for(String val: tdata)
			{
				if(this.Classify(val).compareTo(label) != 0)
				{
					return false;
				}
			}
		}
		return res;
	}
	public void crossVarify() throws Exception {
		String csvTrainFile = "./target/tmp/csvtraincopy.csv";
		// Data2Features.Traindata2CSV(this.trainData, csvTrainFile+"mock.csv",
		// rf);
		CSVReader cReader = new CSVReader(
				new FileReader(new File(csvTrainFile)),',','"','\0');
		String[] header = cReader.readNext();
		cReader.close();
		lmp = new LogisticModelParameters();
		lmp.setTargetVariable("label");
		lmp.setMaxTargetCategories(5);
		lmp.setNumFeatures(header.length - 1);
		List<String> typeList = Lists.newArrayList();
		typeList.add("numeric");
		List<String> predictorList = Lists.newArrayList();
		for (String attr : header) {
			if (attr.compareTo("label") != 0) {
				predictorList.add(attr);
			}
		}
		lmp.setTypeMap(predictorList, typeList);
		// lmp.setUseBias(!getBooleanArgument(cmdLine, noBias));
		lmp.setLambda(1e-4);
		lmp.setLearningRate(50);
		int passes = 100;
		CsvRecordFactory csv = lmp.getCsvRecordFactory();
		OnlineLogisticRegression lr = lmp.createRegression();
		HashMap<String,String> data = new HashMap<String,String>();
		for (int pass = 0; pass < passes; pass++) {
			BufferedReader in = new BufferedReader(new FileReader(new File(
					csvTrainFile)));
			;
			try {
				// read variable names
				String attrs = in.readLine();
				csv.firstLine(attrs);
				String line = in.readLine();
				while (line != null && line.length() > 0) {
					String[] labs = line.split(",");
					if(!data.containsKey(line))
						data.put(line,labs[labs.length-1].substring(1,labs[labs.length-1].length()-1));
					// for each new line, get target and predictors
					RandomAccessSparseVector input = new RandomAccessSparseVector(
							lmp.getNumFeatures());
					int targetValue = csv.processLine(line, input);
				    //String xlabel = csv.getTargetCategories().get(lr.classifyFull(input).maxValueIndex());
					// now update model
					lr.train(targetValue, input);
					line = in.readLine();
				}
			} catch (Exception e) {
				logger.info(e.toString());
			} finally {
				Closeables.closeQuietly(in);
			}
		}
		labels = csv.getTargetCategories();	
		for(String key:data.keySet())
		{
			RandomAccessSparseVector row = new RandomAccessSparseVector(header.length-1);
			csv = lmp.getCsvRecordFactory();
			csv.processLine(key, row);
			DenseVector dvec = (DenseVector) lr.classifyFull(row);
			String label = labels.get(dvec.maxValueIndex());
			if(label.compareTo(data.get(key))!= 0)
			{
				System.out.println(""+key);
			}
		}
		
	}

	public String Classify(String instance) {
		Collection<Feature> cfeat = rf.computeFeatures(instance, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		// row.add(f.getName());
		RandomAccessSparseVector row = new RandomAccessSparseVector(x.length);
		String line = "";
		for (int k = 0; k < cfeat.size(); k++) {
			line += x[k].getScore() + ",";
		}
		line += "label"; // dummy class label for testing
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
			logger.error(e.getMessage());
		}
		if (cf == null) {
			return "";
		} else {
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


	public void testClassifier(HashMap<String, ArrayList<String>> traindata) {
		HashSet<String[]> testData = new HashSet<String[]>();
		double threshold = 1.0;
		for (String ckey : traindata.keySet()) {
			int tLength = traindata.get(ckey).size();
			int stop = (int) (tLength * threshold);
			for (int i = 0; i < tLength; i++) {
				if (i < stop)
					this.addTrainingData(traindata.get(ckey).get(i), ckey);

				String[] pair = { traindata.get(ckey).get(i), ckey };
				testData.add(pair);
			}
		}
		this.learnClassifer();
		for (String[] elem : testData) {
			String l = this.Classify(elem[0]);
			if (l.compareTo(elem[1]) != 0) {
				logger.info("label: "+l);
				logger.info(Arrays.toString(elem));
			}
		}
	}

	public static void main(String[] args) {
		RecordClassifier2 rc2 = new RecordClassifier2();
		//rc2.TestFile();
		try {
			rc2.crossVarify();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(rc2.Classify("1865-c. 1906"));
	}
}
