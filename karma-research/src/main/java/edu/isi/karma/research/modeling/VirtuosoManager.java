package edu.isi.karma.research.modeling;
/*
 *  $Id: VirtuosoTest.java,v 1.9 2008/06/30 14:29:27 source Exp $
 *
 *  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
 *  project.
 *
 *  Copyright (C) 1998-2008 OpenLink Software
 *
 *  This project is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; only version 2 of the License, dated June 1991.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */


//package virtuoso.sesame.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.research.Params;

import virtuoso.sesame2.driver.VirtuosoRepository;

public class VirtuosoManager {

	private static Logger logger = LoggerFactory.getLogger(VirtuosoManager.class);

	public String VIRTUOSO_INSTANCE = "fusionRepository.isi.edu";
	public int VIRTUOSO_PORT = 1200;  
	public String VIRTUOSO_USERNAME = "dba";
	public String VIRTUOSO_PASSWORD = "dba";
	Repository repository;
	
	public VirtuosoManager(String host, int port, String username, String password) {
		this.VIRTUOSO_INSTANCE = host; // "fusionRepository.isi.edu"
		this.VIRTUOSO_PORT = port; //1200
		this.VIRTUOSO_USERNAME = username ; //dba
		this.VIRTUOSO_PASSWORD = password; //dba

		repository = new VirtuosoRepository("jdbc:virtuoso://" + VIRTUOSO_INSTANCE + ":" + 
				VIRTUOSO_PORT + "/charset=UTF-8/log_enable=2", VIRTUOSO_USERNAME, VIRTUOSO_PASSWORD);

	}
	
	public RepositoryConnection getConnection() throws RepositoryException {
		RepositoryConnection con = repository.getConnection();
		return con;
	}
	
	public void closeConnection(RepositoryConnection con) throws RepositoryException {
		con.close();
	}

