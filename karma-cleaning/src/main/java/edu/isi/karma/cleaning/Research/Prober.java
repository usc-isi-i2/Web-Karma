package edu.isi.karma.cleaning.Research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import edu.isi.karma.cleaning.Partition;
import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;
import edu.isi.karma.cleaning.Patcher;

public class Prober {
	public static ArrayList<String> track1 = new ArrayList<String>();
	public static String target;
	public static long adaptedProg = 0;
	public static MultiIndex records = new MultiIndex();
	public static String PartitionDisplay1(Vector<Partition> vp) {
		String res = "";
		for (Partition p : vp) {
			res += "Partition: " + p.label + "\n";
			for (int i = 0; i < p.tarNodes.size(); i++) {
				String line = UtilTools.print(p.orgNodes.get(i)) + "|"
						+ UtilTools.print(p.tarNodes.get(i));
				res += line + "\n";
			}
		}
		return res;
	}
	public static void trackProgram(Vector<Partition> pars, ProgramRule r)
	{
		for(Partition p: pars)
		{
			String rule = r.getStringRule(p.label);
			for(Vector<TNode> nodes: p.orgNodes)
			{
				String exp = UtilTools.print(nodes);
				String[] keys = {exp};
				records.add(Arrays.asList(keys), rule);
			}
		}
	}
	public static void displayProgram()
	{
		List<String> pList = records.getPathes();
		for(String line: pList)
		{
			System.out.println(""+line);
		}
	}
	public static void tracePatchers(String ptree,ArrayList<Patcher> errNodes, ArrayList<String[]> iexps, String[] exp, boolean valid,HashMap<String, String> dics)
	{
		System.out.println("========Programs to fix==========");
		System.out.println("Program: "+ptree);
		String res = "";
		for(String[] x: iexps)
		{
			res += String.format("%s   %s\n", x[0], x[1]);
		}
		
		for(String k:dics.keySet())
		{
			System.out.println("dict: "+String.format("%s, %s", k, dics.get(k)));
		}
		for(Patcher p:errNodes)
		{
			System.out.println("old Exp: \n"+res);
			System.out.println("new Exp: "+ String.format("%s  %s", exp[0],exp[1]));
			System.out.println("old subs: "+p.programNodes);
			System.out.println("new subs: "+p.replaceNodes);
			if(!valid)
				System.out.println("Unsuccessful adaptation");
			else{
				adaptedProg ++;
			}
		}
		System.out.println("=================================");
	}
}
