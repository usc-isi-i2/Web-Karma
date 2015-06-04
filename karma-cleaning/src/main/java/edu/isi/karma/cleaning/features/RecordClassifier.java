package edu.isi.karma.cleaning.features;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;
/*import de.bwaldvogel.liblinear.Feature;
 import de.bwaldvogel.liblinear.FeatureNode;
 import de.bwaldvogel.liblinear.Linear;
 import de.bwaldvogel.liblinear.Model;
 import de.bwaldvogel.liblinear.parameterseter;
 import de.bwaldvogel.liblinear.Problem;
 import de.bwaldvogel.liblinear.SolverType;*/
import edu.isi.karma.cleaning.PartitionClassifierType;
import edu.isi.karma.cleaning.UtilTools;

public class RecordClassifier implements PartitionClassifierType {
	Logger ulogger = LoggerFactory.getLogger(RecordClassifier.class);
	RecordFeatureSet rf;
	svm_model model;
	ArrayList<svm_node[]> trainData = new ArrayList<svm_node[]>();
	ArrayList<String> rawData = new ArrayList<String>();
	ArrayList<Double> targets = new ArrayList<Double>();
	int exampleCnt = 0;
	HashMap<String, Double> labelMapping = new HashMap<String, Double>();
	public double[] maxValues;
	public double[] minValues;
	public int ctype = svm_parameter.C_SVC; 
	public double nu = 0.5;
	public int feature_cnt = 0;
	public RecordClassifier() {
		this.rf = new RecordFeatureSet();
	}

	public RecordClassifier(RecordFeatureSet rf) {
		this.rf = rf;
	}
	public RecordClassifier(RecordFeatureSet rfs, int type){
		this.rf = rfs;
		this.ctype = type;
		if(ctype == svm_parameter.ONE_CLASS){
			this.nu = 0.01;
		}	
	}
	public void init(String[] vocb) {
		this.trainData = new ArrayList<svm_node[]>();
		this.targets = new ArrayList<Double>();
		labelMapping = new HashMap<String, Double>();
		exampleCnt = 0;
		model = null;
		this.minValues = null;
		this.maxValues = null;
		this.rf.init();
		this.rf.addVocabulary(vocb);
	}

	public void addTrainingData(String v, double[] value, String label) {
		// convert value to feature vector
		rawData.add(v);
		svm_node[] testNodes = new svm_node[value.length];
		feature_cnt = feature_cnt != 0 ? feature_cnt: value.length;
		for (int k = 0; k < testNodes.length; k++) {
			svm_node node = new svm_node();
			node.index = k;
			node.value = value[k];
			testNodes[k] = node;
		}
		this.trainData.add(testNodes);
		// convert label to a double class label
		if (labelMapping.containsKey(label)) {
			this.targets.add(labelMapping.get(label));
		} else {
			double lb = 0;
			if (!this.labelMapping.isEmpty()) {
				lb = Collections.max(labelMapping.values()) + 1;
			}
			this.labelMapping.put(label, lb);
			this.targets.add(lb);
		}
	}

	public void addTrainingData(String value, String label) {
		// convert value to feature vector
		rawData.add(value);
		Collection<Feature> cfeat = rf.computeFeatures(value, "");
		feature_cnt = cfeat.size();
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		// row.add(f.getName());
		svm_node[] testNodes = new svm_node[cfeat.size()];
		for (int k = 0; k < cfeat.size(); k++) {
			svm_node node = new svm_node();
			node.index = k;
			node.value = x[k].getScore();
			testNodes[k] = node;
		}
		this.trainData.add(testNodes);
		// convert label to a double class label
		if (labelMapping.containsKey(label)) {
			this.targets.add(labelMapping.get(label));
		} else {
			double lb = 0;
			if (!this.labelMapping.isEmpty()) {
				lb = Collections.max(labelMapping.values()) + 1;
			}
			this.labelMapping.put(label, lb);
			this.targets.add(lb);
		}
	}