	public int getPatternCount(RepositoryConnection con, String sparqlQuery) {
		
		try {
			long start = System.currentTimeMillis();
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
			// maxQueryTime - The maximum query time, measured in seconds. 
			// A negative or zero value indicates an unlimited query time (which is the default).
			// tupleQuery.setMaxQueryTime(5); 
			TupleQueryResult result = tupleQuery.evaluate();
			try {
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					Value value = bindingSet.getValue("count");
					if (value != null) {
						return Integer.parseInt(value.stringValue());
					} 
				}  
				return 0;
			} finally {
				long responseTime = System.currentTimeMillis() - start;
				logger.debug("response time: " + (responseTime/1000F));
				result.close();
			}

		} catch (MalformedQueryException e) {
			e.printStackTrace();
			return 0;
		} catch (QueryEvaluationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return 0;
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} 
	}
	
	public void extractObjectProperties(List<String> graphIRIs, String filename) {

		try {
			PrintWriter resultFile = new PrintWriter(new File(filename));
			RepositoryConnection con = repository.getConnection();
			try {

				long start = System.currentTimeMillis();
				String fromClause = "";
				if (graphIRIs != null && !graphIRIs.isEmpty()) {
					for (String iri : graphIRIs) {
						fromClause += "FROM <" + iri + ">\n"; 
					}
				}
				String queryString = 
						"SELECT DISTINCT ?c1 ?p ?c2 (COUNT(*) as ?count) \n" +
								fromClause +
								"WHERE { ?x rdf:type ?c1. \n" +
								"?y rdf:type ?c2. \n" +
								"?x ?p ?y. \n" +
								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/2000/01/rdf-schema#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/2000/01/rdf-schema#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/2002/07/owl#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/2002/07/owl#\")) \n" +
								"} \n" +
								"GROUP BY ?c1 ?p ?c2 \n" + 
								"ORDER BY DESC(?count)";

				System.out.println(queryString);

				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value valueOfC1 = bindingSet.getValue("c1");
						Value valueOfP = bindingSet.getValue("p");
						Value valueOfC2 = bindingSet.getValue("c2");
						Value valueOfCount = bindingSet.getValue("count");

						if (valueOfC1 != null) {
							System.out.print(valueOfC1.stringValue() + "\t");
							resultFile.print(valueOfC1.stringValue() + ",");
						}
						if (valueOfP != null) {
							System.out.println(valueOfP.stringValue() + "\t");
							resultFile.print(valueOfP.stringValue() + ",");
						}
						if (valueOfC2 != null) {
							System.out.println(valueOfC2.stringValue() + "\t");
							resultFile.print(valueOfC2.stringValue() + ",");
						}
						if (valueOfCount != null) {
							System.out.println(valueOfCount.stringValue() + "\t");
							resultFile.print(valueOfCount.stringValue());
						}
						System.out.println();
						resultFile.print("\n");
					}  
				} finally {
					long responseTime = System.currentTimeMillis() - start;
					logger.info("response time: " + (responseTime/1000F));
					result.close();
				}

			} catch (MalformedQueryException e) {
				e.printStackTrace();
			} catch (QueryEvaluationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				con.close();
				resultFile.close();
			}

		}
		catch (RepositoryException e) {
			// handle exception
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

	public void extractDataProperties(List<String> graphIRIs, String filename) {

		try {
			PrintWriter resultFile = new PrintWriter(new File(filename));
			RepositoryConnection con = repository.getConnection();
			try {

				long start = System.currentTimeMillis();
				String fromClause = "";
				if (graphIRIs != null && !graphIRIs.isEmpty()) {
					for (String iri : graphIRIs) {
						fromClause += "FROM <" + iri + ">\n"; 
					}
				}

				String queryString = 
						"SELECT DISTINCT ?c ?p (COUNT(*) as ?count) \n" +
								fromClause + 
								"WHERE { ?x rdf:type ?c. \n" +
								"?x ?p ?y. \n" +
								"FILTER isLiteral(?y). \n" +
								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/2000/01/rdf-schema#\")) \n" +
								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/2002/07/owl#\")) \n" +
								"} " +
								"GROUP BY ?c ?p \n" + 
								"ORDER BY DESC(?count)";

				System.out.println(queryString);

				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value valueOfC = bindingSet.getValue("c");
						Value valueOfP = bindingSet.getValue("p");
						Value valueOfCount = bindingSet.getValue("count");

						if (valueOfC != null) {
							System.out.print(valueOfC.stringValue() + "\t");
							resultFile.print(valueOfC.stringValue() + ",");
						}
						if (valueOfP != null) {
							System.out.println(valueOfP.stringValue() + "\t");
							resultFile.print(valueOfP.stringValue() + ",");
						}
						if (valueOfCount != null) {
							System.out.println(valueOfCount.stringValue() + "\t");
							resultFile.print(valueOfCount.stringValue());
						}
						System.out.println();
						resultFile.print("\n");
					}  
				} finally {
					long responseTime = System.currentTimeMillis() - start;
					logger.info("response time: " + (responseTime/1000F));
					result.close();
				}

			} catch (MalformedQueryException e) {
				e.printStackTrace();
			} catch (QueryEvaluationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				con.close();
				resultFile.close();
			}

		}
		catch (RepositoryException e) {
			// handle exception
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

	public static void main(String[] args) {

		String host = "fusionRepository.isi.edu";
		int port = 1200;  
		String username = "dba";
		String password = "dba";
		
		VirtuosoManager vm = new VirtuosoManager(host, port, username, password);

		//		extractObjectProperties(repository, "http://europeana.eu", Params.LOD_OBJECT_PROPERIES_FILE);
		//		extractDataProperties(repository, "http://europeana.eu", Params.LOD_DATA_PROPERIES_FILE);

		//		extractObjectProperties(repository, "http://amsterdammuseum.nl", Params.LOD_OBJECT_PROPERIES_FILE);
		//		extractDataProperties(repository, "http://amsterdammuseum.nl", Params.LOD_DATA_PROPERIES_FILE);

		List<String> graphIRIs = new ArrayList<String>();
		graphIRIs.add("http://musicbrainz.org");
		graphIRIs.add("http://dbtune.org/jamendo");
		graphIRIs.add("http://dbtune.org/magnatune");
		graphIRIs.add("http://dbtune.org/bbc/peel");
		graphIRIs.add("http://dbtune.org/bbc/playcount");
		vm.extractDataProperties(graphIRIs, Params.LOD_DATA_PROPERIES_FILE);
		vm.extractObjectProperties(graphIRIs, Params.LOD_OBJECT_PROPERIES_FILE);

	}


}
