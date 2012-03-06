package edu.isi.karma.cleaning;

import java.util.ArrayList;

public class GrammarTree {
	public GTNode root;
	private int size;
	public GrammarTree(GTNode e)
	{
		root = e;
		//traversal the tree 
		
	}
	public GTNode randomChoose()
	{
		return null;
	}
	public void setSize(int a)
	{
		size = a;
	}
	public int getNontermSize()
	{
		return size;
	}

}

class GTNode{
	private String nonterm;
	private int choice; // the No of the production used for this non-terminal
	public ArrayList<GTNode> children;
	public GTNode(String a,int b)
	{
		nonterm = a;
		choice = b;
		children = new ArrayList<GTNode>();
	}
	public void addChildren(GTNode t)
	{
		children.add(t);
	}
	public void replaceChildren(int i,GTNode t)
	{
		children.remove(i);
		children.add(i, t);
	}
	public void setName(String a)
	{
		nonterm = a;
	}
	public void setNum(int b)
	{
		choice = b;
	}
	
	public String getName()
	{
		return nonterm;
	}
	public int getNum()
	{
		return choice;
	}
	
}
