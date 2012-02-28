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
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.FunctionPredicate;
import edu.isi.mediator.rule.Rule;
import edu.isi.mediator.rule.Term;
import edu.isi.mediator.gav.util.MediatorLogger;

public class RuleRDFMapper extends RuleRDFGenerator{

	/**
	 * URI for which we do the mapping
	 */
	private String inputURI;
	/**
	 * map that contains variable names and their corresponding object name.
	 * Ex: if INCIDENT_ID belongs to table INCIDEND, key=INCIDENT_ID;val=?o_INCIDENT
	 * val is the object corresponding to the URI that we will map INCIDENT_ID to
	 */
	private Map<String,String> objectURI;

	private static final MediatorLogger logger = MediatorLogger.getLogger(RuleRDFMapper.class.getName());

	/**
	 * Constructs a RuleRDFMapper.
	 * <br>NOTE: DOES NOT write namespaces to the output stream.
	 * @param rule
	 * 		source description
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
	public RuleRDFMapper(Rule rule, Map<String,String> sourceNamespaces, Map<String,String> ontologyNamespaces, 
			PrintWriter outWriter) throws MediatorException, IOException{
		super(rule, sourceNamespaces, ontologyNamespaces, outWriter);
	}
	
	/**
	 * Generates triples for all rows of data provided in values.
	 * @param values
	 * 		rows of variable/value pairs
	 * 		<br>each row is a map containing <column_name,value> where "column_name" are attribute names used in the rule.
	 * @return
	 * 		true if MAX_NUMBER_OF_TRIPLES was not reached, false otherwise.
	 * @throws MediatorException
	 * @throws UnsupportedEncodingException
	 * It is the responsibility of the programmer to close the writer when he/she is done.
	 */
	public boolean generateTriples(List<Map<String,String>> values, String inputURI, Map<String,String> objectURI) throws MediatorException, UnsupportedEncodingException{
		//outWriter.println();
		//outWriter.print("# Rule " +  rule.getHead());

        this.inputURI = inputURI;
        this.objectURI = objectURI;
		for(int i=0; i<values.size(); i++){
			Map<String,String> oneRow = values.get(i);
			boolean tripleAdded = generateTriples(oneRow);
			if(!tripleAdded){
				//abort the process; max number of triples was reached
				return false;
			}
			if(i%10000==0 && i>0)
				logger.info("Processed " + i + " rows.");
		}
		return true;
	}

	/**
	 * Returns a value valid for RDF N3 notation. It either escapes the quotes if it is a string value
	 *  or it prepares the URI if value is a URI (removes the marker OntologyMapper:URI: from the uri). 
	 * @param value
	 * @return
	 * 	a value valid for RDF N3 notation.
	 */
	protected String prepareValue(String value){
		//handle URI's separately
		if(value.startsWith(RuleRDFMapper.URI_FLAG)){
			int ind1 = value.indexOf("http");
			if(ind1>0)
				value = "<" + value.substring(ind1) + ">";
			else value="";
			return value;
		}
		else
			return RDFUtil.escapeQuote(value);
	}

