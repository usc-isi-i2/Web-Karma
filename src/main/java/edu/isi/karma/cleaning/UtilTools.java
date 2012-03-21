package edu.isi.karma.cleaning;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import edu.isi.karma.cleaning.features.Data2Features;
import edu.isi.karma.cleaning.features.Feature;
import edu.isi.karma.cleaning.features.RegularityFeatureSet;

import au.com.bytecode.opencsv.CSVWriter;

public class UtilTools {
	public static Vector<String> results = new Vector<String>();
	public static int index = 0;
	public static void clearTmpVars()
	{
		results.clear();
		index = 0;
	}
	//s is the transformed result,seperated by \n
	//dic contain the former results and their times
	//tar is the finally result
	//output the arff file 3 indicates the correct ones otherwise else 
	public static void clusterResult(String s, HashMap<String,Integer> dic)
	{
		if(dic.containsKey(s))
		{
			dic.put(s, dic.get(s)+1);
		}
		else
		{
			dic.put(s, 1);
		}
	}
	public static String dic2Arff(HashMap<String,Integer> dic,String s)
	{
		UtilTools.clearTmpVars();
		try
		{
			CSVWriter cw = new CSVWriter(new FileWriter(new File("./tmp.csv")),',');
			//write header into the csv file
			Vector<String> tmp = new Vector<String>();
			Vector<String> tmp1 = new Vector<String>();
			RegularityFeatureSet rfs = new RegularityFeatureSet();
			Collection<Feature> cols = rfs.computeFeatures(tmp, tmp1);
			String[] xyz = new String[rfs.fnames.size()+1];
			for(int i = 0; i<xyz.length-1; i++)
			{
				xyz[i] ="a_"+i;
			}
			xyz[xyz.length-1] = "label";
			cw.writeNext(xyz);
			//write the data
			Set<String> ss = dic.keySet();
			Iterator<String> iter = ss.iterator();
			Vector<String> examples = new Vector<String>();
			String[] z = s.split("\n");
			for(String elem:z)
			{
				if(elem.trim().length()>0)
				{
					examples.add(elem.trim());
				}
			}
			while(iter.hasNext())
			{
				String o = iter.next();
				UtilTools.results.add(o);
				Vector<String> row = new Vector<String>();
				if(o.compareTo(s)==0)
				{
					RegularityFeatureSet rf = new RegularityFeatureSet();
					Vector<String> oexamples = new Vector<String>();
					String[] y = o.split("\n");
					for(String elem:y)
					{
						if(elem.trim().length()>0)
						{
							oexamples.add(elem.trim());
						}
					}
					Collection<Feature> cf = rf.computeFeatures(oexamples,examples);
					Feature[] x = cf.toArray(new Feature[cf.size()]);
					//row.add(f.getName());
					for(int k=0;k<cf.size();k++)
					{
						row.add(String.valueOf(x[k].getScore()));
					}
					row.add("3"); // change this according to the dataset.
				}
				else
				{
					RegularityFeatureSet rf = new RegularityFeatureSet();
					Vector<String> oexamples = new Vector<String>();
					String[] y = o.split("\n");
					for(String elem:y)
					{
						if(elem.trim().length()>0)
						{
							oexamples.add(elem.trim());
						}
					}
					Collection<Feature> cf = rf.computeFeatures(oexamples,examples);
					Feature[] x = cf.toArray(new Feature[cf.size()]);
					//row.add(f.getName());
					for(int k=0;k<cf.size();k++)
					{
						row.add(String.valueOf(x[k].getScore()));
					}
					row.add("0"); // change this according to the dataset.

				}
				cw.writeNext((String[])row.toArray(new String[row.size()]));
			}
			cw.flush();
			cw.close();
			Data2Features.csv2arff("./tmp.csv", "./tmp.arff");
			return "./tmp.arff";
		}
		catch(Exception e)
		{
			System.out.println(""+e.toString());
			return "";
		}
		
	}
	public static int rank(HashMap<String,Integer> dic,String s,String trainPath)
	{
		String fpath = UtilTools.dic2Arff(dic, s);
		RegularityClassifer rc = new RegularityClassifer(trainPath);
		try
		{
			int rank = rc.getRank(fpath);
			if(rank < 0)
			{
				return -1;
			}
			else
				return rank;
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
			return -1;
		}
	}
}
