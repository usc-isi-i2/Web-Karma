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
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;

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
			ontManager.doImportAndUpdateCache(f[i]);
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
		semanticTypes.add( new SemanticType(null, new Label("http://mohsen.isi.edu/sample.owl#hasName"), new Label("http://mohsen.isi.edu/sample.owl#Person"), null, 0.0, false) );
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
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#PharmGKBId"), 
				new Label("http://halowiki/ob/category#Gene"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#PharmGKBId"), 
				new Label("http://halowiki/ob/category#Pathway"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#PharmGKBId"), 
				new Label("http://halowiki/ob/category#Disease"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#PharmGKBId"), 
				new Label("http://halowiki/ob/category#Drug"), null, 0.0, false) );
		
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#Label"), 
				new Label("http://halowiki/ob/category#Gene"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#Label"), 
				new Label("http://halowiki/ob/category#Pathway"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#Label"), 
				new Label("http://halowiki/ob/category#Disease"), null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, new Label("http://halowiki/ob/property#Label"), 
				new Label("http://halowiki/ob/category#Drug"), null, 0.0, false) );
		
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Disease"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Drug"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Gene"), null, 0.0) );
//		semanticTypes.add( new SemanticType(null, new URI("http://halowiki/ob/category#Pathway"), null, 0.0) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput3() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, new Label("http://www.sri.com/ontologies/DovetailOnto.owl#Attack"), null, null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://www.sri.com/ontologies/DovetailOnto.owl#Nation"), null, null, 0.0, false) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput4() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://vivoweb.org/ontology/core#email"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://vivoweb.org/ontology/core#phoneNumber"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://xmlns.com/foaf/0.1/firstName"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://xmlns.com/foaf/0.1/lastName"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://www.w3.org/2000/01/rdf-schema#label"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://vivoweb.org/ontology/core#preferredTitle"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://www.usc.edu/ontology/local#organizationID"), 
				new Label("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://www.w3.org/2000/01/rdf-schema#label"), 
				new Label("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, new Label("http://www.usc.edu/ontology/local#personID"), 
				new Label("http://vivoweb.org/ontology/core#FacultyMember"), null, 0.0, false) );




//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Position"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, new URI("http://vivoweb.org/ontology/core#Department"), null, 0.0, false) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput5() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType("h1", new Label("http://www.w3.org/2003/01/geo/wgs84_pos#lat"), 
				new Label("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h2", new Label("http://www.w3.org/2003/01/geo/wgs84_pos#long"), 
				new Label("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h3", new Label("http://www.geonames.org/ontology#name"), 
				new Label("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h4", new Label("http://www.geonames.org/ontology#name"), 
				new Label("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h5", new Label("http://www.geonames.org/ontology#countryCode"), 
				new Label("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );
		semanticTypes.add( new SemanticType("h6", new Label("http://www.geonames.org/ontology#name"), 
				new Label("http://www.geonames.org/ontology#Feature"), null, 0.0, false) );


		return semanticTypes;
	}
	
	public static DirectedWeightedMultigraph<Node, Link> getVivoTree() {
		OntologyManager ontManagar = new OntologyManager();
		loadOntologies(ontManagar);
		Alignment alignment = new Alignment(ontManagar);
		return alignment.getSteinerTree();
	}

	public static DirectedWeightedMultigraph<Node, Link> getGeoNamesNeighbourhoodTree() {
		OntologyManager ontManagar = new OntologyManager();
		loadOntologies(ontManagar);
		
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

		
		Alignment alignment = new Alignment(ontManagar);
		DirectedWeightedMultigraph<Node, Link> steinerTree = alignment.getSteinerTree();
		
//		alignment.duplicateDomainOfLink("http://www.geonames.org/ontology#name3");
//		
//		alignment.addUserLink("http://www.geonames.org/ontology#neighbour10");
//		alignment.addUserLink("http://www.geonames.org/ontology#nearby7");
//		alignment.addUserLink("http://www.geonames.org/ontology#parentFeature3");

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

	}
	
	private static void testAlignment() {
		OntologyManager ontManagar = new OntologyManager();
		loadOntologies(ontManagar);
//		System.out.println(ontManagar.getOntModel().getNsURIPrefix(new URI("http://vivoweb.org/ontology/core#"));
//		System.out.println(ontManagar.getOntModel().getNsPrefixURI("vivo"));
//		System.out.println(ontManagar.getOntModel().get("vivo"));
//		if (true) return;

		createTestInput1();
		createTestInput2();
		createTestInput3();
		createTestInput4();
		createTestInput5();


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
