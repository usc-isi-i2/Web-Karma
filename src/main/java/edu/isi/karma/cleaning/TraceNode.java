package edu.isi.karma.cleaning;

import java.util.Vector;


public class TraceNode {
	public GrammarTreeNode node;
	public Segment curSegment;
	public Loop loop;
	public Vector<TraceNode> children = new Vector<TraceNode>();
	public String repString;
	public TraceNode(GrammarTreeNode node)
	{
		this.node = node;
		if(node.getNodeType().compareTo("loop")==0)
		{
			this.loop = (Loop)node;
		}
		else if(node.getNodeType().compareTo("segment")==0)
		{
			this.curSegment = (Segment)node;
		}
		inite();
	}
	public TraceNode(Segment seg)
	{
		curSegment = seg;
		node = seg;
		inite();
	}
	public TraceNode(Loop p)
	{
		this.loop = p;
		node = p;
		inite();
	}
	public void inite()
	{
		repString = node.getrepString();
	}
	
	public String toString()
	{
		String s = node.toString();
		return s;
	}
}
