// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;

import edu.isi.mediator.gav.main.MediatorException;

/**
 * @author mariam
 *
 */
public class MediatorUtil {

	/**
	 * @param var
	 * @return true if var is not between quotes AND is not a number
	 * 			false otherwise
	 */
	static public boolean isVar(String var){
		if(var.toUpperCase().equals(MediatorConstants.NULL_VALUE))
			return false;
		if(!var.startsWith("\"") && !var.startsWith("'")){
			//if it's not between quotes AND is not a number
			try{
				new BigDecimal(var);
				return false;
			}
			catch(Exception e){
				return true;
			}
		}
		else return false;
	}

	static public String removeBacktick(String s){
		s = s.trim();
		if(s.startsWith("`")){
			s=s.substring(1, s.length()-1);
		}
		return s;
	}
	
    /**
     * @param name
     * 			name of file (location independent)
     * @return
     */
    static public BufferedReader getReaderForFile(String name, Class<?> theClass){
    	try{
		URL u = theClass.getResource(name);
		if(u == null){
			System.out.println("File not found::" + name);
		}
		BufferedReader raf = new BufferedReader(new InputStreamReader(u.openStream()));
		System.out.println("File::" + name);
		return raf;
    	}catch(Exception e){
    		System.out.println("An error occured::" + e.getMessage());
    		return null;
    	}
    }

	/**
	 * @param file
	 * 			path to file
	 * @return
	 * 		    content of file.
	 * @throws MediatorException
	 */
	public static String getFileAsString(String file) throws MediatorException{
		String domainStr="";
				
		try
		{
			RandomAccessFile raf = new RandomAccessFile(file, "r");

			String line = "";
			line = raf.readLine();
			while (line != null)
			{
				domainStr += line + "\n";
				line = raf.readLine();
			}
		}
		catch (Exception exp)
		{
			throw new MediatorException("MediatorException:" + exp.getMessage());
		}
		return domainStr;
	}

	/**
	 * @param file
	 * @return
	 * 		    content of file.
	 * @throws MediatorException
	 */
	public static String getFileAsString(BufferedReader file) throws MediatorException{
		String domainStr="";
		
		try
		{
			String line = "";
			line = file.readLine();
			while (line != null)
			{
				domainStr += line + "\n";
				line = file.readLine();
			}
		}
		catch (Exception exp)
		{
			throw new MediatorException("MediatorException:" + exp.getMessage());
		}
		return domainStr;
	}

	/**
	 * @param fileAsResource
	 * @return
	 * 		    content of file.
	 * @throws MediatorException
	 */
	public static String getFileAsStringFromResource(String fileAsResource, Class<?> theClass) throws MediatorException{

		//System.out.println("F=" + fileAsResource + " " + theClass);
		BufferedReader br = getReaderForFile(fileAsResource, theClass);
		return getFileAsString(br);
	}		
	
	/**
	 * Saves a string to a file.
	 * @param str
	 * @param fileName
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void saveStringToFile(String str, String fileName) throws UnsupportedEncodingException, FileNotFoundException{
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8");
		BufferedWriter bw = new BufferedWriter (fw);
		PrintWriter outWriter = new PrintWriter (bw);
		outWriter.println(str);
		outWriter.close();
	}
}
