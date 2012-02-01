package edu.isi.karma.sourcedescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.hp.hpl.jena.ontology.OntModel;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.modeling.ontology.ImportOntology;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rdf.SourceDescription;
import edu.isi.karma.rdf.WorksheetRDFGenerator;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticType.Origin;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.SampleDataFactory;
import edu.isi.mediator.gav.main.MediatorException;

public class GenerateSourceDescriptionTest extends TestCase {
	
	private Workspace workspace;
	private RepFactory f;
	private Worksheet worksheet;

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.workspace = f.createWorkspace();
		//uncomment for nesting tables
		this.worksheet = SampleDataFactory.createSamplePathwaysWithNestingWorksheet(workspace);
		//this.worksheet = SampleDataFactory.createSamplePathwaysWorksheet(workspace);
		
		//for(HNode n: worksheet.getHeaders().getHNodes())
		//System.out.println("Columns = "+ n.getHNodePath(f).toColumnNames());
		
		for(HNode n: f.getAllHNodes()){
			System.out.println("Columns factory= "+ n.getHNodePath(f).toColumnNames());
		}
		
		// Setup semantic types
		String c1_ID = worksheet.getHeaders().getHNodeFromColumnName("ACCESSION_ID").getId();
		String c2_ID = worksheet.getHeaders().getHNodeFromColumnName("NAME").getId();
		//no nesting
		//String c3_ID = worksheet.getHeaders().getHNodeFromColumnName("DRUG_ID").getId();
		//String c4_ID = worksheet.getHeaders().getHNodeFromColumnName("DRUG_NAME").getId();
		//uncomment for nesting tables
		//with nesting; the ID for values column
		String c4_ID = "HN17";
		String c3_ID = "HN11";
		/////////////////
		String c5_ID = worksheet.getHeaders().getHNodeFromColumnName("GENE_ID").getId();
		String c6_ID = worksheet.getHeaders().getHNodeFromColumnName("GENE_NAME").getId();
		String c7_ID = worksheet.getHeaders().getHNodeFromColumnName("DISEASE_ID").getId();
		String c8_ID = worksheet.getHeaders().getHNodeFromColumnName("DISEASE_NAME").getId();
		
		worksheet.getSemanticTypes().addType(new SemanticType(c1_ID, "http://halowiki/ob/property#pharmGKBId", "http://halowiki/ob/category#Pathway",Origin.User, 1.0, true));
		worksheet.getSemanticTypes().addType(new SemanticType(c2_ID, "http://halowiki/ob/property#name", "http://halowiki/ob/category#Pathway",Origin.User, 1.0, false));
		worksheet.getSemanticTypes().addType(new SemanticType(c3_ID, "http://halowiki/ob/property#pharmGKBId", "http://halowiki/ob/category#Drug",Origin.User, 1.0, true));
		worksheet.getSemanticTypes().addType(new SemanticType(c4_ID, "http://halowiki/ob/property#name", "http://halowiki/ob/category#Drug",Origin.User, 1.0, false));
		worksheet.getSemanticTypes().addType(new SemanticType(c5_ID, "http://halowiki/ob/property#pharmGKBId", "http://halowiki/ob/category#Gene",Origin.User, 1.0, true));
		worksheet.getSemanticTypes().addType(new SemanticType(c6_ID, "http://halowiki/ob/property#name", "http://halowiki/ob/category#Gene",Origin.User, 1.0, false));
		worksheet.getSemanticTypes().addType(new SemanticType(c7_ID, "http://halowiki/ob/property#pharmGKBId", "http://halowiki/ob/category#Disease",Origin.User, 1.0, true));
		worksheet.getSemanticTypes().addType(new SemanticType(c8_ID, "http://halowiki/ob/property#name", "http://halowiki/ob/category#Disease",Origin.User, 1.0, false));
		
		// Import the ontology
		OntModel model = OntologyManager.Instance().getOntModel();
		ImportOntology imp = new ImportOntology(model, new File(
				"./src/test/karma-data/Wiki.owl"));
		imp.doImport();
	}
	
	public void testGenerate() throws KarmaException, MediatorException, ClassNotFoundException, IOException {
		SemanticTypes semTypes = worksheet.getSemanticTypes();
		// Get the list of semantic types
		List<SemanticType> types = new ArrayList<SemanticType>();
		for (SemanticType type : semTypes.getTypes().values()) {
			types.add(type);
		}
		
		Alignment alignment = new Alignment(types);
		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment
				.getSteinerTree();
		
		GraphUtil.printGraph(tree);
		
		//false=use HNodePath in the SD
		SourceDescription sd = new SourceDescription(f, tree, alignment.GetTreeRoot(),"http://localhost:8080/source/", false);
		String domainFile = sd.generateSourceDescription();
		System.out.println("SourceDescription:\n" + domainFile);
		System.out.println("Headers=" + worksheet.getHeaders().prettyPrint(f));
		WorksheetRDFGenerator wrg = new WorksheetRDFGenerator(f, domainFile, null);
		//wrg.generateTriplesRow(worksheet);
		wrg.generateTriplesCell(worksheet);
	}
}
