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



public class TestJAVA {
	void generate_combos(Vector<Long> indexs,Vector<Vector<Integer>> configs) {
	    int k = indexs.size();
		int[] com = new int[k];
	    for (int i = 0; i < k; i++) 
	    		com[i] = 0;
	    while (com[k - 1] < indexs.get(k-1)) {
	    		Vector<Integer> res = new Vector<Integer>();
	        for (int i = 0; i < k; i++)
	        {
	        		//System.out.print(""+com[i]);
	            res.add(com[i]);
	        }
	        //System.out.println("");
	        configs.add(res);
	        int t = k - 1;
	        while (t != 0 && com[t] == indexs.get(t)-1) 
	        		t--;
	        com[t]++;
	        if(t==0 && com[t] >= indexs.get(0))
	        {
	        		break;
	        }
	        for (int i = t + 1; i < k; i++) 
	        		com[i] = 0;
	    }
	}
	public static void main(String[] args)
	{
		Vector<Long> x = new Vector<Long>();
		x.add((long)9);
		x.add((long)6);
		x.add((long)4);
		Vector<Vector<Integer>> cfig = new Vector<Vector<Integer>>();
		TestJAVA jx = new TestJAVA();
		jx.generate_combos(x, cfig);
		for(int i = 0; i< cfig.size(); i++)
		{
			for(int j = 0; j< cfig.get(i).size(); j++)
			{
				System.out.print(cfig.get(i).get(j));
			}
			System.out.println("");
		}
		
		
	}
}
