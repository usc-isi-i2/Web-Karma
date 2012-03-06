package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterLexer;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterParser;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterTree;

// generator all possible rules from the grammar
public class RuleGenerator {

	private String origin;
	private String target;
	public HashMap<String,ArrayList<ArrayList<String>>> nonterminals;
	public HashMap<String,String> consterminals; 
	public HashMap<String,Integer> curRuleConfig; // integer is the index of rule
	public RuleGenerator()
	{
		nonterminals = new HashMap<String,ArrayList<ArrayList<String>>>();
		initconsterminals();
		curRuleConfig = new HashMap<String,Integer>();
	}
	public boolean checkOnExamples(ArrayList<String[]> examples,String rule)
	{
		for(int i=0; i<examples.size(); i++)
		{
			if(!checkRule(examples.get(i)[0],examples.get(i)[1],rule))
			{
				return false;
			}
		}
		return true;
	}
	public boolean checkRule(String org,String tar,String rule)
	{
		try
		{
			Ruler r = new Ruler();
			r.setNewInput(org);
			CharStream cs =  new ANTLRStringStream(rule);
			RuleInterpreterLexer lexer = new RuleInterpreterLexer(cs);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        RuleInterpreterParser parser= new RuleInterpreterParser(tokens);
	        CommonTreeNodeStream nodes = new CommonTreeNodeStream(parser.rule().getTree());
		    RuleInterpreterTree inter = new RuleInterpreterTree(nodes);
		    inter.setRuler(r);
	    		inter.rule();
	    		//System.out.println(""+r.toString());
	    		if(r.toString().compareTo(tar)==0)
	    		{
	    			return true;
	    		}
	    		else
	    		{
	    			return false;
	    		}
	    }
	    catch(Exception ex)
	    {
	    		return false;
	    }
	}
	//replace the tar with a set of new values. each of this new value become a new rule
	public static Vector<String> substitueRuleVar(String rule,Vector<String> newV,String tar)
	{
		Vector<String> rs = new Vector<String>();		
		for(String nv:newV)
		{
			String tmps = rule;
			tmps = tmps.replaceFirst(tar, nv);
			rs.add(tmps);
		}
		return rs;
	}
	public Vector<String> replacetokspec(Collection<String> whats,Collection<TNode> examplex,int spos,int epos)
	{
		Vector<String> res = new Vector<String>();
		ArrayList<TNode> example = new ArrayList<TNode>(examplex.size());
		example.addAll(examplex);
		for(String s:whats)
		{
			String x = s.trim();
			String[] tokens = x.split(" ");
			if(tokens.length!=epos-spos+1)
				continue; // only keep the 1 and n two options
			boolean valid = true; 
			for(int i = 0; i<tokens.length;i++)
			{
				if(tokens[i].compareTo("TOKEN")==0)
				{
					tokens[i] = "\""+example.get(spos+i).text+"\"";
				}
				else
				{
					if(tokens[i].compareTo(example.get(spos+i).getType())!=0&&tokens[i].compareTo(TNode.ANYTOK)!=0)
					{
						valid = false;
						break;
					}
				}
			}
			if(valid)
			{
				String q = "";
				for(String p:tokens)
				{
					q+=p+" ";
				}
				res.add(q.trim());
			}
		}
		return res;
	}
	//assign the values to the variables using the examples
	// s is the rule contains the token that need to be assign value
	//example is the token sequence for the orignial string
	public Vector<String> assignValue(String s,Vector<Vector<String>> vs)
	{
		Vector<String> rules = new Vector<String>();
		Vector<String> resrules = new Vector<String>();
		rules.add(s);
		//tokenize the example
		//identify the variance part
		
		Vector<String> wNum = new Vector<String>();
		wNum = vs.get(0);
		Vector<String> wToken = new Vector<String>();
		wToken = vs.get(1);
		//remove
		Vector<String> sNum = new Vector<String>();
		sNum = vs.get(2);
		Vector<String> sToken = new Vector<String>();
		sToken = vs.get(3);
		Vector<String> eNum = new Vector<String>();
		eNum = vs.get(4);
		Vector<String> eToken = new Vector<String>();
		eToken = vs.get(5);
		int pos1 = s.indexOf("from");
		int pos2 = s.indexOf("from", pos1+1);
		// for token
		int p;
		while(rules.size()>0)
		{
			String fr = rules.get(0);
			rules.remove(0); // pop out the first element
			if((fr.indexOf("NUM")== -1)&&(fr.indexOf("TOKEN")==-1))
			{
				resrules.add(fr);
			}
			if((p = fr.indexOf("NUM"))!= -1)
			{
				if(p<pos1)
				{
					Vector<String> res = substitueRuleVar(fr,wNum,"NUM");
					rules.addAll(res);
					continue;
				}
				else if(p>pos1 && p<pos2)
				{
					Vector<String> res = substitueRuleVar(fr,sNum,"NUM");
					rules.addAll(res);
					continue;
				}
				else if(p>pos2)
				{
					Vector<String> res = substitueRuleVar(fr,eNum,"NUM");
					rules.addAll(res);
					continue;
				}
			}
			if((p = fr.indexOf("TOKEN"))!= -1)
			{
				if(p<pos1)
				{
					Vector<String> res = substitueRuleVar(fr,wToken,"TOKEN");
					rules.addAll(res);
					continue;
				}
				else if(p>pos1 && p<pos2)
				{
					Vector<String> res = substitueRuleVar(fr,sToken,"TOKEN");
					rules.addAll(res);
					continue;
				}
				else if(p>pos2)
				{
					Vector<String> res = substitueRuleVar(fr,eToken,"TOKEN");
					rules.addAll(res);
					continue;
				}
			}
		}
		return resrules;
	}
	private void initconsterminals()
	{
		consterminals = new HashMap<String,String>();
		consterminals.put("DEL", "del");
		consterminals.put("MOV", "mov");
		consterminals.put("INS", "ins");
		consterminals.put("ANYNUM", "anynumber");
		consterminals.put("ANYTYP", "ANYTYP");
		consterminals.put("NUMTYP", "Number");
		consterminals.put("WRDTYP", "Word");
		consterminals.put("SYBTYP", "Symbol");
		consterminals.put("BNKTYP", "Blank");
		consterminals.put("ANYTOK", "ANYTOK");	
		consterminals.put("FRMB", "from_beginning");
		consterminals.put("FRME", "from_end");
		consterminals.put("FST", "first");
		consterminals.put("LST", "last");
		consterminals.put("INCLD", "incld");
		consterminals.put("ANYTOKS", "anytoks");
		//deal with NUM and token type
		
	}
	//do the cross join between ArrayList<String>
	public Vector<String> crossJoin(ArrayList<HashSet<String>> array)
	{
		Vector<String> y1=new Vector<String>();
		Vector<String> y2=new Vector<String>();
		y1 = new Vector<String>();
		y1.addAll(array.get(0));
		Vector<String> y3;
		Vector<String> y4 = new Vector<String>();
		if(array.size()==1)
		{
			 y4.addAll(array.get(0));
			 return y4;
		}
		for(int i=0; i<array.size()-1; i++)
		{
			HashSet<String> x = array.get(i);
			if(i%2==0)
			{
				y3 = y1;
				y2 = new Vector<String>();
				y4 = y2;
			}
			else
			{
				y3 = y2;
				y1 = new Vector<String>();
				y4 = y1;
			}
			for(String str:y3)
			{
				for(String s:array.get(i+1))
				{
					String ts = str+" "+s;
					y4.add(ts.trim());
				}
			}
			if(i%2==0)
			{
				y2 = y4;
			}
			else
			{
				y1 = y4;
			}
		}
		return y4;
	}
	public HashSet<String> printRules(String name,int cnt)
	{
		//all the production rules corresponding to name
		ArrayList<ArrayList<String>> prods = nonterminals.get(name);
		HashSet<String> allchoic = new HashSet<String>();
		for(int j=0;j<prods.size();j++)
		{
			ArrayList<String> rule = prods.get(j);
			// mix all the nontermin into the same Arraylist<String>
			ArrayList<HashSet<String>> v = new ArrayList<HashSet<String>>();
			for(int k=0; k<rule.size();k++)
			{					
				if(nonterminals.containsKey(rule.get(k)))
				{
					// to prevent infinite loop
					if(rule.get(k).compareTo(name)==0)
					{
						if(cnt == 0)
						{
							HashSet<String> s =new HashSet<String>();
							s.add("");
							v.add(s);
							continue;
						}
						HashSet<String> s = printRules(rule.get(k),cnt-1);
						v.add(s);
					}
					else
					{
						HashSet<String> s = printRules(rule.get(k),cnt);
						v.add(s);
					}
				}
				else // terminal
				{
					HashSet<String> x = new HashSet<String>();
					x.add(rule.get(k).trim());
					v.add(x);
				}
			}
			//mix v
			Vector<String> z = this.crossJoin(v);
			allchoic.addAll(z);
		}
		return allchoic;
	}
	public void addElement(ArrayList<ArrayList<String>> s,String name,boolean isoptional)
	{
		if(s.size()==0)
		{
			if (isoptional)
			{
				ArrayList<String> sub1 = new ArrayList<String>();
				sub1.add(name);
				ArrayList<String> sub2 = new ArrayList<String>();
				sub2.add("");
				s.add(sub1);
				s.add(sub2);
				
			}
			else
			{
				ArrayList<String> sub1 = new ArrayList<String>();
				sub1.add(name);
				s.add(sub1);
			}
		}
		else
		{
			if(isoptional)
			{
				int size = s.size();
				for(int i= 0;i<size;i++)
				{
					//create a new string list and added to s
					ArrayList<String> al = (ArrayList<String>)s.get(i).clone(); 
					al.add("");
					s.add(al);
					//add a new element at the end
					s.get(i).add(name);
				}
			}
			else
			{
				for(int i=0;i<s.size();i++)
				{
					s.get(i).add(name);
				}
			}
		}
	}
	public void initRule(String name, ArrayList<ArrayList<String>> rule)
	{
		ArrayList<ArrayList<String>> rules = new ArrayList<ArrayList<String>>();
		if(!nonterminals.containsKey(name))
		{
			nonterminals.put(name, rules);
			this.addRule(name, rule);
		}
		else
		{
			this.addRule(name, rule);
		}
	}
	public void addRule(String name, ArrayList<ArrayList<String>> rule)
	{
		for(int i =0;i<rule.size();i++)
		{
			nonterminals.get(name).add(rule.get(i));
		}
	}
	public String revital(String x)
	{
		for(String s :this.consterminals.keySet())
		{
			if(x.contains(s))
			{
				x= x.replaceAll(s, ""+this.consterminals.get(s)+"");
			}
		}
		return x;
	}
	// at least 11 production rules would be used in deriving a rule
	// 
	//set the prior. prefer using fewer production rules
	//name is the non-terminal symbol
	private double alpha = 2; // the ratio that the terminal should be bigger than nonterms
	private HashMap<String,double[]> nontermprobs = new HashMap<String,double[]>();
	public int state = 0;
	public void initialNonterminalProbs()
	{
		Set<String> keys = this.nonterminals.keySet();
		for(String key:keys)
		{
			ArrayList<ArrayList<String>> rules = nonterminals.get(key);
			if(rules.size()<=1)
			{
				
				double[] p = {1.0};
				nontermprobs.put(key, p);
			}
			else
			{
				double[] probs = new double[rules.size()];
				int nontcnt = 0;
				int tcnt = 0;
				for(int i=0;i<rules.size();i++)
				{
					ArrayList<String> rule = rules.get(i);
					if(containNonterm(rule))
					{
						probs[i] = 2;
						nontcnt ++;
					}
					else
					{
						probs[i] = 1;
						tcnt ++;
					}
				}
				//add the computed probs
				double base = 1.0/(nontcnt+tcnt*alpha);
				
				for(int i = 0; i<probs.length; i++)
				{
					if(probs[i]==2) // rule contains non-terminal
					{
						probs[i] = base;
					}
					else // rule doesn't contain non-terminal
					{
						probs[i] = base*alpha;
					}
				}
				nontermprobs.put(key, probs);
			}
		}
	}
	public boolean containNonterm(ArrayList<String> rule)
	{
		for(String x:rule)
		{
			if(nonterminals.containsKey(x))
				return true;
		}
		return false;
	}
	//
	public String genRule(String name)
	{
		//all the production rules corresponding to name
		ArrayList<ArrayList<String>> prods = nonterminals.get(name);
		Random r = new Random();
		int size = prods.size();
		int index = r.nextInt(size);
		ArrayList<String> rule = prods.get(index);
		//only the non-terminals having multiple rules could be mutated
		if(size>1)
		{
			this.curRuleConfig.put(name, index);
		}
		String res = "";
		for(int k=0; k<rule.size();k++)
		{					
			if(nonterminals.containsKey(rule.get(k)))
			{
				// to prevent infinite loop
				if(rule.get(k).compareTo(name)==0)
				{
					res += " "+rule.get(k);
					continue;
				}
				else
				{
					 res += " "+genRule(rule.get(k));
				}
			}
			else // terminal
			{
				return rule.get(k).trim();
			}
		}
		return res;
	}
	//get the non-terminals set that has multiple production rules
	private double curTransprob = 0.0;
	private double revTransprob = 0.0;
	public double transProb(HashMap<String,Integer> from, HashMap<String,Integer> to)
	{
		
		return 0.0;
	}
	//  
	public String genNextRule()
	{
		//choose the non-terminals that has multiple production rules
		//choose the rule according to the multinominal distribution defined by alpha  (1-alpha)
		Random r = new Random();
		int ind = r.nextInt(this.curRuleConfig.size());
		Set<String> keys = curRuleConfig.keySet();
		Iterator<String> iptr = keys.iterator();
		int i = 0;
		String key = "";
		// move the randomly choosen non-terminal
		while(i<=ind)
		{
			key = iptr.next();
			i++;
		}
		// get the production rules of the non-terminal
		ArrayList<ArrayList<String>> prods = this.nonterminals.get(key);
		double[] probs = nontermprobs.get(key);
		// choose one production rule based on the mulitnominal probability
		int rulePos = multinominalSampler(probs);
		//update the reverse transition probability
		revTransprob = 1.0/curRuleConfig.size()*getPrior(key);
		curRuleConfig.put(key, rulePos);//update the current rule for non-terminal 'syb'
		for(String s:prods.get(rulePos))
		{
			if(nonterminals.containsKey(s))
			{
				genRule(s); // the genRule would update the current rule for mutated non-term
			}
		}
		//update the current transition prob
		curTransprob = 1.0/curRuleConfig.size()*getPrior(key);
		this.prepriorprob = this.curpriorprob;
		curpriorprob = this.getPrior("rule");
		this.state ++;
		return "";
	}
	//probs is the multinominal distribution of the production rules
	public int multinominalSampler(double[] probs)
	{
		Random r = new Random();
		double x = r.nextDouble();
		if(x<=probs[0])
		{
			return 0;
		}
		for(int i = 1;i<probs.length;i++)
		{
			if(x<=probs[i] && x > probs[i-1])
			{
				return i;
			}
		}
		return 0;
	}
	//compute the current prior distribution
	//in order to compute the MCMC need the current state prior prob and also the next state prior prob.
	private double prepriorprob = 1.0;
	private double curpriorprob = 1.0;
	// return the probability of  a terminal tree generate from the grammar
	
	public double getPrior(String name)
	{
		double prob = 1.0;
		ArrayList<ArrayList<String>> prods = nonterminals.get(name);
		int index = 0;
		if(curRuleConfig.containsKey(name))
		{
			index = curRuleConfig.get(name);
			prob = prob*nontermprobs.get(name)[index];
		}
		ArrayList<String> rule = prods.get(index);
		for(int k=0; k<rule.size();k++)
		{					
			if(nonterminals.containsKey(rule.get(k)))
			{
				prob = prob*getPrior(rule.get(k));
			}
		}
		return prob;
	}
	//
	public double getTransProb()
	{
		return curTransprob;
	}
	public double getreverseTransProb()
	{
		return revTransprob;
	}
	public double getcurPriorProb()
	{
		return curpriorprob;
	}
	public double getprePriorProb()
	{
		return prepriorprob;
	}
	public static void main(String[] args)
	{
		
	}
}
