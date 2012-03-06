package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Test {
	public static HashMap<String, ArrayList<ArrayList<String>>> deepclone(HashMap<String, ArrayList<ArrayList<String>>> x)
	{
		HashMap<String, ArrayList<ArrayList<String>>> nx = new HashMap<String, ArrayList<ArrayList<String>>>();
		Iterator<String> iter = x.keySet().iterator();
		while(iter.hasNext())
		{
			String key = iter.next();
			ArrayList<ArrayList<String>> value = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> copy = x.get(key);
			for(int i = 0; i<copy.size(); i++)
			{
				ArrayList<String> xy = (ArrayList<String>)copy.get(i).clone();
				value.add(xy);
			}
			nx.put(key, value);
		}
		return nx;
	}
	public static void main(String[] args)
	{
		HashMap<String, ArrayList<ArrayList<String>>> a = new HashMap<String, ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> aas = new ArrayList<ArrayList<String>>();
		ArrayList<String> x = new ArrayList<String>();
		x.add("1");
		x.add("2");
		aas.add(x);
		ArrayList<ArrayList<String>> bbs = new ArrayList<ArrayList<String>>();
		ArrayList<String> y = new ArrayList<String>();
		y.add("a");
		y.add("b");
		bbs.add(y);
		a.put("a", aas);
		a.put("b", bbs);
		HashMap<String, ArrayList<ArrayList<String>>> t = Test.deepclone(a);
		t.get("a").get(0).add("3");
		System.out.println("really");
	}
}
