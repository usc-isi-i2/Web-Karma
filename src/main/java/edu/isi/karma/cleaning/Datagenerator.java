/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.cleaning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

//used for generate a larger bunch of training data
public class Datagenerator {

	//fpath raw data
	//rule:right rule
	//output the csv file
	public String generateTruth(String fpath, String rule)
	{
		return RuleUtil.applyRule(rule, fpath);
	}
	public String generateRules(Vector<String[]> examples)
	{
		try
		{
			return RuleUtil.genRule(examples).get(0);
		}
		catch(Exception e)
		{
			System.out.println(""+e.toString());
			return "";
		}
	}
	//fpath is the csv file contain the raw the correct result
	//rulefile all the rules
	//output the wrost result for this dataset
	public void chooseWrost(String fpath, String rulefile)
	{
		try {
			ArrayList<String> worst = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(new File(rulefile)));
			String rule = "";
			CSVReader cr = new CSVReader(new FileReader(new File(fpath)),'\t');
			List<String[]> pairs = cr.readAll();
			int leng = pairs.size();
			double wst = -1.0;
			while((rule=br.readLine())!=null)
			{
				 ArrayList<String> tmp = new ArrayList<String>();
				int cnt = 0;
				for(String[] pair:pairs)
				{
					String s = pair[0];
					String s1 = pair[1];
					String r = RuleUtil.applyRuleS(rule, s1);
					if(r.compareTo(s1)!=0)
					{
						cnt ++;
					}
					tmp.add(r);
				}
				if(cnt*1.0/leng>0.5)
				{
					RuleUtil.write2file(tmp,(new File(fpath)).getAbsolutePath()+"_wst.txt");
					return;
				}
				if(cnt*1.0/leng>wst)
				{
					wst = cnt*1.0/leng;
					worst = tmp;
				}				
			}
			RuleUtil.write2file(worst,(new File(fpath)).getName()+"_wst.txt");
		} catch (Exception e) {
			System.out.println(""+e.toString());
		}
	}
	public static void main(String[] args)
	{
		//list all the files under a dir
		//generate the ground truth
		//generate all rules from given example
		//choose the wrost result
		try {
			String dpath = "/Users/bowu/Research/dataclean/data/40dataset";
			File f= new File(dpath);
			Datagenerator dg = new Datagenerator();
			String rfile = "/Users/bowu/Research/dataclean/data/40dataset/cRule.ru";
			BufferedReader br = new BufferedReader(new FileReader(new File(rfile)));
			String line = "";
			Vector<String> rules = new Vector<String>();
			while((line=br.readLine())!=null)
			{
				rules.add(line);
			}
			br.close();
			//read the example
			String efile = "/Users/bowu/Research/dataclean/data/40dataset/example.ru";
			BufferedReader br1 = new BufferedReader(new FileReader(new File(efile)));
			String line1 = "";
			line1 = br1.readLine();
			String[] paras = line1.split("\\|");
			br1.close();
			int cnt = 0;
			for(File tf:f.listFiles() )
			{
				if(!tf.getName().contains("txt")||tf.getName().contains("swp")||tf.getName().contains("pair"))
				{
					continue;
				}
				String p = dg.generateTruth(tf.getAbsolutePath(), rules.get(cnt));
				Vector<String[]> examples = new Vector<String[]>();
				examples.add(paras[0].split("%"));
				String rpath = dg.generateRules(examples);
				//dg.chooseWrost(p, rpath);
				cnt ++;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
		
}
