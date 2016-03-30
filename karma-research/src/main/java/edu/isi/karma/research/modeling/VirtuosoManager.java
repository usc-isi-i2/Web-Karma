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

import virtuoso.sesame2.driver.VirtuosoRepository;
import edu.isi.karma.modeling.research.Params;

public class VirtuosoManager {

	private static Logger logger = LoggerFactory.getLogger(VirtuosoManager.class);

	private String VIRTUOSO_INSTANCE;
	private int VIRTUOSO_PORT;  
	private String VIRTUOSO_USERNAME;
	private String VIRTUOSO_PASSWORD;
	private Integer VIRTUOSO_QUERY_TIMEOUT;
	
	private Repository repository;
	
	public VirtuosoManager(VirtuosoConnector virtuosoConnector) {
		this.VIRTUOSO_INSTANCE = virtuosoConnector.getInstance();
		this.VIRTUOSO_PORT = virtuosoConnector.getPort();
		this.VIRTUOSO_USERNAME = virtuosoConnector.getUsername();
		this.VIRTUOSO_PASSWORD = virtuosoConnector.getPassword();
		this.VIRTUOSO_QUERY_TIMEOUT = virtuosoConnector.getQueryTimeout();
		
		
		String connection = "jdbc:virtuoso://" + VIRTUOSO_INSTANCE + ":" + VIRTUOSO_PORT;
		connection += "/charset=UTF-8";
		if (VIRTUOSO_QUERY_TIMEOUT != null)
			connection += "/timeout=" + VIRTUOSO_QUERY_TIMEOUT.intValue();
		connection += "/log_enable=2";
		
		repository = new VirtuosoRepository(connection, VIRTUOSO_USERNAME, VIRTUOSO_PASSWORD);

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
			//tupleQuery.setMaxQueryTime(1); 
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
			System.out.println("fail to get results in the timeout=" + this.VIRTUOSO_QUERY_TIMEOUT);
//			e1.printStackTrace();
			return 1;
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} 
	}
	
	public void extractObjectProperties(List<String> graphIRIs, String filename) {

		try {
			PrintWriter resultFile = new PrintWriter(new File(filename));
			resultFile.print("c1,op12,c2,count\n");
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
						"SELECT DISTINCT ?c1 ?op12 ?c2 (COUNT(*) as ?count) \n" +
								fromClause +
								"WHERE { ?x rdf:type ?c1. \n" +
								"?y rdf:type ?c2. \n" +
								"?x ?op12 ?y. \n" +
								"FILTER (?x != ?y). \n " +
//								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/2000/01/rdf-schema#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/2000/01/rdf-schema#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/2002/07/owl#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/2002/07/owl#\")) \n" +
								"} \n" +
								"GROUP BY ?c1 ?op12 ?c2 \n" + 
								"ORDER BY DESC(?count)";

//				System.out.println(queryString);

				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value valueOfC1 = bindingSet.getValue("c1");
						Value valueOfP = bindingSet.getValue("op12");
						Value valueOfC2 = bindingSet.getValue("c2");
						Value valueOfCount = bindingSet.getValue("count");

						if (valueOfC1 != null) {
//							System.out.print(valueOfC1.stringValue() + "\t");
							resultFile.print(valueOfC1.stringValue() + ",");
						}
						if (valueOfP != null) {
//							System.out.println(valueOfP.stringValue() + "\t");
							resultFile.print(valueOfP.stringValue() + ",");
						}
						if (valueOfC2 != null) {
//							System.out.println(valueOfC2.stringValue() + "\t");
							resultFile.print(valueOfC2.stringValue() + ",");
						}
						if (valueOfCount != null) {
//							System.out.println(valueOfCount.stringValue() + "\t");
							resultFile.print(valueOfCount.stringValue());
						}
//						System.out.println();
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
			resultFile.print("c1,dp1a,count\n");
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
						"SELECT DISTINCT ?c1 ?dp1a (COUNT(*) as ?count) \n" +
								fromClause + 
								"WHERE { ?x rdf:type ?c1. \n" +
								"?x ?dp1a ?y. \n" +
								"FILTER isLiteral(?y). \n" +
//								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/2000/01/rdf-schema#\")) \n" +
//								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/2002/07/owl#\")) \n" +
								"} " +
								"GROUP BY ?c1 ?dp1a \n" + 
								"ORDER BY DESC(?count)";

//				System.out.println(queryString);

				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value valueOfC = bindingSet.getValue("c1");
						Value valueOfP = bindingSet.getValue("dp1a");
						Value valueOfCount = bindingSet.getValue("count");

						if (valueOfC != null) {
//							System.out.print(valueOfC.stringValue() + "\t");
							resultFile.print(valueOfC.stringValue() + ",");
						}
						if (valueOfP != null) {
//							System.out.println(valueOfP.stringValue() + "\t");
							resultFile.print(valueOfP.stringValue() + ",");
						}
						if (valueOfCount != null) {
//							System.out.println(valueOfCount.stringValue() + "\t");
							resultFile.print(valueOfCount.stringValue());
						}
//						System.out.println();
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

		String instance = "fusionRepository.isi.edu";
//		int port = 1140;//music;  
//		int port = 1300;  
		int port = 1500;  
		String username = "dba";
		String password = "dba";
		
		VirtuosoConnector vc = new VirtuosoConnector(instance, port, username, password);
		VirtuosoManager vm = new VirtuosoManager(vc);

		
		String baseGraph = "http://weapon-lod/";
		String filename, graphNameSuffix, graphName;
		String patternInputDirStr;
		List<String> graphIRIs = new ArrayList<String>();
		File f = new File(Params.SOURCE_DIR);
		File[] files = f.listFiles();
//		int i = 4; {
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			graphIRIs.clear();
			filename = file.getName();
			System.out.println("extracting patterns with length " + 1 + " for graph corresponding to " + filename);
			graphNameSuffix = filename.substring(0, filename.lastIndexOf("."));
			graphName = baseGraph + graphNameSuffix;
			graphIRIs.add(graphName);
			patternInputDirStr = Params.LOD_DIR + graphNameSuffix + "/" + Params.PATTERNS_INPUT_DIR;
			File patternInputDir = new File(patternInputDirStr);
			if (!patternInputDir.exists()) {
				patternInputDir.mkdirs();
			}
			
			vm.extractObjectProperties(graphIRIs, patternInputDirStr + Params.LOD_OBJECT_PROPERIES_FILE);
//			vm.extractDataProperties(graphIRIs, patternInputDirStr + Params.LOD_DATA_PROPERIES_FILE);
		}
		
		
//		List<String> graphIRIs = new ArrayList<String>();
//		graphIRIs.add("http://musicbrainz.org");
//		graphIRIs.add("http://dbtune.org/jamendo");
//		graphIRIs.add("http://dbtune.org/magnatune");
//		graphIRIs.add("http://dbtune.org/bbc/peel");
//		graphIRIs.add("http://dbtune.org/bbc/playcount");
////		vm.extractDataProperties(graphIRIs, Params.LOD_DATA_PROPERIES_FILE);
//		File patternDir = new File(Params.LOD_DIR + "musicbrainz/" + Params.PATTERNS_INPUT_DIR);
//		if (!patternDir.exists()) {
//			patternDir.mkdirs();
//		}
//		vm.extractObjectProperties(graphIRIs, patternDir.getAbsolutePath() + "/" + Params.LOD_OBJECT_PROPERIES_FILE);

	}


}
