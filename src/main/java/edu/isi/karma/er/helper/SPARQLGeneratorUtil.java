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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.isi.karma.kr2rml.ObjectMap;
import edu.isi.karma.kr2rml.Predicate;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.R2RMLMapping;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.TriplesMap;

public class SPARQLGeneratorUtil {
	
	private StringBuffer select_params;
	private HashMap<String, String> prefix_list;
	private int var_count;
	private HashMap<String, ParentMapingInfo> ParentMapingInfoList;
	
	private void SPARQLGeneratorUtil() {
		this.var_count = 0;
	}
	// this 
	private class ParentMapingInfo {
		public TriplesMap parent;
		public Predicate predicate;
		public ParentMapingInfo(TriplesMap triple, Predicate predicate) {
			this.parent = triple;
			this.predicate = predicate;
		}
		
	}
	
	
	private void generate_sparql(TriplesMap node, String node_symbol, String graph) {
		
		ArrayList<Object> queue = new ArrayList<Object>();
		queue.add(node);
		StringBuffer query = new StringBuffer();
		this.var_count = 1;
		this.prefix_list = new HashMap<String, String>();
		this.select_params = new StringBuffer();
		HashMap<TriplesMap, String> markedTriples = new HashMap<TriplesMap, String>();
		
		this.ParentMapingInfoList = new HashMap<String, SPARQLGeneratorUtil.ParentMapingInfo>();
		
		HashMap<Predicate, String> predicateList = new HashMap<Predicate, String>();
		
		// using a BFS approach, we traverse the tree from the root node and add triples/predicates to the queue
		while(queue.size() > 0) {
			Object currentObj = queue.remove(0);
			
			// if this is a tripleMap, then add all its RefObjects to the queue
			// for the predicates, add only the ones that satisfy the criteria of being <...hasValue>
			if (currentObj instanceof TriplesMap) {
				String var = "x"+var_count;
				TriplesMap triple = (TriplesMap)currentObj;
				boolean foundHasValue = false;
				List<PredicateObjectMap> predicates = triple.getPredicateObjectMaps();
				
				for (PredicateObjectMap p_map : predicates) {
					
					if(p_map.getObject().hasRefObjectMap()) {
						RefObjectMap objMap = p_map.getObject().getRefObjectMap();
						queue.add(objMap.getParentTriplesMap());
						
						System.out.println(triple.getSubject().getId() + "  ---> " + objMap.getParentTriplesMap().getSubject().getId());
						
						// maintain a list of mapping properties between triples
						ParentMapingInfoList.put(objMap.getParentTriplesMap().getSubject().getId(), 
								new ParentMapingInfo(triple, p_map.getPredicate()));
						
					} else if(!foundHasValue) {
						if(p_map.getPredicate().getTemplate().toString().equalsIgnoreCase("<http://www.opengis.net/gml/hasValue>")) {
							queue.add(p_map.getPredicate());
							predicateList.put(p_map.getPredicate(), var);
							foundHasValue = true;
						}
					}
				}
				// if this triple is marked to be included in the query,
				// we add it to the markedTriples list and add to the query string
				// for its class type Eg. 
				// Prefix pref1: <.../.../Input>
				// x2 a pref1:
				if (foundHasValue) {
					markedTriples.put(triple, var);
					String rdfsTypes = triple.getSubject().getRdfsType().get(0).toString();
					this.prefix_list.put(rdfsTypes, "pref"+var_count);
					query.append(" ?"+var + " a pref"+var_count+": .");
					
					// if the parent of this triple is also marked for the query
					// then we add the relation to between triples to the query. Eg.
					
//					TriplesMap parentTriple = parent.get(triple.getSubject().getId());
					ParentMapingInfo parentTriple = ParentMapingInfoList.get(triple.getSubject().getId());
					
					if( parentTriple != null && markedTriples.containsKey(parentTriple.parent)) {
						String predicate = parentTriple.predicate.getTemplate().toString();
//						PredicateObjectMap parentPredicate = getPredicateBetweenTriples(triple, parentTriple);
						if(predicate != null) {
							query.append(" ?" + markedTriples.get(parentTriple.parent) + " " + 
								predicate + " ?"+var + " . ");
						} else {
							System.out.println("predicate is null from parent : " + triple.getSubject().getRdfsType().toString());
						}
					}
					
				}
				var_count++;
			}
			// if it is a predicate Object, create a variable in in the query string
			else if (currentObj instanceof Predicate) {
				Predicate predicate = (Predicate)currentObj;
				query.append(" ?" + predicateList.get(predicate)
						+ " " + predicate.getTemplate() + " ?z"+ var_count + " . ");
				select_params.append(" ?z" + var_count);
				var_count++;
				
			} 
			// if this is a RefObject add the Child Triple to the queue
			else if (currentObj instanceof RefObjectMap) {
				RefObjectMap refObj = (RefObjectMap)currentObj;
				TriplesMap t = refObj.getParentTriplesMap();
				queue.add(t);
				
			}
		}
		
		// generate the query from the list of prefix and the param lists
		Iterator<String> itr =  this.prefix_list.keySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next();
			System.out.println(" PREFIX " + this.prefix_list.get(key) + ": " + key);
		}
		if(graph == null || graph.isEmpty()) {
			System.out.println("select " + select_params + " where { " + query.toString() + " }");
		} else {
			System.out.println("select " + select_params + 
					" where { GRAPH <"+graph+"> { " + query.toString() + " } }");
		}
	}
	
	public void get_query(R2RMLMapping r2rmlMap, String graph) {
		
//		StringBuffer query = new StringBuffer();
//		StringBuffer select_terms = new StringBuffer();
		
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
		
//		qSPARQL = new StringBuffer();
//		this.select_params = new StringBuffer();
//		this.prefix_list = new HashMap<String, String>();
//		this.class_type_params = new ArrayList<String>();
//		var_count = 1;
		generate_sparql(root_node, "x"+var_count, graph);
//		// print the prefix list
//		
//		Iterator<String> itr =  this.prefix_list.keySet().iterator();
//		while(itr.hasNext()) {
//			String key = itr.next();
//			System.out.println("PREFIX " + this.prefix_list.get(key) + ": " + key);
//		}
//		if(graph == null || graph.isEmpty()) {
//			System.out.println("select " + select_params + " where { " + qSPARQL + " }");
//		} else {
//			System.out.println("select " + select_params + 
//					" where { GRAPH <"+graph+"> { " + qSPARQL + " } }");
//		}
		
//		int var_count = 1;
//		query.append(" ?x ?p <").append(root_node.getSubject().getRdfsType().get(0) + ">");
//		List<PredicateObjectMap> predicates = root_node.getPredicateObjectMaps();
//		for (PredicateObjectMap p_map : predicates) {
//			// this is a nested reference
//			if(p_map.getObject().hasRefObjectMap()) {
//				
//			} 
//			// this is simple reference
//			else  {
//				query.append(" ?xx" + var_count + " <" + p_map.getPredicate().getTemplate() + "> ?zz"+ (var_count++) + " . ");
//			} 
//					
//		}
//		System.out.println(query);
//		
	}

}
