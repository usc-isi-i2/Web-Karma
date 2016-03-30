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
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.Predicate;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.mapping.R2RMLMapping;
import edu.isi.karma.kr2rml.planning.TriplesMap;

/**
 * @author shri
 * */
public class SPARQLGeneratorUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SPARQLGeneratorUtil.class);
	private StringBuffer select_params;
	private HashMap<String, String> prefix_list;
	private int var_count;
	private HashMap<String, ParentMapingInfo> ParentMapingInfoList;
	
	private class ParentMapingInfo {
		public TriplesMap parent;
		public Predicate predicate;
		public ParentMapingInfo(TriplesMap triple, Predicate predicate) {
			this.parent = triple;
			this.predicate = predicate;
		}
		
	}
	
	
	private String generate_sparql(TriplesMap node, String node_symbol, String graph) {
		
		ArrayList<Object> queue = new ArrayList<>();
		queue.add(node);
		StringBuffer query = new StringBuffer();
		this.var_count = 1;
		this.prefix_list = new HashMap<>();
		this.select_params = new StringBuffer();
		HashMap<TriplesMap, String> markedTriples = new HashMap<>();
		
		this.ParentMapingInfoList = new HashMap<>();
		
		HashMap<Predicate, String> predicateList = new HashMap<>();
		
		// using a BFS approach, we traverse the tree from the root node and add triples/predicates to the queue
		while(!queue.isEmpty()) {
			Object currentObj = queue.remove(0);
			
			// if this is a tripleMap, then add all its RefObjects to the queue
			// for the predicates, add only the ones that satisfy the criteria of being <...hasValue>
			if (currentObj instanceof TriplesMap) {
				String var = "x"+var_count;
				TriplesMap triple = (TriplesMap)currentObj;
				boolean foundHasValue = false;
				List<PredicateObjectMap> predicates = triple.getPredicateObjectMaps();
				
				for (PredicateObjectMap p_map : predicates) {
					
					// if there are tripleMaps linked to the current tripleMap, then
					// we need to save their relation/linkage between them
					if(p_map.getObject().hasRefObjectMap()) {
						RefObjectMap objMap = p_map.getObject().getRefObjectMap();
						queue.add(objMap.getParentTriplesMap());
						
						logger.info(triple.getSubject().getId() + "  ---> " + objMap.getParentTriplesMap().getSubject().getId());
						
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
							logger.error("predicate is null from parent : " + triple.getSubject().getRdfsType().toString());
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
		StringBuffer sQuery = new StringBuffer();
		while(itr.hasNext()) {
			String key = itr.next();
			sQuery.append(" PREFIX ").append(this.prefix_list.get(key)).append(": ").append(key);
		}
		if(graph == null || graph.isEmpty()) {
			sQuery.append(" select ").append(select_params).append(" where { ").append(query.toString()).append(" } ");
		} else {
			sQuery.append(" select ").append(select_params).append(" where { GRAPH <").append(graph)
				.append("> { ").append(query.toString()).append(" } }");
		}
		logger.info("Genreated Query : " + sQuery);
		return sQuery.toString();
	}
	
	public String get_query(R2RMLMapping r2rmlMap, String graph) {
		
		List<TriplesMap> triples = r2rmlMap.getTriplesMapList();
		TriplesMap root_node = null;

		// get the root node first
		for (TriplesMap row : triples) {
			if (row.getSubject().isSteinerTreeRootNode() ) {
				root_node = row;
				break;
			}
		}
		return generate_sparql(root_node, "x"+var_count, graph);
	}
	
	
	public String get_query(TriplesMap root, ArrayList<HashMap<String, String>> columns) {
		return get_query(root, columns, false);
	}
	
	/**
	 * @author shri
	 * This method will genereate a sparql query to select the list of columns (in the order they are provided)
	 * 
	 * @param root This object of TriplesMap is the root from which this method begins to fetch columns
	 * @param columns This ArrayList<String> has the list of columns to be fetched. These columns are identifyed by their complete URL as defined in the ontology. <br /> 
	 * For example: <http://isi.edu/integration/karma/ontologies/model/accelerometer#AccelerometerReading>. Now there may exists many instance of a class in within the same ontology. 
	 * */
	public String get_query(TriplesMap root, ArrayList<HashMap<String, String>> columns, boolean distinct_query) {
		
		ArrayList<Object> queue = new ArrayList<>();
		queue.add(root);
		StringBuffer query = new StringBuffer();
		this.var_count = 1;
		this.prefix_list = new HashMap<>();
		this.select_params = new StringBuffer();
//		ArrayList<String> select_param = new ArrayList<String>();
		HashMap<TriplesMap, String> markedTriples = new HashMap<>();
		
		ArrayList<String> visited_columns = new ArrayList<>();
		this.ParentMapingInfoList = new HashMap<>();
		
		// save the column predicate url and the column name to be dislayed
		HashMap<Predicate, String> predicateList = new HashMap<>();
		HashMap<String, String> columnList = new HashMap<>();
		if(columns != null && !columns.isEmpty()) {
//			for (String k : columns) {
//				int index = 0;
//				if(k.indexOf("#") > 0) {
//					index = k.lastIndexOf('#')+1;
//				} else if(k.indexOf("/") > 0) {
//					index = k.lastIndexOf('/')+1;
//				}
//				columnList.put(k, k.substring(index, k.length()));
//			}
			for(HashMap<String, String> col : columns) {
				columnList.put(col.get("name"), col.get("url"));
			}
		}
		
		// using a BFS approach, we traverse the tree from the root node and add triples/predicates to the queue
		while(!queue.isEmpty()) {
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
						
						logger.info(triple.getSubject().getId() + "  ---> " + objMap.getParentTriplesMap().getSubject().getId());
						
						// maintain a list of mapping properties between triples
						ParentMapingInfoList.put(objMap.getParentTriplesMap().getSubject().getId(), 
								new ParentMapingInfo(triple, p_map.getPredicate()));
						
					} else {
							queue.add(p_map.getPredicate());
							predicateList.put(p_map.getPredicate(), var);
							foundHasValue = true;
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
					this.prefix_list.put("pref"+var_count, rdfsTypes);
					query.append(" ?"+var + " a pref"+var_count+": .");
					
					// if the parent of this triple is also marked for the query
					// then we add the relation to between triples to the query. Eg.
					
//					TriplesMap parentTriple = parent.get(triple.getSubject().getId());
					ParentMapingInfo parentTriple = ParentMapingInfoList.get(triple.getSubject().getId());
					
					// from the current node, keep poping out till we reach the last node in the 
					// parent map to see if any of the parents are connected
					if(parentTriple != null) {
						String sq = checkParentMarked(triple, markedTriples, var);
						if (sq.length() > 1) {
							query.append(sq);
						}
						
					}
//					if( parentTriple != null && markedTriples.containsKey(parentTriple.parent)) {
//						String predicate = parentTriple.predicate.getTemplate().toString();
////						PredicateObjectMap parentPredicate = getPredicateBetweenTriples(triple, parentTriple);
//						if(predicate != null) {
//							query.append(" ?" + markedTriples.get(parentTriple.parent) + " " + 
//								predicate + " ?"+var + " . ");
//						} else {
//							System.out.println("predicate is null from parent : " + triple.getSubject().getRdfsType().toString());
//						}
//					}
					
				}
				var_count++;
			}
			// if it is a predicate Object, create a variable in in the query string
			else if (currentObj instanceof Predicate) {
				Predicate predicate = (Predicate)currentObj;
				String k = predicate.getTemplate().toString();
				k = k.replace('<', ' ').replace('>', ' ').trim();
				if(columns != null && !columns.isEmpty()) {
//					if(columnList.containsKey(k)) {
					if(columnList.containsValue(k)) {
						Iterator<String> itr = columnList.keySet().iterator();
						while(itr.hasNext()) {
							String cName = itr.next();
							if(columnList.get(cName).equals(k) && !visited_columns.contains(cName)) {
								// get the column name from the end of this url - either the last '/' or the '#'
								query.append(" ?" + predicateList.get(predicate))
								.append(" ")
								.append(predicate.getTemplate())
								.append(" ?")
								.append(cName + " . ");
								//columnList.remove(cName);
								visited_columns.add(cName);
								var_count++;
								break;
							}
						}	
						// get the column name from the end of this url - either the last '/' or the '#'
//						query.append(" ?" + predicateList.get(predicate))
//							.append(" ")
//							.append(predicate.getTemplate())
//							.append(" ?")
//							.append(columnList.get(k) + " . ");
//						var_count++;
					} else {
						logger.info("ColumnList does not contain : " + k + " " + currentObj);
					}
				} else {
					int index = 0;
					if(k.indexOf("#") > 0) {
						index = k.lastIndexOf('#')+1;
					} else if(k.indexOf("/") > 0) {
						index = k.lastIndexOf('/')+1;
					}
					query.append(" ?" + predicateList.get(predicate))
						.append(" ")
						.append(predicate.getTemplate())
						.append(" ?").append(k.substring(index, k.length()))
						.append(" .");
					var_count++;
				}
				
			} 
			// if this is a RefObject add the Child Triple to the queue
			else if (currentObj instanceof RefObjectMap) {
				RefObjectMap refObj = (RefObjectMap)currentObj;
				TriplesMap t = refObj.getParentTriplesMap();
				queue.add(t);
				
			}
		}
		
		// append the list of prefixes
		Iterator<String> itr =  this.prefix_list.keySet().iterator();
		StringBuffer sQuery = new StringBuffer();
		while(itr.hasNext()) {
			String key = itr.next();
			sQuery.append(" PREFIX ").append(key).append(": ").append(this.prefix_list.get(key));
		}
		// append the columns to be selected in the order they are specified
		sQuery.append(" select ");
		if(distinct_query) {
			sQuery.append(" distinct ");
		}
		for (HashMap<String, String> s : columns) {
				sQuery.append(" ?"+s.get("name"));
		}
		sQuery.append(" where { ").append(query.toString()).append(" } ");
		logger.info("Generated Query : " + sQuery);
		return sQuery.toString();
	 }
	
	private String checkParentMarked(TriplesMap triple, HashMap<TriplesMap, String> markedTriples, String var1) {
		Stack<String> stk = new Stack<>(); 
		ParentMapingInfo parentTriple = this.ParentMapingInfoList.get(triple.getSubject().getId());
		boolean markedParent = false;
		int count = 1;
		String previous = var1;
		while(parentTriple!=null) {
			String predicate = parentTriple.predicate.getTemplate().toString();

			if(markedTriples.get(parentTriple.parent) == null) {
				
				markedTriples.put(parentTriple.parent, "Z"+count);
				String rdfsTypes = parentTriple.parent.getSubject().getRdfsType().get(0).toString();
				this.prefix_list.put("pref"+"Z"+count, rdfsTypes);
				stk.push(" ?Z"+count + " a pref"+"Z"+count+": .");
				stk.push(" ?" + markedTriples.get(parentTriple.parent) + " " +  predicate + " ?"+previous + " . ");
			} else {
				stk.push(" ?" + markedTriples.get(parentTriple.parent) + " " + 
						predicate + " ?"+previous + " . ");
				markedParent = true;
				break;
			}
			previous = markedTriples.get(parentTriple.parent);
			parentTriple = this.ParentMapingInfoList.get(parentTriple.parent.getSubject().getId());
			if(parentTriple == null) {
				break;
			}
			var1 = markedTriples.get(parentTriple.parent);
//			if(markedTriples.containsKey(parentTriple.parent)) {
//				markedParent = true;
//				stk.push(" ?" + markedTriples.get(parentTriple.parent) + " " + 
//						predicate + " ?"+var1 + " . ");
//				break;
//			}
		}
		 
		if (markedParent) {
			return StringUtils.join(stk," ");
		}
		return "";
	}

}
