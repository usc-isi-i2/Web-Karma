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
import java.util.List;
import java.util.Random;



/**
 * This class provides utility methods for performing randomness based operations,
 * such as,
 * generating random numbers within a range.
 * 
 * @author amangoel
 *
 */
public class RandOps {
	
	public static void main(String[] args) throws Exception {
		ArrayList<String> stringList, newList;
		stringList = new ArrayList<String>();
		newList = new ArrayList<String>();
		stringList.add("aman");
		stringList.add("goel2");
		stringList.add("xyz");
		stringList.add("alpha");
		stringList.add("beta");
		Prnt.prn(stringList);
		Prnt.prn(newList);
		getRandomlySelectedItemsFromList(stringList, newList, 3);
		Prnt.prn(stringList);
		Prnt.prn(newList);
		
	}
	
	/**
	 * @param limit Max limit. Numbers will be returned from 0 (inclusive) and limit (exclusive)
	 * @param nNumbers Number of random numbers to be returned (cannot be greater than limit)
	 * @return A list of integers randomly selected within the above mentioned limit
	 */
	public static ArrayList<Integer> uniqSortedRandNums(int limit, int nNumbers) {
		// returns nNumbers unique random numbers between 0 (inclusive) and limit (exclusive)
		if(limit < nNumbers) {
			Prnt.endIt("RandOps.randomNumbers: limit < nNumbers") ;
		}
		Random rand = new Random() ;
		ArrayList<Integer> numbersList = new ArrayList<Integer>() ;
		while(numbersList.size() < nNumbers) {
			int num = rand.nextInt(limit - numbersList.size()) ;
			for(int i=0;i < numbersList.size() && numbersList.get(i) <= num; i++,num++) {
				// empty for loop, all logic is in the first statement
			}
			int j=0;
			for(j=0;j<numbersList.size();j++) {
				if(numbersList.get(j) > num) {
					break ;
				}
			}			
			numbersList.add(j, num) ;
		}
		return numbersList ;
	}
	
	
	/**
	 * @param <T> The type of the lists
	 * @param originalList The original list containing all the items
	 * @param selectedList The list in which randomly selected items will be returned
	 * @param numItems Number of items to be randomly selected from originallist
	 */
	public static <T> void getRandomlySelectedItemsFromList(List<T> originalList, List<T> selectedList, int numItems) {
		ArrayList<Integer> randomIndices;
		if (numItems > originalList.size()) {
			Prnt.endIt("Invalid argument @numItems. It is larger than the size of the list");
		}
		randomIndices = uniqSortedRandNums(originalList.size(), numItems);
		selectedList.clear();
		for(Integer index: randomIndices) {
			selectedList.add(originalList.get(index));
		}
	}
	
	
}
