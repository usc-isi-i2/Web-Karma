package edu.isi.karma.cleaning.Research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MultiIndex {
	public String value = "";
	public ArrayList<MultiIndex> children = new ArrayList<MultiIndex>();
	public MultiIndex()
	{
		
	}
	public void add(List<String> keys, String v)
	{
		if(keys.size() <= 0)
		{
			MultiIndex m = new MultiIndex();
			m.value = v;
			this.children.add(m);
			return;
		}
		else
		{
			String key = keys.get(0);
			List<String> rkeys = keys.subList(1, keys.size());
			//check if any key is the same
			boolean isfind = false;
			for(MultiIndex m: children)
			{
				if(m.value.compareTo(key)==0)
				{
					m.add(rkeys, v);
					isfind = true;
				}
			}
			if(!isfind)
			{
				MultiIndex multiIndex = new MultiIndex();
				multiIndex.value = key;
				multiIndex.add(rkeys, v);
				this.children.add(multiIndex);
			}
		}
	}
	public void delete(ArrayList<String> keys)
	{
		
	}
	public List<String> get(List<String> keys)
	{
		//get to the root node
		MultiIndex rootNode = null;
		MultiIndex tmpNode = this;
		for(String k:keys)
		{
			boolean isfind = false;
			for(MultiIndex m:tmpNode.children)
			{
				if(m.value == k)
				{
					tmpNode = m;
					isfind = true;
					break;
				}
			}
			if(isfind)
			{
				rootNode = tmpNode;
			}
		}
		ArrayList<String> res = new ArrayList<String>();
		if(rootNode == null)
			return res;
		//retrive all the leaf ndoe of the root node
		getAllLeaf(rootNode, res);
		return res;
	}
	public void getAllLeaf(MultiIndex m, ArrayList<String> res)
	{
		if(m.children.size() == 0)
		{
			res.add(m.value);
		}
		else
		{
			for(MultiIndex iter: m.children)
			{
				getAllLeaf(iter, res);
			}
		}
	}
	public String toString()
	{
		String res = "";
		res += value;
		res+= "(";
		for(MultiIndex i:children)
		{
			res+= " "+i.value;
		}
		res += ")";
		return res;
	}
	public List<String> getPathes()
	{
		String s = "";
		ArrayList<String> ps = new ArrayList<String>();
		this.travers(s, this, ps);
		return ps;
	}
	public void travers(String s, MultiIndex node, ArrayList<String> paths)
	{
		if(node.children.size() == 0)
		{
			String x = s+","+node.value;
			paths.add(x);
		}
		else
		{
			for(MultiIndex m: node.children)
			{
				String xString = s+","+node.value;
				node.travers(xString, m, paths);
			}
		}
	}
	public void testMultiIndex()
	{
		String[] keys1 = {"e1", "segmentloopsegment", "1"};
		String[] keys2 = {"e1", "segmentloopsegment", "2"};
		String[] keys3 = {"e2", "segmentloopsegment", "2"};
		String[] keys4 = {"e3", "segmentloopsegment", "1"};
		MultiIndex multiIndex = new MultiIndex();
		multiIndex.add(Arrays.asList(keys1), "p1");
		multiIndex.add(Arrays.asList(keys2), "p2");
		multiIndex.add(Arrays.asList(keys3), "p1");
		multiIndex.add(Arrays.asList(keys4), "p1");
		System.out.println(""+multiIndex.get(Arrays.asList(keys1)));		
	}

}