	public void printTraindata(svm_node[][] data) {
		String res = "";
		String line = Arrays.toString(this.rf.getFeatureNames().toArray(
				new String[this.feature_cnt]));
		res += line + "\n";
		for (svm_node[] l : data) {
			String tmp = "";
			for (svm_node n : l) {
				tmp += n.index + ":" + n.value + " ";
			}
			tmp = tmp.substring(0, tmp.length() - 1);
			res += tmp + "\n";
		}
	}

	// duplicate data to avoid the situation that one instance per class (svm
	// doesn't support prob estimation for this case)
	public void duplicateData() {
		int size = trainData.size();
		for (int i = 0; i < size; i++) {
			// insert row into traindata
			svm_node[] row = trainData.get(i);
			trainData.add(row);
			// insert target into the target values
			Double val = targets.get(i);
			targets.add(val);
		}
	}

	// used to balance unbalanced data.
	public HashMap<Double, Double> adjustunbalancedData() {
		HashMap<Double, Double> class2weight = new HashMap<Double, Double>();
		for (Double b : targets) {
			if (class2weight.containsKey(b)) {
				class2weight.put(b, class2weight.get(b) + 1);
			} else {
				class2weight.put(b, 1.0);
			}
		}
		Double maxNumber = Collections.max(class2weight.values());
		for (Double key : class2weight.keySet()) {
			class2weight.put(key, maxNumber / class2weight.get(key) * 1.0);
		}
		return class2weight;
	}

	public void dignose() {
		String s = Arrays.toString(this.rf.vocabs);
		ulogger.info("Diagnose info....Vocbulary learned: " + s);
	}

	public void rescale(svm_node[] tmp) {
		double[] maxvals = this.maxValues;
		double[] minvals = this.minValues;
		for (int i = 0; i < tmp.length; i++) {
			if (maxvals[i] > minvals[i]) {
				tmp[i].value = (tmp[i].value - minvals[i]) * 1.0
						/ (maxvals[i] - minvals[i]);
			} else {
				tmp[i].value = 0;
			}
		}
	}

	public void convertToCSVfile() {
		ArrayList<String[]> xArrayList = new ArrayList<String[]>();
		String[] attrname = new String[feature_cnt + 1];
		String[] names = rf.getFeatureNames().toArray(
				new String[attrname.length - 1]);
		// add attribute names
		System.arraycopy(names, 0, attrname, 0, names.length);
		attrname[attrname.length - 1] = "label";
		xArrayList.add(attrname);
		for (svm_node[] tmp : trainData) {
			String[] row = new String[tmp.length + 1];
			for (int i = 0; i < tmp.length; i++) {
				row[i] = tmp[i].value + "";
			}
			row[row.length - 1] = "c" + targets.get(trainData.indexOf(tmp));
			xArrayList.add(row);
		}
		try {
			CSVWriter cr = new CSVWriter(new FileWriter(
					"/Users/bowu/Research/testdata/tmp/data.csv"));
			for (String[] line : xArrayList) {
				cr.writeNext(line);
			}
			cr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void NormalizeTrainingData() {
		int featuresize = feature_cnt;
		double[] maxvals = new double[featuresize];
		maxvals = UtilTools.initArray(maxvals, -1);
		double[] minvals = new double[featuresize];
		minvals = UtilTools.initArray(minvals, Double.MAX_VALUE);
		for (svm_node[] tmp : trainData) {
			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i].value > maxvals[i]) {
					maxvals[i] = tmp[i].value;
				}
				if (tmp[i].value < minvals[i]) {
					minvals[i] = tmp[i].value;
				}
			}
		}
		minValues = minvals;
		maxValues = maxvals;

