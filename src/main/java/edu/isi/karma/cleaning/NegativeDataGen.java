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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


//generate randomly mixed data for training
//the negative data generation process is as belows
//randomly perform a ins operation
//randomly perform a mov operation
//randomly perform a del operation
public class NegativeDataGen {
	public static Vector<TNode> randomEdit(Vector<TNode> x,Vector<TNode> y)
	{
		Random rd = new Random();
		Ruler r = new Ruler();
		r.setNewInput(x);
		//randomly perform a ins operation
		Vector<TNode> toks = new Vector<TNode>();
		if(x.size() == 0)
			return r.vec;
		int dpos = rd.nextInt(x.size());
		if(y.size() == 0)
			return r.vec;
		toks.add(y.get(rd.nextInt(y.size())));
		r.ins(toks, dpos);
		//randomly perform a mov operation
		if(r.vec.size() == 0)
			return r.vec;
		toks.add(y.get(rd.nextInt(y.size())));
		if(r.vec.size() == 0)
			return r.vec;
		int spos = rd.nextInt(r.vec.size());
		if(spos == r.vec.size()-1)
			spos = r.vec.size()-2;
		int epos = rd.nextInt(2)+spos;
		if(epos >= r.vec.size()-1)
			epos = r.vec.size()-1;
		if(r.vec.size() == 0)
			return r.vec;
		int dpos1 = rd.nextInt(r.vec.size());
		while(dpos1<=epos && dpos1 >= spos)
		{
			//System.out.println(r.vec.size()+","+dpos1);
			dpos1 = rd.nextInt(r.vec.size());
		}
		r.mov(null, dpos1, spos, epos);
		System.out.println(""+r.vec);
		//randomly perform a del operation
		if(r.vec.size() == 0)
			return r.vec;
		int st = rd.nextInt(r.vec.size());
		if(st == r.vec.size())
			st = r.vec.size()-2;
		if(r.vec.size()-st <= 0)
			return r.vec;
		int ed = rd.nextInt(r.vec.size()-st)+st;
		r.det(1, null, st, ed);
		return r.vec;
	}

	// fetch all the files under a dir
	//concatenate all string into one
	//token the string and randomly remove random number < size tokens
	public static void main(String[] args)
	{
		File dir = new File("/Users/bowu/Research/dataclean/data/RuleData/rawdata/pairs/pos");	
		File[] allfiles = dir.listFiles();
		try
		{	
			for(File f:allfiles)
			{
					if(f.getName().indexOf(".csv")==(f.getName().length()-4)&&f.isFile())
					{
						CSVWriter cw = new CSVWriter(new FileWriter(new File("/Users/bowu/Research/dataclean/data/RuleData/rawdata/pairs/neg/"+f.getName().substring(0, f.getName().indexOf("."))+"_neg.csv")),'\t');
						CSVReader cr = new CSVReader(new FileReader(f),'\t');
						String[] pair;
						while ((pair=cr.readNext())!=null)
						{
							Ruler r1 = new Ruler();
							r1.setNewInput(pair[0]);
							Ruler r2 = new Ruler();
							r2.setNewInput(pair[1]);
							Vector<TNode> res = NegativeDataGen.randomEdit(r1.vec,r2.vec);
							String x = "";
							for(int i = 0; i<res.size();i++)
							{
								x+= res.get(i).text;
							}
							pair[1] = x;
							cw.writeNext(pair);
						}
						cw.flush();
						cw.close();
					}
				
			}
			
		}
		catch(Exception e)
		{
			System.out.println(""+e.toString());
		}
	}
}
