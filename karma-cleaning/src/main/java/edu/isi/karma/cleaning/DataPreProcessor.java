package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
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
	RecordFeatureSet rfs = new RecordFeatureSet();

	public DataPreProcessor(Collection<String> data) {
		this.data = data;
	}
	public HashMap<String, double[]> getStandardData()
	{
		return data2Vector;
	}
	public void run() {
		Vector<String> toks = buildDict(data);
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
				double[] row = getFeatureArray(line);
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

	public double[] getFeatureArray(String s) {
		Collection<Feature> cfeat = rfs.computeFeatures(s, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		double[] res = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[i].getScore();
		}
		return res;
	}

	public static Vector<String> buildDict(Collection<String> data) {
		HashMap<String, Integer> mapHashSet = new HashMap<String, Integer>();
		for (String pair : data) {
			String s1 = pair;
			if (s1.contains("<_START>")) {
				s1 = s1.replace("<_START>", "");
			}
			if (s1.contains("<_END>")) {
				s1 = s1.replace("<_END>", "");
			}
			Ruler r = new Ruler();
			r.setNewInput(s1);
			Vector<TNode> v = r.vec;
			HashSet<String> curRow = new HashSet<String>();
			for (TNode t : v) {
				String k = t.text;
				k = k.replaceAll("[0-9]+", "DIGITs");
				// filter punctuation
				if (k.trim().length() == 1) {
					if (!Character.isLetterOrDigit(k.charAt(0))) {
						continue;
					}
				}
				if (k.trim().length() == 0)
					continue;
				// only consider K once in one row
				if (curRow.contains(k)) {
					continue;
				} else {
					curRow.add(k);
				}
				if (mapHashSet.containsKey(k)) {
					mapHashSet.put(k, mapHashSet.get(k) + 1);
				} else {
					mapHashSet.put(k, 1);
				}
			}
		}
		// prune infrequent terms
		int thresdhold = (int) (data.size() * 0.10);
		Iterator<Entry<String, Integer>> iter = mapHashSet.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Integer> e = iter.next();
			if (e.getValue() < thresdhold) {
				iter.remove();
			}
		}
		Vector<String> res = new Vector<String>();
		res.addAll(mapHashSet.keySet());
		return res;
	}

}
