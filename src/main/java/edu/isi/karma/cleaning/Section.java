package edu.isi.karma.cleaning;

import java.util.Vector;


public class Section implements GrammarTreeNode {
	public Position[] pair;
	public Vector<int[]> rules = new Vector<int[]>();
	public int curState = 0;
	public boolean isinloop = false;
	public Section(Position[] p,boolean isinloop)
	{
		pair = p;
		this.createTotalOrderVector();
		this.isinloop = isinloop;
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
			return new Section(pa,loop);
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
