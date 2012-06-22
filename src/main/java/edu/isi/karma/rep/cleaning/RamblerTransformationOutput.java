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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import edu.isi.karma.cleaning.EditOper;
import edu.isi.karma.cleaning.RuleUtil;

public class RamblerTransformationOutput implements TransformationOutput {

	private RamblerTransformationInputs input;
	private HashMap<String,Transformation> transformations; 
	private Vector<EditOper> preEditOpers;
	public RamblerTransformationOutput(RamblerTransformationInputs input)
	{
		this.input = input;
		transformations = new HashMap<String,Transformation>();
		try {		
			this.learnTransformation();
		} catch (Exception e) {
			System.out.println("Exception in learning transformations");
		}
	}
	private void learnTransformation() throws Exception
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
		if(trans == null)
		{
			return;
		}
		for(int i = 0; i<trans.size(); i++)
		{
			String[] rules = trans.get(i).split("<RULESEP>");
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

}
