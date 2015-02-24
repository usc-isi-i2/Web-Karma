package edu.isi.karma.cleaning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVWriter;
import edu.isi.karma.cleaning.features.RecordClassifier;

public class UtilTools {
	public static int index = 0;
	public static Vector<String> results = new Vector<String>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map sortByComparator(Map unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		// put sorted list into map again
		// LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	// math operators

	// inner product
	public static double product(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i] * b[i];
		}
		return sum;
	}

	// sum over vectors
	public static double[] sum(Collection<double[]> vec) {
		Iterator<double[]> iter = vec.iterator();
		if (vec.size() <= 0)
			return null;
		double[] res = new double[iter.next().length];
		while (iter.hasNext()) {
			double[] tmp = iter.next();
			for (int i = 0; i < res.length; i++) {
				res[i] += tmp[i];
			}
		}
		return res;
	}

	// init array using one element
	public static double[] initArray(double[] array, double a) {
		double[] res = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			res[i] = a;
		}
		return res;
	}

	// scalar multiply vector
	public static double[] produce(double coeff, double[] vec) {
		double[] res = new double[vec.length];
		for (int i = 0; i < vec.length; i++) {
			res[i] = coeff * vec[i];
		}
		return res;
	}

	public static double distance(double[] a, double[] b, double[] w) {
		double res = 0.0;
		for (int i = 0; i < a.length; i++) {
			res += Math.pow(a[i] - b[i], 2) * w[i];
		}
		return Math.sqrt(res);
	}

	public static double distance(double[] a, double[] b) {
		double res = 0.0;
		for (int i = 0; i < a.length; i++) {
			res += Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(res);
	}

	public static Vector<Integer> getStringPos(int tokenpos,
			Vector<TNode> example) {

		Vector<Integer> poss = new Vector<Integer>();
		if (tokenpos < 0)
			return poss;
		int pos = 0;
		int strleng = 0;
		for (int i = 0; i < example.size(); i++) {
			strleng += example.get(i).text.length();
		}
		for (int i = 0; i < tokenpos; i++) {
			pos += example.get(i).text.length();
		}
		poss.add(pos); // forward position
		poss.add(pos - strleng); // backward position
		return poss;
	}

	public static Vector<GrammarTreeNode> convertSegVector(Vector<Segment> x) {
		Vector<GrammarTreeNode> res = new Vector<GrammarTreeNode>();
		for (Segment e : x) {
			res.add(e);
		}
		return res;
	}

	public static int multinominalSampler(double[] probs) {
		Random r = new Random();
		double x = r.nextDouble();
		if (x <= probs[0]) {
			return 0;
		}
		x -= probs[0];
		for (int i = 1; i < probs.length; i++) {
			if (x <= probs[i]) {
				return i;
			}
			x -= probs[i];
		}
		return 0;
	}

	public static int randChoose(int n) {
		Random r = new Random();
		return r.nextInt(n);
	}

	public static String print(Vector<TNode> x) {
		String str = "";
		if (x == null)
			return "null";
		for (TNode t : x)
			if (t.text.compareTo("ANYTOK") == 0)
				str += t.getType();
			else
				str += t.text;
		return str;
	}

	public static boolean samesteplength(Vector<Integer> s) {
		if (s.size() <= 1)
			return false;
		if (s.size() == 2) {
			if (s.get(1) - s.get(0) >= 1) {
				return true;
			} else {
				return false;
			}
		}
		int span = s.get(1) - s.get(0);
		for (int i = 2; i < s.size(); i++) {
			if ((s.get(i) - s.get(i - 1)) != span)
				return false;
		}
		return true;
	}

	public static void StringColorCode(String org, String res,
			HashMap<String, String> dict) throws Exception {
		int segmentCnt = 0;
		Vector<int[]> allUpdates = new Vector<int[]>();
		String pat = "((?<=\\{_L\\})|(?=\\{_L\\}))";
		String pat1 = "((?<=\\{_S\\})|(?=\\{_S\\}))";
		String orgdis = "";
		String tardis = "";
		String tar = "";
		String[] st = res.split(pat);
		boolean inloop = false;
		for (String token : st) {
			if (token.compareTo("{_L}") == 0 && !inloop) {
				inloop = true;
				continue;
			}
			if (token.compareTo("{_L}") == 0 && inloop) {
				inloop = false;
				continue;
			}
			String[] st1 = token.split(pat1);
			for (String str : st1) {
				if (str.compareTo("{_S}") == 0 || str.compareTo("{_S}") == 0) {
					continue;
				}
				if (str.indexOf("{_C}") != -1) {
					String[] pos = str.split("\\{_C\\}");
					int[] poses = { Integer.valueOf(pos[0]),
							Integer.valueOf(pos[1]), segmentCnt };
					boolean findPos = false;
					for (int i = 0; i < allUpdates.size(); i++) {
						int[] cur = allUpdates.get(i);
						if (poses[0] <= cur[0]) {
							findPos = true;
							allUpdates.add(i, poses);
							break; // avoid infinite adding
						}
					}
					if (!findPos) {
						allUpdates.add(poses);
					}
					String tarseg = org.substring(Integer.valueOf(pos[0]),
							Integer.valueOf(pos[1]));

					if (inloop) {

						tardis += String.format(
								"<span class=\"a%d\">%s</span>", segmentCnt,
								tarseg);
						// orgdis +=
						// String.format("<span class=\"a%d\">%s</span>",
						// segmentCnt,tarseg);
						tar += tarseg;
					} else {
						tardis += String.format(
								"<span class=\"a%d\">%s</span>", segmentCnt,
								tarseg);
						// orgdis +=
						// String.format("<span class=\"a%d\">%s</span>",
						// segmentCnt,tarseg);
						segmentCnt++;
						tar += tarseg;
					}

				} else {
					tardis += String.format("<span class=\"ins\">%s</span>",
							str);
					tar += str;
				}
			}
		}
		int pre = 0;
		for (int[] update : allUpdates) {
			if (update[0] >= pre) {
				orgdis += org.substring(pre, update[0]);
				orgdis += String.format("<span class=\"a%d\">%s</span>",
						update[2], org.substring(update[0], update[1]));
				pre = update[1];
			}
		}
		if (org.length() > pre)
			orgdis += org.substring(pre);
		dict.put("Org", org);
		dict.put("Tar", tar);
		dict.put("Orgdis", orgdis);
		dict.put("Tardis", tardis);
	}

	public static String escape(String s) {
		s = s.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		HashMap<String, String> dict = new HashMap<String, String>();
		dict.put("\\(", "\\\\(");
		dict.put("\\)", "\\\\)");
		dict.put("\\+", "\\\\+");
		dict.put("\\.", "\\\\.");
		dict.put("\\?", "\\\\?");
		dict.put("\\$", "\\\\\\$");
		dict.put("\\*", "\\\\*");
		dict.put("\\^", "\\\\^");
		dict.put("\\]", "\\\\]");
		dict.put("\\[", "\\\\[");
		dict.put("\\/", "\\\\/");
		dict.put("\\'", "\\\\'");
		dict.put("\\|", "\\\\|");
		dict.put("\\\"", "\\\\\"");
		for (String key : dict.keySet()) {
			s = s.replaceAll(key, dict.get(key));
		}
		return s;
	}

	public static Vector<TNode> subtokenseqs(int a, int b, Vector<TNode> org) {
		Vector<TNode> xNodes = new Vector<TNode>();
		if (a < 0 || b >= org.size() || a > b) {
			return null;
		} else {
			for (int i = a; i <= b; i++) {
				xNodes.add(org.get(i));
			}
			return xNodes;
		}
	}

	public static void clearTmpVars() {
		results.clear();
		index = 0;
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

	public static String formatExp(String[] e) {
		String res = String.format("%s|%s", e[0], e[1]);
		return res;
	}

	// test whether a covers b
	public static boolean iscovered(String a, String b) {
		String[] elems = b.split("\\*");
		boolean covered = true;
		for (String e : elems) {
			if (a.indexOf(e) == -1) {
				covered = false;
				break;
			}
		}
		return covered;
	}

	public static String createkey(ArrayList<String[]> examples) {
		ArrayList<String> tmp = new ArrayList<String>();
		for (String[] ele : examples) {
			String t = UtilTools.formatExp(ele);
			tmp.add(t);
		}
		Collections.sort(tmp);
		String key = "";
		for (String e : tmp) {
			key += e + "*";
		}
		return key;
	}

	public static ArrayList<String[]> extractExamplesinPartition(
			Collection<Partition> pars) {
		ArrayList<String[]> examples = new ArrayList<String[]>();
		for (Partition par : pars) {
			for (int i = 0; i < par.orgNodes.size(); i++) {
				String[] exp = { UtilTools.print(par.orgNodes.get(i)),
						UtilTools.print(par.tarNodes.get(i)) };
				examples.add(exp);
			}
		}
		return examples;
	}

	public static void clusterData(
			Collection<String[]> data, RecordClassifier rcf) {
		HashMap<String, ArrayList<String[]>> res = new HashMap<String, ArrayList<String[]>>();
		for (String[] elem : data) {
			String[] n = { elem[0], elem[1] };
			String lab = rcf.getLabel(elem[0]);
			if (res.containsKey(lab)) {
				res.get(lab).add(n);
			} else {
				ArrayList<String[]> x = new ArrayList<String[]>();
				x.add(n);
				res.put(lab, x);
			}
		}
		for (String key : res.keySet()) {
			ArrayList<String[]> cdata = res.get(key);
			File f = new File("./log/" + key + ".csv");
			CSVWriter cWriter;
			try {
				cWriter = new CSVWriter(new FileWriter(f));

				for (String[] line : cdata) {
					cWriter.writeNext(line);
				}
				cWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static Vector<TNode> convertStringtoTNodes(String s1){
		Ruler r = new Ruler();
		r.setNewInput(s1);;
		return r.vec;
	}
}
