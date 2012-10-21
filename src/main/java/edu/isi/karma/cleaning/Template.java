package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.Vector;

import org.python.antlr.PythonParser.else_clause_return;
import org.python.apache.xerces.xni.grammars.Grammar;

public class Template implements GrammarTreeNode{
	public Vector<HashMap<String,GrammarTreeNode>> segmentlist = new Vector<HashMap<String,GrammarTreeNode>>();
	public Vector<Vector<Segment>> examples = new Vector<Vector<Segment>>();
	public Vector<TNode> org;
	public Vector<TNode> tar;
	public boolean isinloop = false;
	public Template(Vector<GrammarTreeNode> vgt,int t)
	{
		Vector<HashMap<String,GrammarTreeNode>> list = new Vector<HashMap<String,GrammarTreeNode>>();
		for(int i=0;i<vgt.size();i++)
		{
			HashMap<String, GrammarTreeNode> x = new HashMap<String, GrammarTreeNode>();
			x.put(vgt.get(i).toString(), vgt.get(i));
			list.add(x);
		}
		this.segmentlist = list;
	}
	public Template(Vector<HashMap<String,GrammarTreeNode>> list)
	{
		this.segmentlist = list;
	}
	public int size()
	{
		int size = 0;
		for(HashMap<String,GrammarTreeNode> gt:this.segmentlist)
		{
			GrammarTreeNode node = gt.get(gt.keySet().iterator().next());
			if(node.getNodeType().compareTo("segment")==0)
			{
				size+= 1;
			}
			else if(node.getNodeType().compareTo("loop")==0)
			{
				size += ((Loop)node).loopbody.size();
			}
		}
		return size;
	}
	public String getDescription(GrammarTreeNode e)
	{
		String reString = "";
		if(e.getNodeType().compareTo("segment")==0)
		{
			Segment s = (Segment)e;
			for(int i = s.lstart; i<=s.lend; i++)
			{
				reString += this.tar.get(i).type;
			}
		}
		else if(e.getNodeType().compareTo("loop")==0)
		{
			reString = "";//this result should appear here
		}
		return reString;
	}
	//only applicable for initialization.
	public Vector<Template> produceVariations()
	{
		Vector<Template> vars = new Vector<Template>();
		//detect loop statement.
		HashMap<String, Vector<Integer>> adds = new HashMap<String,Vector<Integer>>();
		for(int i = 0; i<this.segmentlist.size(); i++)
		{
			String s = this.getDescription(this.segmentlist.get(i).get(this.segmentlist.get(i).keySet().iterator().next()));
			if(adds.containsKey(s))
			{
				adds.get(s).add(i);
			}
			else
			{
				Vector<Integer> vi = new Vector<Integer>();
				vi.add(i);
				adds.put(s, vi);
			}
		}
		for(String key:adds.keySet())
		{
			Vector<Integer> vec = adds.get(key);
			if(!UtilTools.samesteplength(vec))
				continue;
			//generate loop body
			Template pTemplate = genLoopTemplate(vec);
			if(pTemplate!=null)
				vars.add(pTemplate);
		}
		return vars;
	}
	public Template genLoopTemplate(Vector<Integer> rep)
	{
		int span = rep.get(1)-rep.get(0);
		Vector<GrammarTreeNode> nodelist = new Vector<GrammarTreeNode>();
		if(span == 1)
		{
			int startpos = rep.get(0);
			int endpos = rep.get(rep.size()-1);
			Segment seg = (Segment)segmentlist.get(startpos).get(segmentlist.get(startpos).keySet().iterator().next());
			for(int i=0;i<this.segmentlist.size();i++)
			{
				Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
				if(i<startpos)
				{
					nodelist.add(segb);
				}
				else if(i>= startpos+1 && i<=endpos)
				{
					seg = seg.mergewith(segb);
					if(seg == null)
						return null;
					if(i==endpos)
					{
						Vector<GrammarTreeNode> vgt = new Vector<GrammarTreeNode>();		
						vgt.add(seg);
						Loop pLoop = new Loop(vgt);
						nodelist.add(pLoop);
					}
				}
				else if(i>endpos)
				{
					nodelist.add(segb);
				}
			}
		}
		else 
		{
			int startpos = rep.get(0);
			int endpos = rep.get(rep.size()-1);
			//left overflow
			if((startpos - span+1)<0 && endpos+span<this.segmentlist.size())
			{
				//create first loop body
				Vector<GrammarTreeNode> lbody = new Vector<GrammarTreeNode>();
				for(int i = startpos; i<startpos+span; i++)
				{
					Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
					lbody.add(segb);
				}
				Loop lp = new Loop(lbody);
				for(int k = 1; k<=rep.size()-1; k++)
				{
					Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
					for(int i = rep.get(k); i<span+rep.get(k);i++)
					{
						Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
						tmp.add(segb);
					}
					Loop lpn = new Loop(tmp);
					lp = lp.mergewith(lpn);
					if(lp == null)
						return null;
				}
				for(int i = 0; i<this.segmentlist.size();i++)
				{
					Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
					if(i<startpos)
					{
						nodelist.add(segb);
					}
					if(i==endpos+span-1)
					{
						nodelist.add(lp);
					}
					else if(i>= endpos+span)
					{
						nodelist.add(segb);
					}
				}
			}
			//right overflow
			else if((startpos - span+1)>=0 && endpos+span>=this.segmentlist.size())
			{
				//create first loop body
				Vector<GrammarTreeNode> lbody = new Vector<GrammarTreeNode>();
				for(int i = startpos-span+1; i<=startpos; i++)
				{
					Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
					lbody.add(segb);
				}
				Loop lp = new Loop(lbody);
				for(int k = 1; k<=rep.size()-1; k++)
				{
					Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
					for(int i = rep.get(k)-span+1; i<=rep.get(k);i++)
					{
						Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
						tmp.add(segb);
					}
					Loop lpn = new Loop(tmp);
					lp = lp.mergewith(lpn);
					if(lp == null)
						return null;
				}
				for(int i = 0; i<this.segmentlist.size();i++)
				{
					Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
					if(i<startpos-span+1)
					{
						nodelist.add(segb);
					}
					if(i==endpos)
					{
						nodelist.add(lp);
					}
					else if(i> endpos)
					{
						nodelist.add(segb);
					}
				}
			}
			//two direction overflow
			else if((startpos - span+1)<0 && endpos+span>=this.segmentlist.size())
			{
				return null;
			}
			else
			{
				//create first loop body
				Vector<GrammarTreeNode> lbody = new Vector<GrammarTreeNode>();
				for(int i = startpos; i<startpos+span; i++)
				{
					Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
					lbody.add(segb);
				}
				Loop lp = new Loop(lbody);
				for(int k = 1; k<=rep.size()-1; k++)
				{
					Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
					for(int i = rep.get(k); i<span+rep.get(k);i++)
					{
						Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
						tmp.add(segb);
					}
					Loop lpn = new Loop(tmp);
					lp = lp.mergewith(lpn);
					if(lp == null)
						return null;
				}
				for(int i = 0; i<this.segmentlist.size();i++)
				{
					Segment segb =  (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
					if(i<startpos)
					{
						nodelist.add(segb);
					}
					if(i==endpos+span-1)
					{
						nodelist.add(lp);
					}
					else if(i>= endpos+span)
					{
						nodelist.add(segb);
					}
				}
			}
		}
		Vector<HashMap<String, GrammarTreeNode>> xHashMaps = new Vector<HashMap<String,GrammarTreeNode>>();
		for(GrammarTreeNode node:nodelist)
		{
			HashMap<String, GrammarTreeNode> xq = new  HashMap<String, GrammarTreeNode>();
			xq.put(node.toString(), node);
			xHashMaps.add(xq);
		}
		Template template = new Template(xHashMaps);
		return template;
	}
	public void initeDescription(Vector<TNode> example,Vector<TNode> target,HashMap<String, Segment> segdict)
	{
		this.org = example;
		this.tar = target;
		for(int i = 0; i<segmentlist.size(); i++)
		{
			Segment seg = (Segment)segmentlist.get(i).get(segmentlist.get(i).keySet().iterator().next());
			String keyString = seg.toString();
			keyString = keyString.substring(1,keyString.indexOf("||"));
			keyString += seg.lend+","+seg.lstart;
			if(segdict.containsKey(keyString))
			{
				segmentlist.get(i).put(seg.toString(),segdict.get(keyString));
				continue;
			}
			if(seg.start<0 && seg.end <0)
			{
				int s = seg.lstart;
				int e = seg.lend;
				for(int j = s; j<=e;j++)
				{
					seg.constNodes.add(target.get(j));
				}
				segdict.put(keyString, seg);
				continue;
			}
			TNode lm;
			TNode rm;
			if(seg.start==0)
			{
				lm = null;
			}
			else 
			{
				lm = example.get(seg.start-1);
			}
			if(seg.end >= example.size()-1)
			{
				rm = null;
			}
			else {
				rm = example.get(seg.end+1);
			}
			seg.setPosition(Segment.LEFTPOS, lm,example.get(seg.start), this.getStringPos(seg.start, example, Segment.LEFTPOS));
			seg.setPosition(Segment.RIGHTPOS, example.get(seg.end), rm, this.getStringPos(seg.end, example, Segment.RIGHTPOS));
			segdict.put(keyString, seg);
		}
	}
	public Vector<Integer> getStringPos(int tokenpos,Vector<TNode> example,String option)
	{
		Vector<Integer> poss = new Vector<Integer>();
		if(tokenpos <= 0)
			return poss;
		int pos = 0;
		int strleng = 0;
		for(int i=0;i<example.size();i++)
		{
			strleng += example.get(i).text.length();
		}
		for(int i = 0; i<tokenpos;i++)
		{
			pos += example.get(i).text.length();
		}
		if(option.compareTo(Segment.LEFTPOS)==0)
		{
			poss.add(pos);
			poss.add(pos-strleng);
			return poss;
		}
		else if(option.compareTo(Segment.RIGHTPOS)==0)
		{
			int x = pos+example.get(tokenpos).text.length();
			int y = x-strleng;
			poss.add(x);
			if(y!=0)
				poss.add(y);
			return poss;
		}
		else
		{
			return poss;
		}
	}
	public String toString()
	{
		String string = "";
		for(HashMap<String, GrammarTreeNode> s:this.segmentlist)
		{
			string += s.get(s.keySet().iterator().next());
		}
		return string;
	}
	public HashMap<String, GrammarTreeNode> SameNodesetMerge(HashMap<String, GrammarTreeNode> asegs,HashMap<String, GrammarTreeNode> bsegs)
	{
		HashMap<String,GrammarTreeNode> hsg = new HashMap<String, GrammarTreeNode>();
		for(String x:asegs.keySet())
		{
			for(String y:bsegs.keySet())
			{
				GrammarTreeNode p = asegs.get(x);
				GrammarTreeNode q = bsegs.get(y);
				GrammarTreeNode a = p.mergewith(q);
				if(a == null)
					return null;
				hsg.put(a.toString(), a);
			}
		}
		return hsg;
	}
	public Vector<Vector<GrammarTreeNode>> genloops(Vector<HashMap<String, GrammarTreeNode>> segs,Vector<Vector<GrammarTreeNode>> vecs,int i)
	{
		if(i>= segs.size())
		{
			return vecs;
		}
		if(vecs.size() ==0)
		{
			for(String key:segs.get(i).keySet())
			{
				Vector<GrammarTreeNode> vgt = new Vector<GrammarTreeNode>();
				vgt.add(segs.get(i).get(key));
				vecs.add(vgt);
			}
			return genloops(segs,vecs,++i);
		}
		else {
			Vector<Vector<GrammarTreeNode>> nvecs = new Vector<Vector<GrammarTreeNode>>();
			for(int j = 0; j<vecs.size(); j++)
			{
				for(String key:segs.get(i).keySet())
				{
					Vector<GrammarTreeNode> vgt = new Vector<GrammarTreeNode>();
					vgt.addAll(vecs.get(j));
					vgt.add(segs.get(i).get(key));
					nvecs.add(vgt);
				}
			}
			return genloops(segs,nvecs,++i);
		}
		
	}
	public HashMap<String, GrammarTreeNode> SegmentLoopMerge(HashMap<String, GrammarTreeNode> loops,Vector<HashMap<String, GrammarTreeNode>> segs)
	{
		HashMap<String,GrammarTreeNode> hsg = new HashMap<String, GrammarTreeNode>();
		Vector<Vector<GrammarTreeNode>> vecs = new Vector<Vector<GrammarTreeNode>>();
		vecs = genloops(segs,vecs,0);
		for(Vector<GrammarTreeNode> vgts:vecs)
		{
			Loop lp = new Loop(vgts);
			for(GrammarTreeNode l:loops.values())
			{
				Loop q = (Loop)l;
				Loop zLoop = q.mergewith(lp);
				if(zLoop != null)
					hsg.put(zLoop.toString(), zLoop);
			}
		}
		return hsg;
	}
	
	public Template mergewith(Template b)
	{
		Vector<HashMap<String,GrammarTreeNode>> vsSegments = new Vector<HashMap<String,GrammarTreeNode>>();
		if(b.size() != this.size())
			return null;
		int a_ptr=0,b_ptr = 0;
		while(a_ptr<this.segmentlist.size()&&b_ptr<b.segmentlist.size())
		{
			HashMap<String,GrammarTreeNode> n = new HashMap<String, GrammarTreeNode>();
			HashMap<String,GrammarTreeNode> gt1 = this.segmentlist.get(a_ptr);
			HashMap<String,GrammarTreeNode> gt2 = b.segmentlist.get(b_ptr);
		
			GrammarTreeNode p = gt1.get(gt1.keySet().iterator().next());
			GrammarTreeNode q = gt2.get(gt2.keySet().iterator().next());
			if(p.getNodeType().compareTo("segment")==0&&q.getNodeType().compareTo("segment")==0)
			{
				
				HashMap<String, GrammarTreeNode> xHashMap = SameNodesetMerge(gt1, gt2);
				if(xHashMap!=null)
				{
					vsSegments.add(xHashMap);
				}
				else {
					return null;
				}
				a_ptr++;
				b_ptr++;
			}
			else if(p.getNodeType().compareTo("segment")==0&&q.getNodeType().compareTo("loop")==0)
			{
				//create a new loop node
				Loop xLoop = (Loop)q;	
				int span = xLoop.loopbody.size();
				Vector<HashMap<String,GrammarTreeNode>> vecs = new Vector<HashMap<String,GrammarTreeNode>>();
				int count = 0;
				while(count<span)
				{
					vecs.add(this.segmentlist.get(count));
					count++;
					a_ptr ++;
				}
				HashMap<String,GrammarTreeNode> hsg = SegmentLoopMerge(gt2, vecs);
				if(hsg!=null)
				{
					vsSegments.add(hsg);
				}
				else
				{
					return null;
				}
				b_ptr++;
			}
			else if(p.getNodeType().compareTo("loop")==0&&q.getNodeType().compareTo("loop")==0)
			{
				HashMap<String, GrammarTreeNode> xHashMap = SameNodesetMerge(gt1, gt2);
				if(xHashMap!=null)
				{
					vsSegments.add(xHashMap);
				}
				else {
					return null;
				}
				a_ptr++;
				b_ptr++;
			}
			else if(p.getNodeType().compareTo("loop")==0&&q.getNodeType().compareTo("segment")==0)
			{
				//create a new loop node
				Loop xLoop = (Loop)p;	
				int span = xLoop.loopbody.size();
				Vector<HashMap<String,GrammarTreeNode>> vecs = new Vector<HashMap<String,GrammarTreeNode>>();
				int count = 0;
				while(count<span)
				{
					vecs.add(this.segmentlist.get(b_ptr));
					count++;
					b_ptr ++;
				}
				HashMap<String,GrammarTreeNode> hsg = SegmentLoopMerge(gt1, vecs);
				if(hsg!=null)
				{
					vsSegments.add(hsg);
				}
				else
				{
					return null;
				}
				a_ptr++;
			}
		}
		Template temp = new Template(vsSegments);
		return temp;
	}
	public void setinLoop(boolean res)
	{
		this.isinloop = res;
	}
	private double score = 0.0;
	public double getScore()
	{
		double r =  score;
		this.score = 0.0;
		return r;
	}
	public Template TempUnion(Template b)
	{
		Vector<HashMap<String, GrammarTreeNode>> res = new Vector<HashMap<String,GrammarTreeNode>>();
		for(int i = 0; i<this.segmentlist.size(); i++)
		{
			HashMap<String, GrammarTreeNode> x = this.segmentlist.get(i);
			HashMap<String, GrammarTreeNode> y = b.segmentlist.get(i);
			x.putAll(y);
			res.add(x);
		}
		Template template = new Template(res);
		return template;
	}
	public String toProgram()
	{
		String resString = "";
		for(int i = 0; i<this.segmentlist.size(); i++)
		{
			HashMap<String, GrammarTreeNode> hsg = segmentlist.get(i);
			int c = UtilTools.randChoose(hsg.keySet().size());
			String keyString = "";
			for(int j = 0; j<=c; j++)
			{
				keyString = hsg.keySet().iterator().next();
			}
			GrammarTreeNode gt = hsg.get(keyString);
			resString += gt.toProgram()+"+";
			score += gt.getScore();
		}
		score = score/this.segmentlist.size();
		resString = resString.substring(0,resString.length()-1);
		return resString;
	}
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getNodeType()
	{
		return "template";
	}
}
