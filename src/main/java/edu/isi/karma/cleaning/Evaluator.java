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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class Evaluator {
	Ruler r;
	public Vector<String> pos;
	public Vector<String> cht;
	public Evaluator()
	{
		pos=new Vector<String>();
		cht = new Vector<String>();
	}
	public void addPos(Vector<Integer> xpos)
	{
		for(int i = 0; i< xpos.size() ; i++)
		{
			pos.add(String.valueOf(xpos.get(i)));
		}
	}
	public void addCnt(Vector<TNode> x)
	{
		for(int i = 0; i< x.size() ; i++)
		{
			cht.add(x.get(i).text);
		}
	}
	public void setRuler(Ruler r)
	{
		this.r = r;
	}
	
	public double calShannonEntropy(Vector<String> data)
	{
		double entropy = 0;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(int i = 0; i< data.size(); i++)
		{
			String key = data.get(i);
			if(map.containsKey(key))
			{
				map.put(key, map.get(key)+1);
			}
			else
			{
				map.put(key, 1);
			}
		}
		// compute the entropy
		Set<String> keys = map.keySet();
		Iterator<String> ptr = keys.iterator();
		while(ptr.hasNext())
		{
			double freq = (double)(map.get(ptr.next()))/data.size();
			entropy -= freq*Math.log(freq);
		}
		return entropy;
	}
	public double score(double e1, double e2)
	{
		return Math.min(e1, e2);
	}

}
