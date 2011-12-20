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

		int size = 5;
		File[] f = new File[size];
		
		
//		f[0] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\vivo-core.owl");
//		f[1] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\sample.owl");
		f[2] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\Wiki.owl");
//		f[3] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\DoveTailOntoRDF.owl");
//		f[4] = new File("D:\\Academic\\ISI\\_GIT\\Web-Karma\\test\\Dovetail_ISI_mod.owl");
		
		for (int i = 2; i < 3; i++) {
			ImportOntology imp = new ImportOntology(OntologyManager.Instance().getOntModel(), f[i]);
			imp.doImport();
		}
	}
	
	private static List<SemanticType> createTestInput1() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#City", null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasCode", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#zipCode", null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasModel", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Country", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Country", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#zipCode", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#City", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasName", "http://mohsen.isi.edu/sample.owl#Person", null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasName", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#hasName", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#live", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Place", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Plant", null, 0.0) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Person", null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, "http://mohsen.isi.edu/sample.owl#Animal", null, 0.0, false) );
//		semanticTypes.add( new SemanticType(null, "http://www.w3.org/2002/07/owl#Thing", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://www.w3.org/2002/07/owl#Thing", null, 0.0) );
		
		return semanticTypes;
	}

	private static List<SemanticType> createTestInput2() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Gene", null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Pathway", null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Disease", null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#PharmGKBId", 
				"http://halowiki/ob/category#Drug", null, 0.0, false) );
		
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Gene", null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Pathway", null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Disease", null, 0.0, false) );
		
		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/property#Label", 
				"http://halowiki/ob/category#Drug", null, 0.0, false) );
		
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Disease", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Drug", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Gene", null, 0.0) );
//		semanticTypes.add( new SemanticType(null, "http://halowiki/ob/category#Pathway", null, 0.0) );
		
		return semanticTypes;
	}
	
	private static List<SemanticType> createTestInput3() {
		
		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
		
		semanticTypes.add( new SemanticType(null, "http://www.sri.com/ontologies/DovetailOnto.owl#Attack", null, 0.0, false) );
		semanticTypes.add( new SemanticType(null, "http://www.sri.com/ontologies/DovetailOnto.owl#Nation", null, 0.0, false) );
		
		return semanticTypes;
	}
	
	public static void main(String[] args) {
		
		loadOntologies();
		OntologyCache.Instance();
		

		List<SemanticType> semTypes1 = createTestInput1();
		List<SemanticType> semTypes2 = createTestInput2();
		List<SemanticType> semTypes3 = createTestInput3();

		Alignment alignment = null;
		alignment = new Alignment(semTypes1);
		alignment = new Alignment(semTypes3);
		alignment = new Alignment(semTypes2);
		
		
//		alignment.getSteinerTree();
		
		GraphUtil.printGraph(alignment.getSteinerTree());

//		alignment.addUserLink("http://halowiki/ob/property#involves1");
//		GraphUtil.printGraph(alignment.getSteinerTree());
//		alignment.addUserLink("http://halowiki/ob/property#IsInvolvedIn1");
//		GraphUtil.printGraph(alignment.getSteinerTree());

		
//		alignment.getSteinerTree();
//		GraphUtil.printGraph(alignment.getSteinerTree());
		
//		alignment.addUILink("http://halowiki/ob/property#Causes1");
//		GraphUtil.printGraph(alignment.getSteinerTree());



	}
}
