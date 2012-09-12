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
	@Override
	public String toProgram() {
		//Template
		Template a = new Template(this.loopbody);
		for(GrammarTreeNode g:a.segmentlist)
		{
			Segment segment = (Segment)g;
			segment.setinLoop(true);
		}
		String prog = a.toProgram();
		score += a.getScore();
		String s = String.format("loop(value,\"%s\")",prog);
		return s;
	}
	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		return this.mergewith((Loop)a);
	}
	public String getNodeType()
	{
		return "loop";
	}
}
