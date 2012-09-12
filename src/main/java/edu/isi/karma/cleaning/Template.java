package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.Vector;

public class Template implements GrammarTreeNode{
	public Vector<GrammarTreeNode> segmentlist = new Vector<GrammarTreeNode>();
	public Vector<Vector<Segment>> examples = new Vector<Vector<Segment>>();
	public Vector<TNode> org;
	public Vector<TNode> tar;
	public boolean isinloop = false;
	public Template(Vector<GrammarTreeNode> list)
	{
		this.segmentlist = list;
	}
	public int size()
	{
		int size = 0;
		for(GrammarTreeNode gt:this.segmentlist)
		{
			if(gt.getNodeType().compareTo("segment")==0)
			{
				size+= 1;
			}
			else if(gt.getNodeType().compareTo("loop")==0)
			{
				size += ((Loop)gt).loopbody.size();
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
	public Vector<Template> produceVariations()
	{
		Vector<Template> vars = new Vector<Template>();
		//detect loop statement.
		HashMap<String, Vector<Integer>> adds = new HashMap<String,Vector<Integer>>();
		for(int i = 0; i<this.segmentlist.size(); i++)
		{
			String s = this.getDescription(this.segmentlist.get(i));
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
			Segment seg = (Segment)segmentlist.get(startpos);
			for(int i=0;i<this.segmentlist.size();i++)
			{
				if(i<startpos)
				{
					nodelist.add(segmentlist.get(i));
				}
				else if(i>= startpos+1 && i<=endpos)
				{
					seg = seg.mergewith((Segment)segmentlist.get(i));
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
					nodelist.add(segmentlist.get(i));
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
					lbody.add(segmentlist.get(i));
				}
				Loop lp = new Loop(lbody);
				for(int k = 1; k<=rep.size()-1; k++)
				{
					Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
					for(int i = rep.get(k); i<span+rep.get(k);i++)
					{
						tmp.add(segmentlist.get(i));
					}
					Loop lpn = new Loop(tmp);
					lp = lp.mergewith(lpn);
					if(lp == null)
						return null;
				}
				for(int i = 0; i<this.segmentlist.size();i++)
				{
					if(i<startpos)
					{
						nodelist.add(segmentlist.get(i));
					}
					if(i==endpos+span-1)
					{
						nodelist.add(lp);
					}
					else if(i>= endpos+span)
					{
						nodelist.add(segmentlist.get(i));
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
					lbody.add(segmentlist.get(i));
				}
				Loop lp = new Loop(lbody);
				for(int k = 1; k<=rep.size()-1; k++)
				{
					Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
					for(int i = rep.get(k)-span+1; i<=rep.get(k);i++)
					{
						tmp.add(segmentlist.get(i));
					}
					Loop lpn = new Loop(tmp);
					lp = lp.mergewith(lpn);
					if(lp == null)
						return null;
				}
				for(int i = 0; i<this.segmentlist.size();i++)
				{
					if(i<startpos-span+1)
					{
						nodelist.add(segmentlist.get(i));
					}
					if(i==endpos)
					{
						nodelist.add(lp);
					}
					else if(i> endpos)
					{
						nodelist.add(segmentlist.get(i));
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
					lbody.add(segmentlist.get(i));
				}
				Loop lp = new Loop(lbody);
				for(int k = 1; k<=rep.size()-1; k++)
				{
					Vector<GrammarTreeNode> tmp = new Vector<GrammarTreeNode>();
					for(int i = rep.get(k); i<span+rep.get(k);i++)
					{
						tmp.add(segmentlist.get(i));
					}
					Loop lpn = new Loop(tmp);
					lp = lp.mergewith(lpn);
					if(lp == null)
						return null;
				}
				for(int i = 0; i<this.segmentlist.size();i++)
				{
					if(i<startpos)
					{
						nodelist.add(segmentlist.get(i));
					}
					if(i==endpos+span-1)
					{
						nodelist.add(lp);
					}
					else if(i>= endpos+span)
					{
						nodelist.add(segmentlist.get(i));
					}
				}
			}
		}
		Template template = new Template(nodelist);
		return template;
	}
	public void initeDescription(Vector<TNode> example,Vector<TNode> target,HashMap<String, Segment> segdict)
	{
		this.org = example;
		this.tar = target;
		for(int i = 0; i<segmentlist.size(); i++)
		{
			Segment seg = (Segment)segmentlist.get(i);
			String keyString = seg.toString();
			keyString = keyString.substring(1,keyString.indexOf("||"));
			keyString += seg.lend+","+seg.lstart;
			if(segdict.containsKey(keyString))
			{
				segmentlist.set(i,segdict.get(keyString));
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
		for(GrammarTreeNode s:this.segmentlist)
		{
			string += s.toString();
		}
		return string;
	}
	/*public Template mergewith2(Template b)
	{
		boolean doable = true;
		Vector<Segment> vsSegments = new Vector<Segment>();
		if(b.segmentlist.size() != this.segmentlist.size())
			return null;
		for(int i = 0; i<this.segmentlist.size(); i++)
		{
			Segment p = this.segmentlist.get(i).mergewith(b.segmentlist.get(i));
			if(p==null)
			{
				return null;
			}
			vsSegments.add(p);
		}
		Template temp = new Template(vsSegments);
		return temp;
	}*/
	public Template mergewith(Template b)
	{
		boolean doable = true;
		Vector<GrammarTreeNode> vsSegments = new Vector<GrammarTreeNode>();
		if(b.segmentlist.size() != this.segmentlist.size())
			return null;
		int a_ptr=0,b_ptr = 0;
		while(a_ptr<this.segmentlist.size()&&b_ptr<b.segmentlist.size())
		{
			GrammarTreeNode p = this.segmentlist.get(a_ptr);
			GrammarTreeNode q = b.segmentlist.get(b_ptr);
			if(p.getNodeType().compareTo("segment")==0&&q.getNodeType().compareTo("segment")==0)
			{
				
				GrammarTreeNode a = p.mergewith(q);
				if(a == null)
					return null;
				vsSegments.add(a);
				a_ptr++;
				b_ptr++;
			}
			else if(p.getNodeType().compareTo("segment")==0&&q.getNodeType().compareTo("loop")==0)
			{
				//create a new loop node
				Loop xLoop = (Loop)q;	
				int span = xLoop.loopbody.size();
				Vector<GrammarTreeNode> vecs = new Vector<GrammarTreeNode>();
				int count = 0;
				while(count<span)
				{
					vecs.add(this.segmentlist.get(count));
					count++;
					a_ptr ++;
				}
				Loop yLoop = new Loop(vecs);
				Loop zLoop = xLoop.mergewith(yLoop);
				if(zLoop == null)
					return null;
				vsSegments.add(zLoop);
				b_ptr++;
			}
			else if(p.getNodeType().compareTo("loop")==0&&q.getNodeType().compareTo("loop")==0)
			{
				GrammarTreeNode a = p.mergewith(q);
				if(a == null)
					return null;
				vsSegments.add(a);
				a_ptr++;
				b_ptr++;
			}
			else if(p.getNodeType().compareTo("loop")==0&&q.getNodeType().compareTo("segment")==0)
			{
				//create a new loop node
				Loop xLoop = (Loop)p;
				int span = xLoop.loopbody.size();
				Vector<GrammarTreeNode> vecs = new Vector<GrammarTreeNode>();
				int count = 0;
				while(count<span)
				{
					vecs.add(b.segmentlist.get(count));
					count++;
					b_ptr++;
				}
				Loop yLoop = new Loop(vecs);
				Loop zLoop = xLoop.mergewith(yLoop);
				if(zLoop == null)
					return null;
				vsSegments.add(zLoop);
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
	public String toProgram()
	{
		String resString = "";
		for(int i = 0; i<this.segmentlist.size(); i++)
		{
			resString += segmentlist.get(i).toProgram()+"+";
			score += segmentlist.get(i).getScore();
		}
		score = score/this.segmentlist.size();
		resString = resString.substring(0,resString.length()-1);
		return resString;
	}
	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getNodeType()
	{
		return "template";
	}
}
