package edu.isi.karma.cleaning;

import java.util.Vector;

public class Template implements GrammarTreeNode {
	public static final int temp_limit = 2048;
	public int supermode = 1;
	public Vector<GrammarTreeNode> body = new Vector<GrammarTreeNode>();
	public Vector<Vector<Integer>> indexes = new Vector<Vector<Integer>>();
	public int curState = 0;
	public long size = 1;

	public Template(Vector<GrammarTreeNode> body) {
		this.body = body;
		Vector<Long> x = new Vector<Long>();
		for (GrammarTreeNode g : body) {
			x.add(g.size());
		}
		for (Long k : x) {
			size *= k;
		}
		getCrossIndex(x, indexes);
	}

	public String toString() {
		String str = "";
		for (GrammarTreeNode gt : body) {
			str += gt.toString();
		}
		return str;
	}

	/*
	 * public void getCrossIndex(Vector<Long> indexs, int cur, String path,
	 * Vector<Vector<Integer>> configs) { String tpath = path; if
	 * (configs.size() > temp_limit) { return; } if (cur >= indexs.size()) {
	 * String[] elems = tpath.split(","); Vector<Integer> line = new
	 * Vector<Integer>(); for (String s : elems) { String x = s.trim(); if
	 * (x.length() > 0) { line.add(Integer.parseInt(x)); } } if (line.size() >
	 * 0) { configs.add(line); } return; } for (int i = 0; i < indexs.get(cur);
	 * i++) { String xtpath = path + i + ","; getCrossIndex(indexs, cur + 1,
	 * xtpath, configs); } }
	 */
	// iteratively generate combinations
	public void getCrossIndex(Vector<Long> indexs,
			Vector<Vector<Integer>> configs) {
		int k = indexs.size();
		int[] com = new int[k];
		for (int i = 0; i < k; i++)
			com[i] = 0;
		while (com[k - 1] < indexs.get(k - 1)) {
			Vector<Integer> res = new Vector<Integer>();
			for (int i = 0; i < k; i++) {
				// System.out.print(""+com[i]);
				res.add(com[i]);
			}
			configs.add(res);
			if (configs.size() > temp_limit) {
				return;
			}
			int t = k - 1;
			while (t != 0 && com[t] == indexs.get(t) - 1)
				t--;
			com[t]++;
			if (t == 0 && com[t] >= indexs.get(0)) {
				break;
			}
			for (int i = t + 1; i < k; i++)
				com[i] = 0;
		}
	}

	public String prog1() {
		String res = "";
		for (int i = 0; i < this.body.size(); i++) {
			GrammarTreeNode gt = body.get(i);
			if (gt.getNodeType().compareTo("segment") == 0) {
				Segment seg = (Segment) gt;
				String s = seg.verifySpace();
				if (s.indexOf("null") != -1)
					return "null";
				res += s + "+";
			} else if (gt.getNodeType().compareTo("loop") == 0) {
				Loop p = (Loop) gt;
				String x = p.verifySpace();
				if (x.indexOf("null") != -1)
					return "null";
				if (p.looptype == Loop.LOOP_START) {
					res += "loop(value,r\"" + x + "+";
				} else if (p.looptype == Loop.LOOP_END) {
					res += x;
					res += "\")+";
				} else if (p.looptype == Loop.LOOP_MID) {
					res += x + "+";
				} else if (p.looptype == Loop.LOOP_BOTH) {
					res += "loop(value,r\"" + x + "\")+";
				}
			}
		}
		if (res.charAt(res.length() - 1) == '+') {
			res = res.substring(0, res.length() - 1);
		}
		if (res.length() > 0 && !res.contains("null")) {
			return res;
		} else {
			return null;
		}
	}

	public String toProgram() {
		if (supermode == 0) {
			return prog0();
		} else {
			return prog1();
		}
	}

	public String prog0() {
		while (true) {
			if (curState >= indexes.size())
				return "null";
			Vector<Integer> xIntegers = indexes.get(curState);
			String res = "";
			for (int i = 0; i < xIntegers.size(); i++) {
				GrammarTreeNode gt = body.get(i);
				int ind = xIntegers.get(i);
				if (gt.getNodeType().compareTo("segment") == 0) {
					res += ((Segment) gt).getRule(ind) + "+";
				} else if (gt.getNodeType().compareTo("loop") == 0) {
					Loop p = (Loop) gt;
					String x = p.getRule(ind);
					if (p.looptype == Loop.LOOP_START) {
						res += "loop(value,r\"" + x + "+";
					} else if (p.looptype == Loop.LOOP_END) {
						res += x;
						res += "\")+";
					} else if (p.looptype == Loop.LOOP_MID) {
						res += x + "+";
					} else if (p.looptype == Loop.LOOP_BOTH) {
						res += "loop(value,r\"" + x + "\")+";
					}
				}
			}
			if (res.charAt(res.length() - 1) == '+') {
				res = res.substring(0, res.length() - 1);
			}
			if (res.length() > 0 && !res.contains("null")) {
				curState++; // as Template is conbinatioin of different
							// strings
				return res;
			} else {
				this.curState += 1;
			}
		}
	}

	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Vector<GrammarTreeNode> line2 = ((Template) a).body;
		Vector<GrammarTreeNode> line1 = this.body;
		Vector<GrammarTreeNode> nLine = new Vector<GrammarTreeNode>();
		if (line1.size() != line2.size()) {
			return null;
		}
		for (int i = 0; i < line2.size(); i++) {
			GrammarTreeNode gt = line1.get(i).mergewith(line2.get(i));
			if (gt == null) {
				return null;
			}
			nLine.add(gt);
		}
		Template x = new Template(nLine);
		return (GrammarTreeNode) x;
	}

	@Override
	public String getNodeType() {
		// TODO Auto-generated method stub
		return "template";
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getrepString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createTotalOrderVector() {
		// TODO Auto-generated method stub

	}

	@Override
	public void emptyState() {
		for (GrammarTreeNode treeNode : body) {
			treeNode.emptyState();
		}

	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return this.size;
	}

}
