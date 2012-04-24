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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Alignment {
	//follow the framework of INS MOV DEL
	//generat all possible edit operations
	//a target token sequence b orginal token sequence
	public static Vector<Vector<EditOper>> genEditOperation(Vector<TNode> a, Vector<TNode> b)
	{
		Alignment.paths.clear();
		//generate INS operation (only need to record the content)
		Vector<EditOper> inss = Alignment.insopers(a,b);
		//apply the learned insert operation
		Vector<TNode> clonea = (Vector<TNode>)a.clone();
		for(EditOper eo:inss)
		{
			NonterminalValidator.applyins(eo, clonea);
			eo.before = a;
			eo.after = clonea;
		}
		//generate MOV and DEL operation
		Alignment.movopers(clonea,b);
		Vector<Vector<EditOper>> movdels = Alignment.paths;
		//generate all combination of the two set of operation sequence
		Vector<Vector<EditOper>> res = new Vector<Vector<EditOper>>();
		for(Vector<EditOper> v:movdels)
		{
			Vector<EditOper> z = new Vector<EditOper>();
			z.addAll(inss);
			z.addAll(v);
			res.add(z);
			System.out.println(""+z.toString());
		}
		return res;
	}
	public static Vector<Integer> delopers(Vector<int[]> a,Vector<TNode> before)
	{
		Vector<Integer> v = new Vector<Integer>();
		for(int i = 0; i< before.size(); i++)
		{
			boolean isFind = false;
			for(int j = 0 ; j < a.size();j++)
			{
				if(i == a.get(j)[0])
				{
					isFind = true;
					break;
				}
			}
			if(!isFind)
			{
				v.add(i);
			}
		}
		return v;
	}
	public static boolean isAdjacent(int a,int b, Vector<Integer> sortedmks)
	{
		int index = sortedmks.indexOf(a); 
		if(a<=b && index<sortedmks.size()-1)
		{
			if(sortedmks.get(index+1) == b)
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
	public static void movopers(Vector<TNode> a, Vector<TNode> b)
	{
		Vector<Vector<int[]>> mapping = map(a,b);
		for(int i= 0; i< mapping.size();i++)
		{
			Vector<EditOper> ev = new Vector<EditOper>();
			Vector<int[]> tmapping = mapping.get(i);
			//detect deleted part
			//Vector<Integer> dels= delopers(tmapping,b);
			Vector<Integer> positions =  new Vector<Integer>();
			//record the positions showed be moved
			//record the original order
			Vector<Integer> mks = new Vector<Integer>(b.size());
			for(int  n = 0;n<b.size();n++)
			{
				mks.add(-1);
			}
			for(int n=0; n< a.size();n++) //
			{
				positions.add(n);
				//check if n is in the mapping array
				boolean isfind = false;
				for(int m = 0; m<tmapping.size();m++)
				{
					if(tmapping.get(m)[0]==n)
					{
						mks.set(tmapping.get(m)[1], n);
						break;
					}
				}
			}
			//update the positions to be order of target sequence.
			for(int q = 0; q<mks.size();q++)
			{
				int cnt = -1;
				int value = 0;
				for(int p = 0;p<positions.size();p++)
				{
					for(int k = 0;k<mks.size();k++)
					{
						if(mks.get(k)==p)
						{
							cnt ++;
							break;
						}
					}
					if(cnt == q)
					{
						value = p;
						break;
					}
				}
				for(int p = 0;p<positions.size();p++)
				{
					if(p==mks.get(q))
					{
						positions.set(p, value);
					}
				}				
			}
			//detect the continous segment
			Vector<int[]> segments =  deterContinus(positions,mks);
			Vector<EditOper> xx = new Vector<EditOper>();
			Vector<Vector<Integer>> history = new Vector<Vector<Integer>>();
			transMOVDEL(segments,positions,mks,xx,history,a);
		}		
	}
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
					break;
				}
				
			}
		}
		return cnt;
	}
	public static Vector<Vector<EditOper>> paths = new Vector<Vector<EditOper>>();
	/*
	 * segments: all the continous part
	 * position: contains the unsorted positions
	 * x == mks: all the position need to be moved
	 * eo: for record the path for one move and delete sequence
	 * history is used to store all previous state to prevent visit some visited state dead loop
	 */
	public static void transMOVDEL(Vector<int[]> segments,Vector<Integer> positon,Vector<Integer> x,Vector<EditOper> eo,Vector<Vector<Integer>> history,Vector<TNode> a)
	{
		if(eo.size()>positon.size()-1)//prune, the number of mov should be less than total size -1 
			return;
		if(history.size()>0)
		{
			for(Vector<Integer> l:history)
			{
				if(positon.equals(l))
				{
					return;// visited before
				}
			}
		}
		Vector<Vector<Integer>> history1 = (Vector<Vector<Integer>>)history.clone(); 
		history1.add(positon);
		boolean globalcontrary = false;
		boolean localcontrary = false;
		for(int i = 0; i<segments.size(); i++)
		{
			localcontrary = false;
			int minNum = Integer.MAX_VALUE;
			EditOper eox = new EditOper();
			Vector<Integer> positonx = new Vector<Integer>();
			//move to the back
			for(int k=i+1;k<segments.size();k++)
			{
				if(positon.get(segments.get(i)[0])>positon.get(segments.get(k)[0]))
				{
					localcontrary = true;
					globalcontrary = true;
					//swamp the pair two directions
					EditOper eo2 = new EditOper();
					eo2.starPos = segments.get(i)[0];
					eo2.endPos = segments.get(i)[1];
					eo2.oper = "mov";
					eo2.dest = segments.get(k)[1]+1;//after the first segment
					Vector<Integer> positon2 = (Vector<Integer>)positon.clone();
					//update the position2
					List<Integer> tmp2 = positon.subList(eo2.starPos, eo2.endPos+1);
					positon2.removeAll(tmp2);
					//insert first then delete
					positon2.addAll(eo2.dest-tmp2.size(), tmp2);
					int score = Alignment.getReverseOrderNum(positon2);
					if(score <minNum)
					{
						minNum = score;
						eox = eo2;
						positonx = positon2;
					}
				}
			}	
			//move to the front
			for(int k=0;k<i;k++)
			{
				if(positon.get(segments.get(i)[0])<positon.get(segments.get(k)[0]))
				{
					localcontrary = true;
					globalcontrary = true;
					EditOper eo1 = new EditOper();
					eo1.starPos = segments.get(i)[0];
					eo1.endPos = segments.get(i)[1];
					eo1.oper = "mov";
					eo1.dest = segments.get(k)[0];// before the first segment
					//Vector<EditOper> seq1 = (Vector<EditOper>)eo.clone();
					Vector<Integer> positon1 = (Vector<Integer>)positon.clone();
					//seq1.add(eo1);
					List<Integer> tmp1 = positon.subList(eo1.starPos, eo1.endPos+1);
					//update positon1 array
					positon1.removeAll(tmp1);
					positon1.addAll(eo1.dest, tmp1);
					int score = Alignment.getReverseOrderNum(positon1);
					if(score <minNum)
					{
						minNum = score;
						eox = eo1;
						positonx = positon1;
					}
				}
			}
			if(localcontrary)
			{
				Vector<EditOper> seqx = (Vector<EditOper>)eo.clone();
				seqx.add(eox);			
				Vector<int[]> newsegments1 = deterContinus(positonx,x);
				transMOVDEL(newsegments1,positonx,x,seqx,history1,a);
			}
		}
		if(!globalcontrary)
		{
			//add the delete operation
			int start = 0;
			int cnt = 0;//offset the influence of deleted element
			boolean started = false;
			for(int h = 0; h<positon.size();h++)
			{
				//start of segment
				if(!started&&!x.contains(positon.get(h))&&(a.get(positon.get(h)).type != TNode.STARTTYP)&&(a.get(positon.get(h)).type != TNode.ENDTYP))
				{
					start = h;
					started = true;
					continue;
				}
				else if(started&&x.contains(positon.get(h)))//reach the end
				{
					EditOper deo = new EditOper();
					deo.starPos = start-cnt;
					deo.endPos = h-1-cnt;
					deo.oper = "del";
					eo.add(deo);
					started = false;
					cnt += h-1-start+1;
					continue;
				}
			}
			//output the last segment
			if(started)
			{
				EditOper deo = new EditOper();
				deo.oper = "del";
				deo.starPos = start-cnt;
				deo.endPos = positon.size()-1-cnt;
				eo.add(deo);
				started = false;
			}
			paths.add(eo);
			return; // ordered and do need to sort now
		}
		return;
	}
	public static Vector<EditOper> insopers(Vector<TNode> a, Vector<TNode> b)
	{
		Vector<EditOper> eo = new Vector<EditOper>();
		boolean[] marks = new boolean[a.size()];
		for(int i= 0;i<marks.length;i++)
		{
			marks[i] = false;
		}
		for(int i=0;i<b.size();i++)
		{
			boolean isFind = false;
			for(int j = 0; j<a.size();j++)
			{
				if(b.get(i).sameNode(a.get(j))&&!marks[j])
				{
					marks[j]= true;
					isFind= true;
					break;
				}
			}
			if(!isFind)
			{
				//create a new insertion operation
				EditOper eox= new EditOper();
				eox.oper = "ins";
				eox.dest = 0;
				eox.tar.add(b.get(i));
				eo.add(eox);
			}
		}
		return eo;
	}
	//merge continouse operation together
	public static Vector<Vector<int[]>> mergeOperation(Vector<Vector<int[]>> res)
	{
		return null;
	}
	public static Vector<Vector<int[]>> map(Vector<TNode> a,Vector<TNode> b)
	{
		Vector<Vector<int[]>> all = new Vector<Vector<int[]>>();
		boolean[] pack1 = new boolean[a.size()];
		for(int i=0;i<pack1.length;i++)
			pack1[i] = false;
		boolean[] pack2 = new boolean[b.size()];
		for(int i=0;i<pack2.length;i++)
			pack2[i] = false;
		for(int i=0;i<a.size();i++)
		{
			Vector<int[]> tmp = new Vector<int[]>();
			for(int j=0; j<b.size(); j++)
			{
				if(a.get(i).sameNode(b.get(j)))
				{
					//check whether two whitespaces are counterpart
					if(a.get(i).text.compareTo(" ")==0)
					{
						if(i-1>=0 && j-1>=0)
						{
							if(!a.get(i-1).sameNode(b.get(j-1)))
							{
								continue;
							}
						}
						if(i+1<a.size() && j+1<b.size())
						{
							if(!a.get(i+1).sameNode(b.get(j+1)))
							{
								continue;
							}
						}
					}
					int[] x = {i,j};
					pack1[i] = true;
					pack2[j] = true;
					tmp.add(x);
				}
			}	
			if(tmp.size()!=0)
				all.add(tmp);
		}
		/*map the unmapped same node*/
		for(int i=0;i<a.size();i++)
		{
			Vector<int[]> tmp = new Vector<int[]>();
			for(int j=0; j<b.size(); j++)
			{
				//check whether two whitespaces are counterpart
				if(a.get(i).text.compareTo(" ")==0&&!pack1[i]&&!pack2[j])
				{
					int[] x = {i,j};
					pack1[i] = true;
					pack2[j] = true;
					tmp.add(x);
				}
			}
			if(tmp.size()!=0)
				all.add(tmp);
		}
		
		// do cross join 
		Vector<Vector<int[]>> res = new Vector<Vector<int[]>>();
		for(int i = 0; i<all.size(); i++)
		{
			Vector<Vector<int[]>> res1 = new Vector<Vector<int[]>>();
			for(int j=0;j<all.get(i).size();j++)
			{
				Iterator<Vector<int[]>> iter = res.iterator();
				if(res.size()==0)
				{
					Vector<int[]> tmp = new Vector<int[]>();
					tmp.add(all.get(i).get(j));
					res1.add(tmp);
					continue;
				}
				while(iter.hasNext())
				{
					Vector<int[]> x = iter.next();
					// there might be redundant destination positions
					boolean isRedun = false;
					int index = 0;
					for(int[] ptr:x)
					{
						if(ptr[1]==all.get(i).get(j)[1])
						{
							isRedun = true;
							break;
						}
					}
					if(isRedun)
					{
						//Vector<int[]> tmp = (Vector<int[]>)x.clone();
						//res1.add(tmp);
						continue;
					}
					Vector<int[]> tmp = (Vector<int[]>)x.clone();
					tmp.add(all.get(i).get(j));
					res1.add(tmp);
				}
			}
			res = res1;
		}
		//number of symbols in original token seq > number of same symbols in target token seq
		//handle the missing mapping token
		return res;
	}	
	/****************************************************************/
	
	// this method should be able to detect the operation range of three
	// kinds of operations "delete, insert, Move" '
	// this method could also be wrong, so the best would be this alignment 
	//just give weak supervision, the later component would adjust it self to learn the right rule
	public static Vector<EditOper> alignment1(Vector<TNode> a, Vector<TNode> b)
	{
		HashMap<Integer,Integer> res = new HashMap<Integer,Integer>();
		int matrix[][] = new int[a.size()+1][b.size()+1];// the first row and column is kept for empty
		// initialize the first row and column
		int stepCost = 1;
		for(int i=0;i<a.size()+1;i++)
		{
			for(int j=0;j<b.size()+1;j++)
			{
				if(i==0)
				{
					matrix[i][j] = j;
					continue;
				}
				if(j==0)
				{
					matrix[i][j] = i;
					continue;
				}
				int cost =stepCost;
				if(a.get(i-1).sameNode(b.get(j-1)))
				{
					cost = 0;
					matrix[i][j] = Math.min(matrix[i-1][j-1]+cost, Math.min(matrix[i-1][j]+1, matrix[i][j-1]+1));
				}
				else // No substitution
				{
					matrix[i][j] =  Math.min(matrix[i-1][j]+1, matrix[i][j-1]+1);
				}
			}
		}
		//find the edit operations and use move to replace the del and ins as many as possible
		int ptr1 = a.size();
		int ptr2 = b.size();
		//use the alignment to derive the edit operations
		Vector<EditOper> editv = new Vector<EditOper>();
		HashMap<String,Vector<Integer>> dtmp = new HashMap<String,Vector<Integer>>();
		HashMap<String,Vector<Integer>> itmp = new HashMap<String,Vector<Integer>>();
		// 0th column and 0th row always represents the epsilon
		while(!(ptr1==0&&ptr2==0))
		{
			int value = matrix[ptr1][ptr2];
			//search the three directions  
			if(ptr1-1>=0 && ptr2-1>=0 && matrix[ptr1-1][ptr2-1] == value && a.get(ptr1-1).sameNode(b.get(ptr2-1)))// matched
			{
				ptr1 -= 1;
				ptr2 -= 1;
			}
			else if(ptr1-1>=0 && matrix[ptr1-1][ptr2] == value-1) //del the node
			{
				
				EditOper eo = new EditOper();
				eo.oper = "del";
				eo.starPos = ptr1-1;
				eo.endPos = ptr1-1;
				editv.add(eo);
				if(dtmp.containsKey(a.get(ptr1-1).text))
				{
					dtmp.get(a.get(ptr1-1).text).add(editv.size()-1);
				}
				else
				{
					Vector<Integer> ax = new Vector<Integer>();
					ax.add(editv.size()-1); // add the current index into the sequence
					dtmp.put(a.get(ptr1-1).text, ax);
				}
				ptr1 -= 1;
			}
			else if(ptr2-1>=0 && matrix[ptr1][ptr2-1] == value-1) //ins a node
			{
				
				EditOper eo = new EditOper();
				eo.oper = "ins";
				Vector<TNode> t = new Vector<TNode>();
				t.add( b.get(ptr2-1));
				eo.tar = t;
				eo.dest = ptr1;
				editv.add(eo);
				if(itmp.containsKey(b.get(ptr2-1).text))
				{
					itmp.get(b.get(ptr2-1).text).add(editv.size()-1);
				}
				else
				{
					Vector<Integer> ax = new Vector<Integer>();
					ax.add(editv.size()-1); // add the current index into the sequence
					itmp.put(b.get(ptr2-1).text, ax);
				}
				ptr2 -= 1;
			}
		}
		//replace the del ins of same symbol with mov operation
		Vector<Object> rv = new Vector<Object>();
		
		for(String x:dtmp.keySet())
		{
			if(itmp.containsKey(x))
			{
				//p and q are the index of the edit operation sequene the bigger, the latter
				Vector<Integer> q = dtmp.get(x);
				Vector<Integer> p = itmp.get(x);
				Iterator<Integer> t1 = q.iterator();//delete
				Iterator<Integer> t2 = p.iterator();//insert
				while(t1.hasNext()&&t2.hasNext())
				{
					Integer i1 = t1.next();
					Integer i2 = t2.next();
					if(i1>i2)
					{
						//update the two operation with mov operation
						editv.get(i2).oper = "mov";
						int delcnt = 0;
						for(int m=0;m<editv.size();m++)
						{
							if((editv.get(m).oper.compareTo("ins")==0||editv.get(m).oper.compareTo("mov")==0)&& m< i2)
								delcnt ++;
						}
						editv.get(i2).dest = editv.get(i2).dest-delcnt;
						editv.get(i2).starPos = editv.get(i1).starPos;
						editv.get(i2).endPos = editv.get(i1).endPos;
						//editv.remove(y);
						rv.add(editv.get(i1));
						t1.remove();
						t2.remove();
					}
					else
					{
						editv.get(i2).oper = "mov";
						editv.get(i2).starPos = editv.get(i1).starPos;
						editv.get(i2).endPos = editv.get(i1).endPos;
						//editv.remove(z);
						rv.add(editv.get(i1));
						t1.remove();
						t2.remove();
					}
				}
			}
		}
		for(Object o:rv)
		{
			editv.remove(o);
		}
		//merge the continuous same type of operations
		Vector<EditOper> newEo = new Vector<EditOper>();
		int pre = 0;
		for(int ptr = 1; ptr <editv.size();ptr++)
		{
			EditOper eo = editv.get(ptr);
			if(eo.oper.compareTo(editv.get(pre).oper)==0)
			{
				if(eo.oper.compareTo("mov")==0)
				{
					if(((eo.starPos-1==editv.get(ptr-1).starPos&&eo.starPos>=eo.dest)||(eo.starPos+1==editv.get(ptr-1).starPos&&eo.starPos<=eo.dest)) && eo.dest == editv.get(ptr-1).dest)
					{
						if(ptr<editv.size()-1)
							continue;
						else
						{
							ptr = ptr+1;
						}
					}
					if(ptr-pre>1) // need to merge
					{
						EditOper e = editv.get(ptr-1);
						Vector<TNode> x = new Vector<TNode>();
						e.starPos = editv.get(pre).starPos<=e.starPos?editv.get(pre).starPos:e.starPos;
						e.endPos = editv.get(pre).endPos>=e.endPos?editv.get(pre).endPos:e.endPos;
						for(int k = pre;k<ptr;k++)
						{
							x.addAll(editv.get(k).tar); // concate the tar tokens
						}
						e.tar = x;
						newEo.add(e);
						continue;
					}
					newEo.add(editv.get(pre));
					pre = ptr;	
					continue;
				}
				else if(eo.oper.compareTo("ins")==0)
				{
					if(eo.dest == editv.get(ptr-1).dest)
					{
						if(ptr<editv.size()-1)
							continue;
						else
						{
							ptr = ptr+1;
						}
					}
					if(ptr-pre>1) // need to merge
					{
						EditOper e = editv.get(ptr-1);
						Vector<TNode> x = new Vector<TNode>();
						e.endPos = editv.get(pre).endPos;
						for(int k = pre;k<ptr;k++)
						{
							x.addAll(editv.get(k).tar); // concate the tar tokens
						}
						e.tar = x;
						newEo.add(e);
						continue;
					}
					newEo.add(editv.get(pre));
					pre = ptr;
					continue;
				}
				else if(eo.oper.compareTo("del")==0)
				{
					if(eo.starPos+1==editv.get(ptr-1).starPos)
					{
						if(ptr<editv.size()-1)
							continue;
						else
						{
							ptr = ptr+1;
						}
					}				
					if(ptr-pre>1) // need to merge
					{
						EditOper e = editv.get(ptr-1);
						Vector<TNode> x = new Vector<TNode>();
						e.endPos = editv.get(pre).starPos;
						for(int k = pre;k<ptr;k++)
						{
							x.addAll(editv.get(k).tar); // concate the tar tokens
						}
						e.tar = x;
						newEo.add(e);
						continue;
					}
					newEo.add(editv.get(pre));
					pre = ptr;
					continue;
				}
			}
			newEo.add(editv.get(pre));
			pre = ptr;
		}
		if(pre== editv.size()-1)
			newEo.add(editv.get(pre));
		return newEo;
	}
	//used to generate template candidate tree for sampling
	public static void generateSubtemplate(HashMap<String,Vector<GrammarTreeNode>> container)
	{
		
	}
	public static Vector<int[]> getParams(Vector<TNode> org,Vector<TNode> tar)
	{
		//specially for del,  
		int optr = 0;
		int tptr= 0;
		boolean start = false;
		int a[] = new int[2];
		Vector<int[]> poss = new Vector<int[]>();
		while(optr<org.size() && tptr<tar.size())
		{
			if(org.get(optr).sameNode(tar.get(tptr)))
			{
				optr ++ ;
				tptr ++ ;
				if(start)
				{
					a[1] = optr-1;
					start = false;
					poss.add(a);
				}
			}
			else
			{
				if(!start)
				{
					a = new int[2];
					a[0]=optr;
					start = true;
				}
				optr ++ ;
			}
		}
		if(tptr == tar.size() && optr<org.size())
		{
			int p[] = {tptr,org.size()-1};
			poss.add(p);
		}
		return poss;
	}
	public static Vector<int[]> getParams(String sorg,String star)
	{
		Ruler ru = new Ruler();
		ru.setNewInput(sorg);
		Ruler ru1 = new Ruler();
		ru1.setNewInput(star);
		Vector<TNode> org = ru.vec;
		Vector<TNode> tar = ru1.vec;
		//specially for del,  
		int optr = 0;
		int tptr= 0;
		boolean start = false;
		int a[] = new int[2];
		Vector<int[]> poss = new Vector<int[]>();
		while(optr<org.size() && tptr<tar.size())
		{
			if(org.get(optr).sameNode(tar.get(tptr)))
			{	
				if(start)
				{
					a[1] = optr-1;
					start = false;
					poss.add(a);
				}
				optr ++ ;
				tptr ++ ;
			}
			else
			{
				if(!start)
				{
					a = new int[2];
					a[0]=optr;
					start = true;
				}
				optr ++ ;
			}
		}
		if(tptr == tar.size() && optr<org.size())
		{
			int p[] = {tptr,org.size()-1};
			poss.add(p);
		}
		return poss;
	}
}
//a Class aims to store the parameter for edit operations
class EditOper{
	public String oper="";
	public int starPos=-1;
	public int endPos=-1;
	public int dest = -1;
	public Vector<TNode> tar = new Vector<TNode>();
	public Vector<TNode> before = new Vector<TNode>();
	public Vector<TNode> after = new Vector<TNode>();
	public EditOper()
	{
		
	}
	public String toString()
	{
		return oper+": "+starPos+","+endPos+","+dest+tar.toString();
	}
}
class Comparator1 implements Comparator{
	public int compare(Object ob1,Object ob2)
	{
		EditOper x = (EditOper)ob1;
		EditOper y = (EditOper)ob2;
		if(x.dest > y.dest)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
}
