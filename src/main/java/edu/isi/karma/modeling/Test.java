package edu.isi.karma.modeling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.ontology.ImportOntology;
import edu.isi.karma.modeling.ontology.OntologyManager;

public class Test {

	private static void loadOntologies() {
		File f1 = new File("D:\\Academic\\ISI\\_SVN\\karma\\trunk\\WS Modeling\\Ontologies\\myont.owl");
		File f2 = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\sample.owl");
		ImportOntology imp1 = new ImportOntology(OntologyManager.Instance().getOntModel(), f1);
		imp1.doImport();
		ImportOntology imp2 = new ImportOntology(OntologyManager.Instance().getOntModel(), f2);
		imp2.doImport();
	}
	
	public static void main(String[] args) {
		
		loadOntologies();
		
		String ns1 = "http://mohsen.isi.edu/sample.owl#";
//		String ns2 = "http://www.w3.org/2002/07/owl#";
		
		List<NameSet> semanticTypes = new ArrayList<NameSet>();
//		semanticTypes.add(new NameSet(ns1, "City"));
//		semanticTypes.add(new NameSet(ns1, "hasCode"));
		semanticTypes.add(new NameSet(ns1, "zipCode"));
//		semanticTypes.add(new NameSet(ns1, "hasModel"));
//		semanticTypes.add(new NameSet(ns1, "Country"));
//		semanticTypes.add(new NameSet(ns1, "Country"));
//		semanticTypes.add(new NameSet(ns1, "zipCode"));
//		semanticTypes.add(new NameSet(ns1, "City"));
//		semanticTypes.add(new NameSet(ns1, "hasName"));
//		semanticTypes.add(new NameSet(ns1, "hasName"));
//		semanticTypes.add(new NameSet(ns1, "live"));
//		semanticTypes.add(new NameSet(ns1, "Place"));
//		semanticTypes.add(new NameSet(ns1, "Plant"));
		semanticTypes.add(new NameSet(ns1, "Person"));
		semanticTypes.add(new NameSet(ns1, "Animal"));
//		semanticTypes.add(new NameSet(ns2, "Thing"));
//		semanticTypes.add(new NameSet(ns2, "Thing"));
		
//		String e1 = ns1 + "hasName1";
//		String e2 = ns1 + "hasName2";
//		String e3 = ns2 + "hasSubClass10";
		String e4 = ns1 + "zipCode2";
		
		Alignment alignment = new Alignment(semanticTypes);
		alignment.duplicateDomainOfLink(e4);
		GraphUtil.printGraph(alignment.getSteinerTree());

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

//		List<LabeledWeightedEdge> alternatives = alignment.getAlternatives(ns1 + "Country_1", false);
//		for (LabeledWeightedEdge edge: alternatives)
//			System.out.println(edge.getLocalID());


	}
}
