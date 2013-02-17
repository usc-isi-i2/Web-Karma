package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Traces{

	public TraceNode root; // create a dummy node as the root node
	public HashMap<Integer, Vector<TraceNode>> pos2Segs = new HashMap<Integer, Vector<TraceNode>>();
	public Vector<TNode> orgNodes;
	public Vector<TNode> tarNodes;
	public Traces()
	{
	}
	public Traces(Vector<TNode> org,Vector<TNode> tar)
	{
		this.orgNodes = org;
		this.tarNodes = tar;
		//inite root node
		Segment dummy = new Segment();
		dummy.start = dummy.end = 0;
		root = new TraceNode(dummy);
	}
	//initialize the tree to represent the grammar tree
	public void createTraces()
	{
		ArrayList<TraceNode> que = new ArrayList<TraceNode>();
		que.add(root);
		//find all possible segments starting from a position
		while(que.size()>0)
		{
			TraceNode seg = que.remove(0);
			System.out.println(""+seg.toString());
			int curPos = seg.curSegment.end;
			Vector<TraceNode> children;
			if (pos2Segs.containsKey(curPos))
				children = pos2Segs.get(curPos);
			else {
				children = findSegs(curPos);
			}
			seg.children = children;
			que.addAll(children);
		}
	}
	// find all segments starting from pos
	// 
	Vector<TraceNode> findSegs(int pos)
	{	
		Vector<TraceNode> segs = new Vector<TraceNode>();
		if(pos >= tarNodes.size())
			return segs;
		//identify the const string
		Vector<TNode> tmp = new Vector<TNode>();
		tmp.add(tarNodes.get(pos));
		int q = Ruler.Search(orgNodes, tmp, 0);
		if(q == -1)
		{
			int cnt = pos;
			Vector<TNode> tvec = new Vector<TNode>();
			
			while(q == -1)
			{
				tvec.add(tarNodes.get(cnt));
				cnt ++;
				tmp.clear();
				tmp.add(tarNodes.get(cnt));
				q = Ruler.Search(orgNodes, tmp, 0);
			}
			Segment seg = new Segment();
			seg.start = pos;
			seg.end = cnt;
			seg.setCnt(tvec);
			TraceNode traceNode = new TraceNode(seg);
			segs.add(traceNode);
			return segs;
		}
		
		for(int i=pos; i<tarNodes.size(); i++)
		{
			Vector<TNode> tvec = new Vector<TNode>();
			for(int j = pos; j<=i;j++)
			{
				tvec.add(tarNodes.get(j));
			}
			Vector<Integer> mappings = new Vector<Integer>();
			int r = Ruler.Search(orgNodes, tvec, 0);
			while(r != -1)
			{
				mappings.add(r);
				r = Ruler.Search(orgNodes, tvec, r+1);
			}
			if(mappings.size() >1)
			{
				Vector<int[]> corrm = new Vector<int[]>();
				for(int t:mappings)
				{
					int[] m = {t,t+tvec.size()};
					corrm.add(m);
				}
				//create a segment now
				Segment s = new Segment(pos,i+1,corrm);  
				TraceNode traceNode = new TraceNode(s);
				segs.add(traceNode);
				continue;
			}
			else if(mappings.size() == 1)
			{
				Vector<int[]> corrm = new Vector<int[]>();
				// creating based on whether can find segment with one more token
				if(i>=(tarNodes.size() -1))
				{
					int[] m = {mappings.get(0), mappings.get(0)+tvec.size()};
					corrm.add(m);
					Segment s = new Segment(pos,i+1,corrm);
					TraceNode traceNode = new TraceNode(s);
					segs.add(traceNode);
				}
				else 
				{
					tvec.add(tarNodes.get(i+1));
					int p = Ruler.Search(orgNodes, tvec, 0);
					if(p == -1)
					{
						int[] m = {mappings.get(0), mappings.get(0)+tvec.size()};
						corrm.add(m);
						Segment s = new Segment(pos,i+1,corrm);
						TraceNode traceNode = new TraceNode(s);
						segs.add(traceNode);
					}
					else {
						continue;
					}
				}			
			}
			else
			{
				break;
			}
		}
		return segs;
	}
	public static boolean test()
	{
		String[] xStrings = {"<_START>AAB<_END>","BAA"};
		//	String[] yStrings ={"c d e f g","c f g e d"};
		Vector<String[]> examples = new Vector<String[]>();
		examples.add(xStrings);
	//	examples.add(yStrings);
		Vector<Vector<TNode>> org = new Vector<Vector<TNode>>();
		Vector<Vector<TNode>> tar = new Vector<Vector<TNode>>();
		for(int i =0 ; i<examples.size();i++)
		{
			Ruler r = new Ruler();
			r.setNewInput(examples.get(i)[0]);
			org.add(r.vec);
			Ruler r1 = new Ruler();
			r1.setNewInput(examples.get(i)[1]);
			tar.add(r1.vec);
		}
		Traces traces = new Traces(org.get(0), tar.get(0));
		traces.createTraces();
		return true;
	}
	public static void main(String[] args)
	{
		Traces.test();
	}
}
