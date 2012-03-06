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
package edu.isi.karma.webserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ServerStart extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ServerStart.class);

	public void init() throws ServletException {
		// Populate the ServletContextParameterMap data structure
		// Only the parameters that are specified in the
		// ServletContextParameterMap are valid. So, to use a context init
		// parameter, add it to the ServletContextParameterMap
		ServletContext ctx = getServletContext();
		Enumeration<?> params = ctx.getInitParameterNames();
		ArrayList<String> validParams = new ArrayList<String>();
		for (ContextParameter param : ContextParameter.values()) {
			validParams.add(param.name());
		}
		while (params.hasMoreElements()) {
			String param = params.nextElement().toString();
			if (validParams.contains(param)) {
				ContextParameter mapParam = ContextParameter.valueOf(param);

				ServletContextParameterMap.setParameterValue(mapParam,
						ctx.getInitParameter(param));
			}
		}

		// Prepare the CRF Model
		try {
			SemanticTypeUtil.prepareCRFModelHandler();
		} catch (IOException e) {
			logger.error("Error creating CRF Model file!", e);
		}

		// Load the geospatial ontology
		// ImportOntology imp = new ImportOntology(OntologyManager.Instance()
		// .getOntModel(), new File("./Preloaded_Ontologies/geo_2007.owl"));
		// imp.doImport();
		// // Load the Chevron ontology
		// imp = new ImportOntology(OntologyManager.Instance()
		// .getOntModel(), new File("./Preloaded_Ontologies/oilwell.owl"));
		// imp.doImport();

		// ImportOntology imp = new ImportOntology(OntologyManager.Instance()
		// .getOntModel(), new File("./SampleData/OWL/Wiki.owl"));
		// imp.doImport();
		/*
		//vivo ontology
		imp = new ImportOntology(OntologyManager.Instance()
		.getOntModel(), new File("../demofiles/vivo1.4-protege.owl"));
		imp.doImport();

		//rdfs ontology
		imp = new ImportOntology(OntologyManager.Instance()
		.getOntModel(), new File("../demofiles/rdfs-small.owl"));
		imp.doImport();
		*/
		System.out.println("************");
		System.out.println("Server start servlet initialized successfully..");
		System.out.println("***********");

	}		
}
