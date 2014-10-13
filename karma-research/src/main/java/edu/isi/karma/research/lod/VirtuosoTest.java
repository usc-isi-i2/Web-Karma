package edu.isi.karma.research.lod;
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
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoExtendedString;
import virtuoso.jdbc4.VirtuosoRdfBox;
import virtuoso.sesame2.driver.VirtuosoRepository;
import edu.isi.karma.modeling.research.Params;

public class VirtuosoTest {

	private static Logger logger = LoggerFactory.getLogger(VirtuosoTest.class);

	public static final String VIRTUOSO_INSTANCE = "fusionRepository.isi.edu";
	public static final int VIRTUOSO_PORT = 1130;  // Web UI: 8990
	public static final String VIRTUOSO_USERNAME = "dba";
	public static final String VIRTUOSO_PASSWORD = "dba";

	static int PASSED = 0;
	static int FAILED = 0;
	static int testCounter = 0;

	public static void startTest() {
		testCounter++;
		System.out.println("== TEST " + testCounter + ": " + " : Start");
	}

	public static void log(String mess) {
		System.out.println("   " + mess);
	}

	public static void endTest(boolean OK) {
		System.out.println("== TEST " + testCounter + ": " + " : End");
		System.out.println((OK ? "PASSED:" : "***FAILED:") + " TEST " + testCounter + "\n");
		if (OK) PASSED++;
		else FAILED++;
	}

	public static void getTotal() {
		System.out.println("============================");
		System.out.println("PASSED:" + PASSED + " FAILED:" + FAILED);
	}

