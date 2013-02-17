package edu.isi.karma.cleaning;

import java.util.Vector;

public class TraceNode {
	public Segment curSegment;
	public Vector<TraceNode> children;
	public String repreString;
	public TraceNode(Segment seg)
	{
		curSegment = seg;
	}
	public String initrepreString()
	{
		return "";
	}
	public String toString()
	{
		String s = curSegment.toString();
		return s;
	}
	
}
