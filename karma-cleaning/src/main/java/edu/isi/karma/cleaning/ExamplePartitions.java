package edu.isi.karma.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

public class ExamplePartitions {
	public HashMap<String, Partition> expPartitions = new HashMap<String, Partition>();
	public ExamplePartitions()
	{
		
	}
	public Partition createPartition(String[] example)
	{
		Vector<Vector<TNode>> orgNodes = new Vector<Vector<TNode>>();
		Vector<Vector<TNode>> tarNodes = new Vector<Vector<TNode>>();
		Ruler ruler = new Ruler();
		ruler.setNewInput("<_START>"+example[0]+"<_END>");
		orgNodes.add(ruler.vec);
		ruler.setNewInput(example[1]);
		tarNodes.add(ruler.vec);
		
		Partition t = new Partition(orgNodes, tarNodes);
		this.addPartition(example, t);
		return t;
	}
	public void addPartition(String[] example, Partition p)
	{
		String key = String.format("%s|%s", example[0],example[1]);
		expPartitions.put(key, p);
	}
	public Partition getPartition(String[] example)
	{
		String key = String.format("%s|%s", example[0],example[1]);
		return expPartitions.get(key);
	}
	public Vector<Vector<Segment>> getCurrentSegments(String[] example)
	{
		Vector<Vector<Segment>> res = new Vector<Vector<Segment>>();
		Traces t = this.getPartition(example).trace;
		Collection<Template> x = t.traceline.values();
		for(Template tmp:x)
		{
			Vector<Segment> line = new Vector<Segment>();
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
	public Vector<int[]> getSegmentPos(Segment s)
	{
		if(s.isConstSegment())
		{
			return null;
		}
		else
		{
			Vector<int[]> poses = new Vector<int[]>();
			for(Section sec:s.section)
			{
				int[] x = {sec.pair[0].absPosition.get(0),sec.pair[1].absPosition.get(0)};
				poses.add(x);
			}
			return poses;
		}
	}
}
