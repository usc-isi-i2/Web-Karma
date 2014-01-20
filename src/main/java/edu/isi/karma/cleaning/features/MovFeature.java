package edu.isi.karma.cleaning.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;

public class MovFeature implements Feature {
	String name = "";
	double score = 0.0;
	Vector<TNode> pa;

	public MovFeature(ArrayList<Vector<TNode>> v, ArrayList<Vector<TNode>> n,
			Vector<TNode> t) {
		pa = t;
		score = calFeatures(v, n);
	}

	// x is the old y is the new example
	public double calFeatures(ArrayList<Vector<TNode>> x,
			ArrayList<Vector<TNode>> y) {
		HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
		for (int i = 0; i < x.size(); i++) {
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
			cnt = cnt - cnt1;
			int direction = 0;// indicate the moving direction 0 left 1 right 2
								// still
			if (cnt < 0) {
				direction = 1;
			} else if (cnt > 0) {
				direction = 0;
			} else {
				direction = 2;
			}
			if (tmp.containsKey(direction)) {
				tmp.put(direction, tmp.get(direction) + 1);
			} else {
				tmp.put(direction, 1);
			}
		}
		Integer a[] = new Integer[tmp.keySet().size()];
		tmp.values().toArray(a);
		int b[] = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[i].intValue();
		}
		return RegularityFeatureSet.calShannonEntropy(b) * 1.0
				/ Math.log(x.size());
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	public double getScore() {
		// TODO Auto-generated method stub
		return this.score;
	}

}
