package edu.isi.karma.cleaning;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.debug.ParseTreeBuilder;

import edu.isi.karma.cleaning.changed_grammar.RuleInterpreterLexer;

public class ReducedGrammar {
	// use to store all variations of the parse tree
	public HashMap<String,ArrayList<ArrayList<String>>> nonterminals;
	private String gtype = "";
	public ReducedGrammar(String type,String gfpath)
	{
		nonterminals = new HashMap<String,ArrayList<ArrayList<String>>>();
		gtype =type;
		initTerminals(type);
		init(gfpath);
	}
	//gf path specify the current grammar file's path
	public void init(String gfpath)
	{
		try
		{
			String fname = gfpath;
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
	        this.nonterminals = gen.nonterminals;
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
	// inite the value for those terminals for the given type of grammar
	public void initTerminals(String type)
	{
		
	}
	//add a new example which would generate the reduced grammar
	//org is original tokensequence 
	//tar is the transformed tokensequence
	//opers are the operations done with the position information and token sequence information.
	//these operations are equivalent  to each other.
	public void addExample(Vector<TNode> org,Vector<TNode> tar,Vector<EditOper> opers)
	{
	}
	//converst parse tree into arrayList<ArrrayList<String>>
	//should be called when parsering the tree
	public void handletokenspec(Vector<TNode> sequence,String nonterm)
	{
		try
		{
			CharStream cs =  new ANTLRStringStream(sequence.toString());
			Ruler r = new Ruler();
			ParseTreeBuilder builder = new ParseTreeBuilder("tokenspec");//only need to obtain the parse tree for tokenspec non-terminal
			RuleInterpreterLexer lexer = new RuleInterpreterLexer(cs);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	       // RuleInterpreterParser parser= new RuleInterpreterParser(tokens,builder);
	      //  CommonTreeNodeStream nodes = new CommonTreeNodeStream((CommonTree)parser.tokenspec().getTree());
	        //RuleInterpreterTree evaluator = new RuleInterpreterTree(nodes);
	        
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
