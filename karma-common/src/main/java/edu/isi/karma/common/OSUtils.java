package edu.isi.karma.common;

public class OSUtils {

	private static String OS = null;

	private OSUtils() {
	}

	public static String getOsName() {
		if(OS == null) { OS = System.getProperty("os.name"); }
		//System.out.println("OS:" + OS);
		return OS;
	}
	
	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

}
