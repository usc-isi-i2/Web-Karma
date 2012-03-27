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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.changed_grammar.INSInterpreterLexer;
import edu.isi.karma.cleaning.changed_grammar.INSInterpreterParser;
import edu.isi.karma.cleaning.changed_grammar.INSInterpreterTree;
import edu.isi.karma.cleaning.changed_grammar.MOVInterpreterLexer;
import edu.isi.karma.cleaning.changed_grammar.MOVInterpreterParser;
import edu.isi.karma.cleaning.changed_grammar.MOVInterpreterTree;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterLexer;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterParser;
import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterTree;

public class RuleUtil {
	public  static void write2file(Collection<String> x,String fname)
	{
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fname)));
			for(String s:x)
			{
				bw.write(s+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
	// 
	public static Vector<String> applyRuleF(Vector<String> rules,String fpath0)
	{
		try
		{
			Vector<String> res = new Vector<String>();
			BufferedReader br = new BufferedReader(new FileReader(fpath0));
			String line = "";
			while((line = br.readLine())!= null)
			{
				if(line.length() == 0)
					continue;
				line = "%"+line+"@";
				System.out.println(""+line);
				line = RuleUtil.applyRule(rules, line);
				res.add(line);
			}
			return res;
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
			return null;
		}
	}
	//apply a sequence of rules
	public static String applyRule(Vector<String> rules, String s)
	{
		try
		{
		Ruler r = new Ruler();
		r.setNewInput(s);
		Vector<TNode> x = r.vec;
		for(String rule:rules)
		{
			x = applyRule(rule,x);
		}
		String z = "";
		for(int i = 0; i<x.size();i++)
		{
			z+= x.get(i).text;
		}
		return z;
		}
		catch(Exception e)
		{
			//System.out.println(""+e.toString());
			return "";
		}
	}
	//apply a rule on a token sequence and return a token sequence
	public static Vector<TNode> applyRule(String rule,Vector<TNode> before)
	{
		try 
		{
			CharStream cs =  new ANTLRStringStream(rule);
			Ruler r = new Ruler();
			//decide which class of rule interpretor apply
			rule = rule.trim();
			if(rule.indexOf("mov")==0)
			{
				MOVInterpreterLexer lexer = new MOVInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);	        
		        MOVInterpreterParser parser= new MOVInterpreterParser(tokens);
		        CommonTree x = (CommonTree)parser.rule().getTree();
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream(x);
		        MOVInterpreterTree evaluator = new MOVInterpreterTree(nodes);  
		        r.setNewInput(before);
		        evaluator.setRuler(r);
		        evaluator.rule();
		        
			}
			else if(rule.indexOf("del")==0)
			{
				RuleInterpreterLexer lexer = new RuleInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);
		        RuleInterpreterParser parser= new RuleInterpreterParser(tokens);
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		        RuleInterpreterTree evaluator = new RuleInterpreterTree(nodes);
		        r.setNewInput(before);
		        evaluator.setRuler(r);
		        evaluator.rule();
			}
			else if(rule.indexOf("ins")==0)
			{
				INSInterpreterLexer lexer = new INSInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);
		        INSInterpreterParser parser= new INSInterpreterParser(tokens);
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		        INSInterpreterTree evaluator = new INSInterpreterTree(nodes);
		        r.setNewInput(before);
		        evaluator.setRuler(r);
		        evaluator.rule();
			}
	        return r.vec;
			
		} catch (Exception e) {
			return null;
		}
		
	}
	//apply a rule on String and return a token sequence
	public static Vector<TNode> applyRulet(String rule,String s)
	{
		try 
		{
			CharStream cs =  new ANTLRStringStream(rule);
			Ruler r = new Ruler();
			//decide which class of rule interpretor apply
			rule = rule.trim();
			if(rule.indexOf("mov")==0)
			{
				MOVInterpreterLexer lexer = new MOVInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);	        
		        MOVInterpreterParser parser= new MOVInterpreterParser(tokens);
		        CommonTree x = (CommonTree)parser.rule().getTree();
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream(x);
		        MOVInterpreterTree evaluator = new MOVInterpreterTree(nodes);  
		        r.setNewInput(s);
		        evaluator.setRuler(r);
		        evaluator.rule();
		        
			}
			else if(rule.indexOf("del")==0)
			{
				RuleInterpreterLexer lexer = new RuleInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);
		        RuleInterpreterParser parser= new RuleInterpreterParser(tokens);
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		        RuleInterpreterTree evaluator = new RuleInterpreterTree(nodes);
		        r.setNewInput(s);
		        evaluator.setRuler(r);
		        evaluator.rule();
			}
			else if(rule.indexOf("ins")==0)
			{
				INSInterpreterLexer lexer = new INSInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);
		        INSInterpreterParser parser= new INSInterpreterParser(tokens);
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		        INSInterpreterTree evaluator = new INSInterpreterTree(nodes);
		        r.setNewInput(s);
		        evaluator.setRuler(r);
		        evaluator.rule();
			}
	        return r.vec;
			
		} catch (Exception e) {
			return null;
		}
	}
	//apply a rule on a string
	public static String applyRuleS(String rule,String s)
	{
		try 
		{
			CharStream cs =  new ANTLRStringStream(rule);
			Ruler r = new Ruler();
			//decide which class of rule interpretor apply
			rule = rule.trim();
			if(rule.indexOf("mov")==0)
			{
				MOVInterpreterLexer lexer = new MOVInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);	        
		        MOVInterpreterParser parser= new MOVInterpreterParser(tokens);
		        CommonTree x = (CommonTree)parser.rule().getTree();
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream(x);
		        MOVInterpreterTree evaluator = new MOVInterpreterTree(nodes);  
		        r.setNewInput(s);
		        evaluator.setRuler(r);
		        evaluator.rule();
		        
			}
			else if(rule.indexOf("del")==0)
			{
				RuleInterpreterLexer lexer = new RuleInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);
		        RuleInterpreterParser parser= new RuleInterpreterParser(tokens);
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		        RuleInterpreterTree evaluator = new RuleInterpreterTree(nodes);
		        r.setNewInput(s);
		        evaluator.setRuler(r);
		        evaluator.rule();
			}
			else if(rule.indexOf("ins")==0)
			{
				INSInterpreterLexer lexer = new INSInterpreterLexer(cs);
		        CommonTokenStream tokens = new CommonTokenStream(lexer);
		        INSInterpreterParser parser= new INSInterpreterParser(tokens);
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.rule().getTree());
		        INSInterpreterTree evaluator = new INSInterpreterTree(nodes);
		        r.setNewInput(s);
		        evaluator.setRuler(r);
		        evaluator.rule();
			}
	        return r.toString();
			
		} catch (Exception e) {
			return "";
		}
		
	}
	public static String applyRule(String rule, String fpath)
	{
		 ResultViewer rv = new ResultViewer();
		 try
		 {
			
			String xline = "";
			File f = new File(fpath);
			BufferedReader xbr = new BufferedReader(new FileReader(f));
			while((xline=xbr.readLine())!=null)
			{
				Vector<String> xrow = new Vector<String>();
				if(xline.compareTo("")==0)
					break;
				String s = RuleUtil.applyRuleS(rule, xline);
		        System.out.println(s);
		        xrow.add(xline);
			    xrow.add(s);
			    //xrow.add(r.toString());
				rv.addRow(xrow);
			}
			rv.print(f.getAbsolutePath()+"_pair.txt");
			return f.getAbsolutePath()+"_pair.txt";
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
			return "";
		}	 
	}
	public static Vector<Vector<String>> getLitervalue(Vector<TNode> example, int sPos, int ePos,RuleGenerator gen)
	{
		//tokenize the example
		//identify the variance part
		int length = ePos - sPos +1; // the deleted number of Tnodes
		Vector<String> wNum = new Vector<String>();
		wNum.add(String.valueOf(length));
		//wNum.add("1"); // all the token to be deleted
		Vector<String> wToken = new Vector<String>();
		wToken = gen.replacetokspec(gen.printRules("tokenspec",(ePos-sPos)), example, sPos, ePos);
		//remove
		Vector<String> sNum = new Vector<String>();
		sNum.add(String.valueOf(sPos+1));
		sNum.add("0");
		sNum.add(String.valueOf(example.size()-sPos-1));
		Vector<String> sToken = new Vector<String>();
		sToken.add("\""+example.get(sPos).text+"\"");
		Vector<String> eNum = new Vector<String>();
		eNum.add(String.valueOf(ePos));
		eNum.add("0");
		eNum.add(String.valueOf(example.size()-ePos-1));
		Vector<String> eToken = new Vector<String>();
		eToken.add("\""+example.get(ePos).text+"\"");
		Vector<Vector<String>> vs = new Vector<Vector<String>>();
		vs.add(wNum);
		vs.add(wToken);
		vs.add(sNum);
		vs.add(sToken);
		vs.add(eNum);
		vs.add(eToken);
		return vs;
	}
	public static void filter(String nonterm, Vector<String> rules,Vector<TNode> org,Vector<TNode> tar,Vector<EditOper> eos)
	{
		return;
	}
	public static int sgsnum = 0;
	//ops is corresponding editoperation of multiple sequence
	public static Vector<String> genRule(Vector<String[]> examples)
	{
		Vector<String> rules = new Vector<String>();
		try
		{	
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
			Vector<Vector<GrammarParseTree>> trees = RuleUtil.genGrammarTrees(org, tar);
			sgsnum = trees.size();
			Vector<Integer> l = new Vector<Integer>();
			Vector<Integer> sr = new Vector<Integer>();
			Vector<String> pls = new Vector<String>();
			for(Vector<GrammarParseTree> gt:trees)
			{
				l.add(gt.size());
				sr.add(1);
			}
			int lhod = 8;
			int uhod = 50;
			int deadend = 0;
			if(l.size()*0.5>lhod &&l.size()*0.5<uhod)
				deadend = (int) (l.size()*0.5);
			else if(l.size()*0.5<lhod)
				deadend = lhod;
			else
				deadend = uhod;
			for(int c=0; c<deadend;c++)
			{
				int index = MarkovDP.sampleByScore(l,sr);
				//Random r = new Random();
				//int index = r.nextInt(1);
				Vector<GrammarParseTree> gt = trees.get(index);
				System.out.print(gt.size()+","+index+" ");	
				HashMap<MDPState,MDPState> his = new HashMap<MDPState,MDPState>();
				int sccnt = 0;
				for(int ct = 0;ct <100;ct++)
				{
					MarkovDP mdp = new MarkovDP(org,tar,gt);
					mdp.setHistory(his);
					mdp.run();
					if(mdp.isValid())
					{
						pls.add(mdp.getPolicies());
						sccnt += 1;
					}
				}
				sr.set(index, sccnt);
			}
			return pls;
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
			return null;
		}
	}
	public static String genRules(ArrayList<String[]> examples) throws Exception
	{
		try
		{
			String fname = "./grammar/grammar.txt";
			FileInputStream   file   =   new   FileInputStream(fname); 
			byte[]   buf   =   new   byte[file.available()];     
			file.read(buf,   0,   file.available());   // 
			String   str   =   new   String(buf); 
			CharStream cs =  new ANTLRStringStream(str);
			GrammarparserLexer lexer = new GrammarparserLexer(cs);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        GrammarparserParser parser= new GrammarparserParser(tokens);
	        RuleGenerator gen = new RuleGenerator();
	        parser.setGen(gen);
	        parser.alllines();
	        HashSet<String> s = gen.printRules("rule",0);
	        //use examples to learn some weak params
    			Vector<int[]> x = Alignment.getParams(examples.get(0)[0], examples.get(0)[1]);
    			Ruler r = new Ruler();
    			r.setNewInput(examples.get(0)[0]);
    			Vector<TNode> vt = r.vec;
	        Set<String> st = new HashSet<String>();
	        // for multiple tokens
	        if(x.size()==0)
	        {
	        		System.out.println("Nothing needs to be deleted");
	        		System.exit(0);
	        		return "";
	        }
	        Vector<Vector<String>> vs = RuleUtil.getLitervalue(vt, x.get(0)[0],x.get(0)[1],gen);
	        for(String l:s)
	        {
	        		l = gen.revital(l);
	        		Vector<String> ro = gen.assignValue(l,vs);
	        		for(String xx:ro)
	        		{
	        			xx = xx.replaceAll("( )+", " ");
	        			xx = xx.trim();
		        		if(!gen.checkOnExamples(examples, xx))
		        			continue;
		        		if(!st.contains(xx))
		        		{
		        			st.add(xx);
		        		}
	        		}
	        }
	        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/Users/bowu/mysoft/Allrules.txt")));
	        for(String ts:st)
	        {
	        		bw.write(ts+"\n");
	        }
	        bw.close();
	        System.out.println("ji");
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
        return "/Users/bowu/mysoft/Allrules.txt";
	}
	public static Vector<Vector<Vector<EditOper>>> genEditOpers(Vector<Vector<TNode>> orgs,Vector<Vector<TNode>> tars) throws Throwable
	{
		Vector<Vector<Vector<EditOper>>> tmp =new Vector<Vector<Vector<EditOper>>>();
		HashMap<String,Integer> filter = new HashMap<String,Integer>();//use the concatenation of the oper name as the key
		for(int i=0;i<orgs.size();i++)
		{
			HashMap<String,Integer> tmpfilter = new HashMap<String,Integer>();
			Vector<TNode> x = orgs.get(i);
			Vector<TNode> y = tars.get(i);
			Vector<Vector<EditOper>> ops = Alignment.genEditOperation(x, y);//ops contains multiple edit sequence
			for(int j = 0; j<ops.size();j++)
			{
				
				String sign = "";
				for(EditOper xeo:ops.get(j))
				{
					sign+=xeo.oper;
				}
				if(tmpfilter.containsKey(sign))
				{
					tmpfilter.put(sign, tmpfilter.get(sign)+1);
				}
				else
				{
					
					tmpfilter.put(sign, 1);
				}
			}
			for(String key:tmpfilter.keySet())
			{
				if(filter.containsKey(key))
				{
					filter.put(key, filter.get(key)+1);
				}
				else
				{
					filter.put(key, 1);
				}
			}
		}
		for(int i=0;i<orgs.size();i++)
		{
			Vector<TNode> x = orgs.get(i);
			Vector<TNode> y = tars.get(i);
			Vector<Vector<EditOper>> ops = Alignment.genEditOperation(x, y);//ops contains multiple edit sequence
			Vector<Vector<EditOper>> tx = new Vector<Vector<EditOper>>();
			
			for(int j = 0; j<ops.size();j++)
			{
				String sign = "";
				Vector<TNode> cur = x;
				for(EditOper xeo:ops.get(j))
				{
					sign+=xeo.oper;
					xeo.before = cur;
					Ruler r = new Ruler();
					r.setNewInput((Vector<TNode>)cur.clone());
					if(xeo.oper.compareTo("del")==0)
					{
						r.doOperation("del", "1", null, xeo.starPos, xeo.endPos);
					}
					else if(xeo.oper.compareTo("mov")==0)
					{
						r.doOperation("mov", ""+xeo.dest, null, xeo.starPos, xeo.endPos);
					}
					else if(xeo.oper.compareTo("ins")==0)
					{
						r.doOperation("ins", "1", xeo.tar, xeo.dest, 0);
					}
					xeo.after = (Vector<TNode>)(r.vec.clone());
					cur = r.vec;
				}
				if(filter.get(sign)>=2 || orgs.size()<=1)
				{
					tx.add(ops.get(j));
					//find before and after for each edit operator
					
				}
			}
			tmp.add(tx);
		}
		return tmp;
	}
	//edit operation number is the same
	//edit operation type is the same
	//move direction is the same
	//param: curSeqs edit sequence 
	//return: the signature of current  edit sequence
	public static String getSign(Vector<EditOper> curSeq)
	{
		String sign = "";
		//size
		int size = curSeq.size();
		sign = String.valueOf(size);
		// type and direction
		for(EditOper eo:curSeq)
		{
			sign += eo.oper;
			if(eo.oper.compareTo("mov")==0)
			{
				//find the direction left or right
				if(eo.dest>eo.endPos)
				{
					sign += "";
				}
				else if(eo.dest<eo.starPos)
				{
					sign += "";
				}
			}
		}
		return sign;
	}
	public static Vector<Vector<GrammarParseTree>> genGrammarTrees(Vector<Vector<TNode>> orgs,Vector<Vector<TNode>> tars)
	{
		Vector<Vector<Vector<EditOper>>> tmp = new Vector<Vector<Vector<EditOper>>>();
		try
		{	
			tmp = genEditOpers(orgs,tars);
		}
		catch(Throwable ex)
		{
			System.out.println("genEditOpers error: "+ex.toString());
		}
		// do cross join between the editsequence between different examples
		//if number of operation is not the same continue
		//       (one edit)
		HashMap<String,GrammarTreeNode> global_temp = new HashMap<String,GrammarTreeNode>();
		Vector<Vector<Vector<HashSet<String>>>> descriptions = new Vector<Vector<Vector<HashSet<String>>>>();//store description for multiple sequence
		Description dcrpt = new Description();
		Vector<Vector<Vector<Tuple>>> sequences = new Vector<Vector<Vector<Tuple>>>();
		Vector<String> sign = new Vector<String>();
		HashSet<String> hs = new HashSet<String>();
		for(int i=0;i<tmp.size();i++)
		{
			Vector<String> newsign = new Vector<String>();
			Vector<Vector<EditOper>> curSeqs = tmp.get(i);
			if(descriptions.size() == 0)
			{		
				for(Vector<EditOper> eos: curSeqs)
				{
					Vector<Vector<Tuple>> seq = new Vector<Vector<Tuple>>();
					Vector<TNode> p = (Vector<TNode>)orgs.get(i).clone();
					Vector<Vector<HashSet<String>>> seqscurExamp = new Vector<Vector<HashSet<String>>>(); //store the description for one editsequence 
					for(EditOper eo:eos)//
					{
						Vector<Tuple> vt = new Vector<Tuple>();
						vt.add(new Tuple(eo.before,eo.after));
						seq.add(vt);
						Vector<HashSet<String>> allParams = new Vector<HashSet<String>>();//store the all description for one operation
						HashSet<String> set1 = NonterminalValidator.genendendContext(eo, p);
						for(String s: set1)
						{
							NonterminalValidator.genTemplate("etokenspec", s, global_temp);
						}
						allParams.add(set1);
						HashSet<String> set2 = NonterminalValidator.genstartContext(eo, p);
						for(String s: set2)
						{
							NonterminalValidator.genTemplate("stokenspec", s, global_temp);
						}
						allParams.add(set2);
						HashSet<String> set3 = NonterminalValidator.generateWhatTemp(eo, p);
						for(String s: set3)
						{
							NonterminalValidator.genTemplate("tokenspec", s, global_temp);
						}
						allParams.add(set3);
						HashSet<String> set4 = NonterminalValidator.genNum(eo, p);
						for(String s: set4)
						{
							NonterminalValidator.genTemplate("qnum", s, global_temp);
						}
						allParams.add(set4);
						HashSet<String> set5 = NonterminalValidator.genPostion(eo, p, "start");
						for(String s: set5)
						{
							NonterminalValidator.genTemplate("snum", s, global_temp);
						}

						allParams.add(set5);
						HashSet<String> set6 = NonterminalValidator.genPostion(eo, p, "end");
						for(String s: set6)
						{
							NonterminalValidator.genTemplate("tnum", s, global_temp);
						}
						allParams.add(set6);
						HashSet<String> set7 = NonterminalValidator.genPostion(eo, p, "dest");
						for(String s: set7)
						{
							NonterminalValidator.genTemplate("dnum", s, global_temp);
						}
						allParams.add(set7);
						HashSet<String> set8 = NonterminalValidator.gendestContext(eo, p);
						for(String s: set8)
						{
							NonterminalValidator.genTemplate("dtokenspec", s, global_temp);
						}
						allParams.add(set8);
						//add the name of the operater 
						HashSet<String> set9 = new HashSet<String>();
						set9.add(eo.oper);
						allParams.add(set9);
						NonterminalValidator.applyoper(eo, p, eo.oper);		
						seqscurExamp.add(allParams);
					}
					if(hs.contains(seqscurExamp.toString()))
					{
						continue;
					}
					else
					{
						newsign.add(RuleUtil.getSign(eos));
						//descriptions1.add(seqscurExamp);
						dcrpt.addDesc(seqscurExamp);
						dcrpt.addSeqs(seq);
						hs.add(seqscurExamp.toString());
					}
				}
			}
			else
			{		
				for(int j = 0;j<descriptions.size(); j++)
				{
					//iterate through all possible edit sequence for current example
					for(int l = 0; l<curSeqs.size();l++)
					{
						//signature are the same
						if(RuleUtil.getSign(curSeqs.get(l)).compareTo(sign.get(j))==0)
						{
							Vector<TNode> p = (Vector<TNode>)orgs.get(i).clone();
							Vector<Vector<HashSet<String>>> seqscurExamp = new Vector<Vector<HashSet<String>>>();
							//used to keep the edit history
							Vector<Vector<Tuple>> seq = new Vector<Vector<Tuple>>();
							//clone the history
							for(int k = 0;k<sequences.get(j).size(); k++)
							{
								seq.add((Vector<Tuple>)sequences.get(j).get(k).clone());
							}
							//Vector<Vector<Tuple>> seq = (Vector<Vector<Tuple>>)sequences.get(j).clone();
							for(int m = 0; m<curSeqs.get(l).size();m++)
							{
								//add before and after here
								seq.get(m).add(new Tuple(curSeqs.get(l).get(m).before,curSeqs.get(l).get(m).after));
								Vector<HashSet<String>> allParams = new Vector<HashSet<String>>();
								HashSet<String> set1 = NonterminalValidator.genendendContext(curSeqs.get(l).get(m), p);
								set1.retainAll(descriptions.get(j).get(m).get(0));
								allParams.add(set1);
								HashSet<String> set2 = NonterminalValidator.genstartContext(curSeqs.get(l).get(m), p);
								set2.retainAll(descriptions.get(j).get(m).get(1));
								allParams.add(set2);
								HashSet<String> set3 = NonterminalValidator.generateWhatTemp(curSeqs.get(l).get(m), p);
								set3.retainAll(descriptions.get(j).get(m).get(2));
								allParams.add(set3);							
								HashSet<String> set4 = NonterminalValidator.genNum(curSeqs.get(l).get(m), p);
								set4.retainAll(descriptions.get(j).get(m).get(3));
								allParams.add(set4);
								HashSet<String> set5 = NonterminalValidator.genPostion(curSeqs.get(l).get(m), p, "start");
								set5.retainAll(descriptions.get(j).get(m).get(4));
								allParams.add(set5);
								HashSet<String> set6 = NonterminalValidator.genPostion(curSeqs.get(l).get(m), p, "end");
								set6.retainAll(descriptions.get(j).get(m).get(5));
								allParams.add(set6);
								HashSet<String> set7 = NonterminalValidator.genPostion(curSeqs.get(l).get(m), p, "dest");
								set7.retainAll(descriptions.get(j).get(m).get(6));
								allParams.add(set7);
								HashSet<String> set8 = NonterminalValidator.gendestContext(curSeqs.get(l).get(m), p);
								set8.retainAll(descriptions.get(j).get(m).get(7));
								allParams.add(set8);
								HashSet<String> set9 = new HashSet<String>();
								set9.add(curSeqs.get(l).get(m).oper);
								allParams.add(set9);
								NonterminalValidator.applyoper(curSeqs.get(l).get(m), p,curSeqs.get(l).get(m).oper);
								seqscurExamp.add(allParams);
							}
							if(hs.contains(seqscurExamp.toString()))
							{
								continue;
							}
							else
							{
								dcrpt.addDesc(seqscurExamp);	
								dcrpt.addSeqs(seq);
								newsign.add(RuleUtil.getSign(curSeqs.get(l)));
								hs.add(seqscurExamp.toString());
							}
						}
					}
				}		
			}
			sign = newsign;
			descriptions = dcrpt.getDesc();
			sequences = dcrpt.getSeqs();
			dcrpt.newDesc();
			dcrpt.newSeqs();
			hs.clear();//
			//descriptions = filterDescription(descriptions,sign);
		}
		dcrpt.sequences = sequences;
		dcrpt.desc = descriptions;
		//descriptions = filterDescription(descriptions,sign); // many descriptoins
		//prepare three kind of rule generator
		Vector<Vector<GrammarParseTree>> gps = new Vector<Vector<GrammarParseTree>>();
		//inite parse before all the object are initialized
		Vector<Vector<Vector<Tuple>>> seqs = dcrpt.getSeqs();
		GrammarParseTree.initGrammarParserTrees();
		int validCnt = 0;
		for(int i=0; i<dcrpt.getDesc().size(); i++)
		{
			Vector<Vector<HashSet<String>>> curSeq = dcrpt.getDesc().get(i);
			Vector<Vector<Tuple>> seq = seqs.get(i);
			// iterates through all the operations
			Vector<GrammarParseTree> tmptrees = new Vector<GrammarParseTree>();		
			boolean isvalid = true;
			for(int j=0; j<curSeq.size();j++)
			{
				Vector<HashSet<String>> oper = curSeq.get(j);
				String type = oper.get(8).iterator().next();
				
				GrammarParseTree gt = new GrammarParseTree(type);
				gt.setExample(seq.get(j));//set the before and after for this edit component
				isvalid = gt.initalSubtemplete(oper,global_temp);
				if(!isvalid)
					break;
				tmptrees.add(gt);
				//gt.diagPrint();
			}
			if(isvalid)
			{
				gps.add(tmptrees);
				System.out.println("======="+i+"/"+descriptions.size()+"===========");
				validCnt ++ ;
			}
			else
			{
				//System.out.println("==INVALID=="+i+"/"+descriptions.size()+"===========");
			}
		}
		System.out.println("Valid Count: "+validCnt);
		//generate grammar tree sequence from a sequence of descriptions			
		return gps;
	}
	// filter  descriptions
	public static void filter(Description desc)
	{
		int i = 0;
		Vector<Vector<Vector<HashSet<String>>>> dsc = desc.getDesc();
		while(i < dsc.size())
		{
			Vector<Vector<HashSet<String>>> seq = dsc.get(i);//get one sequence
			boolean isvalid = true;
			//iter through all edit operation
			for(Vector<HashSet<String>> s:seq)
			{
				if((s.get(0).size()==0&&s.get(4).size()==0)||(s.get(1).size()==0&&s.get(3).size()==0)||(s.get(6).size()==0&&s.get(7).size()==0))
				{
					isvalid = false;
					break;
				}
			}
			if(isvalid)
			{
				desc.delComponent(i);
			}
			else
			{
				i++;
			}
		}
	}
	public static void main(String[] args)
	{
		try
		{
			String fp = "/Users/bowu/Research/dataclean/data/RuleData/FullChange.csv";
			CSVReader cr = new CSVReader(new FileReader(new File(fp)),'\t');
			String x[];
			Vector<Vector<TNode>> orgs = new Vector<Vector<TNode>>();
			Vector<Vector<TNode>> tars = new Vector<Vector<TNode>>();
			String[] exmp = {"2011-07-09","07/09/2011"};
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
				//Vector<Vector<EditOper>> res = Alignment.genEditOperation(r1.vec,r2.vec);
				// prepare description for sub tree
				Vector<TNode> p = r1.vec;
				orgs.add(p);
				Vector<TNode> q = r2.vec;
				tars.add(q);
			}
			Vector<Vector<GrammarParseTree>> trees = RuleUtil.genGrammarTrees(orgs, tars);
			Vector<Vector<String>> ress = new Vector<Vector<String>>();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/Users/bowu/mysoft/restransit.txt")));
			int corrNum = 0;
			int cnt = 10;
			Vector<int[]> resut = new Vector<int[]>();
			for(int b = 1; b<21; b++)
			{
				corrNum = 0;
				for(Vector<GrammarParseTree> ts:trees)
				{
					if(ts.size()>=7)
						continue;
					cnt = 0;
					while(cnt < b*10)
					{
						String tar = exmp[0];
						Vector<String> res = new Vector<String>();
						Vector<String> ruls = new Vector<String>();
						for(GrammarParseTree t:ts)
						{
							GrammarTreeNode gn = new GrammarTreeNode("");
							t.buildTree("rule", gn);
							t.root = gn;
							String r = t.toString();
							tar = RuleUtil.applyRuleS(r, tar);
							res.add(tar);
							ruls.add(r);
							if(tar.compareTo(exmp[1])==0)
							{
								corrNum++;
							}
						}
						ress.add(res);
						bw.write(res.toString()+"\n");
						bw.write(ruls.toString()+"\n");
						cnt ++ ;
					}
				}
				int[] elem = {b*10,corrNum};
				resut.add(elem);
			}
			for(int[] xx:resut)
			{
				System.out.println(xx[0]+" "+xx[1]+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