	/**
	 * Returns the URI constructed with the given values.
	 * Constructs varName and varValue and calls getUri().
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
	 * <br>					values and use this value to construct the URI
	 * <br>uri(int) - in this case the integer specifies a seed used during the 
	 * <br>					evaluation of uri() to generate a random number
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
		
		boolean isGensym = false;
		if(varName==null){
			//the param in the uri is a seed not a variable name
			varValue=uri.getTerms().get(0).getVal();
			//varValue = uniqueId + "r" + String.valueOf(rowId)+varValue;
			varValue = RDFUtil.gensym(uniqueId, rowId, varValue);
			varName = "v_" + varValue;
			allVarNames = varName;
			isGensym = true;
		}
		else{
			for(int i=0; i< uri.getTerms().size(); i++){
				Term term = uri.getTerms().get(i);
				varName = term.getVar();
				//get its value
				String val = values.get(varName);
				if(val==null)
					throw new MediatorException("The values map does not contain variable: " + varName + " Map is:" + values);
				if(i>0){ varValue += "_"; allVarNames += "_";}
				if(val.equals("NULL")){
					//create a gensym for this value
					// in case I have more than 1 NULL in a row I have to distinguish
					//between them, so I use the column id to generate unique gensyms
					val = RDFUtil.gensym(uniqueId, rowId, "NULL_c" + i);
					isGensym=true;
				}
				varValue += val;
				allVarNames += varName;
			}
		}
		
		if(className==null){
			//I don't know the class name but it should be in the classes map
			className = classes.get(allVarNames);
			if(className==null){
				throw new MediatorException("Did not find equivalent class for:" + allVarNames + " in " + classes);
			}
		}
		else{
			//add to the classes map
			classes.put(allVarNames,className);
		}
		
		String uriValue = getUri(className, varValue, varName, values, isGensym);
		return uriValue;
	}

	/**
	 * Generates a URI.
	 * <br> Three cases are considered.
	 * <br> 1. a GenSym URI
	 * <br> 2. varValue is the URI (the URI starts with OntologyMapper:URI:. This is the URI we map to)
	 * <br> 3. varValue contains a constant value; BUT we don't construct URIs from constant values.
	 * <br> we have to find the URI that maps to this value. Check what object belongs to this varName (in objectURI).
	 * <br> If the object (?o_tableName) has a value in "values", use that. The only object that will not have
	 * <br> a value in "values" is the object that belongs to the inputURI, so use inputURI.
	 * <br>EX: {INDICATOR_NAME=Targeted, COMBATANT_FLAG=0, o_DEF_NATIONALITY=OntologyMapper:URI:http://www.dovetail.org/wits/SYSTEM.DEF_NATIONALITY/3, 
	 * o_DEF_DAMAGE=OntologyMapper:URI:http://www.dovetail.org/wits/SYSTEM.DEF_DAMAGE/2, TOTAL=3, 
	 * o_FACILITY=NULL, FACILITY_ID=10397, NATIONALITY=Iraq, DAMAGE=Light, 
	 * o_DEF_INDICATOR=OntologyMapper:URI:http://www.dovetail.org/wits/SYSTEM.DEF_INDICATOR/2, FACILITY_TYPE=Vehicle, 
	 * INCIDENT_ID=OntologyMapper:URI:http://www.dovetail.org/wits/SYSTEM.INCIDENT/14638}
	 * 
	 * In objectURI = (Facility_ID,?o_FACILITY); When we have to construct a URI with FACILITY_ID, we see that ?o_FACILITY is
	 * NULL, so we use the input URI : http://www.dovetail.org/wits/SYSTEM.FACILITY/10397
	 * @param className
	 * 		className of this URI
	 * @param varValue
	 * 		either a gensym value, a URI, or a constant
	 * @param varName
	 * 		the variable name
	 * @param values
			map with <column_name, value>
	 * @param isGensym
	 * 		true if a gensym URI should be generated, false otherwise
	 * @return
	 * 		a URI.
	 * @throws UnsupportedEncodingException
	 * @throws MediatorException 
	 */
	private String getUri(String className, String varValue, String varName, Map<String,String> values, boolean isGensym) throws UnsupportedEncodingException, MediatorException{
		//if the value is already a URI just return that
		if(varValue.startsWith(URI_FLAG)){
			int ind = varValue.indexOf("http");
			if(ind>0)
				return "<" + varValue.substring(ind) + ">";
			else return "";
		}
		else if(isGensym){
			String newValue = className +"_" + varValue;
			String subject = sourcePrefix+ ":" + newValue;
			return subject;		
		}
		else{
			//it must be a URI that has to be generated from a VALUE, BUT we don't construct URIs from constant values.
			//we have to find the URI that maps to this value. Check what object belongs to this varName (in objectURI).
			String objName = objectURI.get(varName);
			if(objName==null){
				throw new MediatorException("Should have a corresponding object for " + varName);
			}
			else{
				//use the object uri
				//get the value for this object
				String objValue = values.get(objName);

				if(objValue.startsWith(URI_FLAG)){
					int ind = objValue.indexOf("http");
					if(ind>0)
						return "<" + objValue.substring(ind) + ">";
					else return "";
				}
				else if(objValue.equals("NULL")){
					//use the input URI
					//the input object will not have a value because I replaced in the query all
					//instances of that object with the inputURI
					return "<" + inputURI + ">";
				}
				else{
					//should never get here
					String encodedValue = URLEncoder.encode(className +"_" + varValue, "UTF-8");
					return sourcePrefix+ ":" + encodedValue;
				}
			}
		}
	}

}
