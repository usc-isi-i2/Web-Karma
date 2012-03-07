package edu.isi.karma.cleaning;

import java.util.HashSet;
import java.util.Vector;
class Tuple
{
	//(beforetoks, aftertoks)
	private Vector<Vector<TNode>> tuple = new Vector<Vector<TNode>>();
	public Tuple(Vector<TNode> bef,Vector<TNode> aft)
	{
		tuple.add(bef);
		tuple.add(aft);
	}
	public Vector<TNode> getBefore()
	{
		return tuple.get(0);
	}
	public Vector<TNode> getafter()
	{
		return tuple.get(1);
	}
}
public class Description 
{
	public Vector<Vector<Vector<HashSet<String>>>> desc = new Vector<Vector<Vector<HashSet<String>>>>();
	//((tup11,tup21),(tup12,tup22)....)
	public Vector<Vector<Vector<Tuple>>> sequences = new Vector<Vector<Vector<Tuple>>>();
	public Description()
	{
	}
	public Description(Vector<Vector<Vector<HashSet<String>>>> desc)
	{
		this.desc = desc;
	}
	public void delComponent(int index)
	{
		desc.remove(index);
		sequences.remove(index);
	}
	public Vector<Vector<Vector<HashSet<String>>>> getDesc()
	{
		return desc;
	}
	public Vector<Vector<Vector<Tuple>>> getSeqs()
	{
		return this.sequences;
	}
	
	public void addSeqs(Vector<Vector<Tuple>> seq)
	{
		sequences.add(seq);
	}
	public void addDesc(Vector<Vector<HashSet<String>>> seq)
	{
		desc.add(seq);
	}
	public void newDesc()
	{
		this.desc = new Vector<Vector<Vector<HashSet<String>>>>();
	}
	public void newSeqs()
	{
		this.sequences = new Vector<Vector<Vector<Tuple>>>();
	}
}
