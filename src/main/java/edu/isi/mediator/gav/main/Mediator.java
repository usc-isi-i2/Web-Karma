// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.main;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import uk.org.ogsadai.parser.SQLParserException;

import edu.isi.mediator.gav.main.MediatorInstance;
import edu.isi.mediator.rule.Query;
import edu.isi.mediator.gav.graph.Graph;
import edu.isi.mediator.gav.graph.cost_estimation.StatisticsInfo;
import edu.isi.mediator.gav.util.MediatorUtil;


/**
 * Main Mediator class.
 * @author mariam
 *
 */
public class Mediator
{
	//variable for concurrency
	//public static boolean IN_PLAN_GENERATION = false;

	//this will have the parsed domain model
	private  MediatorInstance mi;
	
	public Mediator(){
		System.out.println("in Mediator constructor");
	}

	/**
	 * @param domainFile
	 * 			path to file that contains the domain model
	 * @throws MediatorException
	 */
	public Mediator(String domainFile) throws MediatorException{
		try{
			RandomAccessFile raf = new RandomAccessFile(domainFile, "r");

			String line = "";
			line = raf.readLine();
			StringBuffer currentDomainStr = new StringBuffer();
			while (line != null)
			{
				currentDomainStr.append(line + "\n");
				line = raf.readLine();
			}
			//this is the instance that will have the parsed domain file
			mi = new MediatorInstance(currentDomainStr.toString());
			//System.out.println("MI=" + mi);
		}catch (Exception exp){
			MediatorException ppe = 
				new MediatorException("Mediator: Domain File could not be read: "+domainFile + ";");
			throw(ppe);
		}
	}
	
	/**
	 * Parse the domain file and update the MediatorInstance member. 
	 * @param domainFile
	 * 			path to file that contains the domain model
	 * @throws MediatorException
	 */
	protected void parseDomain(String domainFile) throws MediatorException{
		try{
			System.out.println("Domain File=" + domainFile);
			RandomAccessFile raf = new RandomAccessFile(domainFile, "r");
			
			String line = "";
			line = raf.readLine();
			StringBuffer currentDomainStr = new StringBuffer();
			while (line != null)
			{
				currentDomainStr.append(line + "\n");
				line = raf.readLine();
			}
			//this is the instance that will have the parsed domain file
			mi = new MediatorInstance(currentDomainStr.toString());
			//System.out.println("MI=" + mi);
		}catch (MediatorException exp){
			throw(exp);
		}catch (Exception exp){
			//MediatorException ppe = new MediatorException(exp.getMessage());
			exp.printStackTrace();
			throw(new MediatorException("Mediator: Either Domain File could not be read: "+domainFile + " or there are parsing errors;"));
			//throw(ppe);
		}
	}	
	
	/**
	 * @param domainFile
	 * 			The path to the domain file.
	 * @param queryStr
	 * 			datalog query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public String rewriteDatalogQuery(String domainFile, String queryStr) throws MediatorException{

		System.out.println("Input Datalog query=" + queryStr);
		
		String domainStr = MediatorUtil.getFileAsString(domainFile);
		return rewriteDatalogQueryFromString(domainStr, queryStr);
	}

	/**
	 * @param domainFile
	 * @param queryStr
	 * 			datalog query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public String rewriteDatalogQuery(BufferedReader domainFile, String queryStr) throws MediatorException{

		System.out.println("Input Datalog query=" + queryStr);
		
		String domainStr = MediatorUtil.getFileAsString(domainFile);
		return rewriteDatalogQueryFromString(domainStr, queryStr);
	}

	/**
	 * @param domainStr
	 * 			content of domain file.
	 * @param queryStr
	 * 			Datalog query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 */
	private String rewriteDatalogQueryFromString(String domainStr, String queryStr) throws MediatorException{

		MediatorInstance mi = new MediatorInstance(domainStr);
		mi.setQuery(queryStr);

		//System.out.println("Expand Query BEGIN:"+Calendar.getInstance().getTime());		
		//expand with domain rules => source query
		mi.expandQuery();
		//System.out.println("Expand Query END:"+Calendar.getInstance().getTime());		
		//System.out.println("MI=" + mi);

		Graph g = mi.generateInitialPlan();
		//////////////////
		//System.out.println("Graph..............." + g.getString());
		//////////////////
		
		String sql = g.getSQL();
		//System.out.println("SQL: " + sql);
		
		return sql;
		
	}

