package edu.isi.karma.modeling.semantictypes.myutils;

import java.util.ArrayList;
import java.util.Random;



public class RandOps {
	
	public static ArrayList<Integer> randomNumbers(int limit, int nNumbers) {
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
	
	/*
	while(numbersList.size() < nNumbers) {
		int num = rand.nextInt(limit - numbersList.size()) ;
		int min = 0 ;
		int max = num ;
		while(true) {
			int a = 0 ;
			for(Integer i : numbersList) {
				if(i <= max && i >= min)
					a++ ;
			}
			num+=a ;
			if(a == 0)
				break ;
			else {
				min = max + 1 ; 
				max = num ;
			}
		}
		numbersList.add(num) ;			
	}
	*/
	
}