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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class DataCollection {
	Vector<FileStat> fstates = new Vector<FileStat>();
	public DataCollection()
	{
		MyLogger myLogger = new MyLogger();
	}
	public void addEntry(FileStat a)
	{
		fstates.add(a);
	}
	public String getDate()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	public void print()
	{
		MyLogger.logsth("============Detail Information==========="+this.getDate()+"\n");
		for(FileStat f: fstates)
		{
			MyLogger.logsth(""+f.toString());
		}
	}
	public void print1()
	{
		// int[] 0 avg total learn time, 1 avg learn time, 2 t gen time, 3 avg gen time
		// 4 tot exec time 5 ave exec time
		// 6 exp num 7 tot ruleNo
		HashMap<String, Double[]> stats = new HashMap<String, Double[]>();
		for(FileStat f:fstates)
		{
			String fname = f.fileNameString;
			if(stats.containsKey(fname))
			{
				stats.get(fname)[0] += f.learnTime;
				stats.get(fname)[2] += f.genTime;
				stats.get(fname)[4] += f.execTime;
				if(stats.get(fname)[6] < f.exp_cnt)
					stats.get(fname)[6] = (double) f.exp_cnt;
				stats.get(fname)[7] += f.ruleNo;
			}
			else
			{
				Double[] x = {(double) f.learnTime,0.0,(double) f.genTime,0.0,(double) f.execTime,0.0,(double) f.exp_cnt,(double) f.ruleNo};
				stats.put(fname, x);
			}
		}
		// get average value
		MyLogger.logsth("============Summary Information===========\n"+this.getDate()+"\n");
		for(String key:stats.keySet())
		{
			
			Double[] value = stats.get(key);
			Double cnt = value[6];
			value[1] = value[0]*1.0/cnt;
			value[3] = value[2]*1.0/cnt;
			value[5] = value[4]*1.0/cnt;
			// long the final stats
			String lineString = String.format("%s,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",key,value[0],value[1],value[2],value[3],value[3],value[4],value[5],value[6],value[7]);
			MyLogger.logsth(lineString);
		}
	}
}
class FileStat
{
	public String fileNameString = "";
	public long learnTime = 0; // time used in constructing the version space
	public long genTime = 0; // time used to generate the first consistent rule
	public long execTime = 0; // time used in applying the rule
	public int exp_cnt = 0; // number of examples
	public String examples = ""; // all the examples
	public String program = "" ;// the correct program
	public long ruleNo = 0; // order of the first consistent rule
	public FileStat(String fname, long l,long g,long e,int exp,Vector<String[]> exps,long ruleNo,String program)
	{
		this.fileNameString = fname;
		this.learnTime = l;
		this.genTime = g;
		this.execTime = e;
		this.exp_cnt = exp;
		this.ruleNo = ruleNo;
		examples += "\n";
		for(String[] p:exps)
		{
			String s = p[0]+"\t"+p[1]+"\n";
			examples += s;
		}
		this.program = program;
	}
	public String toString()
	{
		String resString = String.format("%s,%d,%d,%d,%d,%d", this.fileNameString,this.learnTime,this.genTime,this.execTime,this.exp_cnt,this.ruleNo);
		resString += this.examples;
		resString += this.program+"\n";
		return resString;
	}
	
}