	/**
	 * @param domainFile
	 * 			The path to the domain file.
	 * @param queryStr
	 * 			SQL query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public String rewriteSQLQuery(String domainFile, String queryStr) throws MediatorException, SQLParserException{

		System.out.println("Input SQL query=" + queryStr);
		
		String domainStr = MediatorUtil.getFileAsString(domainFile);
		return rewriteSQLQueryFromString(domainStr, queryStr);
	}

	/**
	 * @param domainFile
	 * @param queryStr
	 * 			SQL query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public String rewriteSQLQuery(BufferedReader domainFile, String queryStr) throws MediatorException, SQLParserException{

		System.out.println("Input SQL query=" + queryStr);
		
		String domainStr = MediatorUtil.getFileAsString(domainFile);
		return rewriteSQLQueryFromString(domainStr, queryStr);
	}

	/**
	 * @param domainStr
	 * 			content of domain file
	 * @param queryStr
	 * 			SQL query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	private String rewriteSQLQueryFromString(String domainStr, String queryStr) throws MediatorException, SQLParserException{

		MediatorInstance mi = new MediatorInstance(domainStr);
		mi.setSQLQuery(queryStr);
		//System.out.println("MI=" + mi);

		//System.out.println("Expand Query BEGIN:"+Calendar.getInstance().getTime());		
		//expand with domain rules => source query
		mi.expandQuery();
		//System.out.println("Expand Query END:"+Calendar.getInstance().getTime());		

		Graph g = mi.generateInitialPlan();
		//////////////////
		//System.out.println("Graph..............." + g.getString());
		//////////////////
		
		String sql = g.getSQL();
		//System.out.println("SQL: " + sql);
		
		return sql;
		
	}

	//Called in DQPMediator;
	//query rewriting is quite fast so we serialize the calls
	//if we want at some point to do them in parallel make sure you construct
	//different instances of "mi"
	/**
	 * @param queryStr
	 * 			Datalog query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 */
	public synchronized String rewriteDatalogQuery(String queryStr) throws MediatorException{
		
		System.out.println("Datalog query=" + queryStr);
		
		mi.setQuery(queryStr);
		
		//System.out.println("Expand Query BEGIN:"+Calendar.getInstance().getTime());		
		//expand with domain rules => source query
		mi.expandQuery();
		//System.out.println("Expand Query END:"+Calendar.getInstance().getTime());		

		//System.out.println("MI=" + mi);

		Graph g = mi.generateInitialPlan();
		//////////////////
		//System.out.println("Graph..............." + g.getString());
		//////////////////
		
		String sql = g.getSQL();
		//System.out.println("SQL: " + sql);
		
		return sql;
		
	}

	//Called in DQPSQLMediator;
	//query rewriting is quite fast so we serialize the calls
	//if we want at some point to do them in parallel make sure you construct
	//different instances of "mi"
	/**
	 * @param queryStr
	 * 			SQL query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public synchronized String rewriteSQLQuery(String queryStr) throws MediatorException, SQLParserException{
		
		System.out.println("SQL query=" + queryStr);

		mi.setSQLQuery(queryStr);		
		
		//System.out.println("Expand Query BEGIN:"+Calendar.getInstance().getTime());		
		//expand with domain rules => source query
		mi.expandQuery();
		//System.out.println("Expand Query END:"+Calendar.getInstance().getTime());		

		//System.out.println("MI=" + mi);

		Graph g = mi.generateInitialPlan();
		//////////////////
		//System.out.println("Graph..............." + g.getString());
		//////////////////
		
		String sql = g.getSQL();
		//System.out.println("SQL: " + sql);
		
		return sql;
		
	}

	/**	Get optimized source query as datalog (used in junit tests)
	 * @param domainFile
	 * @param queryStr
	 * 			datalog query in domain terms.
	 * @return
	 * 			the optimized source query.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public String getOptimizedSourceQuery(BufferedReader domainFile, String queryStr) throws MediatorException{

		System.out.println("Input Datalog query=" + queryStr);
		
		String domainStr = MediatorUtil.getFileAsString(domainFile);
		MediatorInstance mi = new MediatorInstance(domainStr);
		mi.optimizeSourceQuery(true);
		mi.setQuery(queryStr);

		//System.out.println("Expand Query BEGIN:"+Calendar.getInstance().getTime());		
		//expand with domain rules => source query
		mi.expandQuery();

		Graph g = mi.generateInitialPlan();

		ArrayList<Query> dq = mi.getQuery();
		
		String sq = "";
		for(int i=0; i<dq.size(); i++){
			if(i>0) sq += " UNION ";
			Query oneDomainQuery = dq.get(i);
			sq += oneDomainQuery.getSourceQuery();
		}
		sq=sq.replace("\n","");
		sq=sq.replace("\t","");
		return sq;
	}

	/**
	 * @param domainStr
	 * 			content of domain file
	 * @param queryStr
	 * 			SQL query in domain terms.
	 * @return
	 * 			SQL query in source terms.
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public StatisticsInfo getCostEstimation(String domainFile, String queryStr, HashMap<String,StatisticsInfo> sizeEstimation) throws MediatorException, SQLParserException{

		String domainStr = MediatorUtil.getFileAsString(domainFile);

		MediatorInstance mi = new MediatorInstance(domainStr);
		mi.setSQLQuery(queryStr);
		mi.sizeEstimation = sizeEstimation;
		
		//System.out.println("MI=" + mi);

		//System.out.println("Expand Query BEGIN:"+Calendar.getInstance().getTime());		
		//expand with domain rules => source query
		mi.expandQuery();
		//System.out.println("Expand Query END:"+Calendar.getInstance().getTime());		

		Graph g = mi.generateInitialPlan();
		//it is important to get the SQL , so that I have the actual names
		g.getSQL();
		//////////////////
		System.out.println("Graph..............." + g.getString());
		//////////////////

		//return null;
		StatisticsInfo si = g.getCostEstimation();
		//System.out.println("Graph with cost..............." + g.getString());
		return si;
	}

}
