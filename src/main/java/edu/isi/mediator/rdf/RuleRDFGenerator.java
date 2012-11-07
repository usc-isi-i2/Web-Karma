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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorUtil;
import edu.isi.mediator.rule.ConstTerm;
import edu.isi.mediator.rule.FunctionPredicate;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Rule;
import edu.isi.mediator.rule.Term;


/**
 * Class that provides methods for generating RDF.
 * Takes a source description (rule) and an output file 
 * and generates triples. Outputs the RDF triples in N3 notation
 * in the output file or Stdout.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class RuleRDFGenerator {

	/**
	 * if present in front of a value that represents a URI in the source
	 * that URI will be used in the output RDF (instead of building a new URI)
	 */
	static public String URI_FLAG = "IS_URI:";

	static private String PREFIX_SEPARATOR = ":";
	/**
	 * Namespace prefixes
	 */
	protected String sourcePrefix = null;
	private String ontologyPrefix = null;
	
	/**
	 * Rule for which we generate the RDF; can be either LAV or GLAV rule
	 */
	protected Rule rule;
	
	/**
	 * Write for output file or Stdout
	 */
	protected PrintWriter outWriter;
	
	/**
	 * Contains mapping of prefix name to source namespace.
	 * If prefix is not used in the source desc we have only one source namespace.
	 * key=prefix; value=namespace;
	 */
	protected Map<String,String> sourceNamespaces;
	/**
	 * Contains mapping of prefix name to ontology namespace.
	 * If prefix is not used in the source desc we have only one ontology namespace.
	 * key=prefix; value=namespace;
	 */
	protected Map<String,String> ontologyNamespaces;
	
	/**
	 * Contains mapping of column names to class names.
	 * key = column name; value = class name 
	 * key = NATIONALITY_ID; value = Country
	 */
	protected Map<String, String> classes = new HashMap<String,String>();;
	
	/**
	 * Used to generate unique values. Is the current time.
	 */
	protected String uniqueId;
	/**
	 * Used in conjunction with uniqueId to generate unique values.
	 * Each processed tuple has a unique rowId.
	 * This rowId is added to the int in uri(1), uri(2) making sure that the
	 * uri generated for  uri(1), uri(2) are unique, but that every time 
	 * we see uri(1) the same value is generated.
	 */
	protected int rowId = 0;

	/**
	 * Stop RDF generation after MAX_NUMBER_OF_TRIPLES
	 * if = -1; write ALL triples.
	 */
	private int MAX_NUMBER_OF_TRIPLES=-1;
	/**
	 * Triple counter.
	 */
	private int NUMBER_OF_TRIPLES=1;
	

	/**
	 * Constructs a RuleRDFGenerator.
	 * <br>NOTE: DOES NOT write namespaces to the output stream.
	 * @param rule
	 * 		source description; can be either LAV or GLAV rule
	 * <p> For ex: DEF_Nationality(NATIONALITY_ID, NATIONALITY) -> Country(uri(NATIONALITY_ID)) ^ hasName(uri(NATIONALITY_ID), NATIONALITY)
	 * @param sourceNamespaces
	 * 		map containing (prefix,namespace)
	 * <p>For ex: s:http://www.host.com/SOURCE
	 * @param ontologyNamespaces
	 * 		map containing (prefix,namespace)
	 * <p>For ex: o:http://www.host.com/ONTOLOGY
	 * @param outWriter
	 * 		output writer
	 * @throws MediatorException
	 * @throws IOException 
	 */
	public RuleRDFGenerator(Rule rule, Map<String,String> sourceNamespaces, Map<String,String> ontologyNamespaces, 
			PrintWriter outWriter) throws MediatorException, IOException{
		//System.out.println("Create RDFGenerator for:" + rule);
		this.sourceNamespaces = sourceNamespaces;
		this.ontologyNamespaces = ontologyNamespaces;
		//gets populated only if sourceNamespaces & ontologyNamespaces contain only one entry
		//otherwise get the prefix from the source description
		sourcePrefix = RDFUtil.getPrefix(sourceNamespaces);
		ontologyPrefix = RDFUtil.getPrefix(ontologyNamespaces);
		this.rule = rule;
        this.outWriter = outWriter;
        //write namespaces to out file
        //setNamespace();
        
        //generate uniqueId
        this.uniqueId = String.valueOf(Calendar.getInstance().getTimeInMillis());
	}

	//This constructor is called from RDFGenerator
	/**
	 * Constructs a RuleRDFGenerator.
	 * <br>NOTE: DOES NOT write namespaces to the output stream.
	 * @param rule
	 * 		source description; can be either LAV or GLAV rule
	 * <p> For ex: DEF_Nationality(NATIONALITY_ID, NATIONALITY) -> Country(uri(NATIONALITY_ID)) ^ hasName(uri(NATIONALITY_ID), NATIONALITY)
	 * @param sourceNamespaces
	 * 		map containing (prefix,namespace)
	 * <p>For ex: s:http://www.host.com/SOURCE
	 * @param ontologyNamespaces
	 * 		map containing (prefix,namespace)
	 * <p>For ex: o:http://www.host.com/ONTOLOGY
	 * @param outWriter
	 * 		output writer
	 * @param uniqueId
	 * 		unique id used to generate unique values. Is the current time.
	 * @throws MediatorException
	 * @throws IOException 
	 */
	public RuleRDFGenerator(Rule rule, Map<String,String> sourceNamespaces, Map<String,String> ontologyNamespaces,
			PrintWriter outWriter, String uniqueId) throws MediatorException{
		//System.out.println("Create RDFGenerator for:" + bRule);
		this.sourceNamespaces = sourceNamespaces;
		this.ontologyNamespaces = ontologyNamespaces;

		this.rule = rule;
		
		//gets populated only if sourceNamespaces & ontologyNamespaces contain only one entry
		//otherwise get the prefix from the source description
		sourcePrefix = RDFUtil.getPrefix(sourceNamespaces);
		ontologyPrefix = RDFUtil.getPrefix(ontologyNamespaces);
		
		this.outWriter=outWriter;
		this.uniqueId = uniqueId;
	}

	/**
	 * Sets the maximum number of generated triples. Process is aborted after the MAX_NUMBER_OF_TRIPLES is reached.
	 * <br> Use -1 to generate ALL triples.
	 * @param numberOfTriples
	 */
	protected void setMaxNumberOfTriples(int numberOfTriples){
		MAX_NUMBER_OF_TRIPLES = numberOfTriples;
	}
	
	/** Returns the number of generated triples.
	 * @return
	 * 		the number of generated triples.
	 */
	protected int getNumberOfGeneratedTriples(){
		return NUMBER_OF_TRIPLES;
	}
	
	/**
	 * Sets the seed for the gensym URI.
	 * @param s
	 * 		the seed
	 */
	public void setUniqueID(String s){
		uniqueId=s;
	}
	
	/**
	 * Generates triples and writes them to output.
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are attribute names used in the rule.
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @throws MediatorException
	 * @throws UnsupportedEncodingException 
	 */
	public boolean generateTriples(Map<String,String> values) throws MediatorException, UnsupportedEncodingException{
		
		//System.out.println("VALUES=" + values);
		
		//as the class names can come from the value of a column name, we have to clear
		//the classes hash map for every set of values, as the class name can be different
		//in each set; this was not necessary before as the class names were present in
		//the SD
		classes.clear();
		
		//increment rowId for each processed tuple
		rowId++;

		//print info in output file
		outWriter.println();
		//outWriter.print("# Row " + rowId + ":");

		/*
		String valuesStr = "";
		Set<Entry<String,String>> entries = values.entrySet();
		Iterator<Entry<String,String>> it = entries.iterator();
		while(it.hasNext()){
			Entry<String,String> entry = it.next();
			valuesStr += entry.getKey() + ":" + entry.getValue().replaceAll("\r\n", " ") + ";";
		}
		*/
		//System.out.println("VALUES1=" + valuesStr);
		//outWriter.println( "[" + valuesStr +"]");
		//////////////////////////////
		
		boolean tripleAdded = addClassStatements(values);
		if(!tripleAdded){
			//abort the process; max number of triples was reached
			return false;
		}
		tripleAdded = addPredicateStatements(values);
		if(!tripleAdded){
			//abort the process; max number of triples was reached
			return false;
		}
		return true;
	}
	
	/**
	 * Generates triples for unary predicates of type:
	 * <br>ClassName(uri(ColumnName))
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are variable names used in the rule.
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @throws MediatorException
	 * <p>
	 * Example:
	 * <br>Generates triples of the form:X a Y; 
	 * <br>s:Country_10 a dv:Country ;
	 * @throws UnsupportedEncodingException 
	 */
	protected boolean addClassStatements(Map<String,String> values) throws MediatorException, UnsupportedEncodingException{
		
		for(int i=0; i<rule.getConsequent().size(); i++){
			Predicate p = rule.getConsequent().get(i);

			//unary predicate
			if(p.getTerms().size()==1){
				boolean tripleAdded = addClassStatement(p, values);
				if(!tripleAdded){
					//abort the process; max number of triples was reached
					return false;
				}
			}
			else
				continue;			
		}
		return true;
	}

	/**
	 * Generates triples for binary predicates of type:
	 * PredicateName(uri(ColumnName),Value) or
	 * PredicateName(uri(ColumnName1),uri(ColumnName2))
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are variable names used in the rule.
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @throws MediatorException
	 * <p>
	 * Example:
	 * <br>Generates a triple of the form:X Y Z; 
	 * <br>s:Person_78 dv:hasNationality s:Country_10 .
	 * @throws UnsupportedEncodingException 
	 */
	protected boolean addPredicateStatements(Map<String,String> values) throws MediatorException, UnsupportedEncodingException{
		
		for(int i=0; i<rule.getConsequent().size(); i++){
			Predicate p = rule.getConsequent().get(i);

			//binary predicate
			if(p.getTerms().size()==2){
				boolean tripleAdded = addPredicateStatement(p, values);
				if(!tripleAdded){
					//abort the process; max number of triples was reached
					return false;
				}
			}
			else
				continue;
		}
		return true;
	}

	/**
	 * Generates triples for one unary predicate of type:
	 * ClassName(uri(ColumnName))
	 * @param p
	 * 		the predicate
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are variable names used in the rule.
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @throws MediatorException
	 * <p>
	 * Example:
	 * <br>Generates a triple of the form:X a Y; 
	 * <br>s:Country_10 a dv:Country ;
	 * @throws UnsupportedEncodingException 
	 */
	protected boolean addClassStatement(Predicate p, Map<String,String> values) throws MediatorException, UnsupportedEncodingException{
		String className = p.getName();
		//remove backtick
		if(className.startsWith("`"))
			className = className.substring(1,className.length()-1);
		
		//the class name is the VALUE of the column name (p=`expand@column_name(uri(`id`))) ; 
		//look for the value of column_name in values to find the actual class name 
		if(className.startsWith("expand@"))
			className = getExpandedName(className, values);
		
		if(className==null){
			//the value is empty or null; don't include this triple
			return true;
		}
		
		//System.out.println("Class Name=" + className);
		String ontologyPrefixName = ontologyPrefix;
		//split class name
		int ind = className.indexOf(PREFIX_SEPARATOR);
		//http: is not a prefix
		if(ind>0 && !className.startsWith("http:")){
			ontologyPrefixName = className.substring(0,ind); 
			className = className.substring(ind+1); 
		}
		
		//get the uri
		ArrayList<Term> terms = p.getTerms();
		Term t = terms.get(0);
		//should be a uri FunctionTerm uri(VAR_NAME)
		if(!(t instanceof FunctionTerm))
			throw new MediatorException("A subject should have only one uri FunctionTerm: " + p);
		
		FunctionPredicate uri = ((FunctionTerm) t).getFunction();
		
		String subject = evaluateURI(uri, className, values);
		if(subject==null){
			//could not construct the URI; do not add this triple
			return true;
		}
		
		//we assume that class names do not have spaces or other "strange" chars
		//className = URLEncoder.encode(className, "UTF-8");
		//String classFullName = "<" + ontologyPrefixName + ":" + className + ">";
		String classFullName = ontologyPrefixName + ":" + className;
		if(className.startsWith("http:")){
			classFullName = "<" + className + ">";
		}
		
		String statement = subject.toString() + " a " + classFullName + " .";
		
		boolean tripleAdded = addTriple(statement);
		if(!tripleAdded){
			//abort the process; max number of triples was reached
			return false;
		}
		else
			return true;
	}
	
	/**
	 * Generates triple for a binary predicate of type:
	 * <br>PredicateName(uri(ColumnName),Value) or
	 * <br>PredicateName(uri(ColumnName1),uri(ColumnName2))
	 * @param p
	 * 		the predicate
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are variable names used in the rule.
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @throws MediatorException
	 * <p>
	 * Example:
	 * <br>Generates a triple of the form:X Y Z; 
	 * <br>dv:hasNationality s:Person_78 , s:Country_10 .
	 * @throws UnsupportedEncodingException 
	 */
	protected boolean addPredicateStatement(Predicate p, Map<String,String> values) throws MediatorException, UnsupportedEncodingException{
		//remove back-tick
		String predicateName = p.getName();
		//System.out.println("Predicate Name=" + predicateName);
		if(predicateName.startsWith("`"))
			predicateName = predicateName.substring(1,predicateName.length()-1);

		//the predicate name is the VALUE of the column name (p=`expand@predicate_name(uri(`id`))) ; 
		//look for the value of column_name in values to find the actual class name 
		if(predicateName.startsWith("expand@"))
			predicateName = getExpandedName(predicateName, values);

		if(predicateName==null){
			//the value is empty or null; don't include this triple
			return true;
		}

		String ontologyPrefixName = ontologyPrefix;

		//split predicate name
		int ind = predicateName.indexOf(PREFIX_SEPARATOR);
		//http: is not a prefix
		if(ind>0 && !predicateName.startsWith("http:")){
			//remove the back-tick
			ontologyPrefixName = predicateName.substring(0,ind); 
			predicateName = predicateName.substring(ind+1); 
		}
		
		//we assume that predicate names do not have spaces or other "strange" chars
		//predicateName = URLEncoder.encode(predicateName, "UTF-8");
		//String predicate = "<" + ontologyPrefixName + ":" +  predicateName + ">";
		String predicate = ontologyPrefixName + ":" +  predicateName;
		if(predicateName.startsWith("http:")){
			predicate = "<" + predicateName +">";
		}
		
		ArrayList<Term> terms = p.getTerms();
		//should have exactly 2 terms
		//first is the subject, second is the value
		if(terms.size()!=2)
			throw new MediatorException("A predicate should have 2 terms: " + p);
		
		Term t1 = terms.get(0);
		Term t2 = terms.get(1);
		//t1 should be a uri FunctionTerm uri(VAR_NAME); related to the subject
		if(!(t1 instanceof FunctionTerm))
			throw new MediatorException("First term in predicate should be uri FunctionTerm: " + p);
		
		FunctionPredicate uri1 = ((FunctionTerm) t1).getFunction();
		
		String subject = evaluateURI(uri1, null, values);
		if(subject==null){
			//could not construct the URI; do not add this triple
			return true;
		}

		//System.out.println("Subject URI =" + subject);

		//t2 can be either a uri or a concat() or a varTerm
		String statement = subject.toString() + " " + predicate + " ";
		if(t2 instanceof FunctionTerm){
			FunctionPredicate func = ((FunctionTerm) t2).getFunction();
			if(func.getName().equals("uri")){
				//it's a URI
				String value = evaluateURI(func, null, values);
				if(value==null){
					//could not construct the URI; do not add this triple
					return true;
				}

				//System.out.println("Value URI =" + value);
				statement +=  value;
			}
			else{
				//it's another function
				String value = (String)func.evaluate(values);
				statement += prepareValue(value);
			}
		}
		else{
			//it's a VarTerm
			String varName = t2.getVar();
			String varValue = values.get(MediatorUtil.removeBacktick(varName));
			if(varValue==null)
				throw new MediatorException("The values map does not contain variable: " + varName + " Map is:" + values);
			statement += prepareValue(varValue);
			
			//I have a key that has no value OR has NULL value=>don't add this triple
			//all NULL values are transformed to "" before I get here, so that I can
			//handle "" and NULL the same way
			if(varValue.equals("") || varValue.equals("NULL")){
				statement = "";
			}
		}

		statement += " .";

		//ignore the statement if I had a NULL value
		if(!statement.equals(" .")){
			boolean tripleAdded = addTriple(statement);
			if(!tripleAdded){
				//abort the process; max number of triples was reached
				return false;
			}
			else
				return true;
		}
		return true;
	}
	
	/** Given a name of the form "expand@columnName", returns the value of 
	 * columnName from values;
	 * @param name
	 * 	a name of the form "expand@columnName"
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are variable names used in the rule.
	 * @return
	 * @throws MediatorException 
	 */
	private String getExpandedName(String name, Map<String,String> values) throws MediatorException{
		String columnName = name.substring(7);

		String varValue = values.get(columnName);
		if(varValue==null)
			throw new MediatorException("The values map does not contain variable: " + columnName + " Map is:" + values);
		if(varValue.equals("") || varValue.equals("NULL")){
			return null;
		}
		return varValue;
	}
	
	/**
	 * Returns a value valid for RDF N3 notation. It escapes the quotes.
	 * @param value
	 * @return
	 * 	a value valid for RDF N3 notation.
	 * <br>Note: look in RuleRDFMapper for a special case where the value is a URI.
	 */
	protected String prepareValue(String value){
		// Escaping the quotes
		value = value.replaceAll("\\\\", "\\\\\\\\");
		value = value.replaceAll("\"", "\\\\\"");
		
		// If newline present in the value, quote them around with triple quotes
		if (value.contains("\n") || value.contains("\r"))	
			return "\"\"\"" + value + "\"\"\"";
		else
			return "\"" + value + "\"";
	}
	
	/**
	 * Builds a URI FunctionPredicate and evaluates it.
	 * Adds additional terms to the existing predicate, namely: 
	 * sourcePrefix, sourceNamespace,className and isSeed
	 * Returns the URI constructed with the given values.
	 * @param uri
	 * 		the uri() predicate
	 * @param className
	 * 		the class that corresponds to the column names referred to in the uri function
	 * @param values
			map with <column_name, value>
	 * @return
	 * 		the URI constructed with the given values.
	 * @throws MediatorException 
	 * <p>
	 * A uri predicate can be:
	 * <br>uri(var_name) - in this case we get the associated value from 
	 * <br>					values and use this value to construct the URI and term "Seed" = false
	 * <br>uri(int) - in this case the integer specifies a seed used during the 
	 * <br>					evaluation of uri() to generate a random number and term "Seed" =true
	 * @throws UnsupportedEncodingException 
	 */
	protected String evaluateURI(FunctionPredicate uri, String className, Map<String,String> values)
				throws MediatorException, UnsupportedEncodingException {

		//System.out.println("function is:" + uri);
		
		//I want to leave the functions in the initial rule unchanged
		uri = uri.clone();
		
		//get variable name
		String varName = uri.getTerms().get(0).getVar();
		String varValue = "";
		
		//if the uri has more than one var name we have to append them
		//before adding to the classes map
		String allVarNames = "";
		
		if(varName==null){
			//the param in the uri is a seed not a variable name
			varValue=uri.getTerms().get(0).getVal();
			varValue = RDFUtil.gensym(uniqueId, rowId, varValue);
			varName = "v_" + varValue;
			allVarNames = varName;
		}
		else if(varName.startsWith("`expand@")){
				//for cases where we us the URI given as the value of the column
				varValue = getExpandedName(MediatorUtil.removeBacktick(varName), values);
				//if varValue=null or empty don't include this triple
				//don't include any triples involve this class
				return varValue;
		}else{
			for(int i=0; i< uri.getTerms().size(); i++){
				Term term = uri.getTerms().get(i);
				varName = term.getVar();
				//get its value
				String val = values.get(MediatorUtil.removeBacktick(varName));
				if(val==null)
					throw new MediatorException("The values map does not contain variable: " + varName + " Map is:" + values);
				if(i>0){ varValue += "_"; allVarNames += "_";}
				if(val.equals("NULL")){
					//create a gensym for this value
					// in case I have more than 1 NULL in a row I have to distinguish
					//between them, so I use the column id to generate unique gensyms
					val = RDFUtil.gensym(uniqueId, rowId,"NULL_c" + i);
				}
				else if(val.trim().isEmpty()){
					//for empty strings that are keys / I have to generate a URI I generate a gensym
					//create a gensym for this value
					// in case I have more than 1 empty string in a row I have to distinguish
					//between them, so I use the column id to generate unique gensyms
					val = RDFUtil.gensym(uniqueId, rowId,"EmptyStr_c" + i);
				}
				varValue += val;
				allVarNames += varName;
			}
		}
		
		if(className==null){
			//I don't know the class name but it should be in the classes map
			className = classes.get(allVarNames);
			if(className==null){
				if(uri.getTerms().size()==1 && varValue.startsWith(URI_FLAG)){
					//I have a value that is already a URI, so I don't need to construct it
					//I can just use it as is
				}
				else{
					//throw new MediatorException("Did not find equivalent class for:" + allVarNames + " in " + classes);
					//if a class was not found probably the value of the column that defines the class is empty
					//don't include any triples involve this class
					return null;
				}
			}
		}
		else{
			//add to the classes map
			classes.put(allVarNames,className);
		}
		
		//set terms for this function
		ArrayList<Term> terms = uri.getTerms();
		terms.clear();
		//add source prefix
		ConstTerm sourcePrefixTerm = new ConstTerm("SourcePrefix", sourcePrefix);
		terms.add(sourcePrefixTerm);
		//add sourceUri
		ConstTerm sourceUriTerm= new ConstTerm("SourceUri", sourceNamespaces.get(sourcePrefix));
		terms.add(sourceUriTerm);
		//add class name
		ConstTerm theClassName = new ConstTerm("ClassName", className);
		terms.add(theClassName);
		//add the value/seed
		ConstTerm theValue = new ConstTerm(varName, varValue);
		terms.add(theValue);
		
		//now evaluate it
		String subject = (String)uri.evaluate();
		return subject;
	}
	
	/** Writes a triple to the output.
	 * <br> Returns true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @param statement
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 */
	private boolean addTriple(String statement){
		outWriter.println(statement);
		if(MAX_NUMBER_OF_TRIPLES==-1){
			//write all triples
			return true;
		}
		else{
			NUMBER_OF_TRIPLES++;
			if(NUMBER_OF_TRIPLES<=MAX_NUMBER_OF_TRIPLES)
				return true;
			else
				return false;
		}
	}
	
}
