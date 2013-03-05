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

import org.python.antlr.PythonParser.return_stmt_return;

public class ExampleSelection {
	public HashMap<String, Vector<TNode>> org = new HashMap<String, Vector<TNode>>();
	public HashMap<String, Vector<TNode>> tran = new HashMap<String, Vector<TNode>>();
	public HashMap<String, String[]> raw = new HashMap<String, String[]>();
	public static int way = 3;
	public ExampleSelection()
	{
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
		case 4:
			ID = this.way4();
			break;
		default:
			ID = "";
		}
		return ID;
	}
	public void inite(HashMap<String, String[]> exps)
	{
		org.clear();
		tran.clear();
		Ruler ruler = new Ruler();
		for(String keyString:exps.keySet())
		{
			String e = exps.get(keyString)[0];
			ruler.setNewInput(e);
			org.put(keyString, ruler.vec);
			//ruler.setNewInput(exps.get(keyString)[1]);
			//tran.put(keyString, ruler.vec);
		}
		this.raw = exps;
	}
	// choose the most ambiguous
	public String way1()
	{
		String ID = "";
		int maximum = -1;
		for(String key:org.keySet())
		{
			int s = this.ambiguityScore(org.get(key));
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
		for(String key:org.keySet())
		{
			int s = this.ambiguityScore(org.get(key));
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
		for(String key:org.keySet())
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
	//use the cnt of fatal_error number
	public String way4()
	{
		int max = -1;
		String example  = "";
		for(String key:raw.keySet())
		{
			int cnt = raw.get(key)[1].split("_FATAL_ERROR_").length;
			if(cnt > max)
			{
				max = cnt;
				example = key;
			}
		}
		return example;
	}
	public void clear()
	{
		this.raw.clear();
		org.clear();
		tran.clear();
	}

}
