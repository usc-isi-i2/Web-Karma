package edu.isi.karma.cleaning;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import org.json.JSONObject;
import org.python.antlr.PythonParser.return_stmt_return;

import au.com.bytecode.opencsv.CSVWriter;
import edu.isi.karma.cleaning.features.Data2Features;
import edu.isi.karma.cleaning.features.Feature;
import edu.isi.karma.cleaning.features.RegularityFeatureSet;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class UtilTools {
	public static int index = 0;
	public static Vector<String> results = new Vector<String>();
	public static Vector<Integer> getStringPos(int tokenpos,Vector<TNode> example)
	{
		Vector<Integer> poss = new Vector<Integer>();
		if(tokenpos <= 0)
			return poss;
		int pos = 0;
		int strleng = 0;
		for(int i=0;i<example.size();i++)
		{
			strleng += example.get(i).text.length();
		}
		for(int i = 0; i<tokenpos;i++)
		{
			pos += example.get(i).text.length();
		}
		poss.add(pos);
		poss.add(pos-strleng);
		return poss;
	}
	public static Vector<GrammarTreeNode> convertSegVector(Vector<Segment> x)
	{
		Vector<GrammarTreeNode> res = new Vector<GrammarTreeNode>();
		for(Segment e:x)
		{
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
		if(x == null)
			return "null";
		for (TNode t : x)
			if(t.text.compareTo("ANYTOK")==0)
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

	public static String escape(String s) {
		HashMap<String, String> dict = new HashMap<String, String>();
		dict.put("\\(", "\\\\(");
		dict.put("\\)", "\\\\)");
		dict.put("\\+", "\\\\+");
		dict.put("\\.", "\\\\.");
		dict.put("\\?", "\\\\?");
		dict.put("\\$", "\\\\$");
		dict.put("\\*", "\\\\*");
		dict.put("\\^", "\\\\^");
		dict.put("\\]", "\\\\]");
		dict.put("\\[", "\\\\[");
		dict.put("\\/", "\\\\/");
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

	public static String dic2Arff(String[] dic, String s) {
		String dirpathString = ServletContextParameterMap
				.getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
		if (dirpathString.compareTo("") == 0) {
			dirpathString = "./src/main/webapp/";
		}
		UtilTools.clearTmpVars();
		try {
			CSVWriter cw = new CSVWriter(new FileWriter(new File(dirpathString
					+ "grammar/tmp/tmp.csv")), ',');
			// write header into the csv file
			Vector<String> tmp = new Vector<String>();
			Vector<String> tmp1 = new Vector<String>();
			RegularityFeatureSet rfs = new RegularityFeatureSet();
			Collection<Feature> cols = rfs.computeFeatures(tmp, tmp1);
			String[] xyz = new String[rfs.fnames.size() + 1];
			for (int i = 0; i < xyz.length - 1; i++) {
				xyz[i] = "a_" + i;
			}
			xyz[xyz.length - 1] = "label";
			cw.writeNext(xyz);
			// write the data
			Vector<String> examples = new Vector<String>();
			if (s != null && s.length() > 0) {
				String[] z = s.split("\n");
				for (String elem : z) {
					if (elem.trim().length() > 0) {
						examples.add(elem.trim());
					}
				}
			}
			for (String o : dic) {
				UtilTools.results.add(o);
				Vector<String> row = new Vector<String>();
				if (s != null && o.compareTo(s) == 0) {
					RegularityFeatureSet rf = new RegularityFeatureSet();
					Vector<String> oexamples = new Vector<String>();
					String[] y = o.split("\n");
					for (String elem : y) {
						if (elem.trim().length() > 0) {
							oexamples.add(elem.trim());
						}
					}
					Collection<Feature> cf = rf.computeFeatures(oexamples,
							examples);
					Feature[] x = cf.toArray(new Feature[cf.size()]);
					// row.add(f.getName());
					for (int k = 0; k < cf.size(); k++) {
						row.add(String.valueOf(x[k].getScore()));
					}
					row.add("3"); // change this according to the dataset.
				} else {
					RegularityFeatureSet rf = new RegularityFeatureSet();
					Vector<String> oexamples = new Vector<String>();
					String[] y = o.split("\n");
					for (String elem : y) {
						if (elem.trim().length() > 0) {
							oexamples.add(elem.trim());
						}
					}
					Collection<Feature> cf = rf.computeFeatures(oexamples,
							examples);
					Feature[] x = cf.toArray(new Feature[cf.size()]);
					// row.add(f.getName());
					for (int k = 0; k < cf.size(); k++) {
						row.add(String.valueOf(x[k].getScore()));
					}
					row.add("0"); // change this according to the dataset.

				}
				cw.writeNext((String[]) row.toArray(new String[row.size()]));
			}
			cw.flush();
			cw.close();
			Data2Features.csv2arff(dirpathString + "grammar/tmp/tmp.csv",
					"./src/main/webapp/grammar/tmp/tmp.arff");
			return dirpathString + "grammar/tmp/tmp.arff";
		} catch (Exception e) {
			System.out.println("" + e.toString());
			return "";
		}

	}

	public static Vector<Integer> topKindexs(Vector<Double> scores, int k) {
		int cnt = 0;
		Vector<Integer> res = new Vector<Integer>();
		ScoreObj[] sas = new ScoreObj[scores.size()];
		for (int i = 0; i < scores.size(); i++) {
			sas[i] = new ScoreObj(i, scores.get(i));
		}
		Arrays.sort(sas, new DoubleCompare());
		while (cnt < k && cnt < sas.length) {
			res.add(sas[cnt].index);
			cnt++;
		}
		return res;
	}

	// unsupervised learning
	public static Vector<Double> getScores2(String[] res, String cpres) {
		Vector<Double> vds = new Vector<Double>();
		// convert the json format to \n seperated format
		try {
			String[] csvres = new String[res.length];
			for (int i = 0; i < res.length; i++) {
				JSONObject jso = new JSONObject(res[i]);
				Iterator<String> iter = jso.keys();
				String lines = "";
				while (iter.hasNext()) {
					lines += jso.getString(iter.next()) + "\n";
				}
				csvres[i] = lines;
			}
			Vector<String> examples = new Vector<String>();
			String s = cpres;
			String[] sy = cpres.split("\n");
			for(String tp:sy)
			{
				if (tp.trim().length() > 0) {
					examples.add(tp.trim());
				}
			}
			for (String o : csvres) {
				double soc = 0.0;
				RegularityFeatureSet rf = new RegularityFeatureSet();
				Vector<String> oexamples = new Vector<String>();
				String[] y = o.split("\n");
				for (String elem : y) {
					if (elem.trim().length() > 0) {
						oexamples.add(elem.trim());
					}
				}
				Collection<Feature> cf = rf
						.computeFeatures(oexamples, examples);
				Feature[] x = cf.toArray(new Feature[cf.size()]);
				// row.add(f.getName());
				for (int k = 0; k < cf.size(); k++) {
					soc += x[k].getScore();
				}
				vds.add(cf.size() - soc);
			}
			return vds;
		} catch (Exception ex) {
			System.out.println("Get Scores error: " + ex.toString());
			return vds;
		}
	}

	public static int rank(HashMap<String, Integer> dic, String s,
			String trainPath) {
		Set<String> keys = dic.keySet();
		String[] ks = (String[]) keys.toArray(new String[keys.size()]);
		String fpath = UtilTools.dic2Arff(ks, s);
		RegularityClassifer rc = new RegularityClassifer(trainPath);
		try {
			int rank = rc.getRank(fpath);
			if (rank < 0) {
				return -1;
			} else
				return rank;
		} catch (Exception ex) {
			System.out.println("" + ex.toString());
			return -1;
		}
	}

	public static Vector<Double> getScores(String[] res, String trainPath) {
		Vector<Double> vds = new Vector<Double>();
		// convert the json format to \n seperated format
		try {
			String[] csvres = new String[res.length];
			for (int i = 0; i < res.length; i++) {
				JSONObject jso = new JSONObject(res[i]);
				Iterator<String> iter = jso.keys();
				String lines = "";
				while (iter.hasNext()) {
					lines += jso.getString(iter.next()) + "\n";
				}
				csvres[i] = lines;
			}
			String fpath = UtilTools.dic2Arff(csvres, null);
			RegularityClassifer rc = new RegularityClassifer(trainPath);
			try {
				vds = rc.getScores(fpath);
				return vds;
			} catch (Exception ex) {
				System.out.println("get Scores error: " + ex.toString());
				return null;
			}
		} catch (Exception ex) {
			System.out.println("Get Scores error: " + ex.toString());
		}
		return vds;
	}

	public static void main(String[] args) {
		String s = "+";
		System.out.println("" + UtilTools.escape(s));
		// System.out.println(""+s.replace("(", "\\("));
	}

}

// used to sort the score in decend order
class ScoreObj {
	int index;
	double score;

	public ScoreObj(int index, double score) {
		this.index = index;
		this.score = score;
	}

}

class DoubleCompare implements Comparator {
	public int compare(Object x1, Object x2) {
		ScoreObj a1 = (ScoreObj) x1;
		ScoreObj a2 = (ScoreObj) x2;
		if (a1.score > a2.score) {
			return -1;
		} else if (a1.score < a2.score) {
			return 1;
		} else
			return 0;
	}
}
