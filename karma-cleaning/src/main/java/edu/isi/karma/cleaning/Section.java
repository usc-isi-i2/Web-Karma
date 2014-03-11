package edu.isi.karma.cleaning;

import java.util.Vector;

public class Section implements GrammarTreeNode {
	public Position[] pair;
	public static int time_limit = 10;
	public Vector<int[]> rules = new Vector<int[]>();
	public int rule_cxt_size = Segment.cxtsize_limit;
	public int curState = 0;
	public boolean isinloop = false;
	public Vector<String> orgStrings = new Vector<String>();
	public Vector<String> tarStrings = new Vector<String>();
	public static Interpretor itInterpretor = null;
	public static final int supermode = 1;

	public Section(Position[] p, Vector<String> orgStrings,
			Vector<String> tarStrings, boolean isinloop) {
		pair = p;
		this.orgStrings = orgStrings;
		this.tarStrings = tarStrings;
		if (itInterpretor == null)
			itInterpretor = new Interpretor();
		/*
		 * if(supermode == 0) this.createTotalOrderVector();
		 */
		this.reiniteRules();
		this.isinloop = isinloop;
	}

	public String verifySpace() {
		String rule = "";
		this.pair[0].isinloop = this.isinloop;
		this.pair[1].isinloop = this.isinloop;
		long sec_time_limit = System.currentTimeMillis();
		while (curState < this.rules.size()) {
			if ((System.currentTimeMillis() - sec_time_limit) / 1000 > time_limit * 1.0 / 5) {
				return "null";
			}
			String rule1 = this.pair[0].VerifySpace(rules.get(curState)[0]);
			String rule2 = this.pair[1].VerifySpace(rules.get(curState)[1]);
			curState++;
			if (rule1.indexOf("null") == -1 && rule2.indexOf("null") == -1) {
				rule = String.format("substr(value,%s,%s)", rule1, rule2);
				return rule;
			}
			if (rule1.indexOf("null") != -1 && rule2.indexOf("null") != -1) {
				break;
			}
		}
		return "null";

	}

	@Override
	public String toProgram() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Section sec = (Section) a;
		Position x = this.pair[0].mergewith(sec.pair[0]);
		Position y = this.pair[1].mergewith(sec.pair[1]);
		if (x == null || y == null)
			return null;
		else {
			Position[] pa = { x, y };
			boolean loop = this.isinloop || sec.isinloop;
			Vector<String> strs = new Vector<String>();
			Vector<String> tars = new Vector<String>();
			if (this.orgStrings.size() == sec.orgStrings.size()
					&& this.orgStrings.size() == 1
					&& this.orgStrings.get(0).compareTo(sec.orgStrings.get(0)) == 0) {
				// merge within one example. test the loop expression
				//
				strs.addAll(this.orgStrings);
				tars.add(this.tarStrings.get(0) + sec.tarStrings.get(0));
				loop = true;
			} else {
				strs.addAll(this.orgStrings);
				strs.addAll(sec.orgStrings);
				tars.addAll(this.tarStrings);
				tars.addAll(sec.tarStrings);
			}

			Section st = new Section(pa, strs, tars, loop);
			return st;

		}
	}

	@Override
	public String getNodeType() {
		return "section";
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return this.pair[0].getScore() + this.pair[1].getScore();
	}

	@Override
	public String getrepString() {
		// TODO Auto-generated method stub
		return pair[0].getrepString() + pair[1].getrepString();
	}

	@Override
	public void createTotalOrderVector() {
		for (int i = 0; i < pair[0].rules.size(); i++) {
			for (int j = 0; j < pair[1].rules.size(); j++) {
				int[] elem = { i, j };
				rules.add(elem);
			}
		}
	}

	public void reiniteRules() {
		Vector<Long> indexs = new Vector<Long>();
		indexs.add((long) 16);
		indexs.add((long) 16);
		Vector<Vector<Integer>> configs = new Vector<Vector<Integer>>();
		rules.clear();
		getCrossIndex(indexs, 0, "", configs);
		for (int i = 0; i < configs.size(); i++) {
			int[] elem = { configs.get(i).get(0), configs.get(i).get(1) };
			rules.add(elem);
		}
	}

	public void getCrossIndex(Vector<Long> indexs, int cur, String path,
			Vector<Vector<Integer>> configs) {
		String tpath = path;
		if (cur >= indexs.size()) {
			String[] elems = tpath.split(",");
			Vector<Integer> line = new Vector<Integer>();
			for (String s : elems) {
				String x = s.trim();
				if (x.length() > 0) {
					line.add(Integer.parseInt(x));
				}
			}
			if (line.size() > 0) {
				configs.add(line);
			}
			return;
		}
		for (int i = 0; i < indexs.get(cur); i++) {
			String xtpath = path + i + ",";
			getCrossIndex(indexs, cur + 1, xtpath, configs);
		}
	}

	@Override
	public void emptyState() {
		this.curState = 0;
	}

	public String getRule(long index) {
		if (index > this.rules.size())
			return "null";
		String rule = "";
		int[] loc = this.rules.get(((int) index));
		pair[0].isinloop = this.isinloop;
		pair[1].isinloop = this.isinloop;
		rule = String.format("substr(value,%s,%s)", pair[0].getRule(loc[0]),
				pair[1].getRule(loc[1]));
		return rule;
	}

	@Override
	public long size() {
		return pair[0].rules.size() * pair[1].rules.size();
	}

	public String toString() {
		String lp = "";
		String rp = "";
		if (pair[0] != null) {
			lp = pair[0].toString();
		}
		if (pair[1] != null) {
			rp = pair[1].toString();
		}
		return lp + rp;
	}

}
