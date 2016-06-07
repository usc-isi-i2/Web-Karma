/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.kr2rml.writer;

import java.io.PrintWriter;
import java.util.Properties;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;


public abstract class KR2RMLRDFWriter {


	protected PrintWriter outWriter;
	
	public void setWriter(PrintWriter outWriter)
	{
		this.outWriter = outWriter;
	}
	public void initialize(Properties p)
	{
		
	}
	public abstract void setR2RMLMappingIdentifier(R2RMLMappingIdentifier mappingIdentifer);
	
	public abstract void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri);
	
	public abstract void outputTripleWithURIObject(PredicateObjectMap predicateObjectMap, String subjUri, String predicateUri,
			String objectUri);
	
	public abstract void outputTripleWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String language);

	public abstract void outputTripleWithLiteralObject(PredicateObjectMap predicateObjectMap, String subjUri, String predicateUri,
			String value, String literalType, String language);
	
	public abstract void outputQuadWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String language, String graph);
	
	public abstract void outputQuadWithLiteralObject(PredicateObjectMap predicateObjectMap, String subjUri, String predicateUri,
			String value, String literalType, String language, String graph);
	
	public abstract void finishRow();
	public abstract void flush();
	public abstract void close();
}
