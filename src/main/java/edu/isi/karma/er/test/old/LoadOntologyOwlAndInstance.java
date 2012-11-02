package edu.isi.karma.er.test.old;

import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.er.helper.Constants;

public class LoadOntologyOwlAndInstance {

	private static String MATCH_URI = "http://www.isi.edu/ontology/Match/";
	private static String SAAM_URI = "http://americanart.si.edu/saam/";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Model model = ModelFactory.createDefaultModel();
		loadOwl();

	}

	private static void loadOwl() {
		OntModel ont = createOntModel();
		
		System.out.println("***********************  property  ***************************");
		List<OntClass> list = ont.listClasses().toList();
		for (OntClass c : list) {
			System.out.println(c.getLabel(null) + "\t" + c.getURI());
			ExtendedIterator<? extends OntResource> iter = c.listInstances();
			while (iter.hasNext()) {
				OntResource i = iter.next();
				System.out.println("\t" + i.getLabel(null) + "\t" + i.getURI());
			}
		}
		
		System.out.println("***********************  property  ***************************");
		List<OntProperty> plist = ont.listAllOntProperties().toList();
		for (OntProperty p : plist) {
			System.out.println(p.getLabel(null) + "\t" + p.getURI() + "\t" + p.getDomain().getURI() + "\t" + p.getRange().getURI());
		}
	}

	private static OntModel createOntModel( ){
		OntDocumentManager mgr = OntDocumentManager.getInstance();
		mgr.addAltEntry(MATCH_URI, "file:" + Constants.PATH_BASE + "match.owl");
		mgr.addAltEntry(SAAM_URI, "file:" + Constants.PATH_BASE + "saam.owl");
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);
		spec.setDocumentManager(mgr);
		OntModel ont = ModelFactory.createOntologyModel(spec);
		ont.read(MATCH_URI);
		return ont;
	}

}
