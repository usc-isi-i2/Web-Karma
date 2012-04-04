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
package edu.isi.karma.rep.cleaning;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.cleaning.*;

public class RamblerTransformationOutput implements TransformationOutput {

	private RamblerTransformationInputs input;
	private HashMap<String,Transformation> transformations; 
	public RamblerTransformationOutput(RamblerTransformationInputs input)
	{
		this.input = input;
		transformations = new HashMap<String,Transformation>();
		this.learnTransformation();
	}
	private void learnTransformation()
	{
		Collection<TransformationExample> exs =  input.getExamples();
		Vector<String[]> exps = new Vector<String[]>();
		Iterator<TransformationExample> iter = exs.iterator();
		while(iter.hasNext())
		{
			TransformationExample t = iter.next();
			String[] tmp = {t.getBefore(),t.getAfter()};
			exps.add(tmp);
		}
		Vector<String> trans = RuleUtil.genRule(exps);
		for(int i = 0; i<trans.size(); i++)
		{
			String[] rules = trans.get(i).split("#");
			//System.out.println(""+s1);
			Vector<String> xr = new Vector<String>();
			for(int t = 0; t< rules.length; t++)
			{
				if(rules[t].length()!=0)
					xr.add(rules[t]);
			}
			RamblerTransformation r = new RamblerTransformation(xr);
			transformations.put(r.signature, r);
		}
	}
	@Override
	public HashMap<String,Transformation> getTransformations() {
		// TODO Auto-generated method stub
		return transformations;
	}

	@Override
	public ValueCollection getTransformedValues(String TransformatinId) {
		// TODO Auto-generated method stub
		Transformation t = transformations.get(TransformatinId);
		ValueCollection v = input.getInputValues();
		ValueCollection vo = new RamblerValueCollection();
		Collection<String> keys = v.getNodeIDs();
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext())
		{
			String k = iter.next();
			String val = v.getValue(k);
			val = t.transform(val);
			vo.setValue(k, val);
			//System.out.println(k+","+val);
		}
		return vo;
	}

	@Override
	public Collection<String> getRecommandedNextExample() {
		// TODO Auto-generated method stub
		return null;
	}
	//for testing the interfaces
	public static void main(String[] args)
	{
		String dirpath = "/Users/bowu/Research/dataclean/data/RuleData/rawdata/pairs/test";
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		for(File f:allfiles)
		{
			try
			{
				if(f.getName().indexOf(".csv")==(f.getName().length()-4))
				{
					
					CSVReader cr = new CSVReader(new FileReader(f),'\t');
					String[] pair;
					int isadded = 0;
					HashMap<String,String> tx = new HashMap<String,String>();
					int i = 0;
					Vector<TransformationExample> vrt = new Vector<TransformationExample>();
					while ((pair=cr.readNext())!=null)
					{
						
						pair[0] = "%"+pair[0]+"@";
						tx.put(i+"", pair[0]);
						if(isadded<3)
						{
							RamblerTransformationExample rtf = new RamblerTransformationExample(pair[0], pair[1], i+"");
							vrt.add(rtf);
							isadded ++;
						}
						i++;
					}
					RamblerValueCollection vc = new RamblerValueCollection(tx);
					RamblerTransformationInputs inputs = new RamblerTransformationInputs(vrt, vc);
					//generate the program
					RamblerTransformationOutput rtf = new RamblerTransformationOutput(inputs);
					Set<String> keys = rtf.getTransformations().keySet();
					Iterator<String> iter = keys.iterator();
					int index = 0;
					while(iter.hasNext() && index<3)
					{
						ValueCollection rvco = rtf.getTransformedValues(iter.next());
						System.out.println(rvco.getValues());
						index ++;
					}					
				}			
			}
			catch(Exception ex)
			{
				System.out.println(""+ex.toString());
			}	
		}
		
		//RamblerTransformationInputs ri = new RamblerTransformationInputs(examples, inputValues) 
	}

}
