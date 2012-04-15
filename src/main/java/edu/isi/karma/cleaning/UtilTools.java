package edu.isi.karma.cleaning;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

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
	public static String dic2Arff(String[] dic,String s)
	{
		UtilTools.clearTmpVars();
		try
		{
			CSVWriter cw = new CSVWriter(new FileWriter(new File("./tmp/tmp.csv")),',');
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
			Vector<String> examples = new Vector<String>();
			if(s!=null && s.length()>0)
			{
				String[] z = s.split("\n");
				for(String elem:z)
				{
					if(elem.trim().length()>0)
					{
						examples.add(elem.trim());
					}
				}
			}
			for(String o:dic)
			{
				UtilTools.results.add(o);
				Vector<String> row = new Vector<String>();
				if(s!=null && o.compareTo(s)==0)
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
			Data2Features.csv2arff("./tmp/tmp.csv", "./tmp/tmp.arff");
			return "./tmp/tmp.arff";
		}
		catch(Exception e)
		{
			System.out.println(""+e.toString());
			return "";
		}
		
	}
	public static Vector<Integer> topKindexs(Vector<Double> scores, int k)
	{
		int cnt = 0;
		Vector<Integer> res = new Vector<Integer>();
		ScoreObj[] sas = new ScoreObj[scores.size()];
		for(int i= 0; i<scores.size(); i++)
		{
			sas[i] = new ScoreObj(i,scores.get(i));
		}
		Arrays.sort(sas,new DoubleCompare());
		while(cnt<k && cnt<sas.length)
		{
			res.add(sas[cnt].index);
			cnt++;
		}
		return res;
	}
	public static int rank(HashMap<String,Integer> dic,String s,String trainPath)
	{
		Set<String> keys = dic.keySet();
		String[] ks = (String[])keys.toArray(new String[keys.size()]);
		String fpath = UtilTools.dic2Arff(ks, s);
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
	public static Vector<Double> getScores(String[] res,String trainPath)
	{
		Vector<Double> vds = new Vector<Double>();
		//convert the json format to \n seperated format
		try
		{
			String[] csvres = new String[res.length];
			for(int i = 0; i<res.length; i++)
			{
				JSONObject jso = new JSONObject(res[i]);
				Iterator<String> iter = jso.keys();
				String lines ="";
				while(iter.hasNext())
				{
					lines += jso.getString(iter.next())+"\n";
				}
				csvres[i] = lines;
			}
			String fpath = UtilTools.dic2Arff(csvres, null);
			RegularityClassifer rc = new RegularityClassifer(trainPath);
			try
			{
				vds = rc.getScores(fpath);
				return vds;
			}
			catch(Exception ex)
			{
				System.out.println("get Scores error: "+ex.toString());
				return null;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Get Scores error: "+ex.toString());
		}
		return vds;
	}
}
//used to sort the score in decend order
class ScoreObj
{
	int index;
	double score;
	public ScoreObj(int index,double score)
	{
		this.index = index;
		this.score = score;
	}
	
}
class DoubleCompare implements Comparator
{
	public int compare(Object x1,Object x2)
	{
		ScoreObj a1 = (ScoreObj)x1;
		ScoreObj a2 = (ScoreObj)x2;
		if(a1.score > a2.score)
		{
			return -1;
		}
		else if(a1.score<a2.score)
		{
			return 1;
		}
		else
			return 0;
	}
}

