package edu.isi.karma.cleaning.Research;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import edu.isi.karma.cleaning.EmailNotification;

//this class is used to evaluate adaptation function
public class AdaptationTest {
	public static void main(String[] args) {
		EmailNotification notification = new EmailNotification();
		ConfigParameters cfg = new ConfigParameters();
		cfg.initeParameters();
		DataCollection.config = cfg.getString();
		Logger log = Logger.getLogger(AdaptationTest.class.getName());
		//preload all libraries 
		//Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		//start experiment
		Test.test4("/Users/bowu/Research/testdata/TestSingleFile");
		Test.test3("/Users/bowu/Research/testdata/TestSingleFile");
		notification.notify(true,"NewExpr");
	}
}
