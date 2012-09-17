/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.cleaning;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class Alignment {
	public static int Search(Vector<TNode> xvec,Vector<TNode> tvec,int bpos)
	{
		boolean isFind = false;
		int p1 = -1;
		for(int t = bpos;t<xvec.size()-tvec.size()+1;t++)
		{
			p1 = t;
			for(int x = 0; x<tvec.size();x++)
			{
				int p2 = x;
				if(xvec.get(p1).sameNode(tvec.get(p2)))
				{
					p1++;
				}
				else
				{
					isFind = false;
					break;
				}
				isFind = true;
			}
			if(isFind)
			{
				return t;
			}
		}
		return -1;
	}
	//format for segment #p1,p2
	public static void SumitTraceGen(int curPos,Vector<TNode> org,Vector<TNode> tar,String tmpsegments,HashSet<String> traces,HashMap<String, String> dict)
	{
		String segments = tmpsegments;
		if(curPos >= tar.size())
		{
			traces.add(segments);
			//System.out.println(""+segments);
			return;
		}
		for(int i = curPos; i<tar.size();i++)
		{
			Vector<TNode> vTNodes = UtilTools.subtokenseqs(curPos, i, tar);
			String key = "#"+curPos+","+i;
			if(dict.containsKey(key))
			{
				//segments += dict.get(key);
				SumitTraceGen(i+1,org,tar,segments+dict.get(key),traces,dict);
			}
			else 
			{	
				int pos = Alignment.Search(org, vTNodes, 0);
				int epos = pos+vTNodes.size()-1;
				if(pos != -1)
				{
					String seg = "#"+pos+","+epos;
					dict.put(key, seg);
					//segments += seg;
					SumitTraceGen(i+1,org,tar,segments+seg,traces,dict);
				}
				else {
					if(vTNodes.size() == 1)
					{
						String seg = "#-"+i+",-"+i;
						//segments += seg;
						SumitTraceGen(i+1,org,tar,segments+seg,traces,dict);
					}
				}
			}
		}
	}
	public static Vector<Segment> genSumitSegments(String s,HashMap<String, Segment> segdict)
	{
		Vector<Segment> vs = new Vector<Segment>();
		String[] segments = s.split("#");
		for(String seg:segments)
		{
			if(seg.trim().length()<=0)
			{
				continue;
			}
			if(segdict.containsKey(seg))
			{
				vs.add(segdict.get(seg));
				continue;
			}
			String[]pos = seg.trim().split(",");
			int spos = Integer.parseInt(pos[0]);
			int epos = Integer.parseInt(pos[1]);
			Segment st = new Segment();
			if(spos>=0 && epos >=0)
			{
				st.start = spos;
				st.end =epos;
			}
			else if(spos <0 || epos <0)
			{
				st.start = Segment.CONST;
				st.end = Segment.CONST;
				st.lend = -epos;
				st.lstart = -spos;
			}
			segdict.put(seg, st);
			vs.add(st);
		}
		return vs;
	}
	public static boolean isAdjacent(int a,int b, Vector<Integer> sortedmks)
	{
		if(a==b)
			return true; // <S> <E> token
		int index = sortedmks.indexOf(a); 
		if(a<=b && index<sortedmks.size()-1)
		{
			if(sortedmks.get(index+1) == b || a==b)
				return true;
			else
				return false;			
		}
		else if(a>b &&index>=1)
		{
			return false;
		}
		return false;
	}
	//mks contains the positions need to be sorted
	//q contains the unsorted positions,q contains all orignal vector's positions
	//return all the segments start and end position
	public static Vector<int[]> deterContinus(Vector<Integer> q,Vector<Integer> mks)
	{
		Vector<int[]> segments = new Vector<int[]>();
		Vector<Integer> copymks = (Vector<Integer>)mks.clone();
		Collections.sort(copymks);
		int start = 0;// the start of the segment
		int pre = start;//the previous valid element in current segment
		boolean findBegin = false;
		for(int j = 0; j<q.size();j++)
		{
			//identify the beginning
			if(mks.contains(q.get(j))&&!findBegin)
			{
				findBegin = true;
				start = j;
				pre = start;
				continue;
			}
			//proceed
			if(findBegin&&mks.contains(q.get(j))&&isAdjacent(q.get(pre),q.get(j),copymks))
			{
				pre = j;
				continue;
			}
			else if(findBegin&&mks.contains(q.get(j))&&!isAdjacent(q.get(pre),q.get(j),copymks))// identify the end of a segment
			{
				int a[] = new int[2];
				a[0]= start;
				a[1] = pre;
				start = j;
				pre = j;
				segments.add(a); 
				continue;
			}
		}
		//output the last segment
		int b[] = new int[2];
		b[0] = start;
		b[1] = pre;
		segments.add(b);
		return segments;
	}
	//
	public static int getReverseOrderNum(Vector<Integer> position)
	{
		int cnt = 0;
		for(int i = 0; i<position.size(); i++)
		{
			int a = position.get(i);
			for(int j = 0; j<i; j++)
			{
				int  b = position.get(j);
				if(a<b)
				{
					cnt++;
					continue;
				}
				
			}
		}
		return cnt;
	}
	public static void alignment(Vector<AlignObj> a,Vector<AlignObj> b,boolean[] aind,boolean[] bind,String path,HashSet<String> res)
	{
		boolean isend = true;
		for(int i = 0; i< a.size(); i++)
		{
			for(int j = 0; j<b.size(); j++)
			{
				if(aind[i]&&bind[j]&&a.get(i).tNode.sameNode(b.get(j).tNode))
				{
					isend = false;
					String xString = path;
					String subs = "#"+a.get(i).index+","+b.get(j).index;
					int xind = xString.indexOf("#");
					//to remove rudun like 1,1 0,0 and 0,0 1,1
					while(xind!= -1 && a.get(i).index>Integer.parseInt(xString.substring(xind+1,xind+2)))
					{
						xind = xString.indexOf("#", xind+1);
					}
					if(xind == -1)
					{
						xString +=subs;
					}
					else 
					{
						xString = xString.substring(0, xind)+subs+xString.substring(xind);
					}
					//System.out.println(""+xString);
					//aind[i]=false;
					bind[j]=false;
					alignment(a, b, aind, bind, xString,res);
					//aind[i]=true;
					bind[j]=true;
				}
			}	
		}
		if(isend)
		{
			//handle the rest blank spaces
			if(!res.contains(path))
			{
				res.add(path);
			}
				
		}
	}
	public static Vector<Vector<int[]>> map(Vector<TNode> a,Vector<TNode> b)
	{
		Vector<Vector<int[]>> res = new Vector<Vector<int[]>>();
		HashMap<String, Vector<AlignObj>> dic = new HashMap<String, Vector<AlignObj>>();
		HashMap<String, Vector<AlignObj>> revdic = new HashMap<String, Vector<AlignObj>>();
		String blankmapping = "";
		String unmatched = "";
		boolean[] aind = new boolean[a.size()];
		for(int i=0; i<aind.length;i++)
		{
			aind[i] = true;
		}
		boolean[] bind = new boolean[b.size()];
		for(int i =0; i<bind.length; i++)
		{
			bind[i] = true;
		}
		for(int j=0;j<b.size();j++)
		{
			boolean matched = false;
			for(int i = 0; i<a.size(); i++)
			{	
				TNode mNode = a.get(i);
				TNode nNode = b.get(j);
				if(mNode.sameNode(nNode))
				{
					
					if(a.get(i).text.compareTo(" ")==0)
					{
						String cnxt = "";
						//if left side nodes are same
						if(i-1>=0 && j-1>=0)
						{
							if(!a.get(i-1).sameNode(b.get(j-1)))
							{
								continue;
							}
							else {
								cnxt += a.get(i-1).text;
							}
						}
						if(i+1<a.size() && j+1<b.size())//if right side nodes are same.
						{
							if(!a.get(i+1).sameNode(b.get(j+1)))
							{
								continue;
							}
							else {
								cnxt += a.get(i+1).text;
							}
						}
						//blankmapping += "#"+i+","+j;
						//// handle multiple blank alignment
						String key = cnxt+mNode.text;
						if(dic.containsKey(key))
						{
							AlignObj aObj = new AlignObj(nNode, j);
							Vector<AlignObj> vao = dic.get(key);
							boolean isrun = false;
							for(int k = 0; k<vao.size(); k++)
							{
								if(vao.get(k).index == j)
								{
									isrun = true;
								}
							}
							if(!isrun)
								dic.get(key).add(aObj);
						}
						else {
							Vector<AlignObj> vec = new Vector<AlignObj>();
							AlignObj aObj = new AlignObj(nNode, j);
							vec.add(aObj);
							dic.put(key, vec);
						}
						if(revdic.containsKey(key))
						{
							AlignObj aObj = new AlignObj(mNode, i);
							Vector<AlignObj> vao = revdic.get(key);
							boolean isrun = false;
							for(int k = 0; k<vao.size(); k++)
							{
								if(vao.get(k).index == i)
								{
									isrun = true;
								}
							}
							if(!isrun)
								revdic.get(key).add(aObj);
						}
						else {
							Vector<AlignObj> vec = new Vector<AlignObj>();
							AlignObj aObj = new AlignObj(mNode, i);
							vec.add(aObj);
							revdic.put(key, vec);
						}
						////
						aind[i] = false;
						bind[j] = false;
					}
					else 
					{
						String key = mNode.toString();
						if(dic.containsKey(key))
						{
							AlignObj aObj = new AlignObj(nNode, j);
							Vector<AlignObj> vao = dic.get(key);
							boolean isrun = false;
							for(int k = 0; k<vao.size(); k++)
							{
								if(vao.get(k).index == j)
								{
									isrun = true;
								}
							}
							if(!isrun)
								dic.get(key).add(aObj);
						}
						else {
							Vector<AlignObj> vec = new Vector<AlignObj>();
							AlignObj aObj = new AlignObj(nNode, j);
							vec.add(aObj);
							dic.put(key, vec);
						}
						if(revdic.containsKey(key))
						{
							AlignObj aObj = new AlignObj(mNode, i);
							Vector<AlignObj> vao = revdic.get(key);
							boolean isrun = false;
							for(int k = 0; k<vao.size(); k++)
							{
								if(vao.get(k).index == i)
								{
									isrun = true;
								}
							}
							if(!isrun)
								revdic.get(key).add(aObj);
						}
						else {
							Vector<AlignObj> vec = new Vector<AlignObj>();
							AlignObj aObj = new AlignObj(mNode, i);
							vec.add(aObj);
							revdic.put(key, vec);
						}
					}
					matched = true;
				}
			}
			if(!matched)
			{
				unmatched += "#-1"+","+j;
			}
		}
		//generate non-ambiguious mapping
		Set<String> keys = dic.keySet();
		Iterator<String> it = keys.iterator();
		String mappingprefix = "";
		while(it.hasNext())
		{
			String k = it.next();
			if(dic.get(k).size()==1&&revdic.get(k).size()==1)
			{
				mappingprefix +="#"+revdic.get(k).get(0).index+","+dic.get(k).get(0).index;
			}
		}
		//generate blank space mapping
		for(int i=0;i<a.size();i++)
		{
			//Vector<int[]> tmp = new Vector<int[]>();
			for(int j=0; j<b.size(); j++)
			{
				//check whether two whitespaces are counterpart
				if(a.get(i).sameNode(b.get(j))&&a.get(i).text.compareTo(" ")==0&&aind[i]&&bind[j])
				{
					blankmapping +="#"+i+","+j;
					aind[i] = false;
					bind[j] = false;
				}
			}
		}
		String theprefix = mappingprefix+blankmapping+unmatched;		
		//generate ambiguious mapping
		Vector<String> allpathes=new Vector<String>();
		allpathes.add(theprefix);
		Iterator<String> it1 = keys.iterator();
		while(it1.hasNext())
		{
			String k = it1.next();
			if((dic.get(k).size()>=1&&revdic.get(k).size()>1)||(dic.get(k).size()>1&&revdic.get(k).size()>=1))
			{
				//if(dic.get(k).get(0).tNode.type != TNode.BNKTYP)
				//{
					String path = "";
					Vector<AlignObj> x1= dic.get(k);
					Vector<AlignObj> y1=revdic.get(k);
					HashSet<String> pathes = new HashSet<String>();
					boolean[] xind = new boolean[x1.size()];
					for(int i = 0; i<xind.length;i++)
					{
						xind[i] = true;
					}
					boolean[] yind = new boolean[y1.size()];
					for(int i = 0; i<yind.length;i++)
					{
						yind[i] = true;
					}
					alignment(y1,x1,yind,xind, path, pathes);
								
					int cnt = allpathes.size();
					while(cnt>0)
					{
						String pString = allpathes.elementAt(0);
						Iterator<String> ks = pathes.iterator();	
						while(ks.hasNext())
						{
							allpathes.add(pString+ks.next());
						}
						allpathes.remove(0);
						cnt--;
					}				
				//}
			}		
		}
		Iterator<String> iter = allpathes.iterator();
		HashSet<String> checkor = new HashSet<String>();
		while(iter.hasNext())
		{
			String p = iter.next().trim();
			if(p.length()==0)
				continue;
			Vector<int[]> line = new Vector<int[]>();
			String[] mps = p.trim().split("#");
			//check redundancy 
			HashSet<String> repsHashSet = new HashSet<String>();
			for(String x:mps)
			{
				repsHashSet.add(x);
			}
			if(checkor.contains(repsHashSet.toString()))
			{
				continue;
			}
			checkor.add(repsHashSet.toString());
			for(String str:mps)
			{
				String string = str.trim();
				if(string.length()==0)
					continue;
				String[] t = string.split(",");
				int[] q = {Integer.parseInt(t[0]),Integer.parseInt(t[1])};
				line.add(q);
			}
			res.add(line);
		}
		return res;
	}
	public static HashMap<Integer, Vector<Template>> genSegseqList(Vector<Vector<int[]>> mapping)
	{
		HashMap<Integer, Vector<Template>> dics = new HashMap<Integer, Vector<Template>>();
		for(int i = 0; i<mapping.size();i++)
		{
			int[][] seqVector = mapping.get(i).toArray(new int[mapping.get(i).size()][]);
			Arrays.sort(seqVector,new SortBy2nd());
			Vector<Segment> segSeq = genSegments(seqVector);
			Vector<GrammarTreeNode> vgt = new Vector<GrammarTreeNode>();
			for(Segment s:segSeq)
			{
				vgt.add((GrammarTreeNode)s);
			}
			Template temp = new Template(vgt,0);
			if(dics.containsKey(segSeq.size()))
			{
				dics.get(segSeq.size()).add(temp);
			}
			else 
			{
				Vector<Template> vecSeg = new Vector<Template>();
				vecSeg.add(temp);
				dics.put(segSeq.size(), vecSeg);
			}
		}
		return dics;
	}
	public static Vector<Segment> genSegments(int[][] mapping)
	{
		int start = 0;
		int pre =0;
		int lstart = 0;
	    boolean started = false;
	    Vector<Segment> segSeq = new Vector<Segment>();
		for(int i = 0; i<mapping.length;i++)
		{
			if(!started)
			{
				start = mapping[i][0];
				lstart = i;
				started =true;
				pre=i;
				continue;
			}
			//continuous if the target token sequence could be continued
			if(mapping[i][0] != mapping[pre][0]+1||mapping[i][0]<0||mapping[pre][0] <0)
			{
				//create a segment
				Segment segment = new Segment(start,mapping[i-1][0], lstart,i-1);
				segSeq.add(segment);
				started = true;
				start = mapping[i][0];
				lstart = i;
				pre = i;
				continue;
			}
			pre = i;	
		}
		if(started)
		{
			Segment segment = new Segment(start,mapping[mapping.length-1][0], lstart,mapping.length-1);
			segSeq.add(segment);
		}
		return segSeq;
	}
	public static void main(String[] args)
	{
		String[] example = {"<_START>Bulevar kralja Aleksandra&nbsp;156<_END>","Bulevar kralja Aleksandra*156"};
		Ruler r = new Ruler();
		r.setNewInput(example[0]);
		Vector<TNode> org = r.vec;
		Ruler r1 = new Ruler();
		r1.setNewInput(example[1]);
		Vector<TNode> tar = r1.vec;
		HashSet<String> traces = new HashSet<String>();
		HashMap<String, String> dict = new HashMap<String, String>();
		Alignment.SumitTraceGen(0, org, tar, "", traces, dict);
	}
}
class AlignObj
{
	public TNode tNode;
	public int index;
	public AlignObj(TNode t,int index)
	{
		tNode = t;
		this.index = index;
	}
}
class SortBy2nd implements Comparator{
	public int compare(Object a,Object b)
	{
		 int x = ((int[])a)[1];
		 int y = ((int[])b)[1];
		if(x > y)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
}
