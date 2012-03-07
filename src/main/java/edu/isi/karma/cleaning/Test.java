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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Test {
	public static HashMap<String, ArrayList<ArrayList<String>>> deepclone(HashMap<String, ArrayList<ArrayList<String>>> x)
	{
		HashMap<String, ArrayList<ArrayList<String>>> nx = new HashMap<String, ArrayList<ArrayList<String>>>();
		Iterator<String> iter = x.keySet().iterator();
		while(iter.hasNext())
		{
			String key = iter.next();
			ArrayList<ArrayList<String>> value = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> copy = x.get(key);
			for(int i = 0; i<copy.size(); i++)
			{
				ArrayList<String> xy = (ArrayList<String>)copy.get(i).clone();
				value.add(xy);
			}
			nx.put(key, value);
		}
		return nx;
	}
	public static void main(String[] args)
	{
		HashMap<String, ArrayList<ArrayList<String>>> a = new HashMap<String, ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> aas = new ArrayList<ArrayList<String>>();
		ArrayList<String> x = new ArrayList<String>();
		x.add("1");
		x.add("2");
		aas.add(x);
		ArrayList<ArrayList<String>> bbs = new ArrayList<ArrayList<String>>();
		ArrayList<String> y = new ArrayList<String>();
		y.add("a");
		y.add("b");
		bbs.add(y);
		a.put("a", aas);
		a.put("b", bbs);
		HashMap<String, ArrayList<ArrayList<String>>> t = Test.deepclone(a);
		t.get("a").get(0).add("3");
		System.out.println("really");
	}
}
