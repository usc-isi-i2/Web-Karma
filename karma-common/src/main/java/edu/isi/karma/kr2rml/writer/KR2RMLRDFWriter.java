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

import edu.isi.karma.kr2rml.PredicateObjectMap;


public interface KR2RMLRDFWriter {


	void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri);
	
	void outputTripleWithURIObject(PredicateObjectMap predicateObjectMap, String subjUri, String predicateUri,
			String objectUri);
	
	void outputTripleWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType);

	void outputTripleWithLiteralObject(PredicateObjectMap predicateObjectMap, String subjUri, String predicateUri,
			String value, String literalType);
	
	void outputQuadWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String graph);
	
	void outputQuadWithLiteralObject(PredicateObjectMap predicateObjectMap, String subjUri, String predicateUri,
			String value, String literalType, String graph);
	
	void finishRow();
	void flush();
	void close();
}
