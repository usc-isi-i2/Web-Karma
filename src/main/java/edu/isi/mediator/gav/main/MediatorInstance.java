// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import uk.org.ogsadai.parser.SQLParserException;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.domain.parser.QueryParser;
import edu.isi.mediator.domain.parser.DomainParser;
import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.gav.graph.Graph;
import edu.isi.mediator.gav.graph.UnionNode;
import edu.isi.mediator.gav.graph.cost_estimation.StatisticsInfo;
import edu.isi.mediator.gav.sqlquery.SQLQueryBuilder;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.rule.Query;


/**
 * Creates a Mediator Instance, defined by a domain model and a query.
 * @author mariam
 *
 */
public class MediatorInstance{
	
	/**
	 * if true, optimize the source query (using containment algorithm)
	 */
	private boolean OPTIMIZE = false;
	
	/**
	 * Current domain model.
	 */
	protected DomainModel domainModel = new DomainModel();
	/**
	 * Current domain query. If more then one query, head of all queries should be the same (union query)
	 */
	protected ArrayList<Query> domainQuery = new ArrayList<Query>();
	
    /**
     * Size estimation statistics.
     * key=source name; val=statistics for the source
     */
    static public HashMap<String, StatisticsInfo> sizeEstimation = new HashMap<String, StatisticsInfo>();

	/**
	 * Constructs a MediatorInstance given the domain file content.
	 * @param domainStr
	 * 			domain file as string: schemas + rules
	 * @throws MediatorException
	 */
	public MediatorInstance(String domainStr)  throws MediatorException{
		//System.out.println("Parse Domain BEGIN:"+Calendar.getInstance().getTime());
		/////////////////////////
		
		//System.out.println("DS="+ domainStr);
		
		parseDomainModel(domainStr);

		//System.out.println("DM=" + domainModel);
	}

	/**
	 * Returns the Domain Model.
	 * @return
	 */
	public DomainModel getDM(){
		return domainModel;
	}
	
	/** Returns the query.
	 * @return
	 */
	public ArrayList<Query> getQuery(){
		return domainQuery;
	}

	/**
	 * Sets OPTIMIZE flag.
	 * @param b
	 * 		true if we want all queries executed in this MI to be optimized (using containment algorithm)
	 * 		false otherwise
	 */
	public void optimizeSourceQuery(boolean b){
		System.out.println("OPTIMIZATION=" + b);
		OPTIMIZE = b;
	}
	
	/**
	 * Returns true if the source query should be optimized.
	 * @return
	 * 		true if source query should be optimized (using containment algorithm)
	 * 		false otherwise
	 */
	public boolean optimizeSourceQuery(){
		if(OPTIMIZE)
			return true;
		else return false;
	}

	/**
	 * Parse the domain model: schemas + rules
	 * @param domain
	 * 			the domain model
	 * @throws MediatorException
	 */
	private void parseDomainModel(String domain)  throws MediatorException{
		DomainParser sp = new DomainParser();
		domainModel = sp.parseDomain(domain);
	}

	/**
	 * Parse SQL query and build mediator data structures.
	 * @param sqlQ
	 * 			sql query in domain terms
	 * @throws MediatorException
	 * @throws SQLParserException
	 */
	private void parseSQLQuery(String sqlQ)  throws MediatorException, SQLParserException{
		SQLQueryBuilder qb = new SQLQueryBuilder(domainModel);
		ArrayList<Query> queries = qb.parse(sqlQ);
		domainQuery=queries;
	}

	/**
	 * Parse Datalog Query and build data structures.
	 * @param queryStr
	 * 			datalog queries
	 * @throws MediatorException
	 */
	private void parseQuery(String queryStr)  throws MediatorException{
		if(!queryStr.startsWith("QUERIES:"))
			queryStr = "QUERIES:" + queryStr;
		QueryParser sp = new QueryParser();
		sp.parseQueries(queryStr, domainModel, domainQuery);
	}

	/**
	 * Creates the Mediator data structures for this SQL query
	 * @param queryStr
	 * 			the sql query in domain terms
	 * @throws MediatorException
	 * @throws SQLParserException 
	 */
	public void setSQLQuery(String queryStr)  throws MediatorException, SQLParserException{
		//first clear anything related to the previous query if any
		clearPreviousQuery();
		//System.out.println("Parse Query BEGIN:"+Calendar.getInstance().getTime());
		parseSQLQuery(queryStr);
	}

	/**
	 * Creates the Mediator data structures for this Datalog query
	 * @param queryStr
	 * 			the datalog query in domain terms
	 * @throws MediatorException
	 */
	public void setQuery(String queryStr)  throws MediatorException{
		//first clear anything related to the previous query if any
		clearPreviousQuery();
		//System.out.println("Parse Query BEGIN:"+Calendar.getInstance().getTime());
		parseQuery(queryStr);
	}

	/**
	 * Sets the query for this MediatorInstance
	 * @param q
	 * 		Query as Mediator data structure
	 */
	public void setQuery(ArrayList<Query> q){
		domainQuery=q;
	}
	
	/**
	 * Clear anything related to the previous query.
	 */
	private void clearPreviousQuery(){
		domainQuery.clear();		
		Query.index=0;
		MediatorConstants.reset();
	}
	
	// builds the final unions
	/**
	 * Generates query plan.
	 * @return
	 * 		initial plan for this query
	 * @throws MediatorException
	 */
	public Graph generateInitialPlan() throws MediatorException{
		
		//System.out.println("Generate Plan BEGIN nr queries:" + domainQuery.size());		
		UnionNode un = new UnionNode();
		Query oneQ = domainQuery.get(0);
		//needed here for the ORDER BY; I need to add the order by at the top level
		un.setQueryDecomposition(oneQ.getQueryDecomposition());
		un.domainLevelUnion=true;
		Graph graph = oneQ.generateInitialPlan(OPTIMIZE);
		for(int i=1; i<domainQuery.size(); i++){
			if(i==1){
				un.addSubNode(graph.getParentNode());
				graph=new Graph(un);
			}
			oneQ = domainQuery.get(i);
			Graph graphForOneQ = oneQ.generateInitialPlan(OPTIMIZE);
			un.addSubNode(graphForOneQ.getParentNode());
		}
		return graph;
	}	
	
	//express the query in source terms
	//expand the domain rules into it's body
	/**
	 * Rewrites the query (in source terms)
	 * @throws MediatorException
	 */
	public void expandQuery() throws MediatorException{
		for(int i=0; i<domainQuery.size(); i++){
			Query oneDomainQuery = domainQuery.get(i);
			oneDomainQuery.expandQuery(domainModel.getGAVRules());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		s += "CurrentInstance\n";
		//s += domainModel + "\n";
		s += "DomainQueries:\n";
		for(int i=0; i<domainQuery.size(); i++){
			s += domainQuery.get(i).toString() + "\n";
		}
		return s;
	}
}