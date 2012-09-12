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
package edu.isi.karma.cleaning.features;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;

public class Main {
	public static void main(String[] args)
	{
		File dir = new File("/Users/bowu/Research/dataclean/data/RuleData");
		File[] flist = dir.listFiles();
		try
		{
			//BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/bowu/Research/dataclean/data/negadata.out"));
			boolean isfirstRun = true;
			for(int i = 0 ; i<flist.length;i++)
			{
				Vector<String> row = new Vector<String>();
				Vector<String> examples = new Vector<String>();
				Vector<String> oexamples = new Vector<String>();
				System.out.println(flist[i].getName());
				if(!flist[i].getName().contains(".csv"))
					continue;
				CSVReader re = new CSVReader(new FileReader(flist[i]), '\t');
				String[] line = null;
				re.readNext();//discard the first line
				while((line=re.readNext() )!= null)
				{
					oexamples.add(line[0]);
					examples.add(line[1]);
				}
				RegularityFeatureSet rf = new RegularityFeatureSet();
				Collection<Feature> cf = rf.computeFeatures(oexamples,examples);
				//Iterator<Feature> iter = cf.iterator();
				Feature[] x = new Feature[cf.size()];
				cf.toArray(x);
				if(isfirstRun)
				{
					row.add("Featurename");
					for(int l=0;l<x.length;l++)
					{
						row.add(x[l].getName());
					}
					isfirstRun = false;
					row = new Vector<String>();
				}
				if(!isfirstRun)
				{
					row.add(flist[i].getName());
					for(int k=0;k<cf.size();k++)
					{
						row.add(String.valueOf(x[k].getScore()));
					}
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
}
