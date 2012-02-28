/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.mediator.rdf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

import edu.isi.mediator.domain.parser.DomainParser;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Rule;
import edu.isi.mediator.gav.util.MediatorLogger;


/**
 * Class that provides methods for generating RDF aligned to a predefined ontology using Karma source description rules.
 * <br> Generates triples in N3 notation.
 * <br> RDF generation stops after the MAX_NUMBER_OF_TRIPLES is reached.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class OntologyMapper {

	private Model model;
	/**
	 * Database name. Extracted from the inputURI.
	 */
	private String dbName;
	
	/**
	 * Stop RDF generation after MAX_NUMBER_OF_TRIPLES
	 */
	private int MAX_NUMBER_OF_TRIPLES=45000;
	
	/**
	 * Karma source description namespace.
	 */
	static private String isivocab = "http://www.isi.edu/ontologies/DovetailOnto_ISI.owl#";
	/**
	 * Namespace used for D2R properties. 
	 */
	private String propertyNamespace = "";
	
	/**
	 * map that contains variable names and their corresponding object name.
	 * Ex: if INCIDENT_ID belongs to table INCIDENT, key=INCIDENT_ID;val=?o_INCIDENT
	 * val is the object corresponding to the URI that we will map INCIDENT_ID to
	 */
	Map<String,String> objectURI = new HashMap<String,String>();
	
	private static final MediatorLogger logger = MediatorLogger.getLogger(OntologyMapper.class.getName());

	/** Constructs an OntologyMapper given a RDF model.
	 * <br> Maximum number of triples generated is 45000.
	 * @param model
	 * 		the RDF model
	 */
	public OntologyMapper(Model model){
		this.model=model;
	}
	
	/** Constructs an OntologyMapper given a RDF model and the maximum number of triples to be generated.
	 * @param model
	 * 		the RDF model
	 */
	public OntologyMapper(Model model, int maxNumberOfTriples){
		this.model=model;
		MAX_NUMBER_OF_TRIPLES=maxNumberOfTriples;
	}

	/**
	 * Returns RDF triples (in N3 notation) aligned to a predefined ontology defined by
	 * <br> Karma source description rules.
	 * <br> RDF generation stops after the MAX_NUMBER_OF_TRIPLES is reached.
	 * @param uri
	 * 		uri that needs to be aligned.
 	 * @return
	 * 		RDF triples in N3 notation.
	 * @throws MediatorException
	 * @throws IOException 
	 */
	public String getAlignedRdf(String uri) throws MediatorException, IOException{
		
		StringWriter s = new StringWriter();
		getAlignedRdf(uri, s, null);
		return s.toString();
	}

	/**
	 * Generates RDF triples (in N3 notation) aligned to a predefined ontology defined by
	 * <br> Karma source description rules. Generated RDF is written to the file "outputFileName"
	 * <br> RDF generation stops after the MAX_NUMBER_OF_TRIPLES is reached.
	 * @param uri
	 * 		uri that needs to be aligned.
	 * @param outputFileName
	 * 		output file.
	 * @throws MediatorException
	 * @throws IOException 
	 */
	public void getAlignedRdf(String uri, String outputFileName) throws MediatorException, IOException{
		getAlignedRdf(uri, null, outputFileName);
	}

	/**
	 * Generates RDF triples (in N3 notation) aligned to a predefined ontology defined by
	 * <br> Karma source description rules. Generated RDF is written to the file "outputFileName"
	 * <br> RDF generation stops after the MAX_NUMBER_OF_TRIPLES is reached.
	 * <br>	if outStr is null and outFile !=null => write to a file
	 * <br> if outStr != null and outFile =null => write to a string
	 * @param uri
	 * 		uri that needs to be aligned.
	 * @param outStr
	 * 		the output string OR null if the output is written to a file
	 * @param outFile
	 * 		the output file OR null if the output is written to a string
	 * @throws MediatorException
	 * @throws IOException
	 */
	private void getAlignedRdf(String uri, StringWriter outStr, String outFile) throws MediatorException, IOException{
		
		logger.info("Get aligned RDF for: " + uri);
		
		String tableName = RDFUtil.getTableName(uri);
		dbName = RDFUtil.getDatabaseName(uri);
		propertyNamespace = JenaUtil.getNamespace(model, uri, tableName);
		
		//get and parse the SD
		String sourceDesc = getSourceDescription(tableName);
		DomainParser sp = new DomainParser();
		RDFDomainModel dm = (RDFDomainModel)sp.parseDomain(sourceDesc);

		if(dm.getGAVRules().size()!= dm.getLAVRules().size())
			throw new MediatorException("Number of GAV and LAV rules should be the same." +
					"There should be a GAV rule that corresponds to every LAV Rule.");
		
		//create the output writer
		PrintWriter outWriter = getOutWriter(outStr, outFile);
		
		int maxNumberOfTriples = MAX_NUMBER_OF_TRIPLES;
		boolean setNamespace=true;
		//for each rule (LAV and GLAV)
		for(int i=0; i<dm.getAllRules().size(); i++){
			Rule rule = dm.getAllRules().get(i);

			//clear the query related structures
			objectURI.clear();
			
			//get values generated by executing SPARQL queries defined by queryRule.
			List<Map<String, String>> rowValues = getValues(rule, tableName, uri);

			/*
			//count number of empty results
			if(rowValues.isEmpty())
				noResult++;
			System.out.println("No Result " + noResult);
			*/
			
			logger.info("Generate aligned RDF ...");
			//construct a rule mapper
			RuleRDFMapper gen = new RuleRDFMapper(rule, dm.getSourceNamespaces(), dm.getOntologyNamespaces(), outWriter);
			//write namespaces only once
			if(setNamespace){
				RDFUtil.setNamespace(dm.getSourceNamespaces(), dm.getOntologyNamespaces(), outWriter);
				setNamespace=false;
			}
			//set the maximum number of triples to be generated by this rule mapper
			gen.setMaxNumberOfTriples(maxNumberOfTriples);
			boolean allTriplesAdded = gen.generateTriples(rowValues, uri, objectURI);
			if(!allTriplesAdded){
				//abort the process
				//outWriter.print("# Maximum number of triples was reached:" + MAX_NUMBER_OF_TRIPLES);
				break;
			}
			//if more than one rule mapper is needed (we have more than one rule for the inputURI)
			//we have to sure that the maxNumberOfTriples is counted over all the mappers
			int triplesGenerated=gen.getNumberOfGeneratedTriples();
			maxNumberOfTriples = maxNumberOfTriples-triplesGenerated+1;
		}
		outWriter.close();
		logger.info("DONE.");
	}

	/**
	 * Returns a PrintWriter.
	 * <br>	if outStr is null and outFile !=null => PrintWriter to a file
	 * <br> if outStr != null and outFile =null => PrintWriter to a string
	 * @param outStr
	 * 		the output string OR null if the output is written to a file
	 * @param outFile
	 * 		the output file OR null if the output is written to a string
	 * @return
	 * 		a PrintWriter
	 * @throws IOException
	 */
	private PrintWriter getOutWriter(StringWriter outStr, String outFile) throws IOException{
		PrintWriter outWriter = null;
		//create the output writer
		if(outStr==null){
			//create file writer
	        //output file
	        FileWriter fw = new FileWriter (outFile);
	        BufferedWriter bw = new BufferedWriter (fw);
	        outWriter = new PrintWriter (bw);
		}
		else{
			//create string writer
			//output to string
	        BufferedWriter bw = new BufferedWriter (outStr);
	        outWriter = new PrintWriter (bw);
		}
		return outWriter;
	}
	
	/**
	 * Executes each query generated by the queryRule and accumulates all values.
	 * <br>We could have more than one query if we have a relationship table t(var1, var2) 
	 * <br>where D2R generates the triple "var1 tableName var2" (tableName is the property) OR "var2 tableName var1" 
	 * @param rule
	 * 		the mapping rule
	 * @param tableName
	 * 		the table that contains the objects referred to in the URI.
	 * @param uri
	 * 		the URI that needs to be aligned
	 * @return
	 * 		all values generated by executing SPARQL queries defined by queryRule.
	 * @throws MediatorException
	 */
	private List<Map<String, String>> getValues(Rule rule, String tableName, String uri) throws MediatorException{
		//execute each query and accumulate the values
		//we could have more than one query if we have a relationship table t(var1, var2)
		//where D2R generates var1 tableName var2 (tableName is the property) OR var2 tableName var1 
		String select = getSelect(rule);
		List<String> where = getWhere(rule);
		List<Map<String, String>> rowValues = new ArrayList<Map<String, String>>();
		for(int q=0; q<where.size(); q++){
			rowValues.addAll(getValues(select, where.get(q), tableName, uri));
		}
		return rowValues;
	}

	/**
	 * Returns attr/value pairs for all tuples generated by executing a SPARQL query.
	 * <br> In the initial query the object that corresponds to "tableName" is replaced by URI.
	 * <br> URI refers to objects present in the table given by "tableName". 
	 * @param select
	 * 		select part of the query
	 * @param where
	 * 		where part of the query
	 * @param tableName
	 * 		the table that contains the objects referred to in the URI.
	 * @param uri
	 * 		the input URI
	 * @return
	 * 		attr/value pairs for all tuples generated by executing the query.
	 * @throws MediatorException
	 */
	private List<Map<String, String>> getValues(String select, String where,String tableName,String uri) throws MediatorException {
		//prepare query; replace relevant object name with the uri
		String newQuery = where;
		newQuery = newQuery.replaceAll("\\?o_" + tableName + " ", "<" + uri + "> ");
		//System.out.println("newq=" + newQuery);
		
		return JenaUtil.executeQueryExtractValues(model, select + newQuery);
	}

	/**
	 * Returns the select part of a SPARQL query constructed from the queryRule.
	 * <br> Adds to the select all variables and all objects referring to predicates.
	 * <br> These objects contain intermediary URIs necessary when building the final RDF.
	 * <br> Example:
	 * Rule: r(a,b,c,d)<- s1(a,b,c)^s2(`c*`,d) ; c* represents a primary key (used to make the joins in the query)  
 		SELECT DISTINCT   ?a ?b ?c ?d ?o_s1 ?o_s2
	 * @param rule
	 * 		the mapping rule (LAV or GLAV).
	 * @return
	 * 		the select part of a SPARQL query constructed from the queryRule.
	 * @throws MediatorException
	 */
	private String getSelect(Rule rule) throws MediatorException {

		String s = "PREFIX vocab: <" + propertyNamespace + "> ";
		s += "\n SELECT DISTINCT ";
		
		ArrayList<String> headVars = rule.getAllAntecedentVars();
		
		for(int i=0; i<headVars.size(); i++){
			String var = headVars.get(i);
			if(var.endsWith("*")){
				var = var.substring(0, var.length()-1);
			}

			s += " ?" + var + " "; 
		}

		//add to the select list the table objects
		for (int i=0; i<rule.getAntecedent().size(); i++){
			Predicate p = rule.getAntecedent().get(i);
			s += " ?o_" + p.getName();
		}

		/*
		//NOTE: In jena 2.6.4 "As" constructs are allowed (See below)
		//this made it easier to construct the query. I could have all the intermediary objects in the select
		// including the object referenced in the input URI
		//in 2.6.2 I can't have this, so I have to deal with that object separately 
		//look in RuleRDFMapper.getUri()
		for (int i=0; i<queryRule.getBody().size(); i++){
			Predicate p = queryRule.getBody().get(i);
			s += " (?o_" + p.getName() + " as ?o_" + p.getName() + "_return) ";
		}
		*/
		return s;
	}

	/** Returns the where part of a SPARQL query constructed from the queryRule.
	 * 	Multiple queries could be returned if we have relationship predicates. Look at getRelationshipQuery().
	 * <br>The query is generated by combining the predicates from the queryRule.
	 * <br>The objects and property names are constructed from the table/column names.
	 * <br> Example:
	 * Rule: r(a,b,c,d)<- s1(a,b,c)^s2(`c*`,d) ; c* represents a primary key (used to make the joins in the query)  
 		WHERE { ?o_s1 vocab:s1_a ?a . 
 				?o_s1 vocab:s1_b ?b . 
 				?o_s1 vocab:s1_c ?o_s2 . 
 				?o_s2 vocab:s1_c ?c . 
 				?o_s2 vocab:s1_d ?d . 
	  Rule: r(a,b,c,d)<- s1(a,b,`c*`)^s2(c,d) ; and s2 is a relationship predicate
	  2 queries are returned: 
	  WHERE {   ?o_s1 vocab:s1_a ?a . 
 		    	?o_s1 vocab:s1_b ?b . 
 				?o_s1 vocab:s1_c ?o_s2 .
 				?o_s1 vocab:s2 ?d } and 
	  WHERE {   ?o_s1 vocab:s1_a ?a . 
 		    	?o_s1 vocab:s1_b ?b . 
 				?o_s1 vocab:s1_c ?o_s2 .
 				?d vocab:s2 ?o_s1 } and 
	 * @param rule
	 * 		the mapping rule (LAV or GLAV).
	 * @return
	 * 		the where part of a SPARQL query constructed from the queryRule.
	 * @throws MediatorException
	 */
	private List<String> getWhere(Rule rule) throws MediatorException {

		List<String> whereQ = new ArrayList<String>();
		
		//get key-obj pairs; used for the joins
		//these are the obj that we join on
		HashMap<String,String> keyObj = getKeyObjects(rule);
		
		String s = "\n WHERE { ";
		
		//there could be predicates for which we generate 2 queries
		//look at getRelationshipQuery()
		//every time we get a predicate with 2 queries we have to duplicate
		// everything that we have so far
		List<String> where = new ArrayList<String>();
		where.add(" ");
		for (int i=0; i<rule.getAntecedent().size(); i++){
			Predicate p = rule.getAntecedent().get(i);
			String pQ;
			int whereSize=where.size();
			if(isRelationship(p)){
				//we get 2 queries
				String pQs[] = getRelationshipQuery(p, keyObj);
				for(int w = 0; w<whereSize; w++){
					String oneW = where.get(w);
					//System.out.println("PQ0=" + pQs[0]);
					where.set(w, oneW+pQs[0]);
					where.add(w, oneW+pQs[1]);
				}
			}
			else{
				//just one query
				pQ = getQuery(p, keyObj);
				for(int w = 0; w<whereSize; w++){
					String oneW = where.get(w);
					where.set(w, oneW+pQ);
				}
			}
		}
		//complete the where
		for(int w = 0; w<where.size(); w++){
			String oneW = where.get(w);
			//System.out.println("OneW=" + oneW);
			String newS = s + oneW + " } ";
			whereQ.add(newS);
		}
		
		return whereQ;
	}

	/**
	 * Returns a mapping between primary keys and the objects that they belong to.
	 * key = attribute name; value = obj that this attr belongs to.
	 * n attribute is a key if it ends with "*".
	 * Example: {c, o_s2)
	 * @param rule
	 * 		the mapping rule (LAV or GLAV).
	 * @return
	 * 		a mapping between primary keys and the objects that they belong to.
	 * @throws MediatorException 
	 */
	private HashMap<String, String> getKeyObjects(Rule rule) throws MediatorException {
		HashMap<String, String> obj = new HashMap<String, String>();
		for (int i=0; i<rule.getAntecedent().size(); i++){
			Predicate p = rule.getAntecedent().get(i);
			String tableName = p.getName();
			for(int j=0; j<p.getVars().size(); j++){
				String var = p.getVars().get(j);
				if(var.endsWith("*")){
					//I have to add this
					String newVar = var.substring(0, var.length()-1);
					String value = obj.get(newVar);
					if(value!=null){
						throw new MediatorException("A primary key with name " + newVar + " already exists.");
					}
					obj.put(newVar, "o_" + tableName);
				}
			}
		}
		return obj;
	}

	/** Returns SPARQL triples for a given predicate.
	 * <br>Replaces foreign key objects with the objects from keyObjects.
	 * <br>This generates the correct joins in the query.
	 * <br>Example: s1(a,b,c)
	 * 			?o_s1 vocab:s1_a ?a . 
 				?o_s1 vocab:s1_b ?b . 
 				?o_s1 vocab:s1_c ?o_s2 .
	 * @param p
	 * 		the predicate
	 * @param keyObjects
	 * 		primary key/object pairs (constructed by getKeyObjects().
	 * @return
	 * 		SPARQL triples for a given predicate.
	 */
	private String getQuery(Predicate p, HashMap<String, String> keyObjects) {
		String query = "";
		String tableName = p.getName();
		for(int i=0; i<p.getVars().size(); i++){
			String var = p.getVars().get(i);
			String propertyName = var;
			boolean isJoin = false;
			if(var.endsWith("*")){
				var = var.substring(0, var.length()-1);
				propertyName = var;
			}
			else{
				//check if this var is in the hashmap
				//if it is, it is a foreign key, so I have to replace
				//the object with the one from the map
				String obj = keyObjects.get(var);
				if(obj != null){
					var = obj;
					isJoin=true;
				}
			}
			objectURI.put(var, "o_" + tableName);
			if(isJoin)
				query += " ?o_" + tableName + " vocab:" + tableName + "_" + propertyName + " ?" + var + " .  \n";
			else
				query += " OPTIONAL { ?o_" + tableName + " vocab:" + tableName + "_" + propertyName + " ?" + var + " . } \n";
		}
		//System.out.println("PQ=" + query);
		return query;
	}

	/**
	 * Returns two queries for the given predicate. 
	 * <br> Note: it is a relationship predicate with 2 variables.
	 * <br> this is represented in RDF as (var1 prop:tableName var2) or (var2 prop:tableName var1)
	 * <br>Example: s1(a,b) : if a or b are foreign keys we replace ?a or ?b with the corresponfing object
	 * 			?a vocab:db.s1 ?b . 
	 * 			?b vocab:db.s1 ?a . 
	 * @param p
	 *		the predicate
	 * @param keyObjects
	 * 		primary key/object pairs (constructed by getKeyObjects().
	 * @return
	 * @throws MediatorException
	 */
	private String[] getRelationshipQuery(Predicate p, HashMap<String, String> keyObjects) throws MediatorException {
		String query[] = new String[2];
		String tableName = p.getName();
		if(p.getVars().size()!=2){
			throw new MediatorException("A relationship predicate must have exactly 2 attributes.");
		}
		String var1 = p.getVars().get(0);
		String var2 = p.getVars().get(1);
		String obj = keyObjects.get(var1);
		if(obj != null)
			var1 = obj;
		obj = keyObjects.get(var2);
		if(obj != null)
			var2 = obj;
		//I don't know which way the relationship is, so go both ways
		query[0] = " ?" + var2 + " vocab:" + dbName + tableName + " ?" + var1 + " .  \n";
		query[1] = " ?" + var1 + " vocab:" + dbName + tableName + " ?" + var2 + " .  \n";
		
		//System.out.println("PQ=" + query);
		return query;
	}

	/** Returns true if this is a relationship predicate.
	 * <br> The correct algorithm to determine if it's a relationship predicate is to
	 * <br> get all defined predicates and if we find the predicate dbName.(p.getName())
	 * <br> then it's a relationship pred. I couldn't find a way to get all defined predicates.
	 * @param p
	 * @return
	 * 		true if this is a relationship predicate, false otherwise.
	 * @throws MediatorException
	 */
	private boolean isRelationship(Predicate p) throws MediatorException{
		//check if properties represented by the column names exist
		if(p.getName().startsWith("XW_"))
			return true;
		return false;
		
	}
	
	/**
	 * Returns a mediator domain file containing the rules relevant to tableName.
	 * @param tableName
	 * @return
	 * @throws MediatorException
	 */
	private String getSourceDescription(String tableName) throws MediatorException{
				
		String domainFile = "";
		String namespaces = "\nNAMESPACES:\n";
		String glavRules = "";
		String lavRules = "";
		
		String q = "PREFIX isivocab: <" + isivocab + "> " +
		"SELECT DISTINCT ?o ?rule WHERE {" +
		" ?o isivocab:hasRule ?rule  . " +
		" ?o isivocab:hasRelevantSource \"" + tableName + "\". " +
		" }";

		String namespaceQ = "PREFIX isivocab: <" + isivocab + "> " +
		"SELECT DISTINCT ?namespace WHERE {" +
		" ?o isivocab:hasNamespace ?namespace . " +
		" }";
		
		List<Map<String, String>> allRules = JenaUtil.executeQuery(model, q);
		
		if(allRules.isEmpty())
			throw new MediatorException("No transformation rule found for: " + tableName);
		
		for(int i=0; i<allRules.size(); i++){
			Map<String, String> oneR = allRules.get(i);
			String ruleObj = oneR.get("o");
			String rule = oneR.get("rule");

			int ind = rule.lastIndexOf('^', rule.indexOf("->"));
			if(ind>0){
				//it is a GLAV rule
				glavRules += rule + "\n";
			}
			else{
				//it is a LAV rule
				lavRules += rule + "\n"; 
			}
			
			//get namespaces for each of the rules
			String newQ = namespaceQ;
			newQ = newQ.replaceAll("\\?o", "<" + ruleObj + ">");
			//System.out.println("newQ=" + newQ);

			List<Map<String, String>> allNamespaces = JenaUtil.executeQuery(model, newQ);
			for(int j=0; j<allNamespaces.size(); j++){
				Map<String, String> oneN = allNamespaces.get(j);
				String namespace = oneN.get("namespace");
				namespaces += namespace + "\n";
			}
		}
		domainFile = namespaces;
		if(!glavRules.trim().isEmpty()){
			domainFile += "\nGLAV_RULES:\n" + glavRules;
		}
		if(!lavRules.trim().isEmpty()){
			domainFile += "\nLAV_RULES:\n" + lavRules;
		}
		
		logger.debug("DF=" + domainFile);
		return domainFile;
	}
	
}
