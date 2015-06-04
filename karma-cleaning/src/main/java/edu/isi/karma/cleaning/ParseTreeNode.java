package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTreeNode {
	nodetype type = nodetype.root;
	String value = "";
	String tmpEval = "";
	public static enum nodetype {
	    loop,segment, position,root
	}
	ParseTreeNode parent;
	ArrayList<ParseTreeNode> children = new ArrayList<ParseTreeNode>();
	public ParseTreeNode(nodetype type, String value)
	{
		this.type = type;
		this.value = value;
	}
	public void addChildren(ParseTreeNode node)
	{
		this.children.add(node);
		//this.value = ""; //need to update based on 
		node.parent = this;
	}
	public ArrayList<ParseTreeNode> getChildren()
	{
		return this.children;
	}
	//convert the tree back to program
	public String toProgram()
	{
		String prog = "";
		if(this.type == nodetype.root)
		{
			for(ParseTreeNode node:this.children)
			{
				prog+= node.toProgram()+"+";
			}
			if(prog.length() > 0)
			{
				prog =  prog.substring(0,prog.length()-1);
			}
		}
		else if(this.type == nodetype.segment)
		{
			if(this.children.size()==0)
			{
				prog = this.value;
			}
			else
			{
				String template = "";
				String regex = "([a-zA-Z]+)\\(.+\\)";
				Matcher matcher = Pattern.compile(regex).matcher(prog);
				if (matcher.find())
					template = matcher.group(1)+"(substr(value, %s, %s))";
				else{
					template = "substr(value, %s, %s)";
				}
				prog = String.format(template, this.children.get(0).value,this.children.get(1).value);
			}
		}
		return prog;
	}
	public String toString()
	{
		String root = value+"(";
		String res = "";
		for(ParseTreeNode node: children)
		{
			res += node.toString();
		}
		return root+res+")";
	}
	//evaluate the current subexpression on input
	public String eval(String input)
	{
		try
		{
		ProgramRule ptmp = new ProgramRule(this.value);
		String res = "";
		res = ptmp.transform(input);
		//recursively evaluate the children nodes.
		for(ParseTreeNode ptn: this.children)
		{
			ptn.eval(input);
		}
		this.tmpEval = res;
		return res;
		}
		catch(Exception e)
		{
			return "";
		}
	}
}
