package edu.isi.karma.cleaning;

import java.util.Vector;
//a Class aims to store the parameter for edit operations
public class EditOper{
	public String oper="";
	public int starPos=-1;
	public int endPos=-1;
	public int dest = -1;
	public Vector<TNode> tar = new Vector<TNode>();
	public Vector<TNode> before = new Vector<TNode>();
	public Vector<TNode> after = new Vector<TNode>();
	public EditOper()
	{
		
	}
	public String toString()
	{
		return oper+": "+starPos+","+endPos+","+dest+tar.toString();
	}
}
