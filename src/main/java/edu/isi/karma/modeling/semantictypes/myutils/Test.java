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
import java.util.HashSet;


public class Test {
	
	public static void main(String[] args) {
		HashSet<String> list = new HashSet<String>() ;
		list.add("aman") ;
		list.add("goel") ;
		list.add("ruchi") ;
		Prnt.prnCollection(list, "These are the names ") ;
	}
	
	
	
	
	public static void test1() {
		int[] count = new int[10] ;
		
		for(int iter = 0 ; iter < 10000 ; iter++) {
			ArrayList<Integer> randNumbers = RandOps.uniqSortedRandNums(10, 2) ;
			for	(Integer r : randNumbers) {
				count[r]++ ;
			}
		}
		
		for(int i=0;i<10;i++) {
			Prnt.prn(count[i]) ;
		}
	}
		
	
	static void test2() {
		ArrayList<Integer> randNumbers = RandOps.uniqSortedRandNums(100, 10) ;
		Prnt.prn(randNumbers) ;
	}
		
		
	
	
}
