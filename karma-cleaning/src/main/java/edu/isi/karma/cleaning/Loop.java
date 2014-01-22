package edu.isi.karma.cleaning;

public class Loop implements GrammarTreeNode {
	public Segment loopbody;
	public int looptype;
	public static final int LOOP_START = 0;
	public static final int LOOP_END = 1;
	public static final int LOOP_BOTH =2;
	public static final int LOOP_MID = 3;
	public Loop(Segment loopbody,int looptype)
	{
		if(loopbody.section.size() == 0)
		{
			this.loopbody = new Segment(loopbody.constNodes);
		}
		else
		{
			this.loopbody = new Segment(loopbody.section, true);
		}
		this.looptype = looptype;
		this.loopbody.isinloop = true;
	}
	public String verifySpace()
	{
		return this.loopbody.verifySpace();
	}
	public Loop mergewith(Loop a)
	{
		if(this.looptype == a.looptype)
		{
			Segment segment = this.loopbody.mergewith(a.loopbody);
			if(segment == null)
				return null;
			Loop l = new Loop(segment, looptype);
			return l;
		}
		else
		{
			return null;
		}
	}
	public Loop mergewith(Segment a)
	{
		Segment segment = this.loopbody.mergewith(a);
		if(segment == null)
			return null;
		Loop l = new Loop(segment, looptype);
		return l;
	}
	public String toString()
	{
		return "[loop]"+this.loopbody.toString();
	}
	private double score = 0.0;
	public double getScore()
	{
		double r =  score;
		this.score = 0.0;
		return r;
	}
	public String toProgram() {
		String res = this.loopbody.toProgram();
		if(res.length()==0)
		{
			this.loopbody.emptyState();
		}
		return res;
	}
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		if(a.getNodeType().compareTo("loop")==0)
		{
			return this.mergewith((Loop)a);
		}
		else if(a.getNodeType().compareTo("Segment")==0)
		{
			return this.mergewith((Segment)a);
		}
		else
			return null;
	}
	public String getNodeType()
	{
		return "loop";
	}
	public String getrepString()
	{
		return this.loopbody.getrepString();
	}
	@Override
	public void createTotalOrderVector() {
		// TODO Auto-generated method stub
		
	}
	public String getRule(int index)
	{
		if(index>=loopbody.size() || index <0)
		{
			return "null";
		}
		else
		{
			return loopbody.getRule(index);
		}
	}
	public long size()
	{
		return this.loopbody.size();
	}
	@Override
	public void emptyState() {
		this.loopbody.emptyState();
		
	}
}
