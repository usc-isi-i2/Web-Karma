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

package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.mahout.math.Arrays;

import edu.isi.karma.cleaning.QuestionableRecord.OutlierDetector;

public class ExampleSelection {
	public HashMap<String, Vector<TNode>> org = new HashMap<String, Vector<TNode>>();
	public HashMap<String, Vector<TNode>> tran = new HashMap<String, Vector<TNode>>();
	public HashMap<String, String[]> raw = new HashMap<String, String[]>();
	public boolean isDetectingQuestionableRecord = false;
	public OutlierDetector out;
	// testdata rowid:{tar, tarcolor}
	public HashMap<String, HashMap<String, String[]>> testdata = new HashMap<String, HashMap<String, String[]>>();
	public int way = 7;
	public HashSet<String> dictionary = new HashSet<String>();

	public ExampleSelection() {
		this.out = new OutlierDetector();
	}

	public String Choose() {
		String ID = "";
		switch (way) {
		case 1:
			ID = this.way1();
			break;
		case 2:
			ID = this.way2();
			break;
		case 3:
			ID = this.way3();
			break;
		case 4:
			ID = this.way4();
			break;
		case 6:
			ID = this.way6();
			break;
		case 7:
			ID = this.way7();
			break;
		case 8:
			ID = this.way8();
			break;
		case 9:
			ID = this.way9();
			break;
		default:
			ID = "";
		}
		return ID;
	}

	public Vector<String[]> getOrgTarPair(HashMap<String, String[]> exps) {
		Vector<String[]> result = new Vector<String[]>();
		for (String key : exps.keySet()) {
			String[] record = exps.get(key);
			String[] tmp = { record[0], record[1] };
			result.add(tmp);
		}
		return result;
	}

	// exps: rowId: {org, tar, tarcode,classlabel}
	// example: partition id: [{raw,tarcode}]
	public void inite(HashMap<String, String[]> exps,
			HashMap<String, Vector<String[]>> examples) {
		// inite the class center vector

		if (way >= 6) {
			if (firsttime) {
				out = new OutlierDetector();
				out.buildDict(this.getOrgTarPair(exps));
				dictionary = out.dict;
			}
			out.buildMeanVector(examples, dictionary);
		}
		Ruler ruler = new Ruler();
		for (String keyString : exps.keySet()) {
			String e = exps.get(keyString)[0];
			ruler.setNewInput(e);
			org.put(keyString, ruler.vec);
			if (way >= 6) {
				String raw = exps.get(keyString)[0];
				String[] pair = { raw, exps.get(keyString)[2] };
				if (testdata.containsKey(exps.get(keyString)[3])) {
					HashMap<String, String[]> xelem = testdata.get(exps
							.get(keyString)[3]);
					if (!xelem.containsKey(keyString)) {
						xelem.put(keyString, pair);
					}
				} else {
					HashMap<String, String[]> vstr = new HashMap<String, String[]>();
					vstr.put(keyString, pair);
					testdata.put(exps.get(keyString)[3], vstr);
				}
			}
		}

		this.raw = exps;
	}

	// choose the most ambiguous
	public String way1() {
		String ID = "";
		int maximum = -1;
		for (String key : org.keySet()) {
			int s = this.ambiguityScore(org.get(key));
			if (s > maximum) {
				ID = key;
				maximum = s;
			}
		}
		return ID;
	}

	// return the least ambiguous
	public String way2() {
		String ID = "";
		int minimum = Integer.MAX_VALUE;
		for (String key : org.keySet()) {
			int s = this.ambiguityScore(org.get(key));
			if (s < minimum) {
				ID = key;
				minimum = s;
			}
		}
		return ID;
	}

	// return the first incorrect one, simulated ideal user
	public String way3() {
		String ID = "";
		int minimum = Integer.MAX_VALUE;
		for (String key : raw.keySet()) {
			int s = Integer.valueOf(key);
			if (s < minimum) {
				ID = key;
				minimum = s;
			}
		}
		return ID;
	}

	public int ambiguityScore(Vector<TNode> vec) {
		HashMap<String, Integer> d = new HashMap<String, Integer>();
		int score = 0;
		for (int i = 0; i < vec.size(); i++) {
			if (d.containsKey(vec.get(i).text))
				continue;
			for (int j = 0; j < vec.size(); j++) {
				if (vec.get(j).text.compareTo(vec.get(i).text) == 0 && i != j
						&& vec.get(j).text.compareTo(" ") != 0) {
					score++;
				}
			}
			if (!d.containsKey(vec.get(i).text)) {
				d.put(vec.get(i).text, score);
			}
		}
		return score;
	}

	// only try to find the wrong ones
	public static boolean firsttime = true;

	public String way4() {
		if (firsttime) {
			firsttime = false;
			return raw.keySet().iterator().next();
		}
		for (String key : raw.keySet()) {

			if (raw.get(key)[2].indexOf("_FATAL_ERROR_") != -1) {
				return key;
			}
		}
		return this.way2();
	}

