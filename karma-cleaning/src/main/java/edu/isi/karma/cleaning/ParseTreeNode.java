package edu.isi.karma.cleaning;

import java.util.ArrayList;

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
	//convert the tree back to program, only need to be used on the root node
	//not work if only the position expression is updated
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
				prog = String.format("substr(value,%s,%s)", this.children.get(0).value,this.children.get(1).value);
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
