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

package edu.isi.karma.cleaning.QuestionableRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;

public class OutlierDetector {
	public HashMap<String, double[]> rVectors = new HashMap<String, double[]>();
	public double currentMax = -1;
	public HashSet<String> dict = new HashSet<String>();

	public OutlierDetector() {

	}

	public double getDistance(double[] x, double[] y) {
		if (x == null || y==null ||x.length != y.length )
			return Double.MAX_VALUE;
		double value = 0.0;
		for (int i = 0; i < x.length; i++) {
			value += Math.pow(x[i] - y[i], 2);
		}
		return Math.sqrt(value);
	}

	// find all the word appearing in org and tar records
	public void buildDict(Collection<String[]> data) {
		HashMap<String, Integer> mapHashSet = new HashMap<String, Integer>();
		for (String[] pair : data) {
			String s1 = pair[0];
			String s2 = pair[1];
			if (s1.contains("<_START>")) {
				s1 = s1.replace("<_START>", "");
			}
			if (s1.contains("<_END>")) {
				s1 = s1.replace("<_END>", "");
			}
			if (s2.contains("<_START>")) {
				s2 = s2.replace("<_START>", "");
			}
			if (s2.contains("<_END>")) {
				s2 = s2.replace("<_END>", "");
			}
			Ruler r = new Ruler();
			r.setNewInput(s1);
			Vector<TNode> v = r.vec;
			r.setNewInput(s2);
			v.addAll(r.vec);
			HashSet<String> curRow = new HashSet<String>();
			for (TNode t : v) {
				String k = t.text;
				k = k.replaceAll("[0-9]+", "DIGITs");
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
		int thresdhold = (int) (data.size() * 0.5);
		Iterator<Entry<String, Integer>> iter = mapHashSet.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Integer> e = iter.next();
			if (e.getValue() < thresdhold) {
				iter.remove();
			}
		}
		this.dict = new HashSet<String>(mapHashSet.keySet());
	}

	// find outliers for one partition
	// simple 2d distance
	// testdata rowid:{tar, tarcolor}
	public String getOutliers(HashMap<String, String[]> testdata,
			double[] meanVector, double Max, HashSet<String> dic) {
		String Id = "";
		for (String key : testdata.keySet()) {
			String[] vpair = testdata.get(key);
			FeatureVector fvFeatureVector = new FeatureVector(dic);
			Vector<RecFeature> vRecFeatures = fvFeatureVector.createVector(
					vpair[0], vpair[1]);
			double[] x = new double[fvFeatureVector.size()];
			for (int i = 0; i < vRecFeatures.size(); i++) {
				x[i] = vRecFeatures.get(i).computerScore();
			}
			double value = this.getDistance(x, meanVector);
			if (value > Max) {
				Max = value;
				this.currentMax = Max;
				Id = key;
			}
		}
		return Id;
	}

	// pid: [{rawstring, code}]
	public void buildMeanVector(HashMap<String, Vector<String[]>> data,
			HashSet<String> dict) {
		rVectors.clear();
		this.currentMax = -1;
		if (data == null)
			return;
		for (String key : data.keySet()) {
			Vector<String[]> vs = data.get(key);
			FeatureVector fVector = new FeatureVector(dict);
			double[] dvec = new double[fVector.size()];
			for (int i = 0; i < dvec.length; i++) {
				dvec[i] = 0;
			}
			for (String[] elem : vs) {
				Vector<RecFeature> sFeatures = fVector.createVector(elem[0],
						elem[1]);
				for (int j = 0; j < sFeatures.size(); j++) {
					dvec[j] += sFeatures.get(j).computerScore();
				}
			}
			// get average size
			for (int i = 0; i < dvec.length; i++) {
				dvec[i] = dvec[i] * 1.0 / vs.size();
			}
			rVectors.put(key, dvec);
		}
	}

	public String test(double[] row) {
		String string = "";
		for (double d : row) {
			string += d + ",";
		}
		return string.substring(1, string.length() - 1);
	}

}
