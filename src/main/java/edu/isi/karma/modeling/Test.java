package edu.isi.karma.modeling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.ontology.ImportOntology;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.semantictypes.SemanticType;

public class Test {

	private static void loadOntologies() {
		File f1 = new File("D:\\Academic\\ISI\\_SVN\\karma\\trunk\\WS Modeling\\Ontologies\\myont.owl");
		File f2 = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\sample.owl");
		ImportOntology imp1 = new ImportOntology(OntologyManager.Instance().getOntModel(), f1);
		imp1.doImport();
		ImportOntology imp2 = new ImportOntology(OntologyManager.Instance().getOntModel(), f2);
		imp2.doImport();
	}
	
	private static List<SemanticType> createTestInput() {
		
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
		semanticTypes.add( new SemanticType(null, "http://www.w3.org/2002/07/owl#Thing", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://www.w3.org/2002/07/owl#Thing", null, 0.0) );
		
		return semanticTypes;
	}

	public static void main(String[] args) {
		
		loadOntologies();
				
//		String e1 = "http://mohsen.isi.edu/sample.owl#hasName1";
//		String e2 = "http://mohsen.isi.edu/sample.owl#hasName2";
//		String e3 = "http://www.w3.org/2002/07/owl#hasSubClass10";
//		String e4 = "http://www.w3.org/2002/07/owl#zipCode2";
		
		Alignment alignment = new Alignment(createTestInput());
//		alignment.duplicateDomainOfLink(e4);

		GraphUtil.printGraph(alignment.getSteinerTree());

		List<LabeledWeightedEdge> alternatives = alignment.getAlternatives("http://mohsen.isi.edu/sample.owl#hasName1", true);
		for (LabeledWeightedEdge edge: alternatives) {
			System.out.print(edge.getSource().getLocalID() + " ");
			System.out.print(edge.getLocalID() + " ");
			System.out.println(edge.getTarget().getLocalID());
		}

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
