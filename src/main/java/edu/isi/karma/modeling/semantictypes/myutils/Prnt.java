package edu.isi.karma.modeling.semantictypes.myutils ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Prnt {
	static Logger logger = LoggerFactory.getLogger(Prnt.class.getName());
	
	public static void prn(Object obj) {
		// System.out.println(obj) ;
		logger.info(obj.toString());
	}
	
	public static void endIt(String msg) {
		prn(msg);
		System.exit(1) ;
	}

}