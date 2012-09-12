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
package edu.isi.karma.modeling;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.URI;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.semantictypes.SemanticType;

public class Test {

	private static void loadOntologies(OntologyManager ontManager) {

		int size = 5;
		File[] f = new File[size];
		
		f[0] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\vivo-core-public-1.4.owl");
		f[1] = new File("C:\\Users\\mohsen\\Desktop\\karma\\vivo-core-public-1.5.owl");
		f[2] = new File("C:\\Users\\mohsen\\Desktop\\karma\\EDM-v1.owl");
		f[3] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\DovetailOnto_v1_0.rdf");
		f[4] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\geonames\\wgs84_pos-updated.xml");
		
//		f[0] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\vivo1.4-protege.owl");
//		f[1] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\sample.owl");
//		f[2] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\Wiki.owl");
//		f[3] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\DoveTailOntoRDF.owl");
//		f[4] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\Dovetail_ISI_mod.owl");
		
		for (int i = 1; i < 2; i++) {
			ontManager.doImport(f[i]);
		}
	}
	
	private static List<SemanticType> createTestInput1() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#City"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#hasCode"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#zipCode"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#hasModel"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#Country"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#Country"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#zipCode"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#City"), null, 0.0) );
		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#hasName"), new URI("http://mohsen.isi.edu/sample.owl#Person"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#hasName"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#hasName"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#live"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#Place"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#Plant"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#Person"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://mohsen.isi.edu/sample.owl#Animal"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://www.w3.org/2002/07/owl#Thing"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://www.w3.org/2002/07/owl#Thing"), null, 0.0) );
		
		return semanticTypes;
	}

	private static List<SemanticType> createTestInput2() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#PharmGKBId"), 
				new URI("http://halowiki/ob/category#Gene"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#PharmGKBId"), 
				new URI("http://halowiki/ob/category#Pathway"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#PharmGKBId"), 
				new URI("http://halowiki/ob/category#Disease"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#PharmGKBId"), 
				new URI("http://halowiki/ob/category#Drug"), null, 0.0, false) );
		
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#Label"), 
				new URI("http://halowiki/ob/category#Gene"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#Label"), 
				new URI("http://halowiki/ob/category#Pathway"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#Label"), 
				new URI("http://halowiki/ob/category#Disease"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/property#Label"), 
				new URI("http://halowiki/ob/category#Drug"), null, 0.0, false) );
		
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Disease"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Drug"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Gene"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Pathway"), null, 0.0) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput3() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, new URI("http://www.sri.com/ontologies/DovetailOnto.owl#Attack"), null, null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://www.sri.com/ontologies/DovetailOnto.owl#Nation"), null, null, 0.0, false) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput4() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#email"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#phoneNumber"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://xmlns.com/foaf/0.1/firstName"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://xmlns.com/foaf/0.1/lastName"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://www.w3.org/2000/01/rdf-schema#label"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#preferredTitle"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://www.usc.edu/ontology/local#organizationID"), 
				new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://www.w3.org/2000/01/rdf-schema#label"), 
				new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new URI("http://www.usc.edu/ontology/local#personID"), 
				new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );




