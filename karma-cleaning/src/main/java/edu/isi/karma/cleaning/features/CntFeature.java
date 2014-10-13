package edu.isi.karma.cleaning.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;

public class CntFeature implements Feature {
	String name = "";
	double score = 0.0;
	Vector<TNode> pa;

	public void setName(String name) {
		this.name = name;
	}

	public CntFeature(ArrayList<Vector<TNode>> v, ArrayList<Vector<TNode>> n,
			Vector<TNode> t) {
		pa = t;
		score = calFeatures(v, n);
	}

	// x is the old y is the new example
	public double calFeatures(ArrayList<Vector<TNode>> x,
			ArrayList<Vector<TNode>> y) {
		HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
		for (int i = 0; i < y.size(); i++) {
			int cnt = 0;
			Vector<TNode> z = x.get(i);
			Vector<TNode> z1 = y.get(i);
			int bpos = 0;
			int p = 0;
			int bpos1 = 0;
			int p1 = 0;
			int cnt1 = 0;
			while (p != -1) {
				p = Ruler.Search(z, pa, bpos);
				if (p == -1)
					break;
				bpos = p + 1;
				cnt++;
			}
			while (p1 != -1) {
				p1 = Ruler.Search(z1, pa, bpos1);
				if (p1 == -1)
					break;
				bpos1 = p1 + 1;
				cnt1++;
			}
			// use the minus value to compute homogenenity
			// cnt = cnt - cnt1;
			cnt = cnt1;
			if (tmp.containsKey(cnt)) {
				tmp.put(cnt, tmp.get(cnt) + 1);
			} else {
				tmp.put(cnt, 1);
			}
		}
		Integer a[] = new Integer[tmp.keySet().size()];
		tmp.values().toArray(a);
		int b[] = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[i].intValue();
		}
		double res = RegularityFeatureSet.calShannonEntropy(b) * 1.0
				/ Math.log(x.size());
		return res;
	}

	public String getName() {

		return this.name;
	}

	public double getScore() {

		return score;
	}
}
