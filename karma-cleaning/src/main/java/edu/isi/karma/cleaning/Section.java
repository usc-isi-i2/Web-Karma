package edu.isi.karma.cleaning;

import java.util.Vector;

import edu.isi.karma.cleaning.internalfunlibrary.InternalTransformationLibrary;

public class Section implements GrammarTreeNode {
	public Position[] pair;
	public int convert = InternalTransformationLibrary.Functions.NonExist.getValue();
	public static int time_limit = 10;
	public Vector<int[]> rules = new Vector<int[]>();
	public int rule_cxt_size = Segment.cxtsize_limit;
	public int curState = 0;
	public boolean isinloop = false;
	public Vector<String> orgStrings = new Vector<String>();
	public Vector<String> tarStrings = new Vector<String>();
	public static Interpretor itInterpretor = null;
	public static final int supermode = 1;
	public String program = "null";

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
	public boolean isValid(String rule)
	{
		boolean res = true;
		ProgramRule convRule = new ProgramRule(rule);
		for(int i = 0; i < orgStrings.size(); i++)
		{
			if(!(convRule.transform(orgStrings.get(i)).compareTo(tarStrings.get(i))==0))
			{
				res = false;
				return res;
			}
		}
		return res;
	}
	public String verifySpace() {
		String rule = "";
		this.pair[0].isinloop = this.isinloop;
		this.pair[1].isinloop = this.isinloop;
		while (curState < this.rules.size()) {
			String rule1 = this.pair[0].VerifySpace(rules.get(curState)[0]);
			String rule2 = this.pair[1].VerifySpace(rules.get(curState)[1]);
			curState++;
			if (rule1.indexOf("null") == -1 && rule2.indexOf("null") == -1) {
				//rule = String.format("substr(value,%s,%s)", rule1, rule2);
				rule = this.generateRule(rule1, rule2);
				if(isValid(rule))
				{
					this.program = rule;
					return rule;
				}
			}
			if (rule1.indexOf("null") != -1 && rule2.indexOf("null") != -1) {
				break;
			}
		}
		this.program = "null";
		return "null";
	}
	public String generateRule(String lpos, String rpos){
		String def = String.format("substr(value,%s,%s)", lpos, rpos);
		if(convert == InternalTransformationLibrary.Functions.Cap.getValue()){
			String func = InternalTransformationLibrary.getName(convert);
			def = String.format("%s(%s)", func, def);
		}
		else if(convert == InternalTransformationLibrary.Functions.Firstletter.getValue()){
			String func = InternalTransformationLibrary.getName(convert);
			def = String.format("%s(%s)", func, def);
		}
		else if(convert == InternalTransformationLibrary.Functions.Lowercase.getValue()){
			String func = InternalTransformationLibrary.getName(convert);
			def = String.format("%s(%s)", func, def);
		}
		else if(convert == InternalTransformationLibrary.Functions.Uppercase.getValue()){
			String func = InternalTransformationLibrary.getName(convert);
			def = String.format("%s(%s)", func, def);
		}
		return def;
	}
	public String toProgram() {
		// TODO Auto-generated method stub
		return null;
	}

	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Section sec = (Section) a;
		if(this.convert!= sec.convert){
			return null;
		}
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
			st.convert = this.convert;
			return st;

		}
	}

	public String getNodeType() {
		return "section";
	}

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
		if(convert != -1)
		{
			return lp+", "+rp+(this.convert);
		}
		return lp + rp;
	}

	@Override
	public String getProgram() {
		return this.program;
	}

}
