package edu.isi.karma.cleaning;
import java.util.Vector;
import java.util.logging.*;

public class CleaningLogger {
	static private FileHandler fileTxt;
	
	static public void setup() throws Exception
	{
		Logger logger = Logger.getLogger("Cleaning");
		logger.setLevel(Level.INFO);
		fileTxt = new FileHandler("./log/cleaning.log");
		logger.addHandler(fileTxt);
	}
	static public void writeSGS(Description dcrpt)
	{
		
	}
	static public void write(String str)
	{
		Logger.getLogger("Cleaning").info(str);
	}
}
