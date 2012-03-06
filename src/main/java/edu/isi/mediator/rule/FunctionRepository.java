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

package edu.isi.mediator.rule;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import edu.isi.mediator.rdf.RuleRDFMapper;

/**
 * Repository of function evaluated by FunctionPredicate.
 * <p>Functions can have either a fixed number of input parameters
 * <p> (always as string), OR one variable length input parameter,
 * <p> and return an Object type.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class FunctionRepository {
	

	/**
	 * Returns the URI constructed with the given values (the RDF triple "subject").
	 * @param sourceUri
	 * @param ontologyUri
	 * @param className
	 * @param value
	 * @return
	 * 		the URI constructed with the given values (the RDF triple "subject").
	 * 		Example: http://domain/sourceName/className_value
	 * @throws UnsupportedEncodingException 
	 */
	/*
	public static String uri(String sourcePrefix, String sourceUri, String className, String isSeed, String value ) throws UnsupportedEncodingException{
		
		String subject;
		if(Boolean.valueOf(isSeed)==Boolean.FALSE){
			//treat "value" as a value, not a seed
			String newValue = className +"_" + value;
			newValue = URLEncoder.encode(newValue, "UTF-8");
			subject = "<" + sourceUri + newValue + ">";
		}
		else{
			//value is a seed; values should have only one value
			String newValue = className + "_" + value;
			subject = sourcePrefix+ ":" + newValue;
		}
		return subject;		
	}
*/
	/**
	 * Returns the URI constructed with the given values (the RDF triple "subject").
	 * @param sourceUri
	 * @param ontologyUri
	 * @param className
	 * @param value
	 * @return
	 * 		the URI constructed with the given values (the RDF triple "subject").
	 * 		Example: http://domain/sourceName/className_value
	 * @throws UnsupportedEncodingException 
	 */
	public static String uri(String sourcePrefix, String sourceUri, String className,String value ) throws UnsupportedEncodingException{
		
		String subject;
		if(value.startsWith(RuleRDFMapper.URI_FLAG)){
			int ind = value.indexOf("http");
			if(ind>0)
				subject = "<" + value.substring(ind) + ">";
			else subject = "";
		}
		else{
			//treat "value" as a value, not a seed
			String newValue = className +"_" + value;
			//in VIVO + is not allowed in the URI, so I replace the + (equiv to the space) with _
			//also replace the % (not allowed in VIVO
			newValue = newValue.replaceAll(" ", "_");
			String encodedValue = URLEncoder.encode(newValue, "UTF-8");
			//these have to be after the encode
			encodedValue = encodedValue.replaceAll("\\+", "_");
			encodedValue = encodedValue.replaceAll("\\%", "");
			encodedValue = encodedValue.replaceAll("\\.", "_");
			if(newValue.equals(encodedValue)){
				//I don't have special characters, so I can use the prefix
				subject = sourcePrefix+ ":" + newValue;
			}
			else{
				//I have to use the URI
				subject = "<" + sourceUri + encodedValue + ">";
			}
		}
		return subject;		
	}

	/**
	 * Concatenates all values.
	 * @param values
	 * 		a variable list of values
	 * @return
	 * 		the concatenated value.
	 */
	public static String concat(String... values){
		
		String result = "";
		//the first value is the separator
		String sep = values[0];
		for(int i=1; i<values.length; i++){
			if(i>1) result += sep;
			result += values[i];
		}
		return result;
	}

}
