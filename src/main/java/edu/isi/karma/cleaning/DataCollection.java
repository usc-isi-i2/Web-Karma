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

import java.util.Vector;

public class DataCollection {
	Vector<FileStat> fstates = new Vector<FileStat>();
	public DataCollection()
	{
		
	}
	public void addEntry(FileStat a)
	{
		fstates.add(a);
	}
	public void print()
	{
		for(FileStat f: fstates)
		{
			System.out.println(""+f.toString());
		}
	}
}
class FileStat
{
	String fileNameString = "";
	long learnTime = 0; // time used in constructing the version space
	long genTime = 0; // time used to generate the first consistent rule
	long execTime = 0; // time used in applying the rule
	int exp_cnt = 0; // number of examples
	String examples = ""; // all the examples
	long ruleNo = 0; // order of the first consistent rule
	public FileStat(String fname, long l,long g,long e,int exp,Vector<String[]> exps,long ruleNo)
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
	}
	public String toString()
	{
		String resString = String.format("%s,%d,%d,%d,%d,%d", this.fileNameString,this.learnTime,this.genTime,this.execTime,this.exp_cnt,this.ruleNo);
		//resString += this.examples;
		return resString;
	}
	
}
