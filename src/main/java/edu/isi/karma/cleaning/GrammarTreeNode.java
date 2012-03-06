package edu.isi.karma.cleaning;

import java.util.Vector;

public class GrammarTreeNode{
	GrammarTreeNode parent = null;
	Vector<GrammarTreeNode> children = new Vector<GrammarTreeNode>();
	String name = "";
	public GrammarTreeNode(String n)
	{
		name = n;
	}
	public void setParent(GrammarTreeNode parent)
	{
		this.parent = parent;
	}
	public void addChild(GrammarTreeNode e)
	{
		this.children.add(e);
	}
	public String toString()
	{
		return this.name;
	}
	
}
