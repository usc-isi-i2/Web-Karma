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

package edu.isi.karma.modeling.research.experiment2;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SimpleLink;


public class ModelReader2 {
	
//	public static String varPrefix = "var:";
	public static String attPrefix = "att:";
	
	private static String importDir1 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/models/dbpedia/";
	private static String exportDir1 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/dots/dbpedia/";

	private static String importDir2 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/models/schema/";
	private static String exportDir2 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/dots/schema/";

	private static String typePredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static HashMap<String, String> prefixNsMapping;

	static class Statement {
		
		public Statement(String subject, String predicate, String object) {
			this.subject = subject;
			this.predicate = predicate;
			this.object = object;
		}
		
		private String subject;
		private String predicate;
		private String object;
		
		public String getSubject() {
			return subject;
		}
		public String getPredicate() {
			return predicate;
		}
		public String getObject() {
			return object;
		}	
		public void print() {
			System.out.print("subject=" + this.subject);
			System.out.print(", predicate=" + this.predicate);
			System.out.println(", object=" + this.object);
		}
	}
	
	
	public static void main(String[] args) {
		
		List<ServiceModel2> serviceModels1 = null;
		List<ServiceModel2> serviceModels2 = null;

		try {

			serviceModels1 = importServiceModels(importDir1);
			if (serviceModels1 != null) {
				for (ServiceModel2 sm : serviceModels1) {
					sm.print();
					sm.exportModelToGraphviz(exportDir1);
				}
			}

			serviceModels2 = importServiceModels(importDir2);
			if (serviceModels2 != null) {
				for (ServiceModel2 sm : serviceModels2) {
					sm.print();
					sm.exportModelToGraphviz(exportDir2);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void initPrefixNsMapping() {
		
		prefixNsMapping = new HashMap<String, String>();
		
		prefixNsMapping.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixNsMapping.put("gn", "http://www.geonames.org/ontology#");
		prefixNsMapping.put("schema", "http://schema.org/");
		prefixNsMapping.put("dbpprop", "http://dbpedia.org/property/");
		prefixNsMapping.put("dbpedia-owl", "http://dbpedia.org/ontology/");
		prefixNsMapping.put("skos", "http://www.w3.org/2004/02/skos/core#");
		prefixNsMapping.put("tzont", "http://www.w3.org/2006/timezone#");
		prefixNsMapping.put("qudt", "http://qudt.org/1.1/schema/qudt#");
		prefixNsMapping.put("yago", "http://dbpedia.org/class/yago/");
		prefixNsMapping.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixNsMapping.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixNsMapping.put("foaf", "http://xmlns.com/foaf/0.1/");
	}
	
	public static List<ServiceModel2> importServiceModels(String importDir) throws IOException {
		
		initPrefixNsMapping();
		
		List<ServiceModel2> serviceModels = new ArrayList<ServiceModel2>();
		
		File dir = new File(importDir);
		File[] modelExamples = dir.listFiles();

		Pattern fileNamePattern = Pattern.compile("s[0-9](|[0-9])-.*\\.txt", Pattern.CASE_INSENSITIVE);
//		Pattern fileNamePattern = Pattern.compile("s1-.*\\.txt", Pattern.CASE_INSENSITIVE);
		Pattern serviceNamePattern = Pattern.compile("S[0-9](|[0-9]):(.*)\\(", Pattern.CASE_INSENSITIVE);
		Matcher matcher;

		String subject = "", predicate = "", object = "";
		String serviceName = "";
		
		int count = 1;
		
		if (modelExamples != null)
		for (File f : modelExamples) {

			matcher = fileNamePattern.matcher(f.getName());
			if (!matcher.find()) {
				continue;
			}

			ServiceModel2 serviceModel = new ServiceModel2("s" + String.valueOf(count));
			
			LineNumberReader lr = new LineNumberReader(new FileReader(f));
			String curLine = "";
			while ((curLine = lr.readLine()) != null) {
				
				matcher = serviceNamePattern.matcher(curLine);
				if (matcher.find()) {
					serviceModel.setServiceDescription(curLine.trim());
					serviceModel.setServiceNameWithPrefix(f.getName().replaceAll(".txt", ""));
					serviceName = matcher.group(2).trim();
					serviceModel.setServiceName(serviceName);
//					System.out.println(serviceName);
				}
				
				if (!curLine.trim().startsWith("<N3>"))
					continue;
				
				List<Statement> statements = new ArrayList<Statement>();
				while ((curLine = lr.readLine()) != null) {
					if (curLine.trim().startsWith("</N3>"))
						break;
//					System.out.println(curLine);
					
					String[] parts = curLine.trim().split("\\s+");
					if (parts == null || parts.length < 3) {
						System.out.println("Cannot extract statement from \"" + curLine + " \"");
						continue;
					}
					
					subject = parts[0].trim();
					predicate = parts[1].trim();
					object = parts[2].trim();
					Statement st = new Statement(subject, predicate, object);
					statements.add(st);
				}
				
				DirectedWeightedMultigraph<Node, Link> graph = buildGraphsFromStatements2(serviceModel.getId(), statements);
				if (graph != null)
					serviceModel.addModel(graph);
			
			}
			
			lr.close();
			serviceModels.add(serviceModel);
			count++;
		}
		
		return serviceModels;
	}

	
//	private static DirectedWeightedMultigraph<Node, Link> buildGraphsFromStatements(List<Statement> statements) {
//		
//		DirectedWeightedMultigraph<Node, Link> graph = 
//				new DirectedWeightedMultigraph<Node, Link>(Link.class);
//		
//		if (statements == null || statements.size() == 0)
//			return null;
//		
//		HashMap<String, Node> uri2Nodes = new HashMap<String, Node>();
//
//		for (Statement st : statements) {
//			
//			Node subj = uri2Nodes.get(st.getSubject());
//			if (subj == null) {
//				subj = new Node(st.getSubject(), null, null);
//				uri2Nodes.put(st.getSubject(), subj);
//				graph.addNode(subj);
//			}
//
//			Node obj = uri2Nodes.get(st.getObject());
//			if (obj == null) {
//				obj = new Node(st.getObject(), null, null);
//				uri2Nodes.put(st.getObject(), obj);
//				graph.addNode(obj);
//			}
//			
//			Link e = new Link(st.getPredicate(), null, null);
//			graph.addEdge(subj, obj, e);
//			
//		}
//		
//		return graph;
//	}
	
	private static String getUri(String prefixedUri) {
		
		String uri = prefixedUri;
		String prefix = "";
		String name = "";
		if (prefixedUri.indexOf(":") != -1) {
			prefix = prefixedUri.substring(0 , prefixedUri.indexOf(":")).trim();
			name = prefixedUri.substring(prefixedUri.indexOf(":") + 1 , prefixedUri.length()).trim();
			if (prefixNsMapping.containsKey(prefix)) {
				uri = prefixNsMapping.get(prefix) + name;
			}
		}
		return uri;
	}
	
	private static DirectedWeightedMultigraph<Node, Link> buildGraphsFromStatements2(String serviceId, List<Statement> statements) {
		
		DirectedWeightedMultigraph<Node, Link> graph = 
				new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		if (statements == null || statements.size() == 0)
			return null;
		
		// Assumption: there is only one rdf:type for each URI
		HashMap<String, Node> uri2Classes = new HashMap<String, Node>();
		for (Statement st : statements) {
			
			String subjStr = st.getSubject();
			String predicateStr = st.getPredicate();
			String objStr = st.getObject();
			
			subjStr = getUri(subjStr);
			predicateStr = getUri(predicateStr);
			objStr = getUri(objStr);

			if (predicateStr.equalsIgnoreCase(typePredicate)) {
				
				Node classNode = new InternalNode(objStr, new Label(objStr));
				uri2Classes.put(subjStr, classNode);
				graph.addVertex(classNode);
				
			}
		}
		
//		int countOfLiterals = 0;
		
		for (Statement st : statements) {
			
			String subjStr = st.getSubject();
			String predicateStr = st.getPredicate();
			String objStr = st.getObject();
			
			subjStr = getUri(subjStr);
			predicateStr = getUri(predicateStr);
			objStr = getUri(objStr);

			if (predicateStr.equalsIgnoreCase(typePredicate)) 
				continue;
			
			Node subj = uri2Classes.get(subjStr);
			if (subj == null) {
				subj = new InternalNode(subjStr, new Label(subjStr));
				graph.addVertex(subj);
			}

			Node obj = uri2Classes.get(objStr);
			if (obj == null) {
				if (objStr.startsWith(attPrefix))
					obj = new ColumnNode(objStr, null, null, "");
				else if (objStr.indexOf(":") == -1 && objStr.indexOf("\"") != -1) {
//					String literalId = "lit:" + serviceId + "_l" + String.valueOf(countOfLiterals); 
					obj = new LiteralNode(objStr, objStr, null);
//					countOfLiterals ++;
				} else
					obj = new InternalNode(objStr, new Label(objStr));
				
				graph.addVertex(obj);
			}
			
			Link e = new SimpleLink(predicateStr, new Label(predicateStr));
			graph.addEdge(subj, obj, e);
			
		}
		
		return graph;
	}


}