	public String way6() {
		int max = 2; // only the one with _FATAL_ERROR_ inside
		if (firsttime) {
			firsttime = false;
			return this.way2();
		}
		Vector<String> examples = new Vector<String>();
		for (String key : raw.keySet()) {
			int cnt = 0;
			String[] tmp = raw.get(key)[2]
					.split("((?<=_\\d_FATAL_ERROR_)|(?=_\\d_FATAL_ERROR_))");

			for (String tmpstring : tmp) {
				int errnum = 0;
				if (tmpstring.indexOf("_FATAL_ERROR_") == -1) {
					continue;
				}
				errnum = Integer.valueOf(tmpstring.substring(1, 2));
				cnt += errnum;
			}
			if (cnt > max) {
				max = cnt;
				examples.clear();
				examples.add(key);
			}
			if (cnt == max && max > 1) {
				examples.add(key);
			}
		}
		// if now _FATAL_ERROR_ detected use outlier detection
		if (examples.size() == 0) {
			String row = "";
			row = way8();
			return row;
		} else { // select the most ambigious among all the record with same
					// number of FATALERROR
			String idString = "";
			int min = 10000;
			for (String key : examples) {
				int s = this.ambiguityScore(org.get(key));
				if (s < min) {
					min = s;
					idString = key;
				}
			}
			return idString;
		}
	}

	public String way7() {
		// this.printdata();
		int max = 2; // only the one with _FATAL_ERROR_ inside
		if (firsttime) {
			firsttime = false;
			return this.way2();
		}
		Vector<String> examples = new Vector<String>();
		for (String key : raw.keySet()) {
			int cnt = 0;
			String[] tmp = raw.get(key)[2]
					.split("((?<=_\\d_FATAL_ERROR_)|(?=_\\d_FATAL_ERROR_))");
			for (String tmpstring : tmp) {
				int errnum = 0;
				if (tmpstring.indexOf("_FATAL_ERROR_") == -1) {
					continue;
				}
				errnum = Integer.valueOf(tmpstring.substring(1, 2));
				cnt += errnum;
			}
			if (cnt > max) {
				max = cnt;
				examples.clear();
				examples.add(key);
			}
			if (cnt == max && max > 1) {
				examples.add(key);
			}
		}
		// if no _FATAL_ERROR_ detected use outlier detection
		if (examples.size() == 0) {
			isDetectingQuestionableRecord = true;
			String row = "";
			double tmax = -1;
			for (String key : this.testdata.keySet()) {
				String trowid = out.getOutliers(testdata.get(key),
						out.rVectors.get(key), tmax, dictionary);
				tmax = out.currentMax;
				if (trowid.length() > 0) {
					row = trowid;
				}
			}
			return row;
		} else { // select the most ambigious among all the record with same
					// number of FATALERROR
			isDetectingQuestionableRecord = false;
			String idString = "";
			int min = 10000;
			for (String key : examples) {
				int s = this.ambiguityScore(org.get(key));
				if (s < min) {
					min = s;
					idString = key;
				}
			}
			return idString;
		}
	}

	// shortest result
	// exps: rowId: {org, tar, tarcode,classlabel}
	public String way8() {
		if (firsttime) {
			firsttime = false;
			return this.way3();
		}
		String idString = "";
		int shortest = 10000;
		for (String rowid : raw.keySet()) {
			String xrow = raw.get(rowid)[1];
			if (xrow.indexOf("_FATAL_ERROR_") != -1) {
				xrow = raw.get(rowid)[0];
			}
			if (xrow.length() < shortest) {
				shortest = xrow.length();
				idString = rowid;
			}
		}
		return idString;
	}

	// longest result
	public String way9() {
		if (firsttime) {
			firsttime = false;
			return this.way3();
		}
		String idString = "";
		int longest = -1;
		for (String rowid : raw.keySet()) {
			String xrow = raw.get(rowid)[1];
			if (xrow.indexOf("_FATAL_ERROR_") != -1) {
				xrow = raw.get(rowid)[0];
			}
			if (xrow.length() > longest) {
				longest = xrow.length();
				idString = rowid;
			}
		}
		return idString;
	}

	public void clear() {
		this.raw.clear();
		org.clear();
		tran.clear();
		this.testdata.clear();
	}

	public void printdata() {
		String s1 = "";
		String s2 = "";
		for (String key : this.testdata.keySet()) {
			HashMap<String, String[]> r = testdata.get(key);
			s1 += "partition " + key + "\n";
			for (String[] elem : r.values()) {
				s1 += Arrays.toString(elem) + "\n";
			}
		}
		System.out.println("" + s1);
		for (String[] v : this.raw.values()) {
			s2 += Arrays.toString(v) + "\n";
		}
		System.out.println(s2);

	}

}
