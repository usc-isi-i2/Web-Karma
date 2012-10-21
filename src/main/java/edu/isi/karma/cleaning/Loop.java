package edu.isi.karma.cleaning;

import java.util.Vector;


public class Loop implements GrammarTreeNode {
	public Vector<GrammarTreeNode> loopbody = new Vector<GrammarTreeNode>();
	public Loop(Vector<GrammarTreeNode> loopbody)
	{
		this.loopbody = loopbody;
		for(GrammarTreeNode gt:this.loopbody)
		{
			Segment segment = (Segment)gt;
			segment.isinloop = true;
		}
	}
	public Loop mergewith(Loop a)
	{
		Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
		Vector<GrammarTreeNode> b = a.loopbody;
		for(int i = 0; i< b.size(); i++)
		{
			GrammarTreeNode x = b.get(i).mergewith(loopbody.get(i));
			if(x == null)
				return null;
			tmp.add(x);
		}
		Loop p = new Loop(tmp);
		return p;
	}
	public String toString()
	{
		return this.toProgram();
	}
	private double score = 0.0;
	public double getScore()
	{
		double r =  score;
		this.score = 0.0;
		return r;
	}
	public String toProgram() {
		//Template
		String prog = "";
		for(GrammarTreeNode g:this.loopbody)
		{
			Segment segment = (Segment)g;
			prog += segment.toProgram()+"+";
			score += segment.getScore();
			segment.setinLoop(true);
		}
		prog = prog.substring(0,prog.length()-1);
		String s = String.format("loop(value,\"%s\")",prog);
		return s;
	}
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		return this.mergewith((Loop)a);
	}
	public String getNodeType()
	{
		return "loop";
	}
}
