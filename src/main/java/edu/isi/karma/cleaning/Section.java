package edu.isi.karma.cleaning;

import java.util.Vector;


public class Section implements GrammarTreeNode {
	public Position[] pair;
	public Vector<int[]> rules = new Vector<int[]>();
	public int rule_cxt_size = Segment.cxtsize_limit;
	public int curState = 0;
	public boolean isinloop = false;
	public Vector<String> orgStrings = new Vector<String>();
	public Vector<String> tarStrings = new Vector<String>();
	public static Interpretor itInterpretor = null;
	public Section(Position[] p,Vector<String> orgStrings,Vector<String> tarStrings,boolean isinloop)
	{
		pair = p;
		this.orgStrings = orgStrings;
		this.tarStrings = tarStrings;
		if(itInterpretor==null)
			itInterpretor =  new Interpretor();
		this.createTotalOrderVector();
		this.isinloop = isinloop;
	}
	public String verifySpace()
	{
		String rule = "null";
		boolean validSpace = true;
		Interpretor itInterpretor = new Interpretor();
		int index = 0;
		while(rules.size() > 0 )
		{
			if(isinloop)
			{
				rule = String.format("loop(value,\"%s\")", this.getRule(0));
			}
			else
			{
				rule = this.getRule(0);
			}
			boolean isvalid = true;
			for(int i=0;i <orgStrings.size(); i++)
			{
				InterpreterType worker = itInterpretor.create(rule);
				String s2 = worker.execute(orgStrings.get(i));
				String s3 = tarStrings.get(i);
				if (s3.compareTo(s2) != 0) 
				{
					isvalid = false;
				}
			}
			if(!isvalid)
			{
				rules.remove(0);
			}
			else {
				return this.getRule(0);
			}
			index ++;
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
		Section sec = (Section)a;
		Position x = this.pair[0].mergewith(sec.pair[0]);
		Position y = this.pair[1].mergewith(sec.pair[1]);
		if(x== null || y == null)
			return null;
		else
		{
			Position[] pa = {x,y};
			boolean loop = this.isinloop || sec.isinloop;
			Vector<String> strs = new Vector<String>();
			Vector<String> tars = new Vector<String>();
			if(this.orgStrings.size() == sec.orgStrings.size() && this.orgStrings.size() == 1 && this.orgStrings.get(0).compareTo(sec.orgStrings.get(0))==0)
			{
				// merge within one example. test the loop expression
				//
				strs.addAll(this.orgStrings);
				tars.add(this.tarStrings.get(0)+sec.tarStrings.get(0));
				loop = true;
			}
			else 
			{
				strs.addAll(this.orgStrings);
				strs.addAll(sec.orgStrings);
				tars.addAll(this.tarStrings);
				tars.addAll(sec.tarStrings);
			}
			
			
			Section st = new Section(pa,strs,tars,loop);
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
		return this.pair[0].getScore()+this.pair[1].getScore();
	}

	@Override
	public String getrepString() {
		// TODO Auto-generated method stub
		return pair[0].getrepString()+pair[1].getrepString();
	}

	@Override
	public void createTotalOrderVector() {
		for(int i = 0; i< pair[0].rules.size(); i++)
		{
			for(int j = 0; j< pair[1].rules.size(); j++)
			{
				int[] elem = {i,j};
				rules.add(elem);
			}
		}
	}

	@Override
	public void emptyState() {
		this.curState = 0;
	}
	public String getRule(long index)
	{
		if(index > this.rules.size())
			return "null";
		String rule = "";
		int[] loc = this.rules.get(((int)index));
		pair[0].isinloop = this.isinloop;
		pair[1].isinloop = this.isinloop;
		rule = String.format("substr(value,%s,%s)",pair[0].getRule(loc[0]),pair[1].getRule(loc[1]));
		return rule;
	}
	@Override
	public long size() {
		return pair[0].rules.size()*pair[1].rules.size();
	}
	public String toString()
	{
		String lp = "";
		String rp = "";
		if(pair[0]!=null)
		{
			lp = pair[0].toString();
		}
		if(pair[1]!=null)
		{
			rp = pair[1].toString();
		}
		return lp+rp;
	}

}
