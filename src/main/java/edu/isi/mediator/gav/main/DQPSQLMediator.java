// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.main;

import java.io.File;

import uk.org.ogsadai.activity.dqp.preprocessor.PreProcessException;
import uk.org.ogsadai.activity.dqp.preprocessor.PreProcessor;
import uk.org.ogsadai.authorization.SecurityContext;
import uk.org.ogsadai.parser.SQLParserException;
import uk.org.ogsadai.resource.ResourceID;

/**DQPSQLMediator class contains interface methods for DQP<br>
 * use with SQL mediator Interface.
*/
public class DQPSQLMediator extends Mediator implements PreProcessor
{
	private String currentDomainFile = "";
	private File configDir = null;
	
	public DQPSQLMediator(){
		super();
    	//System.out.println("Mediator in constructor; mediatorHashCode()=" + this.hashCode());
	}

 	/**
 	 * Sets the path to the domain file
 	 * @param configDir
 	 * 			path to domain file
 	 * @throws PreProcessException
 	 */
 	public void setConfigDir(File configDir) throws PreProcessException{
 		this.configDir = configDir;

 		//domain is parsed only once after we have both path and file name
 		parseDomain();
 	}
 	
 	/**
     * Set location of doman model file
     * 
     * @param currentDomainFile the location of the domain model file.
     * 
     */
 	public void setCurrentDomainFile(String currentDomainFile) throws PreProcessException{
 		this.currentDomainFile=currentDomainFile;
    	//System.out.println("Mediator in setCurrentDomainFile; mediatorHashCode()=" + this.hashCode());

 		//domain is parsed only once after we have both path and file name
 		parseDomain();
 	}
 	
 	/**
     * 
     * @return the location of domain model file.
     * 
     */

	public String getCurrentDomainFile(){
		return currentDomainFile;
	}
	
	/**
	 * @return path of domain file
	 */
	public File getConfigDir(){
		return configDir;
	}
	
	/**
	 * Parse Domain File.
	 * @throws PreProcessException
	 */
	//Parse the domain file after we have both path to domain and file name
	private void parseDomain() throws PreProcessException{
		if(!currentDomainFile.equals("") && configDir != null){
			System.out.println("Parse Mediator Domain ....");
	    	System.out.println("Domain File=" + configDir.getAbsolutePath() + "/" + currentDomainFile);
	 		try{
	 			parseDomain(configDir.getAbsolutePath() + "/" + currentDomainFile);
	 		}catch(MediatorException e){
				e.printStackTrace();
				System.out.println("Mediator:" + e.getMessage());
				PreProcessException ppe = new PreProcessException("Mediator:" + e.getMessage());
				throw(ppe);
	 		}			
		}
	}	
	
	/**
     * Preprocess query from format X to DQP-Compliant SQL.
     * 
     * This method can use an information obtained from the ProProcessor config 
     * and the input arguments.
     * 
     * The output of the method must be a valid SQL string for DQP to process.
     * 
     * @param inputQuery the input query.
     * @param resourceID the target resource id.
     * @param securityContext the security context.
     * 
     * @return the string represented a DQP-Compliant SQL Query.
     * 
     * @throws PreProcessException when an error occurs in the PreProcessor.
     */
	public String preprocessQuery(String inputQuery, ResourceID resourceID,
			SecurityContext securityContext) throws PreProcessException{
		
		String sql="";
				
    	System.out.println("Calling preprocessQuery(); mediatorHashCode()=" + this.hashCode());

		try{

			sql = rewriteSQLQuery(inputQuery);
			System.out.println("SQL=" + sql);
			
		}catch(MediatorException e){
			e.printStackTrace();
			System.out.println("Mediator:" + e.getMessage());
			PreProcessException ppe = new PreProcessException("Mediator:" + e.getMessage());
			throw(ppe);
		}catch(SQLParserException e){
			e.printStackTrace();
			System.out.println("Mediator SQL Parser:" + e.getMessage());
			PreProcessException ppe = new PreProcessException("Mediator:" + e.getMessage());
			throw(ppe);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Mediator:" + e.getMessage());
			PreProcessException ppe = new PreProcessException("Mediator:" + e.getMessage());
			throw(ppe);
		}
		
		return sql;
	}
		
}
