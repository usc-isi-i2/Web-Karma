package edu.isi.karma.modeling.ontology;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntModel;

public class ImportOntology {

	private OntModel ontologyModel;
	private File sourceFile;
//	private static int ontologyNSIndex;
	
	static Logger logger = Logger.getLogger(ImportOntology.class.getName());
	
	public File getSourceFile() {
		return sourceFile;
	}

	public ImportOntology(OntModel ontologyModel, File sourceFile) {
		this.ontologyModel = ontologyModel;
		this.sourceFile = sourceFile;
		//doImport();
	}
	
	public boolean doImport() {

		logger.debug("Importing " +sourceFile.getName() + " OWL Ontology ...");

		if (sourceFile == null) {
			logger.debug("input file is null.");
			return false;
		}
		
		
		if(!sourceFile.exists()){
			logger.error("file does not exist  " + sourceFile.getAbsolutePath());
			return false;
		}
			
		
		try {
			InputStream s = new FileInputStream(sourceFile);
			ontologyModel.read(s, null);
			
			// Store the new namespaces information in the namespace map maintained in OntologyGraphManager
//			String baseNS = m.getNsPrefixURI("");
//			m.setNsPrefix("dv" + ontologyNSIndex++, baseNS);	
		} catch (Throwable t) {
			logger.error("Error reading the OWL ontology file!", t);
			return false;
		}
		
		/* Record the operation */
		logger.debug("done.");
		return true;
	}

}
