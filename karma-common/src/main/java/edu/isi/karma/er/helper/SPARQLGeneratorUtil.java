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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.Predicate;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.mapping.R2RMLMapping;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.modeling.Uris;

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
	
	/**
	 * @param r2rmlMap The R2RMLMapping for the worksheet
	 * @param graph The graph url
	 * */
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
	
	
	public String get_query(TriplesMap root, ArrayList<HashMap<String, String>> columns,String graph) {
		return get_query(root, columns, false, graph);
	}
	
	/**
	 * @author shri
	 * This method will genereate a sparql query to select the list of columns (in the order they are provided)
	 * 
	 * @param root This object of TriplesMap is the root from which this method begins to fetch columns
	 * @param columns This ArrayList<String> has the list of columns to be fetched. These columns are identifyed by their complete URL as defined in the ontology. <br /> 
	 * For example: <http://isi.edu/integration/karma/ontologies/model/accelerometer#AccelerometerReading>. Now there may exists many instance of a class in within the same ontology. 
	 * */
	public String get_query(TriplesMap root, ArrayList<HashMap<String, String>> columns, boolean distinct_query, String graph) {
		
		ArrayList<Object> queue = new ArrayList<Object>();
		queue.add(root);
		StringBuffer query = new StringBuffer();
		this.var_count = 1;
		this.prefix_list = new HashMap<String, String>();
		this.select_params = new StringBuffer();
//		ArrayList<String> select_param = new ArrayList<String>();
		HashMap<TriplesMap, String> markedTriples = new HashMap<TriplesMap, String>();
		
		ArrayList<String> visited_columns = new ArrayList<String>();
		this.ParentMapingInfoList = new HashMap<String, SPARQLGeneratorUtil.ParentMapingInfo>();
		
		// save the column predicate url and the column name to be dislayed
		HashMap<Predicate, String> predicateList = new HashMap<Predicate, String>();
		HashMap<String, String> columnList = new HashMap<String, String>();
		if(columns != null && columns.size() > 0) {
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
				if(columns != null && columns.size() > 0) {
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
		
		sQuery.append(" where { ");
		
		
		if(graph == null || graph.isEmpty()) {
			sQuery.append(query.toString()).append(" } ");
		} else {
			sQuery.append(" GRAPH <").append(graph)
				.append("> { ").append(query.toString()).append(" } }");
		}
		
		logger.info("Generated Query : " + sQuery);
		return sQuery.toString();
	 }
	
	private String checkParentMarked(TriplesMap triple, HashMap<TriplesMap, String> markedTriples, String var1) {
		Stack<String> stk = new Stack<String>(); 
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

	
	
	public String get_fetch_column_query(String graphName, String nodeId) {
		
		StringBuffer query = new StringBuffer("prefix rr: <http://www.w3.org/ns/r2rml#> prefix km-dev: <http://isi.edu/integration/karma/dev#> ");
		query.append(" select distinct ?parentClass ?parentPredicate ?srcClass ?srcPredicate ?colName")
		.append(" where { ");
		if(graphName != null && !graphName.trim().isEmpty()) {
			query.append(" graph  <" + graphName + "> { ");
		}
		query.append(" 		?y1 rr:subjectMap/km-dev:alignmentNodeId \"").append(nodeId).append("\" . ")
		.append(" 		?y1 (rr:predicateObjectMap/rr:objectMap/rr:parentTriplesMap)* ?y2 .")
		.append(" 		?y2 rr:subjectMap/rr:class ?srcClass .")
		.append(" 		?y2 rr:predicateObjectMap ?pom11 .")
		.append(" 		optional {")
		.append(" 			?y3 rr:predicateObjectMap ?pom12 . ")
		.append(" 			?pom12 rr:objectMap/rr:parentTriplesMap ?y2 .")
		.append(" 			?y3 rr:subjectMap/rr:class ?parentClass .")
		.append(" 			?pom12 rr:predicate ?parentPredicate .")
		.append(" 		} . ")
		.append(" 		?pom11 rr:predicate ?srcPredicate .")
		.append("                 optional {")
		.append("     		   ?pom11 rr:objectMap/rr:column ?colName . } . ");
		if(graphName != null && !graphName.trim().isEmpty()) {
			query.append(" } ");
		}
		query.append(" } ");
		
		return query.toString();
	}
	
	public String getExploreServicesQuery(String graphUri, String rootNodeUri) {
		StringBuffer buf = new StringBuffer();
		buf.append("prefix rr: <http://www.w3.org/ns/r2rml#> ")
		.append("prefix km-dev: <http://isi.edu/integration/karma/dev#> ")

		.append("select distinct ?s1 ?parentClass ?parentPredicate ?srcParentClass ?srcParentPredicate ?srcClass ?srcPredicate ?colName ?totalArgs ?serviceUrl  ?serviceRequestMethod ?servicePostMethodType ?sericeRootNode")
		.append("   where { ")
		.append("	graph <"+graphUri+"> {")
		.append("		?y1 rr:subjectMap/km-dev:alignmentNodeId \""+rootNodeUri+"\" . ")
		.append("		?y1 (rr:predicateObjectMap/rr:objectMap/rr:parentTriplesMap)* ?y2 .")
		.append("		?y2 rr:subjectMap/rr:class ?srcClass .")
		.append("		?y2 rr:predicateObjectMap ?pom11 .	")
		.append("		optional {")
		.append("			?y3 rr:predicateObjectMap ?pom12 .")
		.append("			?pom12 rr:objectMap/rr:parentTriplesMap ?y2 .")
		.append("			?y3 rr:subjectMap/rr:class ?parentClass .")
		.append("			?pom12 rr:predicate ?parentPredicate .")
		.append("		} .")
		.append("		?pom11 rr:predicate ?srcPredicate .")
		.append("	}")
		.append("	{")
		.append("			select ?s1 ?srcParentClass ?srcParentPredicate ?srcClass ?srcPredicate ?colName ?totalArgs ?serviceUrl  ?serviceRequestMethod ?sericeRootNode")
		.append("			?servicePostMethodType  where {")
		.append("			?x1 a rr:TriplesMap .")
		.append("			?x1 rr:subjectMap/rr:class ?srcClass .")
		.append("			?x1 rr:predicateObjectMap ?pom1 .")
		.append("			?s1 a km-dev:webService .")
		.append("			?s1 km-dev:serviceInput ?pom1 .")
		.append("			?s1 km-dev:serviceMetadata ?meta .")
		.append("			?meta km-dev:serviceUrl ?serviceUrl . ")
		.append("			?meta km-dev:serviceRequestMethod ?serviceRequestMethod  . ")
		.append("			?meta km-dev:hasTriplesMap/rr:subjectMap ?s2  . ")
		.append("			?s2 a km-dev:steinerTreeRootNode . ")
		.append("			?s2 km-dev:alignmentNodeId ?sericeRootNode . ")
		.append("			optional { ?meta km-dev:servicePostMethodType ?servicePostMethodType }  . ")
		.append("			?s1 km-dev:serviceInputClass ?x1 .")
		.append("			?pom1 rr:predicate ?srcPredicate .")
		.append("			optional { ?pom1 rr:objectMap/rr:column ?colName } .")
		.append("			optional {")
		.append("				?s1 km-dev:serviceInputClass ?x2 .")
		.append("				?x2 rr:predicateObjectMap ?pom2 .")
		.append("				?pom2 rr:objectMap ?ob1 .")
		.append("				?pom2 rr:predicate ?srcParentPredicate .")
		.append("				?ob1 rr:parentTriplesMap ?x1 .")
		.append("				?x2 rr:subjectMap/rr:class ?srcParentClass .")
		.append("			} .")
		.append("			{")
		.append("				select (count(?arg) as ?totalArgs) ?s1 where {")
		.append("					?s1 a km-dev:webService .")
		.append("					?s1 km-dev:serviceInputColumn ?arg .")
		.append("				} group by ?s1 ")
		.append("			} } } }");

		
		
		
		return buf.toString();
	}
}