	private static void extractObjectProperties(Repository repository, String GraphIRI, String filename) {

		try {
			PrintWriter resultFile = new PrintWriter(new File(filename));
			RepositoryConnection con = repository.getConnection();
			try {

				long start = System.currentTimeMillis();
				String from = GraphIRI == null || GraphIRI.trim().isEmpty() ? "" : "FROM <" + GraphIRI + "> ";

				String queryString = 
						"SELECT DISTINCT ?c1 ?p ?c2 (COUNT(?p) as ?count) " +
								from +
								"WHERE { ?x rdf:type ?c1. " +
								"?y rdf:type ?c2. " +
								"?x ?p ?y. " +
								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) " +
								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) " +
								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/2000/01/rdf-schema#\")) " +
								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/2000/01/rdf-schema#\")) " +
								"FILTER(!STRSTARTS(STR(?c1), \"http://www.w3.org/2002/07/owl#\")) " +
								"FILTER(!STRSTARTS(STR(?c2), \"http://www.w3.org/2002/07/owl#\")) " +
								"} " +
								"GROUP BY ?c1 ?p ?c2";

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

	private static void extractDataProperties(Repository repository, String GraphIRI, String filename) {

		try {
			PrintWriter resultFile = new PrintWriter(new File(filename));
			RepositoryConnection con = repository.getConnection();
			try {

				long start = System.currentTimeMillis();
				String from = GraphIRI == null || GraphIRI.trim().isEmpty() ? "" : "FROM <" + GraphIRI + "> ";

				String queryString = 
						"SELECT DISTINCT ?c ?p (COUNT(?p) as ?count) " +
								from + 
								"WHERE { ?x rdf:type ?c. " +
								"?x ?p ?y. " +
								"FILTER isLiteral(?y). " +
								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")) " +
								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/2000/01/rdf-schema#\")) " +
								"FILTER(!STRSTARTS(STR(?c), \"http://www.w3.org/2002/07/owl#\")) " +
								"} " +
								"GROUP BY ?c ?p";

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

		String[] sa = new String[4];
		sa[0] = VIRTUOSO_INSTANCE;
		sa[1] = VIRTUOSO_PORT + "";
		sa[2] = VIRTUOSO_USERNAME;
		sa[3] = VIRTUOSO_PASSWORD;
		for (int i = 0; i < sa.length && i < args.length; i++) {
			sa[i] = args[i];
		}
		Repository repository = new VirtuosoRepository("jdbc:virtuoso://" + sa[0] + ":" + 
				sa[1]+ "/charset=UTF-8/log_enable=2", sa[2], sa[3]);

		//		extractObjectProperties(repository, "http://europeana.eu", Params.LOD_OBJECT_PROPERIES_FILE);
		//		extractDataProperties(repository, "http://europeana.eu", Params.LOD_DATA_PROPERIES_FILE);

		//		extractObjectProperties(repository, "http://amsterdammuseum.nl", Params.LOD_OBJECT_PROPERIES_FILE);
		//		extractDataProperties(repository, "http://amsterdammuseum.nl", Params.LOD_DATA_PROPERIES_FILE);

		extractDataProperties(repository, null, Params.LOD_DATA_PROPERIES_FILE);
		extractObjectProperties(repository, null, Params.LOD_OBJECT_PROPERIES_FILE);

	}

	public static void main_original(String[] args) {

		String[] sa = new String[4];
		sa[0] = VIRTUOSO_INSTANCE;
		sa[1] = VIRTUOSO_PORT + "";
		sa[2] = VIRTUOSO_USERNAME;
		sa[3] = VIRTUOSO_PASSWORD;
		for (int i = 0; i < sa.length && i < args.length; i++) {
			sa[i] = args[i];
		}
		Repository repository = new VirtuosoRepository("jdbc:virtuoso://" + sa[0] + ":" + sa[1], sa[2], sa[3]);
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();
			con.setAutoCommit(true);

			// // test ask query
			// String ask = "ask { ?s <http://mso.monrai.com/foaf/name> ?o }";
			// doQuery(con, ask);

			// test add data to the repository
			boolean ok = true;
			String query = null;
			String strurl = "http://www.openlinksw.com/dataspace/person/kidehen@openlinksw.com/foaf.rdf";
			URL url = new URL(strurl);
			URI context = repository.getValueFactory().createURI("http://demo.openlinksw.com/demo#this");
			Value[][] results = null;

			startTest();
			// test query data
			query = "SELECT * FROM <" + context + "> WHERE {?s ?p ?o} LIMIT 1";
			try {
				log("Loading data from URL: " + strurl);
				con.add(url, "", RDFFormat.RDFXML, context);
				ok = true;
				results = doTupleQuery(con, query);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && (results.length > 0))); // should return true

			startTest();
			try {
				ok = true;
				con.clear(context);
				log("Clearing triple store");
				long sz = con.size(context);
				ok = (sz == 0);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest(ok); // should return sz == 0

			// test add data from a flat file
			startTest();
			String fstr = "virtuoso_driver" + File.separator + "data.nt";
			log("Loading data from file: " + fstr);
			try {
				ok = true;
				File dataFile = new File(fstr);
				con.add(dataFile, "", RDFFormat.NTRIPLES, context);
				query = "SELECT * FROM <" + context + "> WHERE {?s ?p ?o} LIMIT 1";
				results = doTupleQuery(con, query);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((results != null && results.length > 0)); // should return true


			byte utf8data[] = { (byte)0xd0, (byte)0xbf, (byte)0xd1, (byte)0x80, 
					(byte)0xd0, (byte)0xb8, (byte)0xd0, (byte)0xb2, 
					(byte)0xd0, (byte)0xb5, (byte)0xd1, (byte)0x82 };
			String utf8str = new String(utf8data, "UTF8");

			URI un_testuri = repository.getValueFactory().createURI("http://mso.monrai.com/foaf/unicodeTest");
			URI un_name = repository.getValueFactory().createURI("http://mso.monrai.com/foaf/name");
			Literal un_Value = repository.getValueFactory().createLiteral(utf8str);

			startTest();
			try {
				ok = true;
				con.clear(context);
				log("Loading UNICODE single triple");
				con.add(un_testuri, un_name, un_Value, context);
				query = "SELECT * FROM <" + context + "> WHERE {?s ?p ?o} LIMIT 1";
				results = doTupleQuery(con, query);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			if (ok && results.length > 0) {
				if (!results[0][0].toString().equals(un_testuri.toString())
						|| !results[0][1].toString().equals(un_name.toString())
						|| !results[0][2].toString().equals(un_Value.toString()))
				{
					ok = false;
				}
			}
			endTest((ok && (results.length > 0))); // should return true



			URI shermanmonroe = repository.getValueFactory().createURI("http://mso.monrai.com/foaf/shermanMonroe");
			BNode snode = repository.getValueFactory().createBNode("smonroeNode");
			URI name = repository.getValueFactory().createURI("http://mso.monrai.com/foaf/name");
			Literal nameValue = repository.getValueFactory().createLiteral("Sherman Monroe");

			startTest();
			try {
				ok = true;
				con.clear(context);
				log("Loading single triple");
				con.add(snode, name, nameValue, context);
				query = "SELECT * FROM <" + context + "> WHERE {?s ?p ?o} LIMIT 1";
				results = doTupleQuery(con, query);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && (results.length > 0))); // should return true

			if (results.length > 0) {
				startTest();
				log("Casted value type");
				if (!((results[0][0] instanceof BNode) && (results[0][1] instanceof URI) && (results[0][2] instanceof Literal))) {
					log("TEST 1e Value types: " + (ok && (results[0][0] == null) ? null : results[0][0].getClass().getName()) + ", " + ((results[0][1] == null) ? null : results[0][1].getClass().getName()) + ", " + ((results[0][2] == null) ? null : results[0][2].getClass().getName())); // should
				}
				endTest((ok && (results[0][0] instanceof BNode) && (results[0][1] instanceof URI) && (results[0][2] instanceof Literal))); // should return true
			}

			startTest();
			try {
				ok = true;
				log("Selecting property");
				query = "SELECT * FROM <" + context + "> WHERE {?s <http://mso.monrai.com/foaf/name> ?o} LIMIT 1";
				results = doTupleQuery(con, query);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && (results.length > 0))); // should return true

			boolean exists = false;
			startTest();
			try {
				ok = true;
				con.add(shermanmonroe, name, nameValue, context);
				exists = con.hasStatement(shermanmonroe, name, null, false, context);
				if (!exists) throw new Exception("Triple wasn't added");
				// test remove a statement
				con.remove(shermanmonroe, name, nameValue, (Resource) context);
				// test statement removed
				log("Statement does not exists");
				exists = con.hasStatement(shermanmonroe, name, null, false, context);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && !exists)); // should return false

			startTest();
			try {
				ok = true;
				log("Statement exists (by resultset size)");
				con.add(shermanmonroe, name, nameValue, context);
				exists = con.hasStatement(shermanmonroe, name, null, false, context);
				if (!exists) throw new Exception("Triple wasn't added");
				query = "SELECT * FROM <" + context + "> WHERE {?s <http://mso.monrai.com/foaf/name> ?o} LIMIT 1";
				results = doTupleQuery(con, query);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && (results.length > 0))); // should return true

			startTest();
			try {
				ok = true;
				log("Statement exists (by hasStatement())");
				exists = con.hasStatement(shermanmonroe, name, null, false, context);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && exists)); // should return true

			// test getNamespace
			Namespace testns = null;
			RepositoryResult<Namespace> namespaces = null;
			boolean hasNamespaces = false;

			try {
				namespaces = con.getNamespaces();
				hasNamespaces = namespaces.hasNext();
				while (namespaces.hasNext()) {
					Namespace ns = namespaces.next();
					// LOG("Namespace found: (" + ns.getName() + " " + ns.getPrefix() + ")");
					testns = ns;
				}
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}

			// test getNamespaces and RepositoryResult implementation
			startTest();
			try {
				ok = true;
				log("Retrieving namespaces");
				if (testns != null) {
					// LOG("Retrieving namespace (" + testns.getName() + " " + testns.getPrefix() + ")");
					String ns = con.getNamespace(testns.getPrefix());
					if (hasNamespaces) ok = (ns != null);
					else ok = false;
				}
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest(ok); // should return true

			RepositoryResult<Statement> statements = null;
			// test getStatements and RepositoryResult implementation
			startTest();
			try {
				ok = true;
				log("Retrieving statement (" + shermanmonroe + " " + name + " " + null + ")");
				statements = con.getStatements(shermanmonroe, name, null, false, context);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && statements.hasNext())); // should return true
			while (statements.hasNext()) {
				Statement st = statements.next();
				// System.out.println("Statement found: (" + st.getSubject() + " " + st.getPredicate() + " " + st.getObject() + ")");
			}

			startTest();
			// test export and handlers
			File f = new File("results.n3.txt");
			try {
				ok = true;
				log("Writing the statements to file: (" + f.getAbsolutePath() + ")");
				RDFHandler ntw = new NTriplesWriter(new FileOutputStream(f));
				con.exportStatements(shermanmonroe, name, null, false, ntw);
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && f.exists())); // should return true

