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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterLexer;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterParser;

public class GrammarParseTree {
	// the set of node  which are going to be mutated.
	public Vector<GrammarTreeNode> candiNodes = new Vector<GrammarTreeNode>();
	public int curState = 0;//used to indicte the current sampling step.
	public GrammarTreeNode root;
	//string = parentname+curname, Vector stores all the possible candidate
	public HashMap<String,Vector<GrammarTreeNode>> subtemples = new HashMap<String,Vector<GrammarTreeNode>>();
	public static RuleGenerator movgen;
	public static RuleGenerator delgen;
	public static RuleGenerator insgen;
	public static RuleGenerator gen;
	private  HashMap<String,ArrayList<ArrayList<String>>> nonterminals;
	public Alignment align;
	private String GrammarType = "";
	private static int cnt1 = 0,cnt2 = 0,cnt3 = 0;
	private Vector<Tuple> ba = new Vector<Tuple>(); 
	private Vector<HashSet<String>> diagDesc = new Vector<HashSet<String>>();
	public GrammarParseTree(String type)
	{
		this.GrammarType = type;
		//this.initGrammarParserTrees();
		this.initialRuleSet();
	}
	public  static void initGrammarParserTrees()
	{
		try
		{
			FileInputStream   file   =   new   FileInputStream("./grammar/MOVgrammar.txt"); 
			byte[]   buf   =   new   byte[file.available()];     
			file.read(buf,   0,   file.available());   // 
			String   str   =   new   String(buf); 
			CharStream cs =  new ANTLRStringStream(str);
			GrammarparserLexer lexer = new GrammarparserLexer(cs);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        GrammarparserParser movparser = new GrammarparserParser(tokens);
	        movgen = new RuleGenerator();
	        movparser.setGen(movgen);
	        movparser.alllines();
	        
	        FileInputStream   file1   =   new   FileInputStream("./grammar/INSgrammar.txt"); 
			byte[]   buf1   =   new   byte[file1.available()];     
			file1.read(buf1,   0,   file1.available());   // 
			String   str1   =   new   String(buf1); 
			CharStream cs1 =  new ANTLRStringStream(str1);
			GrammarparserLexer lexer1 = new GrammarparserLexer(cs1);
	        CommonTokenStream tokens1 = new CommonTokenStream(lexer1);
	        GrammarparserParser insparser= new GrammarparserParser(tokens1);
	        insgen = new RuleGenerator();
	        insparser.setGen(insgen);
	        insparser.alllines();
	        
	        FileInputStream   file2   =   new   FileInputStream("./grammar/delgrammar.txt"); 
			byte[]   buf2   =   new   byte[file2.available()];     
			file2.read(buf2,   0,   file2.available());   // 
			String   str2   =   new   String(buf2); 
			CharStream cs2 =  new ANTLRStringStream(str2);
			GrammarparserLexer lexer2 = new GrammarparserLexer(cs2);
	        CommonTokenStream tokens2 = new CommonTokenStream(lexer2);
	        GrammarparserParser delparser= new GrammarparserParser(tokens2);
	        delgen = new RuleGenerator();
	        delparser.setGen(delgen);
	        delparser.alllines();
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
        
        
	}
	public boolean isRuleConsistent(String rule)
	{
		boolean isConsis = true;
		try
		{
		//System.out.println(rule);
		for(int i =0; i<ba.size();i++)
		{
			Vector<TNode> bef = (Vector<TNode>)ba.get(i).getBefore().clone();
			Vector<TNode> tras = RuleUtil.applyRule(rule, bef);
			//System.out.println(i+" "+ba.size()+" "+tras);
			if(tras == null)
			{
				isConsis = false;
				break;
			}
			Vector<TNode> aft = ba.get(i).getafter();
			
			if(tras.toString().compareTo(aft.toString())!=0)
			{
				isConsis = false;
				break;
			}
		}
		}
		catch(Exception e)
		{
			System.out.println("check rule consistency error"+e.toString());
		}
		return isConsis;
	}
	public void setExample(Vector<Tuple> tp)
	{
		this.ba = tp;
	}
	public String toString()
	{
		String rule = this.toString(root);
		rule = gen.revital(rule);
		return rule;
	}
	public static HashMap<String, ArrayList<ArrayList<String>>> deepclone(HashMap<String, ArrayList<ArrayList<String>>> x)
	{
		HashMap<String, ArrayList<ArrayList<String>>> nx = new HashMap<String, ArrayList<ArrayList<String>>>();
		Iterator<String> iter = x.keySet().iterator();
		while(iter.hasNext())
		{
			String key = iter.next();
			ArrayList<ArrayList<String>> value = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> copy = x.get(key);
			for(int i = 0; i<copy.size(); i++)
			{
				ArrayList<String> xy = (ArrayList<String>)copy.get(i).clone();
				value.add(xy);
			}
			nx.put(key, value);
		}
		return nx;
	}
	public void initialRuleSet()
	{
		try
		{
			if(GrammarType.compareTo("mov")==0)
			{
		       gen = movgen;
		       this.nonterminals = deepclone(movgen.nonterminals);
			}
			else if(GrammarType.compareTo("ins")==0)
			{	        
		       gen = insgen;
		       this.nonterminals = deepclone(insgen.nonterminals);
			}
			else if(GrammarType.compareTo("del")==0)
			{
				gen = delgen;
			    this.nonterminals =deepclone(delgen.nonterminals);
			}
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
	// if the non-terminal has no children, delete the non-terminal from the grammar space
	public void trimgrammar(String key)
	{
		HashMap<String,ArrayList<ArrayList<String>>> nons = this.nonterminals;
		for(String ks:nons.keySet())
		{
			Iterator<ArrayList<String>> iter = nons.get(ks).iterator();
			while(iter.hasNext())
			{
				ArrayList<String> rule = iter.next();
				boolean isfind = false;
				for(String s:rule)
				{
					if(s.compareTo(key)==0)
					{
						isfind = true;
						break;
					}
				}
				if(isfind)
				{
					iter.remove();
				}
			}
		}
	}
	//convert the parsetree into a tring rule
	public String toString(GrammarTreeNode x)
	{
		try
		{
			String res = "";
			if(x.children == null)
			{
				return x.name;
			}
			if(x.children.size() == 0)
			{
				return x.name;
			}
			else
			{
				for(GrammarTreeNode n:x.children)
				{
					res += " "+toString(n);
				}
			}
			return res;
		}
		catch(Exception ex)
		{
			System.out.println("toString error: "+x.name+" "+x.children.toString());
			return "";
		}
	}
	//assume 
	public GrammarTreeNode induceTree(Vector<String> input)
	{
		String seg = "";
		for(String s:input)
		{
			seg += " \""+s+"\"";
		}
		// use the rule parse to build the template tree
		try
		{
			CharStream cs =  new ANTLRStringStream(seg);
			RuleInterpreterLexer lexer = new RuleInterpreterLexer(cs);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        RuleInterpreterParser parser= new RuleInterpreterParser(tokens);
	        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
		return null;
	}
	public void buildTree(String name, GrammarTreeNode par)
	{
		//using the rule to generate a possible parse tree comply with the examples
		if(name==null)
			return;
		if(name.length()==0)
			return;
		ArrayList<ArrayList<String>> rules = this.nonterminals.get(name);
		//System.out.println(this.nonterminals);		
		if(rules == null||rules.size()==0)
		{
			GrammarTreeNode t = new GrammarTreeNode(name);
			t.parent = par;
			par.addChild(t);
			return;
		}
		//System.out.println(""+name+":"+rules.size());
		if(rules.size()>1||subtemples.containsKey(name)) // more than one alternatives
		{
			if(subtemples.containsKey(name))
			{
				//System.out.println("UX");
				//randomly choose one from the pool;
				Random r = new Random();
				//System.out.println("Ut");
				int sleng = subtemples.get(name).size();			
				int xindex = r.nextInt(sleng);
				//System.out.println("UU");
				par.addChild(subtemples.get(name).get(xindex));
				if(name.compareTo("tnum") == 0)
				{
					//xindex = r.nextInt(sleng);
					//System.out.print(xindex);
					if(xindex==0)
					{
						cnt1++;
					}
					else if(xindex==1)
					{
						cnt2++;
					}
					else
					{
						cnt3++;
					}
				//	System.out.println("count number>>>"+sleng+"="+cnt1+":"+cnt2+":"+cnt3);
				//	System.out.println("<<>>"+subtemples.get(name)+"<<>>");
				//	System.out.println("<<>>"+subtemples.get(name).get(xindex).children+"<<>>");
				}
				//add this non-terminal into the candidate pool for mutation
				candiNodes.add(subtemples.get(name).get(xindex));
			}
			else
			{
				//System.out.print("mark");
				Random r = new Random();
				int index = r.nextInt(rules.size());
				ArrayList<String> rule = rules.get(index);
				GrammarTreeNode t = new GrammarTreeNode(name);
				/*if(name.compareTo("swherequantifier")==0)
				{
					if(index==0)
					{
						cnt1++;
					}
					else if(index==1)
					{
						cnt2++;
					}
					else
					{
						cnt3++;
					}
					System.out.println("count number>>>"+rules.size()+"="+cnt1+":"+cnt2+":"+cnt3);
				}*/
				t.parent = par;
				par.addChild(t);
				for(String s:rule)
				{
					buildTree(s,t);
				}
			}
		}
		else
		{
			ArrayList<String> rule = rules.get(0);
			GrammarTreeNode t = new GrammarTreeNode(name);
			t.parent = par;
			par.addChild(t);
			for(String s:rule)
			{
				buildTree(s,t);
			}
		}
	}	
	//used to print info for debugging
	public void diagPrint()
	{
		System.out.println("===========Debugging=================");
		System.out.println(diagDesc.toString());
		for(int i = 0; i<ba.size();i++)
		{
			Tuple x = ba.get(i);
			String a = "";
			String b = "";
			for(int j = 0;j<x.getBefore().size();j++)
			{
				a += x.getBefore().get(j).text;
			}
			for(int j = 0;j<x.getafter().size();j++)
			{
				b += x.getafter().get(j).text;
			}
			System.out.println(a+"<<>>"+b);
			
		}
		
	}
	//using edit sequence to generate some candidates pool
	/*
	 * eos is the set of edit operation of multiple examples
	 * p is the original token sequence.
	 */
	public boolean initalSubtemplete(Vector<HashSet<String>> sequen,HashMap<String,GrammarTreeNode> globalTemp)
	{
		diagDesc = sequen;
		//if subtemple exists, we should remove the production rules.
		for(String s:sequen.get(0))
		{
			//System.out.println("etokenspec: "+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("etokenspec",s,globalTemp);
			if(subtemples.containsKey("etokenspec"))
			{
				subtemples.get("etokenspec").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("etokenspec", vg);
			}
		}
		for(String s:sequen.get(1))
		{
			//System.out.println("stokenspec: "+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("stokenspec",s,globalTemp);
			if(subtemples.containsKey("stokenspec"))
			{
				subtemples.get("stokenspec").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("stokenspec", vg);
			}
		}
		for(String s:sequen.get(2))
		{
			//System.out.println("tokenspec: "+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("tokenspec",s,globalTemp);
			if(subtemples.containsKey("tokenspec"))
			{
				subtemples.get("tokenspec").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("tokenspec", vg);
			}
		}
		for(String s:sequen.get(3))
		{
			//System.out.println("quantifier: "+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("qnum",s,globalTemp);
			if(subtemples.containsKey("qnum"))
			{
				subtemples.get("qnum").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("qnum", vg);
			}
		}
		for(String s:sequen.get(4))
		{
			//System.out.println("snum: "+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("snum",s,globalTemp);
			if(subtemples.containsKey("snum"))
			{
				subtemples.get("snum").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("snum", vg);
			}
		}
		for(String s:sequen.get(5))
		{
			//System.out.println("tnum: "+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("tnum",s,globalTemp);
			if(subtemples.containsKey("tnum"))
			{
				subtemples.get("tnum").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("tnum", vg);
			}
		}
		for(String s:sequen.get(6))
		{
			//System.out.println("dnum:"+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("dnum",s,globalTemp);
			if(subtemples.containsKey("dnum"))
			{
				subtemples.get("dnum").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("dnum", vg);
			}
		}
		for(String s:sequen.get(7))//
		{
			//System.out.println("dtokenspec:"+s);
			GrammarTreeNode root = NonterminalValidator.getTemplate("dtokenspec",s,globalTemp);
			if(subtemples.containsKey("dtokenspec"))
			{
				subtemples.get("dtokenspec").add(root);
			}
			else
			{
				Vector<GrammarTreeNode> vg = new Vector<GrammarTreeNode>();
				vg.add(root);
				subtemples.put("dtokenspec", vg);
			}
		}
		if(sequen.get(0).size()==0)
			trimgrammar("etokenspec");
		if(sequen.get(1).size()==0)
			trimgrammar("stokenspec");
		if(sequen.get(2).size()==0)
			trimgrammar("tokenspec");
		if(sequen.get(3).size()==0)
			trimgrammar("qnum");
		if(sequen.get(4).size()==0)
			trimgrammar("snum");
		if(sequen.get(5).size()==0)
			trimgrammar("tnum");
		if(sequen.get(6).size()==0)
			trimgrammar("dnum");
		if(sequen.get(7).size()==0)
			trimgrammar("dtokenspec");
		
		Set<String> keys = this.nonterminals.keySet();
		Iterator<String> iter = keys.iterator();
		//non-terminal has no production rules
		while(iter.hasNext())
		{
			String key = iter.next();
			if(nonterminals.get(key).size()==0)
			{
				System.out.println("Key:  "+key+","+this.GrammarType);
				return false;
			}
		}
		return true;
	}
	//find a node e in the parse tree
	public GrammarTreeNode search(GrammarTreeNode e)
	{
		
		return null;
	}
	// replace a node in the parse tree
	public void replace(GrammarTreeNode before, GrammarTreeNode after)
	{
		GrammarTreeNode parent = before.parent;
		int index = parent.children.indexOf(before);
		parent.children.remove(index);
		parent.children.add(index, after);
	}
	//mutate the current tree into another tree to generate the next parse tree
	public void generateNext()
	{
		//mutate the original subtree
		//choose one node from the set 
		int cansize = candiNodes.size();
		Random r = new Random();
		int index = r.nextInt(cansize);
		GrammarTreeNode gt = candiNodes.get(index);
		GrammarTreeNode x = modifyNode(gt);
		replace(gt,x);
	}
	public GrammarTreeNode modifyNode(GrammarTreeNode e)
	{
		String key = e.parent.name+e.name;
		if(subtemples.containsKey(key))
		{
			Vector<GrammarTreeNode> y = subtemples.get(key);
			int size = y.size();
			if(size == 1)
			{
				System.out.println("Error occured, should be more than 1 element");
				System.exit(1);
			}
			Random r = new Random();
			int index = r.nextInt(size);
			//loop until sample some node different from the former node
			while(y.get(index)==e)
			{
				index = r.nextInt(size);
			}
			return y.get(index);
		}
		return null;
	}
	public static void main(String[] args)
	{
		GrammarParseTree gptree = new GrammarParseTree(null);
		String fp = "/Users/bowu/Research/dataclean/data/RuleData/FullChange.csv";
		try
		{
			CSVReader cr = new CSVReader(new FileReader(new File(fp)),'\t');
			String x[];
			while((x=cr.readNext())!=null)
			{
				Ruler r1 = new Ruler();
				Ruler r2 = new Ruler();
				String s1 = x[0];
				r1.setNewInput(s1);
				String s2 = x[1];
				r2.setNewInput(s2);
				System.out.println(s1+" ===== "+s2);
				//System.out.println(Alignment.alignment1(r1.vec, r2.vec).toString());
				Vector<Vector<EditOper>> res = Alignment.genEditOperation(r1.vec,r2.vec);
				// prepare description for sub tree
				Vector<TNode> p = r1.vec;
				Vector<TNode> q = r2.vec;
			}
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
