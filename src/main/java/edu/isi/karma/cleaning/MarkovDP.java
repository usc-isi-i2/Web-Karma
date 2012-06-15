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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

public class MarkovDP {
	public Vector<MDPState> states = new Vector<MDPState>();
	public Vector<MDPAction> policies = new Vector<MDPAction>();
	public double cta = 0.9; //discount factor
	public Vector<GrammarParseTree> spaces = new Vector<GrammarParseTree>();
	public Vector<Vector<TNode>> sToks = new Vector<Vector<TNode>>();
	public Vector<Vector<TNode>> eToks = new Vector<Vector<TNode>>();
	private static int rulecnt = 0;
	//private static HashSet<String> tmprules = new HashSet<String>();
	// state and its sequence of values.
	private HashMap<MDPState,MDPState> hisStates = new HashMap<MDPState,MDPState>();
	public void setHistory(HashMap<MDPState,MDPState> hisState)
	{
		this.hisStates = hisState;
	}
	// check whether the state existed before
	// if yes, return the state,else return null
	public MDPState isInhistory(MDPState x)
	{
		if(hisStates.containsKey(x))
		{
			return hisStates.get(x);
		}
		else
		{
			return null;
		}
	}
	//return the average score
	public double getavgScore(MDPState s)
	{
		if(s==null)
		{
			System.out.println("NO such state");
			return Double.MAX_VALUE;
		}
		else
		{
			if(s.visitedTime == 0)
			{
				return costFun(s);
			}
			else
			{
				double total = 0;
				for(int i = 0; i<s.hisScore.size();i++)
				{
					total += s.hisScore.get(i);
				}
				return total*1.0/s.hisScore.size();
			}
		}
	}
	public String getPolicies()
	{
		String res = "";
		for(MDPAction a:policies)
		{
			res += "<RULESEP>"+a.rule;
		}
		return res;
	}
	public boolean isValid()
	{
		Vector<Vector<TNode>> elem = states.get(states.size()-1).obs;
		for(int i = 0; i<elem.size(); i++)
		{
			Vector<TNode> x = elem.get(i);
			Vector<TNode> y = eToks.get(i);
			String s1 = "";
			String s2 = "";
			for(TNode t:x)
			{
				s1 += t.text;
			}
			for(TNode t:y)
			{
				s2 += t.text;
			}
			if(s1.compareTo(s2)!=0)
				return false;
		}
		return true;
	}
	public String getStates()
	{
		String res = "";
		String res1 = "";
		for(MDPState a:states)
		{
			Vector<String> r = a.getObservation();
			res += " , "+r.get(0);
			res1 += " , "+r.get(1);
		}
		return res+"\n"+res1;
	}
	public MarkovDP(Vector<Vector<TNode>> s,Vector<Vector<TNode>> e,Vector<GrammarParseTree> gts)
	{
		sToks = s;
		eToks = e;
		spaces = gts;
	}
	public double costFun(MDPState s)
	{
		double fvalue = 0.0;
		if(s== null || s.obs == null)
		{
			return Double.MAX_VALUE;
		}
		for(int cnt = 0; cnt <s.obs.size(); cnt ++)
		{
			Vector<TNode> a = s.obs.get(cnt);
			Vector<TNode> b = eToks.get(cnt);
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
			fvalue +=  matrix[a.size()][b.size()];
		}
		return fvalue;
	}
	public void run()
	{
		//prepare the start states
		MDPState s = new MDPState(sToks,0);
		//System.out.println("T Rule size: "+tmprules.size());
		for(int i = 0; i<spaces.size();i++)
		{
			int dpth = getDeepth();
			MDPAction p = new MDPAction();
			search(s, p, 0, dpth);
			policies.add(p);
			MDPState newS = s.genNewState(p.rule, i+1);
			if(newS == null)
				break;
			states.add(s);
			s = newS;
		}
		states.add(s);
	}
	public Vector<String> sampleAction(int index)
	{
		Vector<String> result = new Vector<String>();
		Vector<String> tmpresult = new Vector<String>(); // if no consistent intermediate rule, use the rest
		if(index >= spaces.size())
		{
			return null;
		}
		try
		{
			GrammarParseTree gtree = spaces.get(index);
			int i = 0;
			while(i<this.getWidth())
			{
				GrammarTreeNode gn = new GrammarTreeNode("");
				gtree.buildTree("rule", gn);
				gtree.root = gn;
				String r = gtree.toString();
				tmpresult.add(r);
				if(gtree.isRuleConsistent(r))
				{	
					result.add(r);
					//i++;
				}
				i++;
			}
			if(result.size()==0)
				return tmpresult;
			return result;
		}
		catch(Exception ex)
		{
			System.out.println("sampling error:"+ex.toString());
			return null;
		}
		
	}
	//search algorithm: the least value of cost
	public double search(MDPState s,MDPAction p,double value,int depth)
	{	
		if(s== null)
		{
			return Double.MAX_VALUE;
		}
		if(depth==0 )
		{
			return value+Math.pow(cta, getDeepth()-depth)*costFun(s);
		}
		s.visitedTime += 1;
		int actualInd = s.index+1;
		Vector<String> res = this.sampleAction(s.index);
		if(res == null)
		{
			return value+Math.pow(cta, getDeepth()-depth)*costFun(s);
		}
		double tcost = Math.pow(cta, getDeepth()-depth)*costFun(s)+value;
		double minnum = Double.MAX_VALUE;
		String bestRule = "";
		Vector<MDPState> children = new Vector<MDPState>();
		Vector<String> rules = new Vector<String>();
		double totalScore = 0.0;
		for(String crule:res)
		{	
			//crule = "   mov     Symbol    from_beginning  first incld    Symbol   from_end   12   from_beginning   7";
			MDPState newS = s.genNewState(crule, actualInd);
			if(newS == null)
				continue;
			MDPState in = isInhistory(newS);
			if(in != null)
			{
				newS = in;
			}
			totalScore += getavgScore(newS);
			children.add(newS);
			rules.add(crule);
			//tmprules.add(crule); // for collecting some statistics 
		}
		MDPState bstState = null;
		for(int i = 0; i<children.size(); i++)
		{	
			MDPState z = children.get(i);
			double x = 0.0;
			if(totalScore == 0)
			{
				x = 0.0;
			}
			else
			{
				x = getavgScore(z)*1.0/totalScore;
			}
			if(z.visitedTime != 0)
			{
				//System.out.println(">>>"+x+","+getavgScore(z));
				//System.out.println(s.visitedTime+","+z.visitedTime);
				double offset = Math.sqrt((Math.log(s.visitedTime))/5.0*z.visitedTime);
				//System.out.println("offset:"+offset);
				x = x-offset;
				//System.out.println("<<<"+offset);
			}
			if(x<minnum)
			{
				minnum = x;
				bestRule = rules.get(i);
				bstState = z;
			}
		}
		double x = search(bstState,p,tcost,depth-1);
		p.setRule(bestRule);
		s.hisScore.add(x);
		hisStates.put(s, s);
		return x;
	}	
	public int getWidth()
	{
		return 8;
	}
	public int getDeepth()
	{
		return 2;
	}
	//sample the index according to length, the short the better
	//length value = 1/Length
	public static int sampleByScore(Vector<Integer> a,Vector<Integer> b)
	{
		HashMap<Integer,Vector<Integer>> length2Num = new HashMap<Integer,Vector<Integer>>();
		for(int i = 0;i<a.size();i++)
		{
			int leng = a.get(i);
			if(length2Num.containsKey(leng))
				length2Num.get(leng).add(i);
			else
			{	
				Vector<Integer> tmp = new Vector<Integer>();
				tmp.add(i);
				length2Num.put(leng, tmp);
			}
		}
		//sample one length and choose one index from the array
		double total = 0.0;
		Set<Integer> hx = length2Num.keySet();
		double[] x = new double[hx.size()];
		int[] sign = new int[x.length];
		Iterator<Integer> iter = hx.iterator();
		int cnt = 0;
		while(iter.hasNext())
		{
			int leng = iter.next();
			sign[cnt] = leng;
			x[cnt] = 1.0/leng;
			total+=1.0/leng;
			cnt ++;
		}
		for(int i = 0; i< x.length; i++)
		{
			x[i] = (x[i]/total);
		}
		int index = multinominalSampler(x);
		Vector<Integer> vi = length2Num.get(sign[index]);
		Random r = new Random();
		int pos = r.nextInt(vi.size());
		return vi.get(pos);
		/*Vector<Double> cv = new Vector<Double>();
		double div = 0.0;
		for(int i = 0; i<a.size(); i++)
		{
			double c = (b.get(i)*1.0/a.get(i));
			cv.add(c);
			div += c;
		}
		//normalize
		double[] x = new double[cv.size()];
		for(int i = 0; i<cv.size();i++)
		{
			x[i] = cv.get(i)*1.0/div;
		}
		int index = multinominalSampler(x);
		return index;*/
		
	}
	public static int multinominalSampler(double[] probs)
	{
		Random r = new Random();
		double x = r.nextDouble();
		if(x<=probs[0])
		{
			return 0;
		}
		x -= probs[0];
		for(int i = 1;i<probs.length;i++)
		{
			if(x<=probs[i])
			{
				return i;
			}
			x -= probs[i];
		}
		return 0;
	}
	public static void main(String[] args)
	{		
		try
		{
			long start = System.currentTimeMillis();
			String fp = "/Users/bowu/Research/dataclean/data/RuleData/FullChange.csv";
			CSVReader cr = new CSVReader(new FileReader(new File(fp)),'\t');
			String x[];
			Vector<Vector<TNode>> orgs = new Vector<Vector<TNode>>();
			Vector<Vector<TNode>> tars = new Vector<Vector<TNode>>();
			while((x=cr.readNext())!=null)
			{
				Ruler r1 = new Ruler();
				Ruler r2 = new Ruler();
				String s1 = x[0];
				r1.setNewInput(s1);
				String s2 = x[1];
				r2.setNewInput(s2);
				System.out.println(s1+" ===== "+s2);
				Vector<TNode> p = r1.vec;
				orgs.add(p);
				Vector<TNode> q = r2.vec;
				tars.add(q);
			}
			Vector<Vector<GrammarParseTree>> trees = RuleUtil.genGrammarTrees(orgs, tars);
			//Vector<Vector<String>> ress = new Vector<Vector<String>>();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/Users/bowu/mysoft/restransit.txt")));
			Vector<String> s = new Vector<String>();
			HashSet<String> s1 = new HashSet<String>();
			Vector<String> s2 = new Vector<String>();
			int corrNum = 0;
			Vector<Integer> l = new Vector<Integer>();
			Vector<Integer> sr = new Vector<Integer>();
			for(Vector<GrammarParseTree> gt:trees)
			{
				l.add(gt.size());
				sr.add(1);
			}
			for(int c=0; c<200;c++)
			{
				int index = MarkovDP.sampleByScore(l,sr);
				Vector<GrammarParseTree> gt = trees.get(index);
				//System.out.println(""+gt.size());
				try
				{
					HashMap<MDPState,MDPState> his = new HashMap<MDPState,MDPState>();
					int sccnt = 0;
					for(int ct = 0;ct <50;ct++)
					{
						MarkovDP mdp = new MarkovDP(orgs,tars,gt);
						mdp.setHistory(his);
						mdp.run();
						if(mdp.getPolicies().length() != 0)
						{
							s.add(mdp.getStates());
							s2.add(mdp.getPolicies());
						}
						if(mdp.isValid())
						{
							s1.add(mdp.getPolicies());
							corrNum++;
							sccnt ++;
						}
					}
					sr.set(index, sccnt);
				}
				catch(Exception ex)
				{
					continue;
				}
			}
			long end = System.currentTimeMillis();	
			/*ResultViewer rv = new ResultViewer();
			for(int z = 0 ; z<s1.size();z++)
			{
				String[] rules = s1.get(z).split("#");
				//System.out.println(""+s1);
				Vector<String> xr = new Vector<String>();
				for(int t = 0; t< rules.length; t++)
				{
					if(rules[t].length()!=0)
						xr.add(rules[t]);
				}
				Vector<String> tres = RuleUtil.applyRuleF(xr, "/Users/bowu/Research/dataclean/data/RuleData/rawdata/50_satallite_name.txt");
				rv.addColumn(tres);
			}*/
			for(int i = 0; i<s.size();i++)
			{
				bw.write(s.get(i)+"\n");
				bw.write(s2.get(i)+"\n");
				bw.write("======\n");
			}
			//rv.print("/Users/bowu/Research/dataclean/data/RuleData/test.csv");
			System.out.println("Time: "+(end-start)+"   Correct Result: "+corrNum);
			System.out.println("C Rulesize: "+s1.size());
			bw.close();
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
//store the token sequence as the state
class MDPState{
	int index = 0;
	int visitedTime = 0;
	Vector<Vector<TNode>> obs = new Vector<Vector<TNode>>();
	public Vector<Double> hisScore = new Vector<Double>();
	public double totalScore = 0.0;
	public double initConst = 12.0;
	public MDPState()
	{		
	}
	public void addScore(double s)
	{
		this.hisScore.add(s);
		this.totalScore += s;
	}
	public boolean isObsequal(Vector<Vector<TNode>> x)
	{
		if((obs.size() != x.size())||x.size() == 0)
		{
			return false;
		}
		else
		{
			for(int i = 0; i<x.size(); i++)
			{
				if(obs.get(i).size() != x.get(i).size())
				{
					return false;
				}
				for(int j = 0; j < x.get(i).size();j++)
				{
					if(!obs.get(i).get(j).sameNode(x.get(i).get(j)))
					{
						return false;
					}
				}
			}
			return true;
		}

	}
	public boolean equals(Object other) 
	{
		MDPState e = (MDPState)other;
		if(e.index == this.index && isObsequal(e.obs))
		{
			return true;
		}
		return false;
			
	}
	public int hashCode()
	{
		String res = "";
		for(int i = 0; i<obs.size(); i++)
		{
			for(int j = 0; j < obs.get(i).size();j++)
			{
				res += obs.get(i).get(j).text;
			}
		}
		res += index;
		return res.hashCode();
	}
	public MDPState(Vector<Vector<TNode>> toks,int index)
	{
		obs = toks;
		this.index = index;
	}
	public MDPState genNewState(String rule,int index)
	{
		MDPState newState = new MDPState();
		newState.setIndex(index);
		//System.out.println(obs.size()+"  "+this.getObservation());
		for(int i = 0; i<obs.size(); i++)
		{
			Vector<TNode> nt = RuleUtil.applyRule(rule, (Vector<TNode>)this.obs.get(i).clone());
			if(nt == null)
				return null;
			newState.obs.add(nt);
		}
		return newState;
	}
	public void updateToks(int index,Vector<TNode> x)
	{
		this.obs.set(index, x);
	}
	public void setIndex(int index)
	{
		this.index = index;
	}
	public Vector<String> getObservation()
	{
		Vector<String> s = new Vector<String>();
		for(int i=0;i<obs.size();i++)
		{
			String res = "";
			for(int j = 0; j<obs.get(i).size(); j++)
			{
				res += obs.get(i).get(j).text;
			}
			s.add(res);
		}
		return s;
	}
}
//store the rule as the action
class MDPAction{
	public String rule;
	public MDPAction()
	{
	}
	public MDPAction(String rule)
	{
		this.rule = rule;
	}
	public void setRule(String r)
	{
		this.rule = r;
	}
	
}
