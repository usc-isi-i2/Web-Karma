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

package edu.isi.karma.modeling.research;
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
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SimpleLink;


public class ModelReader {
	
//	public static String varPrefix = "var:";
	public static String attPrefix = "att:";
	
	private static String examplesDir = "/Users/mohsen/Dropbox/Service Modeling/research/";
	private static String typePredicate = "rdf:type";

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
		
		List<ServiceModel> serviceModels = null;
		try {
			serviceModels = importServiceModels();
		
			if (serviceModels != null) {
				for (ServiceModel sm : serviceModels) {
					sm.computeMatchedSubGraphs();
					sm.computeShortestPaths();
					sm.print();
					sm.exportModelsToGraphviz(examplesDir);
					sm.exportMatchedSubGraphToGraphviz(examplesDir);
					sm.exportShortestPathsToGraphviz(examplesDir);
					
				}
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static List<ServiceModel> importServiceModels() throws IOException {
		
		List<ServiceModel> serviceModels = new ArrayList<ServiceModel>();
		
		File dir = new File(examplesDir);
		File[] modelExamples = dir.listFiles();

		Pattern fileNamePattern = Pattern.compile("s[0-9](|[0-9])-.*\\.txt", Pattern.CASE_INSENSITIVE);
//		Pattern fileNamePattern = Pattern.compile("s1-.*\\.txt", Pattern.CASE_INSENSITIVE);
		Pattern serviceNamePattern = Pattern.compile("S[0-9](|[0-9]):(.*)\\(", Pattern.CASE_INSENSITIVE);
		Matcher matcher;

		String subject = "", predicate = "", object = "";
		String serviceName = "";
		
		if (modelExamples != null)
		for (File f : modelExamples) {

			matcher = fileNamePattern.matcher(f.getName());
			if (!matcher.find()) {
				continue;
			}

			ServiceModel serviceModel = new ServiceModel();
			
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
				
				DirectedWeightedMultigraph<Node, Link> graph = buildGraphsFromStatements2(statements);
				if (graph != null)
					serviceModel.addModel(graph);
			
			}
			
			lr.close();
			serviceModels.add(serviceModel);
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
	
	private static DirectedWeightedMultigraph<Node, Link> buildGraphsFromStatements2(List<Statement> statements) {
		
		DirectedWeightedMultigraph<Node, Link> graph = 
				new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		if (statements == null || statements.size() == 0)
			return null;
		
		// Assumption: there is only one rdf:type for each URI
		HashMap<String, Node> uri2Classes = new HashMap<String, Node>();
		for (Statement st : statements) {
			
			if (st.getPredicate().equalsIgnoreCase(typePredicate)) {
				
				Node classNode = new InternalNode(st.getObject(), null);
				uri2Classes.put(st.getSubject(), classNode);
				graph.addVertex(classNode);
				
			}
		}
		
		for (Statement st : statements) {
			
			if (st.getPredicate().equalsIgnoreCase(typePredicate)) 
				continue;
			
			Node subj = uri2Classes.get(st.getSubject());
			if (subj == null) {
				subj = new InternalNode(st.getSubject(), null);
				graph.addVertex(subj);
			}

			Node obj = uri2Classes.get(st.getObject());
			String objStr = st.getObject();
			if (obj == null) {
				if (objStr.startsWith(attPrefix))
					obj = new ColumnNode(objStr, null, null);
				else if (objStr.indexOf(":") == -1 && objStr.indexOf("\"") != -1)
					obj = new LiteralNode(objStr, objStr, null);
				else
					obj = new InternalNode(objStr, null);
				
				graph.addVertex(obj);
			}
			
			Link e = new SimpleLink(st.getPredicate(), null);
			graph.addEdge(subj, obj, e);
			
		}
		
		return graph;
	}


}