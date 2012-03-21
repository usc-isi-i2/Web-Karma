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
import java.io.FileReader;
import java.util.Vector;

import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instance;
import weka.core.Instances;
import edu.isi.karma.util.Prnt;

public class RegularityClassifer {
	SimpleLogistic cf;
	public RegularityClassifer(String fpath)
	{
		try
		{
			cf = this.train(fpath);
		}
		catch(Exception e)
		{
			System.out.println("Building classifier error");
		}
	}
	public SimpleLogistic train(String fpath) throws Exception
	{
		BufferedReader fileReader = new BufferedReader(new FileReader(fpath)) ;
		//BufferedReader fileReader = new BufferedReader(new FileReader("/Users/amangoel/research/data/iris2.txt")) ;
		
		Instances instances = new Instances(fileReader) ;
		instances.setClassIndex(instances.numAttributes() -1) ;
		
		Prnt.prn(instances.numAttributes()) ;
		
		SimpleLogistic logreg = new SimpleLogistic(10, true, true) ;
		//SimpleLogistic logreg = new SimpleLogistic() ;
		logreg.buildClassifier(instances) ;
		return logreg;
	}
	public void Classify(String fpath) throws Exception
	{
		BufferedReader fileReader = new BufferedReader(new FileReader(fpath)) ;
		//BufferedReader fileReader = new BufferedReader(new FileReader("/Users/amangoel/research/data/iris2.txt")) ;
		
		Instances instances = new Instances(fileReader) ;
		instances.setClassIndex(instances.numAttributes() -1) ;
		Prnt.prn(instances.numAttributes()) ;
		for(int i=0;i<instances.size();i++) {			
			Instance instance = instances.get(i) ;
			double[] dist = cf.distributionForInstance(instance) ;
			//System.out.println(cf.classifyInstance(instance));
			for(double d : dist) {
				System.out.print(d + "  ") ;
			}
			System.out.println() ;
		}
		fileReader.close() ;
		Prnt.prn(cf) ;
	}
	public int getRank(String fpath) throws Exception
	{
		int rank = 0;
		BufferedReader fileReader = new BufferedReader(new FileReader(fpath));
		Instances instances = new Instances(fileReader) ;
		instances.setClassIndex(instances.numAttributes() -1) ;
		Prnt.prn(instances.numAttributes()) ;
		double confidence = -1;
		Vector<Double> posConfid = new Vector<Double>();
		for(int i=0;i<instances.size();i++) {			
			Instance instance = instances.get(i) ;
			double label = instance.value(instances.numAttributes()-1);
			double[] dist = cf.distributionForInstance(instance) ;
			posConfid.add(dist[1]);//keep history of all the confidence
			//System.out.println(cf.classifyInstance(instance));
			if(label == 3)
			{
				//classified correctly
				if(dist[1]>dist[0])
				{
					if(dist[1]>confidence)
					{
						confidence = dist[1]; // find the maximal confidence
					}
				}
				else// classified incorrectly
				{
					//do nothing
				}
			}
		}
		
		fileReader.close() ;
		//Prnt.prn(cf) ;
		//no correct transformation result
		if(confidence == -1)
		{
			return -1;
		}
		//find the rank of confidence
		int tiecnt = 0;
		for(int i=0;i<posConfid.size();i++)
		{
			double d = posConfid.get(i);
			if(d>=confidence)
			{
				if( instances.get(i).value(instances.numAttributes()-1)!=3)
				{
					rank ++;
					UtilTools.index = i;
				}
			}
		}
		return rank;
	}
	public static void main(String[] args)
	{
		try
		{
			RegularityClassifer rc = new RegularityClassifer("/Users/bowu/Research/features.arff");
			rc.Classify("./tmp.arff");
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
