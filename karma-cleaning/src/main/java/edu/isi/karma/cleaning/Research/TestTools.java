package edu.isi.karma.cleaning.Research;

import java.util.ArrayList;
import java.util.Arrays;


public class TestTools {
	public static void print(ArrayList<double[]> res)
	{
		String p = "";
		for(double[] line:res)
		{
			p += Arrays.toString(line);
			p += "\n";
		}
		System.out.println(""+p);
	}
}
