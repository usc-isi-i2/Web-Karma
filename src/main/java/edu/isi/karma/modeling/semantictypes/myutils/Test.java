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