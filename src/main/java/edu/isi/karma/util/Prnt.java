package edu.isi.karma.util ;



import java.util.ArrayList;
import java.util.Collection;

public class Prnt {
	
	public static void prn(Object obj) {
		System.out.println(obj) ;
	}
	
	public static void endIt(String msg) {
		prn(msg);
		System.exit(1) ;
	}
	
	public static void prnCollection(Collection<String> list, String prefix) {
		for(String str : list) {
			prn((prefix == null ? "" : prefix) + str) ;
		}
	}

}