			startTest();
			RepositoryResult<Resource> contexts = null;
			// test retrieve graph ids
			try {
				ok = true;
				log("Retrieving graph ids");
				contexts = con.getContextIDs();
			}
			catch (Exception e) {
				log("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest((ok && contexts != null ? contexts.hasNext() : false)); // should return true
			if (contexts != null) while (contexts.hasNext()) {
				Value id = contexts.next();
				if ((id instanceof Literal)) log("Literal value for graphid found: (" + ((Literal) id).getLabel() + ")");
			}

			startTest();
			// test get size
			try {
				ok = true;
				log("Retrieving triple store size");
				// sz = con.size(context);
				// System.out.println("TEST 10: Passed: " + (sz > 0)); // should return sz > 0 results
			}
			catch (Exception e) {
				System.out.println("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest(ok); // should return sz > 0 results

			// do ask
			startTest();
			boolean result = false;
			try {
				ok = true;
				log("Sending ask query");
				query = "ASK FROM <" + context + "> {?s <http://mso.monrai.com/foaf/name> ?o}";
				result = doBooleanQuery(con, query);
			}
			catch (Exception e) {
				System.out.println("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest(ok && result); // should return sz > 0 results	

			// do construct
			startTest();
			Graph g = new GraphImpl();
			boolean statementFound = false;
			try {
				ok = true;
				log("Sending construct query");
				query = "CONSTRUCT {?s <http://mso.monrai.com/mlo/handle> ?o} FROM <" + context + "> WHERE {?s <http://mso.monrai.com/foaf/name> ?o}";
				g = doGraphQuery(con, query);
				Iterator<Statement> it = g.iterator();
				statementFound = true;
				while(it.hasNext()) {
					Statement st = it.next();
					if( !st.getPredicate().stringValue().equals("http://mso.monrai.com/mlo/handle")) statementFound = false;
				}
			}
			catch (Exception e) {
				System.out.println("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest(ok && g.size() > 0); // should return sz > 0 results	

			// do describe
			startTest();
			g = new GraphImpl();
			statementFound = false;
			try {
				ok = true;
				log("Sending describe query");
				query = "DESCRIBE ?s FROM <" + context + "> WHERE {?s <http://mso.monrai.com/foaf/name> ?o}";
				g = doGraphQuery(con, query);
				Iterator<Statement> it = g.iterator();
				statementFound = it.hasNext();
				//				while(it.hasNext()) {
				//					Statement st = it.next();
				//					if( !st.getPredicate().stringValue().equals("http://mso.monrai.com/mlo/handle")) statementFound = false;
				//				}
			}
			catch (Exception e) {
				System.out.println("Error[" + e + "]");
				e.printStackTrace();
				ok = false;
			}
			endTest(ok && statementFound); // should return sz > 0 results	

			// get total passed and failed
			getTotal();
		}
		catch (Exception e) {
			System.out.println("ERROR Test Failed.");
			e.printStackTrace();
		}
		finally {
			if (con != null) try {
				con.close();
			}
			catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean doBooleanQuery(RepositoryConnection con, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		BooleanQuery resultsTable = con.prepareBooleanQuery(QueryLanguage.SPARQL, query);
		return resultsTable.evaluate();
		//
		//		Vector<Value[]> results = new Vector<Value[]>();
		//		for (int row = 0; bindings.hasNext(); row++) {
		//			// System.out.println("RESULT " + (row + 1) + ": ");
		//			BindingSet pairs = bindings.next();
		//			List<String> names = bindings.getBindingNames();
		//			Value[] rv = new Value[names.size()];
		//			for (int i = 0; i < names.size(); i++) {
		//				String name = names.get(i);
		//				Value value = pairs.getValue(name);
		//				rv[i] = value;
		//				// if(column > 0) System.out.print(", ");
		//				// System.out.println("\t" + name + "=" + value);
		//				// vars.add(value);
		//				// if(column + 1 == names.size()) System.out.println(";");
		//			}
		//			results.add(rv);
		//		}
		//		return (Value[][]) results.toArray(new Value[0][0]);
	}	

	private static Value[][] doTupleQuery(RepositoryConnection con, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
		TupleQueryResult bindings = resultsTable.evaluate();

		Vector<Value[]> results = new Vector<Value[]>();
		for (int row = 0; bindings.hasNext(); row++) {
			// System.out.println("RESULT " + (row + 1) + ": ");
			BindingSet pairs = bindings.next();
			List<String> names = bindings.getBindingNames();
			Value[] rv = new Value[names.size()];
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				Value value = pairs.getValue(name);
				rv[i] = value;
				// if(column > 0) System.out.print(", ");
				// System.out.println("\t" + name + "=" + value);
				// vars.add(value);
				// if(column + 1 == names.size()) System.out.println(";");
			}
			results.add(rv);
		}
		return (Value[][]) results.toArray(new Value[0][0]);
	}

	private static Graph doGraphQuery(RepositoryConnection con, String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		GraphQuery resultsTable = con.prepareGraphQuery(QueryLanguage.SPARQL, query);
		GraphQueryResult statements = resultsTable.evaluate();
		Graph g = new GraphImpl();

		Vector<Value[]> results = new Vector<Value[]>();
		for (int row = 0; statements.hasNext(); row++) {
			Statement pairs = statements.next();
			g.add(pairs);
			//			List<String> names = statements.getBindingNames();
			//			Value[] rv = new Value[names.size()];
			//			for (int i = 0; i < names.size(); i++) {
			//				String name = names.get(i);
			//				Value value = pairs.getValue(name);
			//				rv[i] = value;
			//			}
			//			results.add(rv);
		}
		//		return (Value[][]) results.toArray(new Value[0][0]);
		return g;
	}

	public static void test(String args[]) {
		try {
			String url;
			url = "jdbc:virtuoso://localhost:1111";
			Class.forName("virtuoso.jdbc3.Driver");
			Connection connection = DriverManager.getConnection(url, "dba", "123456");
			java.sql.Statement stmt = connection.createStatement();

			stmt.execute("clear graph <gr>");
			ResultSet rs = stmt.getResultSet();
			while (rs.next());

			stmt.execute("insert into graph <gr> " + "{ <aa> <bb> \"cc\" . <xx> <yy> <zz> . " + "  <mm> <nn> \"Some long literal with language\"@en . " + "  <oo> <pp> \"12345\"^^<http://www.w3.org/2001/XMLSchema#int> }");
			rs = stmt.getResultSet();
			while (rs.next());

			// output:valmode "LONG" turns RDF box on output
			// boolean more = stmt.execute("define output:valmode \"LONG\" select * from <gr> where { ?x ?y ?z }");
			boolean more = stmt.execute("select * from <gr> where { ?x ?y ?z }");
			ResultSetMetaData data = stmt.getResultSet().getMetaData();
			for (int i = 1; i <= data.getColumnCount(); i++)
				System.out.println(data.getColumnLabel(i) + "\t" + data.getColumnTypeName(i));
			System.out.println("===");
			if (more) {
				rs = stmt.getResultSet();
				while (rs.next()) {
					for (int i = 1; i <= data.getColumnCount(); i++) {
						String s = stmt.getResultSet().getString(i);
						Object o = stmt.getResultSet().getObject(i);
						// Value casted =
						System.out.print("Object type is " + o.getClass().getName() + " ");
						System.out.print(data.getColumnLabel(i) + " = ");
						if (o instanceof VirtuosoRdfBox) // Typed literal
						{
							VirtuosoRdfBox rb = (VirtuosoRdfBox) o;
							System.out.println(rb.rb_box + " lang=" + rb.getLang() + " type=" + rb.getType() + " ro_id=" + rb.rb_ro_id);
						}
						else if (o instanceof VirtuosoExtendedString) // String representing an IRI
						{
							VirtuosoExtendedString vs = (VirtuosoExtendedString) o;
							if (vs.iriType == VirtuosoExtendedString.IRI) System.out.println("<" + vs.str + ">");
							else if (vs.iriType == VirtuosoExtendedString.BNODE) System.out.println("<" + vs.str + ">");
							else // not reached atm, literals are String or RdfBox
								System.out.println("\"" + vs.str + "\"");
						}
						else if (stmt.getResultSet().wasNull()) System.out.println("NULL\t");
						else System.out.println(s + " (No extended type availible)\t");
					}
					System.out.println("---");
				}
				more = stmt.getMoreResults();
			}
			stmt.close();

			// Try making new typed literal
			// System.out.println("---");
			// VirtuosoRdfBox rb = new VirtuosoRdfBox (connection, "Some literal with many symbols over 20", null, "cz");
			// System.out.println (rb.rb_box + " lang=" + rb.getLang() + " type=" + rb.getType() + " ro_id=" + rb.rb_ro_id );

			connection.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("eof");
		System.exit(0);
	}

}
