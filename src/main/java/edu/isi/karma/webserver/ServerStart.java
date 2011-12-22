package edu.isi.karma.webserver;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ontology.ImportOntology;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;

public class ServerStart extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ServerStart.class);

	public void init() throws ServletException {
		System.out.println("************");
		System.out
				.println("*** Server start servlet initialized successfully ***..");
		System.out.println("***********");

		// Prepare the CRF Model
		try {
			SemanticTypeUtil.prepareCRFModelHandler();
		} catch (IOException e) {
			logger.error("Error creating CRF Model file!", e);
		}

		// Load the geospatial ontology
		ImportOntology imp = new ImportOntology(OntologyManager.Instance()
				.getOntModel(), new File("./Preloaded_Ontologies/geo_2007.owl"));
		imp.doImport();
	}
}
