package edu.isi.karma.cleaning.Research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.isi.karma.cleaning.Program;
// input: classifier, results for each record
//output: a refined classifier

public class ClassifierRefiner {
	Program prog;
	HashMap<String, HashMap<String,String>> uData;
	HashMap<String, ArrayList<String>> clusters = new HashMap<String, ArrayList<String>>();
	public ClassifierRefiner(Program prog, HashMap<String, HashMap<String,String>> uData)
	{
		this.prog = prog;
		this.uData = uData;
	}
	public HashSet<String> selectUdata()
	{
		HashSet<String> idSet = new HashSet<String>();
		
		return idSet;
	}
	//auxiliary functions
	public void clusterUdata()
	{
		for(Map.Entry<String, HashMap<String, String>> stringHashMapEntry : uData.entrySet())
		{
			HashMap<String, String> dict = stringHashMapEntry.getValue();
			String ckey = dict.get("class");
			String org = dict.get("Org");
			if(clusters.containsKey(ckey))
			{
				clusters.get(ckey).add(org);
			}
			else
			{
				ArrayList<String> xArrayList = new ArrayList<String>();
				xArrayList.add(org);
				clusters.put(ckey, xArrayList);
			}
		}
		//tostring
		for(Map.Entry<String, ArrayList<String>> stringArrayListEntry : clusters.entrySet())
		{
			System.out.println("====="+ stringArrayListEntry.getKey() +"=======");
			for(String val: stringArrayListEntry.getValue())
			{
				System.out.println(String.format("\"%s\", \"%s\"", val, stringArrayListEntry.getKey()));
			}
		}
	}
}
