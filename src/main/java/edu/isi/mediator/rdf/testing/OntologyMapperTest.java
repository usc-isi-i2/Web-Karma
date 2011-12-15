// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rdf.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.rdf.OntologyMapper;

/**
 * Main Ontology Mapper Class.
 * Reads a configuration file called ontologysettings.config located 
 * in the top level directory.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */

public class OntologyMapperTest {

	static private String connectStr = null;
	static private String dbDriver = null;
	static private String modelName = null;
	static private String outputFile = null;

	private static final MediatorLogger logger = MediatorLogger.getLogger(OntologyMapperTest.class.getName());

	   public static void main(String[] args){
  
		   try{
			   processConfigFile();

				Connection localConn = null;
				Class.forName(dbDriver);
				localConn = DriverManager.getConnection(connectStr);
				IDBConnection idbConn = new DBConnection(localConn,"MySQL");

				// create a model maker with the given connection parameters
				ModelMaker maker = ModelFactory.createModelRDBMaker(idbConn);
				ModelRDB model = (ModelRDB) maker.openModel(modelName.trim());

				OntologyMapper m = new OntologyMapper(model);
			   
				if(args.length!=1){
					logger.info("USAGE: OntologyMapperTest uri");
				}

				if(outputFile==null){
					String s = m.getAlignedRdf(args[0]);
					logger.info(s);
				}
				else{
					m.getAlignedRdf(args[0], outputFile);					
				}
				
			   //String s = m.getAlignedRdf("http://www.dovetail.org/wits/SYSTEM.FACILITY/23434");
			   //m.getAlignedRdf("http://www.dovetail.org/wits/SYSTEM.FACILITY/23434", "C:/z.out");
			   
			   localConn.close();
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
					}
					else if (name.equals("DB_DRIVER")){
						dbDriver = val;
					}
					else if (name.equals("MODEL_NAME")){
						modelName = val;
					}
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
			
			if(connectStr==null || dbDriver==null || modelName==null){
				throw new MediatorException("Settings file should contain CONNECT_STR, DB_DRIVER, DB_NAME and MODEL_NAME");
			}
		}
	   
}
