package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class Evaluator {
	Ruler r;
	public Vector<String> pos;
	public Vector<String> cht;
	public Evaluator()
	{
		pos=new Vector<String>();
		cht = new Vector<String>();
	}
	public void addPos(Vector<Integer> xpos)
	{
		for(int i = 0; i< xpos.size() ; i++)
		{
			pos.add(String.valueOf(xpos.get(i)));
		}
	}
	public void addCnt(Vector<TNode> x)
	{
		for(int i = 0; i< x.size() ; i++)
		{
			cht.add(x.get(i).text);
		}
	}
	public void setRuler(Ruler r)
	{
		this.r = r;
	}
	
	public double calShannonEntropy(Vector<String> data)
	{
		double entropy = 0;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(int i = 0; i< data.size(); i++)
		{
			String key = data.get(i);
			if(map.containsKey(key))
			{
				map.put(key, map.get(key)+1);
			}
			else
			{
				map.put(key, 1);
			}
		}
		// compute the entropy
		Set<String> keys = map.keySet();
		Iterator<String> ptr = keys.iterator();
		while(ptr.hasNext())
		{
			double freq = (double)(map.get(ptr.next()))/data.size();
			entropy -= freq*Math.log(freq);
		}
		return entropy;
	}
	public double score(double e1, double e2)
	{
		return Math.min(e1, e2);
	}

}