		for (svm_node[] tmp : trainData) {
			for (int i = 0; i < tmp.length; i++) {
				if (maxvals[i] > minvals[i]) {
					tmp[i].value = (tmp[i].value - minvals[i]) * 1.0
							/ (maxvals[i] - minvals[i]);
				} else {
					tmp[i].value = 0;
				}
			}
		}
	}

	public void parameterselectionandScale() {
		this.NormalizeTrainingData();
		this.gridSearch();
	}

	public double getAccuracy() {
		int totalCnt = trainData.size();
		int errorCnt = 0;
		for (int i = 0; i < trainData.size(); i++) {
			svm_node[] tdata = trainData.get(i);
			double tar = targets.get(i);
			double v = svm.svm_predict(model, tdata);
			if (v != tar) {
				errorCnt++;
			}
		}
		return (totalCnt - errorCnt) * 1.0 / totalCnt;

	}

	@Override
	public String learnClassifer() {
		//dignose();
		this.parameterselectionandScale();
		return "";
	}

	public void gridSearch() {
		double[][] gammas = { { 0.001,0.005, 0.01, 0.03, 0.05,0.1,0.5},
				{ 0.06, 0.07, 0.08, 0.09, 0.1 } };
		double[][] c = { { 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 },
				{ 2.0, 3.0, 4.0, 5.0, 6.0 } };
		double maxAcc = -1;
		double optG = 0.5;
		double optC = 1;
		// coarse level search
		double[] gInd = {
				(gammas[0][0] + gammas[0][gammas[0].length - 1]) * 1.0 / 2,
				(gammas[1][0] + gammas[1][gammas[1].length - 1]) * 1.0 / 2 };
		double[] cInd = { (c[0][0] + c[0][c[0].length - 1]) * 1.0 / 2,
				(c[1][0] + c[1][c[1].length - 1]) * 1.0 / 2 };
		int gIndex = -1;
		int cIndex = -1;
		for (int i = 0; i < gInd.length; i++) {
			for (int j = 0; j < cInd.length; j++) {
				double acc = cross_verify(gInd[i], cInd[j]);
				if (acc > maxAcc) {
					maxAcc = acc;
					gIndex = i;
					cIndex = j;

				}
			}
		}
		maxAcc = -1;
		// fine level search
		for (int i = 0; i < gammas[gIndex].length; i++) {
			for (int j = 0; j < c[cIndex].length; j++) {
				double acc = cross_verify(gammas[gIndex][i], c[cIndex][j]);
				if (acc > maxAcc) {
					maxAcc = acc;
					optC = c[cIndex][j];
					optG = gammas[gIndex][i];
				}
			}
		}
		// retain on all the data
		//System.out.println(String.format("Gamma: %f, C: %f, Acc: %f", optG,optC, maxAcc));
		this.model = internallearnClassifer(optG, optC, trainData, targets);
		return;
	}

	public double cross_verify(double gamma, double c) {
		double acc = 0.0;
		int fold = 5;
		HashMap<Double, ArrayList<Integer>> labPos = new HashMap<Double, ArrayList<Integer>>();
		for (int i = 0; i < targets.size(); i++) {
			Double l = targets.get(i);
			if (labPos.containsKey(l)) {
				labPos.get(l).add(i);
			} else {
				ArrayList<Integer> pos = new ArrayList<Integer>();
				pos.add(i);
				labPos.put(l, pos);
			}
		}

		for (int i = 0; i < fold; i++) {
			ArrayList<svm_node[]> tmpTrain = new ArrayList<svm_node[]>();
			ArrayList<svm_node[]> tmpTest = new ArrayList<svm_node[]>();
			ArrayList<Double> tmpTraintar = new ArrayList<Double>();
			ArrayList<Double> tmpTesttar = new ArrayList<Double>();
			for (Double l : labPos.keySet()) {
				int datasize = labPos.get(l).size();
				if (datasize < fold) {
					// add its own data until reach the fold number
					int times = fold / datasize;
					@SuppressWarnings("unchecked")
					ArrayList<Integer> ditems = (ArrayList<Integer>) labPos.get(l).clone();
					for (int t = 1; t < times+1; t++) {
						labPos.get(l).addAll(ditems);
					}
					datasize = labPos.get(l).size();
				}
				for (int k = 0; k < datasize; k++) {
					int itemIndex = labPos.get(l).get(k);
					if (k % fold == i) {
						tmpTest.add(trainData.get(itemIndex));
						tmpTesttar.add(targets.get(itemIndex));
					} else {
						tmpTrain.add(trainData.get(itemIndex));
						tmpTraintar.add(targets.get(itemIndex));
					}
				}
			}
			svm_model m = internallearnClassifer(gamma, c, tmpTrain,
					tmpTraintar);
			acc += getAccuracy(m, tmpTest, tmpTesttar);
		}
		return acc * 1.0 / fold;
	}

	public double getAccuracy(svm_model m, ArrayList<svm_node[]> tData,
			ArrayList<Double> tars) {
		if (tData.size() == 0) {
			return 0;
		}
		int errorCnt = 0;
		for (int i = 0; i < tData.size(); i++) {
			double v = svm.svm_predict(m, tData.get(i));
			if (v != tars.get(i)) {
				errorCnt++;
			}
		}
		return 1 - errorCnt * 1.0 / tData.size();
	}

	public svm_model internallearnClassifer(double gamma, double c,
			ArrayList<svm_node[]> tData, ArrayList<Double> tars) {
		// duplicateData();
		this.convertToCSVfile();
		svm_problem problem = new svm_problem();
		problem.l = tData.size();
		problem.x = tData.toArray(new svm_node[tData.size()][]); // feature
																	// nodes
		problem.y = ArrayUtils
				.toPrimitive(tars.toArray(new Double[tars.size()])); // target
																		// values
		svm_parameter parameters = new svm_parameter();

		parameters.gamma = gamma;
		parameters.svm_type = ctype;
		parameters.kernel_type = svm_parameter.RBF;
		//parameters.kernel_type = svm_parameter.LINEAR;
		parameters.degree = 3;
		parameters.coef0 = 0;
		parameters.nu = this.nu;
		parameters.cache_size = 100;
		parameters.C = c;
		parameters.eps = 1e-5;
		parameters.p = 0.1;
		parameters.shrinking = 1;
		parameters.probability = 0;

		HashMap<Double, Double> wtsdict = adjustunbalancedData();
		parameters.nr_weight = wtsdict.keySet().size();
		int ptr = 0;
		int[] wts_labels = new int[parameters.nr_weight];
		double[] wts = new double[parameters.nr_weight];
		for (Double key : wtsdict.keySet()) {
			wts_labels[ptr] = key.intValue();
			wts[ptr] = wtsdict.get(key);
			ptr++;
		}
		parameters.weight_label = wts_labels;
		parameters.weight = wts;

		svm.svm_set_print_string_function(new svm_print_interface() {
			public void print(String s) {
			}
		});
		svm.rand.setSeed(0);
		svm_model model = svm.svm_train(problem, parameters);
		return model;
	}
	public String getLabel(double[] value){
		svm_node[] testNodes = new svm_node[value.length];
		for(int k = 0; k < testNodes.length; k++){
			svm_node node = new svm_node();
			node.index = k;
			node.value = value[k];
			testNodes[k] = node;
		}
		double v = svm.svm_predict(model, testNodes);
		return findLable(v);
	}
	@Override
	public String getLabel(String value) {
		Collection<Feature> cfeat = rf.computeFeatures(value, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		// row.add(f.getName());
		svm_node[] testNodes = new svm_node[cfeat.size()];
		for (int k = 0; k < cfeat.size(); k++) {
			svm_node node = new svm_node();
			node.index = k;
			node.value = x[k].getScore();
			testNodes[k] = node;
		}
		/* temp test */

		// double[] prob_estimates = new
		// double[this.labelMapping.keySet().size()];
		rescale(testNodes);
		double v = svm.svm_predict(model, testNodes);
		// find string lable
		return findLable(v);
	}

	public String findLable(double v) {
		String label = "";
		for (String key : labelMapping.keySet()) {
			if (labelMapping.get(key) == v) {
				label = key;
			}
		}
		if (label.compareTo("") == 0) {
		}
		return label;
	}

	public void selfVerify2() {
		for (int i = 0; i < this.trainData.size(); i++) {
			System.out.println("Label: " + this.rawData.get(i) + ", "
					+ this.getLabel(this.rawData.get(i)));
			System.out.println("Test1:" + this.printLine(trainData.get(i)));
			System.out.println("Model: "
					+ svm.svm_predict(model, this.trainData.get(i)));
		}
	}

	public String printLine(svm_node[] line) {
		String x = "";
		for (int i = 0; i < line.length; i++) {
			x += line[i].value + ",";
		}
		x = x.substring(0, x.length() - 1);
		return x;
	}

	public boolean selfVerify() {
		boolean res = true;
		for (int i = 0; i < trainData.size(); i++) {
			svm_node[] tdata = trainData.get(i);
			double tar = targets.get(i);
			double v = svm.svm_predict(model, tdata);
			if (v != tar) {
				return false;
			}
		}
		return res;
	}

	public void testOnFile() {
		String[] vocbs = { "=","\\(", "-","'", "\\.", "/" };
		String fpath1 = "/Users/bowu/Research/testdata/tmp/data.txt";
		String fpath2 = "/Users/bowu/Research/testdata/tmp/labels.txt";
		try {
			BufferedReader br1 = new BufferedReader(new FileReader(fpath1));
			BufferedReader br2 = new BufferedReader(new FileReader(fpath2));
			String line = "";
			ArrayList<String> data = new ArrayList<String>();
			ArrayList<String> labels = new ArrayList<String>();
			while ((line = br1.readLine()) != null) {
				if (line.trim().length() > 0) {
					data.add(line);
				}
			}
			while ((line = br2.readLine()) != null) {
				if (line.trim().length() > 0) {
					labels.add(line);
				}
			}
			RecordFeatureSet rfs = new RecordFeatureSet();
			rfs.addVocabulary(vocbs);
			this.rf = rfs;
			for (int i = 0; i < data.size(); i++) {
				addTrainingData(data.get(i), "c" + labels.get(i));
			}
			learnClassifer();
			for (int i = 0; i < data.size(); i++) {
				String predict = getLabel(data.get(i));
				String rlabel = labels.get(i);
				String tmpline = String.format("%s, %s, %s\n", predict, rlabel, data.get(i));
				System.out.println(""+tmpline);
			}
			selfVerify();
			br1.close();
			br2.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// RecordFeatureSet rfs = new RecordFeatureSet();
		// String[] vocbs = { "D", "E", "G", "(", ")", "H", ".", "in", "I",
		// "DIGITs", "W", "cm", "P", "x" };
		// // String[] vocbs ={};
		// rfs.addVocabulary(vocbs);
		// RecordClassifier rcf = new RecordClassifier(rfs);
		// rcf.addTrainingData(
		// "11.75 in|15.5 in HIGH x 15.75 in|19.75 in WIDE(29.84 cm|39.37 cm HIGH x 40.00 cm|50.16 cm WIDE)",
		// "c1");
		// rcf.addTrainingData(
		// "7.25 in|11.5 in HIGH x 9.25 in|13 in WIDE(18.41 cm|29.21 cm HIGH x 23.49 cm|33.02 cm WIDE)",
		// "c1");
		// rcf.addTrainingData(
		// "9.75 in|16 in HIGH x 13.75 in|19.5 in WIDE(24.76 cm|40.64 cm HIGH x 34.92 cm|49.53 cm WIDE)",
		// "c1");
		// rcf.addTrainingData("20 in. HIGH x 24 in. WIDE", "c2");
		// rcf.addTrainingData("26 in. HIGH x 22.5 in. WIDE", "c2");
		// rcf.addTrainingData("10.25 in. HIGH x 8.5 in. WIDE", "c2");
		// rcf.addTrainingData("59.75 in WIDE(151.76 cm WIDE)", "c3");
		// rcf.addTrainingData("29.75 in HIGH(75.56 cm HIGH)", "c4");
		// rcf.learnClassifer();
		// for (String val : rcf.rawData) {
		// System.out.println("class: " + rcf.getLabel(val));
		// }
		//
		// System.out.println("self verifying....");
		// System.out.println(rcf.selfVerify());
		// // System.out.println("class: "+rcf.getLabel("."));
		// // System.out.println("class: "+rcf.getLabel("&$"));
		/*RecordFeatureSet rfs1 = new RecordFeatureSet();
		String[] vocbs1 = { "by", "G", "L", "M", "K", "ade", "DIGITs" };
		rfs1.addVocabulary(vocbs1);*/
		RecordClassifier rcf1 = new RecordClassifier();
		rcf1.testOnFile();
	}

}
