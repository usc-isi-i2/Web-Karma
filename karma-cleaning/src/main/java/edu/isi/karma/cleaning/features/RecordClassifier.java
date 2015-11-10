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
	public double power = 1.0;
	public svm_model tempModelbuf = null;
	public RecordClassifier() {
		this.rf = new RecordFeatureSet();
	}

	public RecordClassifier(RecordFeatureSet rf) {
		this.rf = rf;
	}

	public RecordClassifier(RecordFeatureSet rfs, int type) {
		this.rf = rfs;
		this.ctype = type;
		if (ctype == svm_parameter.ONE_CLASS) {
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
		feature_cnt = feature_cnt != 0 ? feature_cnt : value.length;
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
			node.value = x[k].getScore(value);
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

	public String printTraindata(svm_node[][] data) {
		String res = "";
		String line = Arrays.toString(this.rf.getFeatureNames().toArray(new String[this.feature_cnt]));
		res += line + "\n";
		for (svm_node[] l : data) {
			String tmp = "";
			for (svm_node n : l) {
				tmp += n.index + ":" + n.value + " ";
			}
			tmp = tmp.substring(0, tmp.length() - 1);
			res += tmp + "\n";
		}
		return res;
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

	public void convertToCSVfile() {
		ArrayList<String[]> xArrayList = new ArrayList<String[]>();
		String[] attrname = new String[feature_cnt + 1];
		String[] names = rf.getFeatureNames().toArray(new String[attrname.length - 1]);
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
			CSVWriter cr = new CSVWriter(new FileWriter("/Users/bowu/Research/testdata/tmp/data.csv"));
			for (String[] line : xArrayList) {
				cr.writeNext(line);
			}
			cr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void NormalizeTestingData(svm_node[] tmp) {
		double[] maxvals = this.maxValues;
		double[] minvals = this.minValues;
		for (int i = 0; i < tmp.length; i++) {
			if (maxvals[i] > minvals[i]) {
				tmp[i].value = Math.pow((tmp[i].value - minvals[i]) * 1.0 / (maxvals[i] - minvals[i]), this.power);
			} else {
				tmp[i].value = 0;
			}
		}
	}

	public void NormalizeTrainingData(double power) {
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
					tmp[i].value = Math.pow((tmp[i].value - minvals[i]) * 1.0 / (maxvals[i] - minvals[i]), power);
				} else {
					tmp[i].value = 0;
				}
			}
		}
	}

	public svm_node[] powerNormalize(double factor, svm_node[] line) {
		svm_node[] newline = new svm_node[line.length];
		for (int i = 0; i < line.length; i++) {
			newline[i] = new svm_node();
			newline[i].index = line[i].index;
			newline[i].value = Math.pow(line[i].value, factor);
		}
		return newline;
	}

	public void parameterselectionandScale() {
		this.NormalizeTrainingData(1.0);
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
		// dignose();
		this.expandData();
		this.parameterselectionandScale();
		return "";
	}

	public double getAccuracyforParameter(double gamma, double c) {
		svm_model model = internallearnClassifer(gamma, c, trainData, targets);
		double x = getAccuracy(model, trainData, targets);
		return x;
	}

	public double mean(double[] array) {
		double sum = 0.0;
		for (double d : array) {
			sum += d;
		}
		return sum * 1.0 / array.length;
	}

	public void gridSearch() {
		double benchMark = 1.0 / this.rf.getFeatureNames().size();
		double[][] gammas = { { benchMark * 0.1, benchMark * 0.2, benchMark * 0.3 }, { benchMark * 0.4, benchMark * 0.5, benchMark * 0.6 },
				{ benchMark * 0.7, benchMark * 0.8, benchMark * 0.9, benchMark }, { benchMark, benchMark * 1.1, benchMark * 1.2, benchMark * 1.3, benchMark * 1.4, benchMark * 1.5 },
				{ benchMark * 1.6, benchMark * 1.7, benchMark * 1.8, benchMark * 1.9, benchMark * 2.0 } };
		double[][] c = { { 0.5, 0.6, 0.7 }, { 0.8, 0.9, 1.0 }, { 2.0, 3.0, 4.0, 5.0, 6.0 } };
		double[][] power = { { 0.1, 0.2, 0.3 }, { 0.4, 0.5, 0.6, 0.7 }, { 0.8, 0.9, 1.0 } };
		double maxAcc = -1;
		double optG = 0.5;
		double optC = 1;
		double optP = 1;
		int gindex = 0, cindex = 0, pindex = 0;
		for (int i = 0; i < gammas.length; i++) {
			for (int j = 0; j < c.length; j++) {
				for (int k = 0; k < power.length; k++) {
					double acc = cross_verify(mean(gammas[i]), mean(c[j]), mean(power[k]));
					if (acc > maxAcc) {
						optG = mean(gammas[i]);
						optC = mean(c[j]);
						optP = mean(power[k]);
						gindex = i;
						cindex = j;
						pindex = k;
						maxAcc = acc;
					}
				}
			}
		}
		maxAcc = -1;
		for (int i = 0; i < gammas[gindex].length; i++) {
			for (int j = 0; j < c[cindex].length; j++) {
				for (int k = 0; k < power[pindex].length; k++) {
					if (maxAcc == 1) {
						break;
					}
					double acc = cross_verify(gammas[gindex][i], c[cindex][j], power[pindex][k]);
					if (acc > maxAcc) {
						maxAcc = acc;
						this.model = this.tempModelbuf;
						optC = c[cindex][j];
						optG = gammas[gindex][i];
						optP = power[pindex][k];
					}
				}
			}
		}
		ArrayList<svm_node[]> normalized = new ArrayList<svm_node[]>();
		this.power = optP;
		for (svm_node[] line : trainData) {
			normalized.add(powerNormalize(optP, line));
		}
		trainData = normalized;
	}

	public int getMultiplierScale(HashMap<Double, ArrayList<Integer>> labPos, int fold) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (Double key : labPos.keySet()) {
			ret.add(labPos.get(key).size());
		}
		Collections.sort(ret);
		int min = ret.get(0);
		if (fold <= min) {
			return 0;
		} else {
			if (min == 0) {
				min = 1;
			}
			return fold / min;
		}
	}
	public void expandData(){
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
		int times = getMultiplierScale(labPos, fold);
		for (Double l : labPos.keySet()) {
			ArrayList<Integer> ditems = (ArrayList<Integer>) labPos.get(l).clone();
			for (int t = 1; t < times; t++) {
				labPos.get(l).addAll(ditems);
			}
		}
		ArrayList<svm_node[]> train = new ArrayList<svm_node[]>();
		ArrayList<Double> tar = new ArrayList<Double>();
		for (Double l : labPos.keySet()) {
			int datasize = labPos.get(l).size();
			for (int k = 0; k < datasize; k++) {
				int itemIndex = labPos.get(l).get(k);
				train.add(deepCopy(trainData.get(itemIndex)));
				tar.add(targets.get(itemIndex));
			}
		}
		this.trainData = train;
		this.targets = tar;
	}
	public svm_node[] deepCopy(svm_node[] org){
		svm_node[] ret = new svm_node[org.length];
		for(int i = 0; i < ret.length; i++){
			svm_node n = new svm_node();
			n.index = org[i].index;
			n.value = org[i].value;
			ret[i] = n;
		}
		return ret;
	}
	public double cross_verify(double gamma, double c, double pow) {
		double acc = 0.0;
		double bestIter = -1;
		svm_model bestModel = null;
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
		/*
		int times = getMultiplierScale(labPos, fold);
		for (Double l : labPos.keySet()) {
			ArrayList<Integer> ditems = (ArrayList<Integer>) labPos.get(l).clone();
			for (int t = 1; t < times + 1; t++) {
				labPos.get(l).addAll(ditems);
			}
		}*/
		for (int i = 0; i < fold; i++) {
			ArrayList<svm_node[]> tmpTrain = new ArrayList<svm_node[]>();
			ArrayList<svm_node[]> tmpTest = new ArrayList<svm_node[]>();
			ArrayList<Double> tmpTraintar = new ArrayList<Double>();
			ArrayList<Double> tmpTesttar = new ArrayList<Double>();
			for (Double l : labPos.keySet()) {
				int datasize = labPos.get(l).size();
				for (int k = 0; k < datasize; k++) {
					int itemIndex = labPos.get(l).get(k);
					if (k % fold == i) {
						tmpTest.add(powerNormalize(pow, trainData.get(itemIndex)));
						tmpTesttar.add(targets.get(itemIndex));
					} else {
						tmpTrain.add(powerNormalize(pow, trainData.get(itemIndex)));
						tmpTraintar.add(targets.get(itemIndex));
					}
				}
			}
			svm_model m = internallearnClassifer(gamma, c, tmpTrain, tmpTraintar);
			double iteracc = getAccuracy(m, tmpTest, tmpTesttar);
			acc += iteracc;
			if(iteracc > bestIter){
				bestIter = iteracc;
				bestModel = m;
			}
		}
		this.tempModelbuf = bestModel;
		double ret =  acc * 1.0 / fold;
		return ret;
	}

	public double getAccuracy(svm_model m, ArrayList<svm_node[]> tData, ArrayList<Double> tars) {
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

	public svm_model internallearnClassifer(double gamma, double c, ArrayList<svm_node[]> tData, ArrayList<Double> tars) {
		// duplicateData();
		this.convertToCSVfile();
		svm_problem problem = new svm_problem();
		problem.l = tData.size();
		problem.x = tData.toArray(new svm_node[tData.size()][]); // feature
																	// nodes
		problem.y = ArrayUtils.toPrimitive(tars.toArray(new Double[tars.size()])); // target
																					// values
		svm_parameter parameters = new svm_parameter();

		parameters.gamma = gamma;
		parameters.svm_type = ctype;
		parameters.kernel_type = svm_parameter.RBF;
		// parameters.kernel_type = svm_parameter.LINEAR;
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

	public String getLabel(double[] value) {
		svm_node[] testNodes = new svm_node[value.length];
		for (int k = 0; k < testNodes.length; k++) {
			svm_node node = new svm_node();
			node.index = k;
			node.value = value[k];
			testNodes[k] = node;
		}
		double v = svm.svm_predict(model, testNodes);
		return findLable(v);
	}
	private svm_node[] getNodes(String value){
		Collection<Feature> cfeat = rf.computeFeatures(value, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		// row.add(f.getName());
		svm_node[] testNodes = new svm_node[cfeat.size()];
		for (int k = 0; k < cfeat.size(); k++) {
			svm_node node = new svm_node();
			node.index = k;
			node.value = x[k].getScore(value);
			testNodes[k] = node;
		}
		NormalizeTestingData(testNodes);
		return testNodes;
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
			node.value = x[k].getScore(value);
			testNodes[k] = node;
		}
		/* temp test */

		// double[] prob_estimates = new
		// double[this.labelMapping.keySet().size()];
		NormalizeTestingData(testNodes);
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
		System.out.println(this.rf.getFeatureNames());
		for (int i = 0; i < this.trainData.size(); i++) {
			//System.out.println("Label: " + this.rawData.get(i) + ", " + this.targets.get(i));
			System.out.println(this.printLine(trainData.get(i)));
			System.out.println(this.printLine(this.getNodes(rawData.get(i))));
			//System.out.println("Model: " + svm.svm_predict(model, this.trainData.get(i)));
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
		String[] vocbs = { };
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
			int testSplit = 20;
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
			selfVerify2();
			br1.close();
			br2.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		RecordClassifier rcf1 = new RecordClassifier();
		rcf1.testOnFile();
	}

}
