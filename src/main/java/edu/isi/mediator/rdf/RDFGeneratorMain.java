// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.rdf.RDFGenerator;

/**
 * Main RDF Generator Class.
 * Reads a configuration file called rdfsettings.config located 
 * in the top level directory.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */

public class RDFGeneratorMain {

	static private String connectStr;
	static private String dbDriver;
	static private String outputFile;
	static private String inputFile;
	static private String accessDb;
	static private String ruleFile;
	
	static public String CSV = "CSV";
	static public String ACCESS = "ACCESS";
	
	private static final MediatorLogger logger = MediatorLogger.getLogger(RDFGeneratorMain.class.getName());

	   public static void main(String[] args){
  
		   try{
			   processConfigFile();

			   logger.info("Start RDF Generation.");
			   RDFGenerator gen = new RDFGenerator(ruleFile, outputFile);
			   if(inputFile!=null){
				   gen.generateTriples(inputFile, CSV);
			   }
			   else if(accessDb!=null){
				   gen.generateTriples(accessDb, ACCESS);
			   }
			   else{
				   gen.generateTriples(connectStr, dbDriver);
			   }
			   
			   logger.info("Output in:" + outputFile);
		   }catch(Exception e){e.printStackTrace();}
		   
	   }

		static private void processConfigFile() throws MediatorException {
			// SETTINGS
			BufferedReader buff = null;
			File f = null;
			try {
				f = new File("./rdfsettings.config");
				buff = new BufferedReader(new FileReader(f));
			} catch (FileNotFoundException e) {
				logger.fatal("Settings file was not found: " + f.toString()
						+ "!");
				return;
			}

			int inputsUsed=0;

			try {
				String line = null;
				while ((line = buff.readLine()) != null) {
					//System.out.println("line="+line);
					if (line.startsWith("#") || line.trim().isEmpty())
						continue;
					int ind = line.indexOf("=");
					if(ind<=0)
						throw new MediatorException("Settings should be of form: PropertyName=PropertyValue " + line);
					String name = line.substring(0,ind).trim();
					String val = line.substring(ind+1).trim();

					
					if (name.equals("CONNECT_STR")){
						connectStr = val;
						inputsUsed++;
					}
					else if (name.equals("DB_DRIVER")){
						dbDriver = val;
					}
					else if (name.equals("INPUT_FILE")){
						inputFile = val;
						inputsUsed++;
					}
					else if (name.equals("ACCESS_DB")){
						accessDb = val;
						inputsUsed++;
					}
					else if (name.equals("SOURCE_DESC"))
						ruleFile = val;
					else if (name.equals("OUTPUT_FILE")){
						if(val.toUpperCase().equals("STDOUT"))
							outputFile=null;
						else
							outputFile=val;
					}
				}
			} catch (IOException e2) {
				logger.fatal("Exception occured:" + e2);
			}
			
			if(ruleFile==null){
				throw new MediatorException("Settings file is missiong SOURCE_DESC.");
			}
			if(inputsUsed==0)
				throw new MediatorException("Settings file should contain either INPUT_FILE, CONNECT_STR or ACCESS_DB");
			if(inputsUsed>1)
				throw new MediatorException("Settings file should contain only ONE of INPUT_FILE, CONNECT_STR or ACCESS_DB.");
			if(connectStr!=null && dbDriver==null){
				throw new MediatorException("Settings file should contain DB_DRIVER.");
			}
		}
	   
}
