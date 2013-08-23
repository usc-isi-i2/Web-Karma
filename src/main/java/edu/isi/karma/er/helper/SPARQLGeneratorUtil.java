/*******************************************************************************
 * Copyright 2012 University of Southern California
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

package edu.isi.karma.er.helper;

import java.util.List;

import edu.isi.karma.kr2rml.ObjectMap;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.R2RMLMapping;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.TriplesMap;

public class SPARQLGeneratorUtil {
	
	private void SPARQLGeneratorUtil() {
		
	}
	
	public static void get_query(R2RMLMapping r2rmlMap) {
		
		StringBuffer query = new StringBuffer();
		StringBuffer select_terms = new StringBuffer();
		
		System.out.println(r2rmlMap.toString());
		List<TriplesMap> triples = r2rmlMap.getTriplesMapList();
		TriplesMap root_node = null;

		// get the root node first
		for (TriplesMap row : triples) {
			if (row.getSubject().isSteinerTreeRootNode() ) {
				root_node = row;
				break;
			}
		}
		int var_count = 1;
		query.append(" ?x ?p <").append(root_node.getSubject().getRdfsType().get(0) + ">");
		List<PredicateObjectMap> predicates = root_node.getPredicateObjectMaps();
		for (PredicateObjectMap p_map : predicates) {
			// this is a nested reference
			if(p_map.getObject().hasRefObjectMap()) {
				
			} 
			// this is simple reference
			else  {
				query.append(" ?xx" + var_count + " <" + p_map.getPredicate().getTemplate() + "> ?zz"+ (var_count++) + " . ");
			} 
					
		}
		System.out.println(query);
		
	}

}
