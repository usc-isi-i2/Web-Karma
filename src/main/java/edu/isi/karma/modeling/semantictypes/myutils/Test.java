package edu.isi.karma.modeling.semantictypes.myutils ;

import java.util.ArrayList;

public class Test {
	
	public static void main(String[] args) {
		test2() ;
	}
	
	
	
	
	public static void test1() {
		int[] count = new int[10] ;
		
		for(int iter = 0 ; iter < 10000 ; iter++) {
			ArrayList<Integer> randNumbers = RandOps.randomNumbers(10, 2) ;
			for	(Integer r : randNumbers) {
				count[r]++ ;
			}
		}
		
		for(int i=0;i<10;i++) {
			Prnt.prn(count[i]) ;
		}
	}
		
	
	static void test2() {
		ArrayList<Integer> randNumbers = RandOps.randomNumbers(100, 10) ;
		Prnt.prn(randNumbers) ;
	}
		
		
	
	
}