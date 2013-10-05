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
package edu.isi.karma.modeling.semantictypes.myutils;

import java.util.ArrayList;

/**
 * This class provides utility methods for sorting small lists based on another list containing scores for them.
 * 
 * @author amangoel
 *
 */
public class ListOps {

	public static void main(String[] args) {
		ArrayList<String> stringList;
		ArrayList<Double> doubleList;
		stringList = new ArrayList<String>();
		doubleList = new ArrayList<Double>();
		stringList.add("aman");
		doubleList.add(1.5);
		stringList.add("goel");
		doubleList.add(3.6);
		stringList.add("xyz");
		doubleList.add(-2.5);
		stringList.add("alpha");
		doubleList.add(10.1);
		stringList.add("beta");
		doubleList.add(35.9);
		Prnt.prn(stringList);
		Prnt.prn(doubleList);
		sortListOnValues(stringList, doubleList);
		Prnt.prn(stringList);
		Prnt.prn(doubleList);
	}
	
	// Sorts values in descending order
	// Rearranges the list so that the objects are at the same index as their corresponding values
	// So, the item that had highest corresponding score will be at position zero and
	// item with the lowest score will be at the end of the list
	public static <T> void sortListOnValues(ArrayList<T> list, ArrayList<Double> values) {
		int pos = 0 ;
		
		for(int i=0;i<list.size()-1;i++) {
			pos = i ;
			for(int j=i+1;j<list.size();j++) {
				if(values.get(j) > values.get(pos)) {
					pos = j ;
				}
			}
			double maxVal = values.get(pos) ;
			values.set(pos, values.get(i)) ;
			values.set(i, maxVal) ;
			
			T obj = list.get(pos) ;
			list.set(pos, list.get(i)) ;
			list.set(i, obj) ;
		}
	}
	
	
}
