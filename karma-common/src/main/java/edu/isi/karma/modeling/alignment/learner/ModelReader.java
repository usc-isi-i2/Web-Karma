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

package edu.isi.karma.modeling.alignment.learner;
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

import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.RandomGUID;

public class ModelReader {
	
//	public static String varPrefix = "var:";
	public static String attPrefix = "att:";
	
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
	
	
	public static void main(String[] args) throws Exception {
		
		List<SemanticModel> semanticModels = null;

		try {

			semanticModels = importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);
//			semanticModels = importSemanticModels(Params.INPUT_DIR);
			if (semanticModels != null) {
				for (SemanticModel sm : semanticModels) {
					sm.print();
					sm.writeGraphviz(Params.GRAPHVIS_DIR + sm.getName() + Params.GRAPHVIS_MAIN_FILE_EXT, true, true);
//					sm.writeJson(Params.MODEL_DIR + sm.getName() + Params.MODEL_MAIN_FILE_EXT);
					
					// To test JsonReader and JsonWriter
//					SemanticModel m = SemanticModel.readJson(Params.MODEL_DIR + sm.getName() + ".main.model.json");
//					m.writeJson(Params.MODEL_DIR + sm.getName() + ".main.model2.json");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		
//		List<ServiceModel> serviceModels = null;
//
//		try {
//
//			serviceModels = importServiceModels(Params.INPUT_DIR);
//			if (serviceModels != null) {
//				for (ServiceModel sm : serviceModels) {
//					sm.print();
//					sm.exportModelToGraphviz(Params.GRAPHVIS_DIR);
//					GraphUtil.serialize(sm.getModel(), Params.JGRAPHT_DIR + sm.getServiceNameWithPrefix() + ".main.jgraph");
//				}
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private static void initPrefixNsMapping() {
		
		prefixNsMapping = new HashMap<String, String>();
		
//		// experiment 1
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
		prefixNsMapping.put("km", "http://isi.edu/integration/karma/dev#");
		
				
		// experiment 2 - museum data
		prefixNsMapping.put("status", "http://metadataregistry.org/uri/RegStatus/");
		prefixNsMapping.put("owl2xml", "http://www.w3.org/2006/12/owl2-xml#");
		prefixNsMapping.put("schema", "http://schema.org/");
		prefixNsMapping.put("aac-ont", "http://www.americanartcollaborative.org/ontology/");
		prefixNsMapping.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixNsMapping.put("reg", "http://metadataregistry.org/uri/profile/RegAp/");
		prefixNsMapping.put("foaf", "http://xmlns.com/foaf/0.1/");
		prefixNsMapping.put("dcterms", "http://purl.org/dc/terms/");
		prefixNsMapping.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixNsMapping.put("DOLCE-Lite", "http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#");
		prefixNsMapping.put("dcmitype", "http://purl.org/dc/dcmitype/");
		prefixNsMapping.put("wgs84_pos", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixNsMapping.put("FRBRentitiesRDA", "http://rdvocab.info/uri/schema/FRBRentitiesRDA/");
		prefixNsMapping.put("saam-ont", "http://americanart.si.edu/ontology/");
		prefixNsMapping.put("wot", "http://xmlns.com/wot/0.1/");
		prefixNsMapping.put("edm", "http://www.europeana.eu/schemas/edm/");
		prefixNsMapping.put("dc", "http://purl.org/dc/elements/1.1/");
		prefixNsMapping.put("ElementsGr2", "http://rdvocab.info/ElementsGr2/");
		prefixNsMapping.put("skos", "http://www.w3.org/2008/05/skos#");
		prefixNsMapping.put("crm", "http://www.cidoc-crm.org/rdfs/cidoc-crm#");
		prefixNsMapping.put("vs", "http://www.w3.org/2003/06/sw-vocab-status/ns#");
		prefixNsMapping.put("frbr_core", "http://purl.org/vocab/frbr/core#");
		prefixNsMapping.put("owl", "http://www.w3.org/2002/07/owl#");
		prefixNsMapping.put("ore", "http://www.openarchives.org/ore/terms/");
		prefixNsMapping.put("abc", "http://metadata.net/harmony/abc#");
		prefixNsMapping.put("dcam", "http://purl.org/dc/dcam/");
		prefixNsMapping.put("rdfg", "http://www.w3.org/2004/03/trix/rdfg-1/");
		prefixNsMapping.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixNsMapping.put("rr", "http://www.w3.org/ns/r2rml#");
		prefixNsMapping.put("km-dev", "http://isi.edu/integration/karma/dev#");

	}
	
	public static List<SemanticModel> importSemanticModelsFromJsonFiles(String path, String fileExtension) throws Exception {

		File ff = new File(path);
		File[] files = ff.listFiles();
		
		List<SemanticModel> semanticModels = new ArrayList<SemanticModel>();
		
		for (File f : files) {
			if (f.getName().endsWith(fileExtension)) {
				SemanticModel model = SemanticModel.readJson(f.getAbsolutePath());
				semanticModels.add(model);
			}
		}
		
		return semanticModels;

	}
	
	public static List<SemanticModel> importSemanticModels(String importDir) throws IOException {
		
		initPrefixNsMapping();
		
		List<SemanticModel> semanticModels = new ArrayList<SemanticModel>();
		
		File dir = new File(importDir);
		File[] modelExamples = dir.listFiles();

		Pattern fileNamePattern = Pattern.compile("s[0-9](|[0-9])-.*\\.txt", Pattern.CASE_INSENSITIVE);
//		Pattern fileNamePattern = Pattern.compile("s1-.*\\.txt", Pattern.CASE_INSENSITIVE);
		Pattern serviceNamePattern = Pattern.compile("S[0-9](|[0-9]):(.*)\\(", Pattern.CASE_INSENSITIVE);
		Matcher matcher;

		String subject = "", predicate = "", object = "";
		
		int count = 1;
		
		if (modelExamples != null)
		for (File f : modelExamples) {

			String id = "s" + String.valueOf(count);
			String name = "", description = "";

			matcher = fileNamePattern.matcher(f.getName());
			if (!matcher.find()) {
				continue;
			}
			
			List<Statement> statements = null;
			LineNumberReader lr = new LineNumberReader(new FileReader(f));
			String curLine = "";
			while ((curLine = lr.readLine()) != null) {
				
				matcher = serviceNamePattern.matcher(curLine);
				if (matcher.find()) {
					name = f.getName().replaceAll(".txt", "");
					description = curLine.trim();
				}
				
				if (!curLine.trim().startsWith("<N3>"))
					continue;
				
				statements = new ArrayList<Statement>();
				while ((curLine = lr.readLine()) != null) {
					if (curLine.trim().startsWith("</N3>"))
						break;
//					System.out.println(curLine);
					if (curLine.trim().startsWith("#"))
						continue;
					
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
				
			}
			
			lr.close();

			DirectedWeightedMultigraph<Node, LabeledLink> graph = buildGraphsFromStatements2(statements);

			SemanticModel semanticModel = new SemanticModel(id, graph);
			semanticModel.setName(name);
			semanticModel.setDescription(description);

			semanticModels.add(semanticModel);
			count++;
		}
		
		return semanticModels;
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
	
	private static DirectedWeightedMultigraph<Node, LabeledLink> buildGraphsFromStatements2(List<Statement> statements) {
		
		if (statements == null || statements.size() == 0)
			return null;

		DirectedWeightedMultigraph<Node, LabeledLink> graph = 
				new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);
		
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
		String id;
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
				if (objStr.startsWith(attPrefix)) {
					id = new RandomGUID().toString();
					obj = new ColumnNode(id, objStr, objStr, null);
					SemanticType semanticType = new SemanticType(((ColumnNode)obj).getHNodeId(), 
							new Label(predicateStr), 
							subj.getLabel(), 
							Origin.User, 
							1.0, 
							false);
					((ColumnNode)obj).setUserSelectedSemanticType(semanticType);

				} else if (objStr.indexOf(":") == -1 && objStr.indexOf("\"") != -1) {
//					String literalId = "lit:" + serviceId + "_l" + String.valueOf(countOfLiterals); 
					obj = new LiteralNode(objStr, objStr, null, false);
//					countOfLiterals ++;
				} else
					obj = new InternalNode(objStr, new Label(objStr));
				
				graph.addVertex(obj);
			}
			
			LabeledLink e;
			if (obj instanceof InternalNode)
				e = new ObjectPropertyLink(LinkIdFactory.getLinkId(predicateStr, subj.getId(), obj.getId()), new Label(predicateStr), ObjectPropertyType.None);
			else
				e = new DataPropertyLink(LinkIdFactory.getLinkId(predicateStr, subj.getId(), obj.getId()), new Label(predicateStr));
			graph.addEdge(subj, obj, e);
			
		}
		
		return graph;
	}


}