//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Position"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput5() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType("h1", new URI("http://www.w3.org/2003/01/geo/wgs84_pos#lat"), 
				new URI("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h2", new URI("http://www.w3.org/2003/01/geo/wgs84_pos#long"), 
				new URI("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h3", new URI("http://www.geonames.org/ontology#name"), 
				new URI("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h4", new URI("http://www.geonames.org/ontology#name"), 
				new URI("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h5", new URI("http://www.geonames.org/ontology#countryCode"), 
				new URI("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h6", new URI("http://www.geonames.org/ontology#name"), 
				new URI("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );


		return semanticTypes;
	}
	
	public static DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getVivoTree() {
		OntologyManager ontManagar = new OntologyManager();
		loadOntologies(ontManagar);
		List<SemanticType> semTypes4 = createTestInput4();
		Alignment alignment = new Alignment(ontManagar, semTypes4);
		return alignment.getSteinerTree();
	}

	public static DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getGeoNamesNeighbourhoodTree() {
		OntologyManager ontManagar = new OntologyManager();
		loadOntologies(ontManagar);
		List<SemanticType> semTypes5 = createTestInput5();
		
//		Alignment alignment = new Alignment(ontManagar, semTypes5, false);
//		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree = alignment.getSteinerTree();
//		GraphUtil.printGraph(steinerTree);
//		
//		alignment.duplicateDomainOfLink(new URI("http://www.geonames.org/ontology#name1");
//		alignment.duplicateDomainOfLink(new URI("http://www.geonames.org/ontology#name1");
//		alignment.duplicateDomainOfLink(new URI("http://www.geonames.org/ontology#name1");
//		
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#name4");
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#name8");
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#name12");
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#countryCode4");
//
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#neighbour1");
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#nearby4");
//		alignment.addUserLink(new URI("http://www.geonames.org/ontology#parentFeature10");

		
		Alignment alignment = new Alignment(ontManagar, semTypes5, true);
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree = alignment.getSteinerTree();
		
		alignment.duplicateDomainOfLink("http://www.geonames.org/ontology#name3");
		
		alignment.addUserLink("http://www.geonames.org/ontology#neighbour10");
		alignment.addUserLink("http://www.geonames.org/ontology#nearby7");
		alignment.addUserLink("http://www.geonames.org/ontology#parentFeature3");

		GraphUtil.printGraphSimple(alignment.getAlignmentGraph());
//		GraphUtil.printGraphSimple(alignment.getSteinerTree());
		steinerTree = alignment.getSteinerTree();
		GraphUtil.printGraphSimple(alignment.getSteinerTree());
		return steinerTree;
	}
	
	public static void testOntologyImport() {
		
		OntologyManager ontManager = new OntologyManager();
		loadOntologies(ontManager);
		
//		OntologyCache cache =  ontManager.getOntCache();
//		List<String> list = ontManager.getDataPropertiesOfClass(new URI("http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing"), false);
//		List<String> list = ontManager.getObjectPropertiesOfClass(new URI("http://www.geonames.org/ontology#Feature"), false);
//		List<String> list = ontManager.getSubClasses(new URI("http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing"), false);
//		List<String> list = ontManager.getObjectPropertiesOfClass("http://www.sri.com/ontologies/DovetailOnto.owl#Entity", true);

		HashMap<String, List<String>> map = ontManager.getOntCache().getPropertyIndirectDomains();//.getPropertyDirectDomains();

		System.out.println(map.size());
		for (String s : map.keySet()) {
			System.out.println(s);
			for (String ss : map.get(s)) 
				System.out.println("\t" + ss);
		}
	}
	
	private static void testAlignment() {
		OntologyManager ontManagar = new OntologyManager();
		loadOntologies(ontManagar);
//		System.out.println(ontManagar.getOntModel().getNsURIPrefix(new URI("http://vivoweb.org/ontology/core#"));
//		System.out.println(ontManagar.getOntModel().getNsPrefixURI("vivo"));
//		System.out.println(ontManagar.getOntModel().get("vivo"));
//		if (true) return;

		List<SemanticType> semTypes1 = createTestInput1();
		List<SemanticType> semTypes2 = createTestInput2();
		List<SemanticType> semTypes3 = createTestInput3();
		List<SemanticType> semTypes4 = createTestInput4();
		List<SemanticType> semTypes5 = createTestInput5();

		Alignment alignment1 = null;
		Alignment alignment2 = null;
		Alignment alignment3 = null;
		Alignment alignment4 = null;
		Alignment alignment5 = null;
		alignment1 = new Alignment(ontManagar, semTypes1);
		alignment2 = new Alignment(ontManagar, semTypes2);
		alignment3 = new Alignment(ontManagar, semTypes3);
		alignment4 = new Alignment(ontManagar, semTypes4);
		alignment5 = new Alignment(ontManagar, semTypes5);
		
		GraphUtil.printGraphSimple(alignment1.getSteinerTree());
		GraphUtil.printGraphSimple(alignment2.getSteinerTree());
		GraphUtil.printGraphSimple(alignment3.getSteinerTree());
		GraphUtil.printGraphSimple(alignment4.getSteinerTree());
		GraphUtil.printGraphSimple(alignment5.getSteinerTree());
		
//		alignment.getSteinerTree();
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree = alignment5.getSteinerTree();
//		GraphUtil.printGraph(steinerTree);
		for (Vertex v : steinerTree.vertexSet()) {
			if (v.getSemanticType() != null)
				System.out.println(v.getSemanticType().getHNodeId());
		}
//		GraphUtil.printGraphSimple(alignment.getSteinerTree());
//		System.out.println(alignment.GetTreeRoot().getID());
//		GraphUtil.printGraphSimple(alignment.getSteinerTree());
//		System.out.println(alignment.GetTreeRoot().getID());
//		alignment.align();
//		GraphUtil.printGraphSimple(alignment.getSteinerTree());
//		System.out.println(alignment.GetTreeRoot().getID());

//		GraphUtil.printGraphSimple(alignment.getSteinerTree());

//		Alignment alignment2 = new Alignment(ontManagar, semTypes4);
//		GraphUtil.printGraphSimple(alignment2.getSteinerTree());
//		Alignment alignment3 = new Alignment(ontManagar, semTypes4);
//		GraphUtil.printGraphSimple(alignment3.getSteinerTree());


//		alignment.addUserLink(new URI("http://halowiki/ob/property#involves1");
//		GraphUtil.printGraph(alignment.getSteinerTree());
//		alignment.addUserLink(new URI("http://halowiki/ob/property#IsInvolvedIn1");
//		GraphUtil.printGraph(alignment.getSteinerTree());

		
//		alignment.getSteinerTree();
//		GraphUtil.printGraph(alignment.getSteinerTree());
		
//		alignment.addUILink(new URI("http://halowiki/ob/property#Causes1");
//		GraphUtil.printGraph(alignment.getSteinerTree());

	}
	
	public static void main(String[] args) {
		boolean test1 = true, test2 = false, test3 = false;
		if (test1) testOntologyImport();
		if (test2) testAlignment();
		if (test3) getGeoNamesNeighbourhoodTree();
	}
}
