package edu.isi.karma.cleaning;
import java.io.*;
import java.util.HashMap;
public class MyLogger {
	public static BufferedWriter out;
	public static HashMap<String, Long> timespan = new HashMap<String,Long>();
	public MyLogger()
	{
		if(out == null)
		{
			try {
				out = new BufferedWriter(new FileWriter(new File("./log/mylog.txt")));
			} catch (Exception e) {
				System.out.println(""+e.toString());
				out = null;
			}
		}
	}
	public static void logsth(String context)
	{
		try {
			out.write(context+"\n");
			out.flush();
		} catch (Exception e) {
			System.out.println(""+e.toString());
		}
	}
	public static void setTime(String id,Long time)
	{
		timespan.put(id, time);
		
	}
	public static long getDuration(String id)
	{
		if(timespan.containsKey(id))
		{
			return (System.currentTimeMillis() - timespan.get(id))/1000;
		}
		else {
			return -1;
		}
	}

}
