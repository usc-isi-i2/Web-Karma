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
import java.util.Vector;

public class ExampleSelection {
	public HashMap<String, Vector<TNode>> examples = new HashMap<String, Vector<TNode>>();
	public int way = -1;
	public ExampleSelection(int way)
	{
		this.way = way;
	}
	public String Choose()
	{
		String ID = "";
		switch (this.way) {
		case 1:
			ID = this.way1();
			break;
		case 2:
			ID = this.way2();
			break;
		case 3:
			ID = this.way3();
			break;
		default:
			ID = "";
		}
		return ID;
	}
	public void inite(HashMap<String, String> exps)
	{
		Ruler ruler = new Ruler();
		examples = new HashMap<String, Vector<TNode>>();
		for(String keyString:exps.keySet())
		{
			String e = exps.get(keyString);
			ruler.setNewInput(e);
			examples.put(keyString, ruler.vec);
		}
	}
	// choose the most ambiguous
	public String way1()
	{
		String ID = "";
		int maximum = -1;
		for(String key:examples.keySet())
		{
			int s = this.ambiguityScore(examples.get(key));
			if(s>maximum)
			{
				ID = key;
				maximum = s;
			}
		}
		return ID;
	}
	//return the least ambiguous
	public String way2()
	{
		String ID = "";
		int minimum = Integer.MAX_VALUE;
		for(String key:examples.keySet())
		{
			int s = this.ambiguityScore(examples.get(key));
			if(s<minimum)
			{
				ID = key;
				minimum = s;
			}
		}
		return ID;
	}
	// return the first incorrect one
	public String way3()
	{
		String ID = "";
		int minimum = Integer.MAX_VALUE;
		for(String key:examples.keySet())
		{
			int s = Integer.valueOf(key);
			if(s<minimum)
			{
				ID = key;
				minimum = s;
			}
		}
		return ID;
	}
	public int ambiguityScore(Vector<TNode> vec)
	{
		HashMap<String, Integer> d = new HashMap<String, Integer>(); 
		int score = 0;
		for(int i = 0; i<vec.size(); i++)
		{
			if(d.containsKey(vec.get(i).text))
				continue;
			for(int j = 0; j< vec.size(); j++)
			{
				if(vec.get(j).text.compareTo(vec.get(i).text)==0 && i != j && vec.get(j).text.compareTo(" ")!=0)
				{
					score ++;
				}
			}
			if(!d.containsKey(vec.get(i).text))
			{
				d.put(vec.get(i).text, score);
			}
		}
		return score;
	}

}
