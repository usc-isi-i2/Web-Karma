// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__


package edu.isi.mediator.domain.parser;

import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.rule.GAVRule;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.rule.Query;
import edu.isi.mediator.gav.main.MediatorException;

/**
 * Domain File parser.
 * @author mariam
 *
 */
public class QueryParser extends DomainParser{
	
	/**
	 * Parse a query.
	 * @param queryStr
	 * 			the query
	 * @return
	 * 		the AST for this query.
	 * @throws MediatorException
	 */
	public CommonTree parseQuery(String queryStr) throws MediatorException{
		
		if(!queryStr.startsWith("QUERIES:"))
			queryStr = "QUERIES:" + queryStr;

		CommonTree t = parse(queryStr);

		//System.out.println("AST=" + t.toStringTree());
		
		return t;
	}

	/**
	 * Parse query file and populate Mediator data structures.
	 * @param queryStr
	 * 			Queries
	 * @param dm
	 * 			the DomainModel
	 * @param domainQuery
	 * 			the domain query
	 * @throws MediatorException
	 */
	public void parseQueries(String queryStr, DomainModel dm, ArrayList<Query> domainQuery) throws MediatorException{
		
		CommonTree t = parse(queryStr);
		
		//System.out.println("AST=" + t.toStringTree());

		for(int i=0; i<t.getChildCount(); i++){
			CommonTree child = (CommonTree) t.getChild(i);
			//System.out.println(child.getText());
			if(child.getText().equals(MediatorConstants.QUERIES)){
				parseQueries(child, domainQuery, dm);
			}
		}
	}

	/**
	 * Parse Datalog Queries and populate Mediator data structures
	 * @param queries
	 * 			queries as AST
	 * @param domainQuery
	 * 			the domain query
	 * @param dm
	 * 			the DomainModel
	 * @throws MediatorException 
	 */
	protected void parseQueries(CommonTree queries, ArrayList<Query> domainQuery, DomainModel dm) throws MediatorException{

		GAVRuleParser ruleParser = new GAVRuleParser();

		for(int i=0; i<queries.getChildCount(); i++){
			CommonTree rule = (CommonTree) queries.getChild(i);
			GAVRule oneRule = ruleParser.parseGAVRule(rule,dm);
			Query q = new Query(oneRule);
			//System.out.println("Parsed query=" + q);
			//q has to have same head as the other queries (it's a union)
			if(isUnionQuery(q, domainQuery))
				domainQuery.add(q);
		}
	}


	/**
	 * Parse one query and return the mediator Query.
	 * @param queryStr
	 * 		the query string
	 * @return
	 * 		the parsed query
	 * @throws MediatorException
	 */
	public Query parseOneQuery(String queryStr) throws MediatorException{
		
		Query q = null;
		CommonTree t = parse(queryStr);
		
		//System.out.println("AST=" + t.toStringTree());
		GAVRuleParser ruleParser = new GAVRuleParser();

		for(int i=0; i<t.getChildCount(); i++){
			CommonTree child = (CommonTree) t.getChild(i);
			//System.out.println(child.getText());
			if(child.getText().equals(MediatorConstants.QUERIES)){
				GAVRule r = ruleParser.parseGAVRule((CommonTree)child.getChild(0), null);
				q = new Query(r);
			}
		}
		return q;
	}
	
	//q has to have same head as the other queries in domainQuery(it's a union)
	/**
	 * Checks if q has same head as the other queries in domainQuery. 
	 * @param q
	 * @param domainQuery
	 * @return true if q has same head as the other queries in domainQuery
	 * 			false otherwise.
	 * @throws MediatorException
	 */
	private boolean isUnionQuery(Query q, ArrayList<Query> domainQuery) throws MediatorException{
		if(domainQuery.isEmpty())
			return true;
		else{
			Query firstQ = domainQuery.get(0);
			Predicate p1 = firstQ.getHead();
			Predicate p2 = q.getHead();
			if(p1.equals(p2))
				return true;
			else{
				throw new MediatorException("(UNION)Head of query " + q + " should be equal to head of query " + firstQ);				
			}
		}
	}

}
