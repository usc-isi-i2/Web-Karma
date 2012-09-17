package edu.isi.karma.cleaning;

import java.util.Vector;


public class Segment implements GrammarTreeNode{
	public Position sPosition;
	public Position ePosition;
	public static final String LEFTPOS = "leftpos";
	public static final String RIGHTPOS = "rightpos";
	public static final int CONST = -1;
	public static final int UNDFN = -2;
	public int start = 0;
	public int end = 0;
	public int lstart = 0;
	public int lend = 0;
	public boolean isinloop = false;
	public Vector<TNode> constNodes = new Vector<TNode>();
	
	public Segment()
	{
		
	}
	public void setinLoop(boolean res)
	{
		this.isinloop = res;
		if(this.sPosition!=null &&this.ePosition!=null)
		{
			this.sPosition.setinLoop(res);
			this.ePosition.setinLoop(res);
		}
	}
	public void setPosition(String option,TNode t1,TNode t2,Vector<Integer> abspos)
	{
		if(option.compareTo(Segment.LEFTPOS)==0)
		{
			Vector<TNode> lNodes = new Vector<TNode>();
			Vector<TNode> rNodes = new Vector<TNode>();
			lNodes.add(t1);
			rNodes.add(t2);
			sPosition = new Position(abspos, lNodes, rNodes);
			sPosition.isinloop = this.isinloop;
		}
		else 
		{
			Vector<TNode> lNodes = new Vector<TNode>();
			Vector<TNode> rNodes = new Vector<TNode>();
			lNodes.add(t1);
			rNodes.add(t2);
			ePosition = new Position(abspos, lNodes, rNodes);
			ePosition.isinloop = isinloop;
		}
	}
	public void setCnt(Vector<TNode> cnst)
	{
		for(TNode t:cnst)
		{
			constNodes.add(t);
		}
	}
	public Segment(int start,int end,int lstart,int lend)
	{
		this.start = start;
		this.end = end;
		this.lstart = lstart;
		this.lend = lend;
	}
	
	public Segment mergewith(Segment s)
	{
		if(this.start == Segment.CONST &&this.end == Segment.CONST)
		{
			if(s.start <0 &&s.end<0)
			{
				if(this.constNodes.size() != s.constNodes.size())
				{
					return null;
				}
				else 
				{
					for(int i = 0; i< this.constNodes.size();i++)
					{
						if(!this.constNodes.get(i).sameNode(s.constNodes.get(i)))
						{
							return null;
						}
					}
				}
			}
			else {
				return null;
			}
			Segment res = new Segment();
			res.start = Segment.CONST;
			res.end = Segment.CONST;
			res.constNodes = this.constNodes;
			return res;
		}
		//merge the position
		Position p1 = sPosition.mergewith(s.sPosition);
		Position p2 = ePosition.mergewith(s.ePosition);
		if(p1==null)
			return null;
		if(p2==null)
			return null;
		Segment res = new Segment();
		res.sPosition = p1;
		res.ePosition = p2;
		return res;
	}
	public String toString()
	{
		if(this.end == Segment.CONST && this.start == Segment.CONST)
		{
			return "("+start+","+end+"|" + "|"+this.constNodes+")";
		}
		else {
			return "("+start+","+end+"|" + "|"+this.sPosition+","+this.ePosition+")";
		}
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
		try 
		{	
			String reString = "";
			String mdString = "";
			if(this.start == Segment.CONST &&this.end == Segment.CONST)
			{	
				for(TNode t:this.constNodes)
				{
					mdString += t.text;
				}
				//mdString= UtilTools.escape(mdString);
				reString = "\'"+mdString+"\'";
				score += 1.0;
			}
			else
			{
				reString = String.format("substr(value,%s,%s)", this.sPosition.toProgram(),this.ePosition.toProgram());
				score += sPosition.getScore();
				score += ePosition.getScore();
			}
			return reString;
		} catch (Exception e) {
			System.out.println(""+e.toString());
			return null;
		}
	}
	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Segment s = (Segment)a;
		s = this.mergewith(s);
		return s;
	}
	public String getNodeType()
	{
		return "segment";
	}
}
