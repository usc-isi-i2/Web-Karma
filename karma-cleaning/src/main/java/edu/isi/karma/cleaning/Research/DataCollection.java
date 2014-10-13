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

package edu.isi.karma.cleaning.Research;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.MyLogger;

public class DataCollection {
	public static String config = "";
	Vector<FileStat> fstates = new Vector<FileStat>();
	public HashSet<String> succeededFiles = new HashSet<String>();
	@SuppressWarnings("unused")
	public DataCollection() {
		MyLogger myLogger = new MyLogger();
	}
	public void addSucceededFile(String fname)
	{
		if( ! succeededFiles.contains(fname));
			succeededFiles.add(fname);
	}
	public void addEntry(FileStat a)
	{
		fstates.add(a);
	}

	public String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public void print() {
		MyLogger.logsth("============Detail Information==========="
				+ this.getDate() + "\n");
		MyLogger.logsth(DataCollection.config + "\n");
		for (FileStat f : fstates) {
			MyLogger.logsth("" + f.toString());
		}
	}

	public void print1() {
		// int[] 0 avg total learn time, 1 avg learn time, 2 t gen time, 3 avg
		// gen time
		// 4 tot exec time 5 ave exec time
		// 6 exp num 7 tot ruleNo 8 checkedrows
		HashMap<String, Double[]> stats = new HashMap<String, Double[]>();
		for (FileStat f : fstates) {
			String fname = f.fileNameString;
			if (stats.containsKey(fname)) {
				stats.get(fname)[0] += f.learnTime;
				stats.get(fname)[2] += f.genTime;
				stats.get(fname)[4] += f.execTime;
				if (stats.get(fname)[6] < f.exp_cnt)
					stats.get(fname)[6] = (double) f.exp_cnt;
				
				stats.get(fname)[7] += f.constraintNo;
				stats.get(fname)[8] += f.checkedrow;
				stats.get(fname)[9] += f.qRecordNum;
				stats.get(fname)[10] += f.clfacc ;
				stats.get(fname)[11] = (double) f.parNum;
			} else {
				Double[] x = { (double) f.learnTime, 0.0, (double) f.genTime,
						0.0, (double) f.execTime, 0.0, (double) f.exp_cnt,
						(double) f.constraintNo, (double) f.checkedrow,
						(double) f.qRecordNum, (double) f.clfacc,(double)f.parNum };
				stats.put(fname, x);
			}
		}
		// get average value
		MyLogger.logsth("============Summary Information===========\n"
				+ this.getDate() + "\n");
		MyLogger.logsth(DataCollection.config + "\n");
		for (String key : stats.keySet()) {
			Double[] value = stats.get(key);
			Double cnt = value[6];
			value[1] = value[0] * 1.0 / cnt;
			value[3] = value[2] * 1.0 / cnt;
			value[5] = value[4] * 1.0 / cnt;
			value[10] = value[10]*1.0 / cnt;
			// long the final stats
			String lineString = "";
			if(this.succeededFiles.contains(key))
				lineString = String.format("%s,T_learn,%f,avg_learn,%f,T_gen,%f,avg_gen,%f,T_exec,%f,avg_exec,%f,exp,%f,constraint,%f,clfacc,%f, parNum, %f\n",key,value[0],value[1],value[2],value[3],value[4],value[5],value[6],value[7],value[10],value[11]);
			else {
				lineString = String.format("%s_failed,T_learn,%f,avg_learn,%f,T_gen,%f,avg_gen,%f,T_exec,%f,avg_exec,%f,exp,%f,constraint,%f,clfacc,%f,parNum,%f\n",key,value[0],value[1],value[2],value[3],value[4],value[5],value[6],value[7],value[10],value[11]);
			}
			MyLogger.logsth(lineString);
		}
	}
}

class FileStat {
	public String fileNameString = "";
	public long learnTime = 0; // time used in constructing the version space
	public long genTime = 0; // time used to generate the first consistent rule
	public long execTime = 0; // time used in applying the rule
	public int exp_cnt = 0; // number of examples
	public String examples = ""; // all the examples
	public String program = "";// the correct program
	public long constraintNo = 0; // order of the first consistent rule
	public long checkedrow = 0;// number of checked rows
	public long qRecordNum = 0;
	public long parNum = 0;
	public double resacc = 0.0;
	public double clfacc = 0.0;

	public FileStat(String fname, long l, long g, long e, int exp,
			Vector<String[]> exps, long constraintNo, long checkedrow,
			long qRecordNum, long parNum, String program,double resacc,double clfacc) {
		this.fileNameString = fname;
		this.learnTime = l;
		this.genTime = g;
		this.execTime = e;
		this.exp_cnt = exp;
		this.constraintNo = constraintNo;
		//this.checkedrow = checkedrow;
		//this.qRecordNum = qRecordNum;
		this.parNum = parNum;
		examples += "\n";
		for (String[] p : exps) {
			String s = p[0] + "\t" + p[1] + "\n";
			examples += s;
		}
		this.program = program;
		this.resacc = resacc;
		this.clfacc = clfacc;
	}
	public String toString()
	{
		String resString = String.format("%s,LearnTime,%d,GenTime,%d,ExecTime,%d,Exp_cnt,%d,Constr_cnt,%d,ParitionNum,%d,Res_acc,%f,Clf_acc,%f", this.fileNameString,this.learnTime,this.genTime,this.execTime,this.exp_cnt,this.constraintNo,this.parNum,this.resacc, this.clfacc);
		resString += this.examples;
		resString += this.program + "\n";
		return resString;
	}

}
