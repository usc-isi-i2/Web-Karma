package edu.isi.karma.cleaning;

import java.util.Vector;

public class Position implements GrammarTreeNode {
	public Vector<TNode> leftContextNodes =new Vector<TNode>();
	public Vector<TNode> rightContextNodes = new Vector<TNode>();
	public Vector<Integer> absPosition = new Vector<Integer>();
	public Vector<Integer> counters = new Vector<Integer>();
	public boolean isinloop = false;
	public Position(Vector<Integer> absPos, Vector<TNode> lcxt,Vector<TNode> rcxt){
		this.absPosition = absPos;
		//to do counter
		this.counters.add(-1);
		this.counters.add(1);
		this.leftContextNodes = lcxt;
		this.rightContextNodes = rcxt;
	}
	public String getString(Vector<TNode> x)
	{
		//add randomness to the representation
		int r = UtilTools.randChoose(7);
		if(x == null)
		{
			score += 2.0;
			return "ANY";
		}
		String s = "";
		for(TNode t:x)
		{
			if(t.text.compareTo("ANYTOK")!=0 && t.text.length()>0&&r<3)
			{
				s += t.text;
				score += 1.0;
				continue;
			}
			if(r==5 || r==6)
			{
				s +="ANY";
				score += 1.0;
				continue;
			}
			
			if(t.type == TNode.NUMTYP)
			{
				s +="NUM";
				score += 2.0;
			}
			else if(t.type == TNode.WORD)
			{
				s +="WORD";
				score += 4.0;
			}
			else if(t.type == TNode.SYBSTYP)
			{
				s +="SYB";
				score += 2.0;
			}
			else if(t.type == TNode.BNKTYP)
			{
				s +="BNK";
				score += 2.0;
			}
			else if(t.type == TNode.UWRDTYP)
			{
				s +="UWRD";
				score += 2.0;
			}
			else if(t.type == TNode.LWRDTYP)
			{
				s +="LWRD";
				score += 2.0;
			}
			else if(t.type == TNode.STARTTYP)
			{
				s +="START";
				score += 1.0;
			}
			else if (t.type == TNode.ENDTYP) {
				s += "END";
				score += 1.0;
			}
			else {
				s += "ANYTYP";
				score += 2.0;
			}
		}
		return s;
	}
	public Vector<TNode> mergeCNXT(Vector<TNode> a, Vector<TNode> b)
	{
		Vector<TNode> xNodes = new Vector<TNode>();
		if(a==null||b==null)
			return null;
		else 
		{
			if(a.size()!=b.size())
				return null;
			else 
			{
				for(int i=0;i<a.size();i++)
				{
					
					TNode t = a.get(i);
					TNode t1 = b.get(i);
					if(t==null || t1==null)
					{
						xNodes.add(null);
						continue;
					}
					if(t.mergableType(t1)==-1)
					{
						return null;
					}
					else 
					{
						int type = t.mergableType(t1);
						if(t.text.compareTo(t1.text)==0)
						{
							TNode tx = new TNode(type,t.text);
							xNodes.add(tx);
						}
						else
						{
							TNode tx = new TNode(type,"ANYTOK");
							xNodes.add(tx);
						}
					}
				}
			}
		}
		return xNodes;
	}
	public Position mergewith(Position b)
	{
		if(this == null||b==null)
			return null;
		Vector<Integer> tmpIntegers = new Vector<Integer>();
		tmpIntegers.addAll(this.absPosition);
		tmpIntegers.retainAll(b.absPosition);
		Vector<Integer> tmpIntegers2 = new Vector<Integer>();
		tmpIntegers2.addAll(this.counters);
		tmpIntegers2.retainAll(b.counters);
		Vector<TNode> tl = b.leftContextNodes;
		Vector<TNode> tr = b.rightContextNodes;
		Vector<TNode> g_lcxtNodes = mergeCNXT(this.leftContextNodes, tl);
		Vector<TNode> g_rcxtNodes = mergeCNXT(this.rightContextNodes, tr);
		//this.leftContextNodes = g_lcxtNodes;
		//this.rightContextNodes = g_rcxtNodes;
		if(tmpIntegers.size() == 0 && g_lcxtNodes == null&&g_rcxtNodes == null)
			return null;
		return new Position(tmpIntegers, g_lcxtNodes, g_rcxtNodes);
	}
	public void setinLoop(boolean res)
	{
		this.isinloop = res;
	}
	// return indexOf(value,left,right) or position 
	private double score = 0.0;
	public double getScore()
	{
		double r =  score;
		this.score = 0.0;
		return r;
	}
	public String toProgram()
	{
		String reString = "";
		if(this.absPosition.size()==0 &&(this.leftContextNodes!=null || this.rightContextNodes!=null))
		{
			String left = this.getString(this.leftContextNodes);
			left=UtilTools.escape(left);
			String right = this.getString(this.rightContextNodes);
			right=UtilTools.escape(right);
			if(!isinloop)
			{
				int t = UtilTools.randChoose(this.counters.size());
				reString = String.format("indexOf(value,\'%s\',\'%s\',%s)", left,right,counters.get(t));
			}
			else {
				reString = String.format("indexOf(value,\'%s\',\'%s\',counter)", left,right);
			}
			return reString;
		}
		if(this.rightContextNodes == null && this.rightContextNodes == null)
		{
			int t = UtilTools.randChoose(this.absPosition.size());
			reString = String.format("%d", this.absPosition.get(t));
			score += 1.0;
			return reString;
		}
		//random choose one represenation from the two
		int opt = UtilTools.randChoose(2);
		if(opt == 0)
		{
			int t = UtilTools.randChoose(this.absPosition.size());
			score += 1.0;
			reString = String.format("%d", this.absPosition.get(t));
		}
		else if(opt == 1)
		{
			String left = this.getString(this.leftContextNodes);
			left=UtilTools.escape(left);
			String right = this.getString(this.rightContextNodes);
			right=UtilTools.escape(right);
			if(!isinloop)
			{
				int t = UtilTools.randChoose(this.counters.size());
				reString = String.format("indexOf(value,\'%s\',\'%s\',%s)", left,right,counters.get(t));
			}
			else {
				reString = String.format("indexOf(value,\'%s\',\'%s\',counter)", left,right);
			}
		}
		else 
		{
			System.out.println("paranormal activity");
		}
		
		return reString;
	}
	public String toString()
	{
		return this.absPosition+"("+this.leftContextNodes+","+this.rightContextNodes+")";
	}
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Position p = (Position)a;
		p = this.mergewith(p);
		return p;
	}
	public String getNodeType()
	{
		return "position";
	}
}
