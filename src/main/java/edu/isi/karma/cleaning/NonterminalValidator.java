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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

import edu.isi.karma.cleaning.changed_grammar.templateParserLexer;
import edu.isi.karma.cleaning.changed_grammar.templateParserParser;

public class NonterminalValidator 
{
	//
	public static void applyoper(EditOper op,Vector<TNode> ex,String type)
	{
		if(type.compareTo("mov")==0)
		{
			applymov(op,ex);
		}
		else if(type.compareTo("ins")==0)
		{
			applyins(op,ex);
		}
		else if(type.compareTo("del")==0)
		{
			applydel(op,ex);
		}
	}
	public static void applymov(EditOper op, Vector<TNode> ex)
	{
		Vector<TNode> excpy = (Vector<TNode>)ex.clone();
		if(op.dest >= op.endPos)
		{
			List<TNode> tmp = excpy.subList(op.starPos, op.endPos+1);
			ex.removeAll(tmp);
			ex.addAll(op.dest-tmp.size(), tmp);
		}
		else if(op.dest<= op.starPos)
		{
			List<TNode> tmp = excpy.subList(op.starPos, op.endPos+1);
			ex.removeAll(tmp);
			ex.addAll(op.dest, tmp);
		}
		else
		{
			System.out.println("applymov error");
		}
	}
	public static void applyins(EditOper op,Vector<TNode> ex)
	{
		ex.addAll(op.dest,op.tar);
	}
	public static void applydel(EditOper op,Vector<TNode> ex)
	{
		Vector<TNode> excpy = (Vector<TNode>)ex.clone();
		List<TNode> tmp = excpy.subList(op.starPos, op.endPos+1);
		ex.removeAll(tmp);
	}
	//generate the what string for parsing
	public static HashSet<String> generateWhatTemp(EditOper ops,Vector<TNode> ex)
	{
		
		HashSet<String> rules = new HashSet<String>();
		int size = 0;
		List<TNode> target;
		if(ops.tar.size()!=0)
		{
			target = ops.tar;
		}
		else
		{
			target = ex.subList(ops.starPos, ops.endPos+1);
		}
		size = target.size();
		//shift operation to find whether a positon is 1
		// 1 means general 0 mean use token
		int threshold = (int)Math.pow(2, size-1);
		for(int i=0;i<=Math.pow(2, size);i++)
		{
			String rule = "";
			for(int j=0;j<size;j++)
			{
				int stat = i>>j;
				if(stat%2==0)
				{
					rule += ""+target.get(j).getType()+"";
				}
				else
				{
					rule += "\""+target.get(j).text+"\"";
				}
			}
			if(rule.length()!=0)
				rules.add(rule);
		}
		//rules.add("ANYTOK");
		return rules;
	}
	public static GrammarTreeNode getTemplate(String symbol,String subrule,HashMap<String,GrammarTreeNode> subtemples)
	{
		String index = symbol+subrule;
		if(subtemples.containsKey(index))
		{
			return subtemples.get(index);
		}
		else
			return null;
		
	}
	//translate the string into parse tree and add them into the template store
	public static void genTemplate(String symbol,String subrule, HashMap<String,GrammarTreeNode> subtemples)
	{
		String key = symbol+subrule;
		try
		{
			CharStream cs =  new ANTLRStringStream(subrule);
			templateParserLexer lexer = new templateParserLexer(cs);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        templateParserParser parser= new templateParserParser(tokens);
	        GrammarTreeNode root;
	        if(symbol.compareTo("qnum")==0)
	        {
	        		root = parser.qnum().tok;
	        }
	        else if(symbol.compareTo("snum")==0)
	        {
	        		root = parser.snum().tok;
	        }
	        else if(symbol.compareTo("tnum")==0)
	        {
	        		root = parser.tnum().tok;
	        }
	        else if(symbol.compareTo("tokenspec")==0)
	        {
	        		root = parser.tokenspec().tok;
	        }
	        else if(symbol.compareTo("stokenspec")==0)
	        {
	        		root = parser.stokenspec().tok;
	        }
	        else if(symbol.compareTo("etokenspec")==0)
	        {
	        		root = parser.etokenspec().tok;
	        }
	        else if(symbol.compareTo("dtokenspec")==0)
	        {
	        		root = parser.dtokenspec().tok;
	        }
	        else if(symbol.compareTo("dnum")==0)
	        {
	        		root = parser.dnum().tok;
	        }
	        else
	        {
	        		root = null;
	        }
	        if(root!=null)
	        {
		        if(subtemples.containsKey(key))
		        		return;
		        else
		        {
		        		subtemples.put(key,root);
		        }
	        }
		}
		catch(Exception x)
		{
			System.out.println(""+x.toString());
		}
	}
	//generate the tokenspec for start end and denstination current key cannot differentiate them
	public static HashSet<String> genstartContext(EditOper ops,Vector<TNode> ex)
	{
		HashSet<String> rules = new HashSet<String>();
		//startPosition
		int spos = ops.starPos;
		if(spos== -1)
		{
			return rules;
		}
		// add the -1 0 +1 three position's token content
		for(int k= -1; k<=1;k++)
		{
			
			if(spos+k>=0&&spos+k<ex.size())
			{
				if(ex.get(spos+k).text.length()>0)
					rules.add("\""+ex.get(spos+k).text+"\"");
				rules.add(""+ex.get(spos+k).getType()+"");
			}
		}
		return rules;
		
	}
	//generate the tokenspec for start end and denstination current key cannot differentiate them
	public static HashSet<String> gendestContext(EditOper ops,Vector<TNode> ex)
	{
		HashSet<String> rules = new HashSet<String>();
		//startPosition
		int dpos = ops.dest;
		if(dpos== -1)
		{
			return rules;
		}
		// add the -1 0 +1 three position's token content
		for(int k= -1; k<=1;k++)
		{
			
			if(dpos+k>=0&&dpos+k<ex.size())
			{
				if(ex.get(dpos+k).text.length()>0)
					rules.add("\""+ex.get(dpos+k).text+"\"");
				rules.add(""+ex.get(dpos+k).getType()+"");
			}
		}
		return rules;
		
	}
	public static HashSet<String> genPostion(EditOper ops, Vector<TNode> ex, String type)
	{
		HashSet<String> pos = new HashSet<String>();
		if(type.compareTo("dest")==0)
		{
			if(ops.dest+1 >=0 && ops.dest+1<=ex.size())
			{
				pos.add(String.valueOf(ops.dest+1));
			}
			if(ex.size()-ops.dest-1 >=0)
			{
				pos.add(String.valueOf(ex.size()-ops.dest-1));
			}
		}
		else if(type.compareTo("start")==0)
		{
			if(ops.starPos+1 >=0 && ops.starPos+1<=ex.size())
			{
				pos.add(String.valueOf(ops.starPos+1));
			}
			if(ex.size()-ops.starPos-1 >=0)
			{
				pos.add(String.valueOf(ex.size()-ops.starPos-1));
			}
		}
		else if(type.compareTo("end")==0)
		{
			if(ops.endPos+1 >=0 && ops.endPos+1<=ex.size())
			{
				pos.add(String.valueOf(ops.endPos+1));
			}
			if(ex.size()-ops.endPos-1 >=0)
			{
				pos.add(String.valueOf(ex.size()-ops.endPos-1));
			}
		}
		return pos;
	}
	public static HashSet<String> genendendContext(EditOper ops,Vector<TNode> ex)
	{
		HashSet<String> rules = new HashSet<String>();
		int epos = ops.endPos;
		if( epos == -1)
		{
			return rules;
		}
		//endPosition
		for(int k= -1; k<=1;k++)
		{
			
			if(epos+k>=0&&epos+k<ex.size())
			{
				if(ex.get(epos+k).text.length()>0)
					rules.add("\""+ex.get(epos+k).text+"\"");
				rules.add(""+ex.get(epos+k).getType()+"");
			}
		}
		return rules;
		
	}
	//generate the number for quantity
	public static HashSet<String> genNum(EditOper ops,Vector<TNode> ex)
	{
		HashSet<String> rules = new HashSet<String>();
		int size = 0;
		/*if(ops.tar.size()!=0)
		{
			size = ops.tar.size();
		}
		else
		{
			size = ops.endPos-ops.starPos+1;
		}
		rules.add(String.valueOf(size));*/
		rules.add("1");
		return rules;
	}
	public static void main(String[] args)
	{
		
	}
	/******************************************************************/
	public static boolean CheckRule(Vector<EditOper> ops,String rule)
	{
		for(EditOper o:ops)
		{
			String af = RuleUtil.applyRuleS(rule, o.before.toString());
			if(af.compareTo(o.after.toString())!=0)
			{
				return false;
			}
		}
		return true;
	}
	public static Vector<Vector<String>> getLiteralValue(Vector<EditOper> ops,Vector<Vector<TNode>> orgexample,RuleGenerator rgen)
	{
		Vector<Vector<String>> x = new Vector<Vector<String>>();
		//check whether can be generalized
		String y = ops.get(0).oper;
		for(int i = 0; i<ops.size();i++)
		{
			if(y.compareTo(ops.get(i).oper)!=0)
			{
				System.out.println("The operations cannot be generalized");
				System.exit(1);
			}
		}
		Vector<String> wToken = new Vector<String>();
		Vector<String> wquan = new Vector<String>();
		Vector<String> p1t = new Vector<String>();
		Vector<String> p1 = new Vector<String>();
		Vector<String> p2t = new Vector<String>();
		Vector<String> p2 = new Vector<String>();
		Vector<String> p3t = new Vector<String>();
		Vector<String> p3 = new Vector<String>();
		//need to get the value for quan,what,pos1 pos2 pos3
		
		int quan = ops.get(0).tar.size();
		Vector<TNode> what = new Vector<TNode>();
		what = ops.get(0).tar;
		int pos1 = ops.get(0).starPos;
		int pos2 = ops.get(0).endPos;
		int pos3 = ops.get(0).dest;
		Vector<String > woken = rgen.replacetokspec(rgen.printRules("tokenspec",(quan)),orgexample.get(0) , ops.get(0).starPos, ops.get(0).endPos);
		wToken.addAll(woken);
		//store the context token to identify the location of a key position
		for(int k = -1; k<=1;k++)
		{
			if(pos1 != -1 && pos1+k>=0 && pos1+k<orgexample.get(0).size())
			{
				p1t.add(orgexample.get(0).get(pos1+k).text);
			}
			if(pos2 != -1 && pos2+k>=0 && pos2+k<orgexample.get(0).size())
			{
				p2t.add(orgexample.get(0).get(pos2+k).text);
			}
			if(pos3 != -1 && pos3+k>=0 && pos3+k<orgexample.get(0).size())
			{
				p3t.add(orgexample.get(0).get(pos3+k).text);
			}
		}
		for(int i=0;i<ops.size();i++)
		{
			EditOper eo = ops.get(i);
			if(eo.tar.size()!=quan)
			{
				quan = -1; // -1 means anynumber
			}
			if(pos1!=eo.starPos)
			{
				pos1 = -1;// -1 means non consistency
			}
			if(pos2 != eo.endPos)
			{
				pos2 = -1;// -1 means no consistency
			}
			// generalized the token sequence
			Vector<String> wTokenx = rgen.replacetokspec(rgen.printRules("tokenspec",(eo.tar.size())),orgexample.get(i) , ops.get(i).starPos, ops.get(i).endPos);
			wToken.retainAll(wTokenx);			
		}
		//
		if(quan != -1)
		{
			wquan.add(String.valueOf(quan));
		}
		if(pos1 != -1)
		{
			p1.add(String.valueOf(pos1));
		}
		if(pos2 != -1)
		{
			p2.add(String.valueOf(quan));
		}
		if(pos3 != -1)
		{
			p3.add(String.valueOf(quan));
		}
		x.add(wToken);
		x.add(wquan);
		x.add(p1);
		x.add(p1t);
		x.add(p2);
		x.add(p2t);
		x.add(p3);
		x.add(p3t);
		return x;
	}
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
	//0 wnum 1 wtoken
	//2 num 3 token
	//4 num 5 token
	//6 num 7 token
	public static Vector<String> assignValue(String rule,Vector<Vector<String>> vs)
	{
		Vector<String> wtokens = new Vector<String>();
		Vector<String> wNum = new Vector<String>();
		Vector<String> p1 = new Vector<String>();
		Vector<String> p1t = new Vector<String>();
		Vector<String> p2 = new Vector<String>();
		Vector<String> p2t = new Vector<String>();
		Vector<String> p3 = new Vector<String>();
		Vector<String> p3t = new Vector<String>();
		if(rule.indexOf("mov")==0)
		{
			wtokens = vs.get(0);
			wNum = vs.get(1);
			p1 = vs.get(2);
			p1t = vs.get(3);
			p2 = vs.get(4);
			p2t = vs.get(5);
			p3 = vs.get(6);
			p3t = vs.get(7);
		}
		else if(rule.indexOf("ins")==0)
		{
			wtokens = vs.get(0);
			wNum = vs.get(1);
			p1 = vs.get(2);
			p1t = vs.get(3);
		}
		else if(rule.indexOf("del")==0)
		{
			wtokens = vs.get(0);
			wNum = vs.get(1);
			p1 = vs.get(2);
			p1t = vs.get(3);
			p2 = vs.get(4);
			p2t = vs.get(5);
		}
		Vector<String> rules = new Vector<String>();
		Vector<String> resrules = new Vector<String>();
		rules.add(rule);
		//find three positions
		int pos1 = rule.indexOf("from");
		int pos2 = rule.indexOf("from", pos1+1);
		if(pos2 == -1)
			pos2 = rule.length();
		int pos3 = rule.indexOf("from",pos2+1);
		if(pos3 == -1)
			pos3 = rule.length();
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
					Vector<String> res = substitueRuleVar(fr,p1,"NUM");
					rules.addAll(res);
					continue;
				}
				else if(p>pos2&&p<pos3)
				{
					Vector<String> res = substitueRuleVar(fr,p2,"NUM");
					rules.addAll(res);
					continue;
				}
				else if(p>pos3)
				{
					Vector<String> res = substitueRuleVar(fr,p3,"NUM");
					rules.addAll(res);
					continue;
				}
			}
			if((p = fr.indexOf("TOKEN"))!= -1)
			{
				if(p<pos1)
				{
					Vector<String> res = substitueRuleVar(fr,wtokens,"TOKEN");
					rules.addAll(res);
					continue;
				}
				else if(p>pos1 && p<pos2)
				{
					Vector<String> res = substitueRuleVar(fr,p1t,"TOKEN");
					rules.addAll(res);
					continue;
				}
				else if(p>pos2 && p<pos3)
				{
					Vector<String> res = substitueRuleVar(fr,p2t,"TOKEN");
					rules.addAll(res);
					continue;
				}
				else if(p> pos3)
				{
					Vector<String> res = substitueRuleVar(fr,p3t,"TOKEN");
					rules.addAll(res);
					continue;
				}
			}
		}
		return resrules;
	}

}
