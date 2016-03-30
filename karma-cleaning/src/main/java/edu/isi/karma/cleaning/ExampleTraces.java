package edu.isi.karma.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
/*
 *store the traces for all the examples
 * */
/*
 *store the traces for all the examples
 * */
public class ExampleTraces {
	public HashMap<String, Traces> expTraces = new HashMap<String, Traces>();
	String contextId;
	public ExampleTraces(String contextId)
	{
		this.contextId = contextId;
	}
	public Traces createTrace(String[] example)
	{
		Vector<TNode> orgNodes = new Vector<TNode>();
		Vector<TNode> tarNodes = new Vector<TNode>();
		Ruler ruler = new Ruler();
		ruler.setNewInput("<_START>"+example[0]+"<_END>");
		orgNodes = ruler.vec;
		ruler.setNewInput(example[1]);
		tarNodes = ruler.vec;
		Traces t = new Traces(orgNodes, tarNodes, contextId);
		this.addTrace(example, t);
		return t;
	}
	public void addTrace(String[] example, Traces t)
	{
		String key = String.format("%s|%s", example[0],example[1]);
		expTraces.put(key, t);
	}
	public Traces getTrace(String[] example)
	{
		String key = String.format("%s|%s", example[0],example[1]);
		return expTraces.get(key);
	}
	public ArrayList<ArrayList<Segment>> getCurrentSegments(String[] example)
	{
		ArrayList<ArrayList<Segment>> res = new ArrayList<ArrayList<Segment>>();
		Traces t = this.getTrace(example);
		Collection<Template> x = t.traceline.values();
		for(Template tmp:x)
		{
			ArrayList<Segment> line = new ArrayList<Segment>();
			for(GrammarTreeNode node:tmp.body)
			{
				line.add((Segment)node);
			}
			res.add(line);
		}
		return res;
	}
	public String getSegmentValue(Segment s)
	{
		if(s.isConstSegment())
		{
			return UtilTools.print(s.constNodes);
		}
		else
		{
			return s.tarString;
		}
	}
	public ArrayList<int[]> getSegmentPos(Segment s)
	{
		if(s.isConstSegment())
		{
			return null;
		}
		else
		{
			ArrayList<int[]> poses = new ArrayList<int[]>();
			for(Section sec:s.section)
			{
				int[] x = {sec.pair[0].absPosition.get(0),sec.pair[1].absPosition.get(0)};
				poses.add(x);
			}
			return poses;
		}
	}
}
