package edu.isi.karma.modeling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.ontology.ImportOntology;
import edu.isi.karma.modeling.ontology.OntologyCache;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.semantictypes.SemanticType;

public class Test {

	private static void loadOntologies() {
//		File f1 = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\vivo-core.owl");
//		File f2 = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\sample.owl");
		File f3 = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\Wiki.owl");
//		ImportOntology imp1 = new ImportOntology(OntologyManager.Instance().getOntModel(), f1);
//		imp1.doImport();
//		ImportOntology imp2 = new ImportOntology(OntologyManager.Instance().getOntModel(), f2);
//		imp2.doImport();
		ImportOntology imp3 = new ImportOntology(OntologyManager.Instance().getOntModel(), f3);
		imp3.doImport();	
	}
	
	private static List<SemanticType> createTestInput1() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#City", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasCode", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#zipCode", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasModel", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Country", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Country", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#zipCode", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#City", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasName", "http://mohsen.isi.edu/sample.owl#Person", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasName", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasName", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#live", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Place", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Plant", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Person", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Animal", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://www.w3.org/2002/07/owl#Thing", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://www.w3.org/2002/07/owl#Thing", null, 0.0) );
		
		return semanticTypes;
	}

	private static List<SemanticType> createTestInput2() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Gene", null, 0.0) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Pathway", null, 0.0) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Disease", null, 0.0) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Drug", null, 0.0) );
		
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Gene", null, 0.0) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Pathway", null, 0.0) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Disease", null, 0.0) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Drug", null, 0.0) );
		
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Disease", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Drug", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Gene", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Pathway", null, 0.0) );
		
		return semanticTypes;
	}
	
	public static void main(String[] args) {
		
		loadOntologies();
		OntologyCache.Instance();
		
//		List<String> dataProperties = OntologyManager.Instance().getDataPropertiesOfClass( "http://mohsen.isi.edu/sample.owl#Address", false);
//		List<String> dataProperties = OntologyManager.Instance().getDataPropertiesOfClass( "http://xmlns.com/foaf/0.1/Agent", true);
//		List<String> dataProperties = OntologyManager.Instance().getDataPropertiesOfClass( "http://vivoweb.org/ontology/core#Committee", true);
//		List<String> dataProperties = OntologyManager.Instance().getDataPropertiesOfClass( "http://vivoweb.org/ontology/core#Address", true);
//		for (int i = 0; i < dataProperties.size(); i++) {
//			System.out.println(dataProperties.get(i));
//		}
//				
//		if (true)
//			return;
		
//		String e1 = "http://mohsen.isi.edu/sample.owl#hasName1";
//		String e2 = "http://mohsen.isi.edu/sample.owl#hasName2";
//		String e3 = "http://www.w3.org/2002/07/owl#hasSubClass10";
//		String e4 = "http://www.w3.org/2002/07/owl#zipCode2";
		
		Alignment alignment = new Alignment(createTestInput1());
		alignment = new Alignment(createTestInput2());
//		alignment.duplicateDomainOfLink(e4);

//		alignment.getSteinerTree();
		
		GraphUtil.printGraph(alignment.getSteinerTree());
		alignment.addUserLink("http://halowiki/ob/property#IsTargetedBy1");
		GraphUtil.printGraph(alignment.getSteinerTree());
		alignment.addUserLink("http://halowiki/ob/property#IsInvolvedIn1");
		GraphUtil.printGraph(alignment.getSteinerTree());

		
//		alignment.getSteinerTree();
//		GraphUtil.printGraph(alignment.getSteinerTree());
		
//		alignment.addUILink("http://halowiki/ob/property#Causes1");
//		GraphUtil.printGraph(alignment.getSteinerTree());

//		GraphUtil.printGraphSimple(alignment.getSteinerTree());
//		GraphUtil.printGraph(alignment.getSteinerTree());
//		GraphUtil.printGraph(alignment.getSteinerTree());

//		List<LabeledWeightedEdge> alternatives = alignment.getAlternatives("http://mohsen.isi.edu/sample.owl#hasName1", true);
//		for (LabeledWeightedEdge edge: alternatives) {
//			System.out.print(edge.getSource().getLocalID() + " ");
//			System.out.print(edge.getLocalID() + " ");
//			System.out.println(edge.getTarget().getLocalID());
//		}

//		alignment.reset();
//		GraphUtil.printGraph(alignment.getSteinerTree());
		
		
//		List<String> selectedLinks = new ArrayList<String>();
//		selectedLinks.add(e1);
//		selectedLinks.add(e2);
//		selectedLinks.add(e3);
//		alignment.addUserLinks(selectedLinks);
//		alignment.clearUserLinks(selectedLinks);
		
//		alignment.addUserLink(e2);
//		GraphUtil.printGraph(alignment.getSteinerTree());

//		alignment.addUserLink(e3);
//		GraphUtil.printGraph(alignment.getSteinerTree());
		
//		alignment.addUserLink(e1);
//		GraphUtil.printGraph(alignment.getSteinerTree());
		
//		System.out.println(alignment.getAssignedLink(ns1 + "Country_1").getLabel());



	}
}
