package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.features.Feature;
import edu.isi.karma.cleaning.features.RecordFeatureSet;
//identify candidate tokens
//vectorize string to feature vectors
//decorrelate features
//rescale features

public class DataPreProcessor {
	public Collection<String> data;
	HashMap<String, double[]> data2Vector = new HashMap<String, double[]>();
	double[] max_values;
	double[] min_values;
	RecordFeatureSet rfs = new RecordFeatureSet();

	public DataPreProcessor(Collection<String> data) {
		this.data = data;
	}
	public HashMap<String, double[]> getStandardData()
	{
		return data2Vector;
	}
	public String[] getAllFeatures(){
		return rfs.xStrings;
	}
	public void run() {
		Vector<String> toks = UtilTools.buildDict(data);
		// vectorize String
		String[] vocb = toks.toArray(new String[toks.size()]);
		rfs.addVocabulary(vocb);
		HashMap<String, double[]> xHashMap = vectorize(data);
		resacle(xHashMap);
		// deCorrelat features
		ArrayList<Integer> toremove = deCorrelate(xHashMap);
		// update the featureSet and Vectorize the data again
		rfs.removeFeatures(toremove);
		// resvectorize the data
		xHashMap = vectorize(data);
		// rescale the data
		// rescale each feature
		resacle(xHashMap);
		this.data2Vector = xHashMap;
	}
	public String[] getFeatureName(){
		Collection<String> names = rfs.getFeatureNames();
		String[] ret = names.toArray(new String[names.size()]);
		return ret;
	}
	public double[] getNormalizedreScaledVector(String data) throws Exception{
		if(data2Vector.containsKey(data)){
			return data2Vector.get(data);
		}
		else{
			throw new Exception(data+" has not been normalized and rescaled");
		}
		
	}
	public void resacle(HashMap<String, double[]> xHashMap)
	{
		double[] maxvals = new double[rfs.getFeatureNames().size()];
		maxvals = UtilTools.initArray(maxvals, -1);
		double[] minvals = new double[rfs.getFeatureNames().size()];
		minvals = UtilTools.initArray(minvals, Double.MAX_VALUE);
		for (double[] tmp : xHashMap.values()) {
			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i] > maxvals[i]) {
					maxvals[i] = tmp[i];
				}
				if (tmp[i] < minvals[i]) {
					minvals[i] = tmp[i];
				}
			}
		}
		this.max_values = maxvals;
		this.min_values = minvals;
		for (String key : xHashMap.keySet()) {
			double[] value = xHashMap.get(key);
			for (int i = 0; i < value.length; i++) {
				if (maxvals[i] > minvals[i]) {
					double tmpval = (value[i] - minvals[i])/(maxvals[i] - minvals[i]);
					value[i] = Math.pow(tmpval, 0.5);
				} else {
					value[i] = 0;
				}
			}
			xHashMap.put(key, value);
		}
	}
	public HashMap<String, double[]> vectorize(Collection<String> data) {
		HashMap<String, double[]> res = new HashMap<String, double[]>();
		for (String line : data) {
			if (!res.containsKey(line)) {
				double[] row = getRawFeatureArray(line);
				res.put(line, row);
			}
		}
		return res;
	}

	public ArrayList<Integer> deCorrelate(HashMap<String, double[]> data) {
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		HashSet<String> signs = new HashSet<String>();
		// build singature for each feature
		for (int i = 0; i < rfs.getFeatureNames().size(); i++) {
			String sg = "";
			for (String key : data.keySet()) {
				sg += data.get(key)[i]+"\n";
			}
			if (signs.contains(sg)) {
				toRemove.add(i);
			} else {
				signs.add(sg);
			}
		}
		return toRemove;

	}
	public double[] getRawFeatureArray(String s){
		Collection<Feature> cfeat = rfs.computeFeatures(s, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		double[] res = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[i].getScore(s);
		}
		return res;
	}
	public double[] getFeatureArray(String s) {
		Collection<Feature> cfeat = rfs.computeFeatures(s, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		double[] res = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[i].getScore(s);
		}
		for(int i = 0; i < res.length; i++){
			if(res[i] >= max_values[i]){
				res[i] = 1;
			}
			else if(res[i] <= min_values[i]){
				res[i] = 0;
			}
			else{
				res[i] = (res[i] - min_values[i])/(max_values[i] - min_values[i]);
			}
		}
		return res;
	}
	public static void main(String[] args){
	}

}
