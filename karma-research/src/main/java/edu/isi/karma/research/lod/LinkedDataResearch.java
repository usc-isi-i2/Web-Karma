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

package edu.isi.karma.research.lod;

import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.ModelReader;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.SemanticType;

public class LinkedDataResearch {

	private static Logger logger = LoggerFactory.getLogger(LinkedDataResearch.class);

//	private OntologyManager ontologyManager = null;
//	private List<ColumnNode> columnNodes = null;
	Repository tripleRespository = null;
	
	public LinkedDataResearch(OntologyManager ontologyManager, List<ColumnNode> columnNodes) {
		if (ontologyManager == null || 
				columnNodes == null || 
				columnNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
//		this.ontologyManager = ontologyManager;
//		this.columnNodes = columnNodes;
		this.initializeRepository();
	}

	private void initializeRepository() {
		String sesameServer = "http://localhost:8080/openrdf-sesame";
		String repositoryID = "amsterdam-museum";

		tripleRespository = new HTTPRepository(sesameServer, repositoryID);
		try {
			tripleRespository.initialize();
		} catch (RepositoryException e) {
			logger.error("Error in initializing the triple store!");
		}
	}
	
	private void extractRelationshipsFromLOD(List<ColumnNode> columnNodes) {
		
		ColumnNode column1, column2;
		String column1Name, column2Name;
		List<SemanticType> column1SemanticTypes, column2SemanticTypes;
		String class1Uri, class2Uri;
		int k = 4;
		
		for (int i = 0; i < columnNodes.size(); i++) {
			
			column1 = columnNodes.get(i);
			column1Name = column1.getColumnName();
			column1SemanticTypes = column1.getTopKSuggestions(k);
			
			for (int j = i + 1; j < columnNodes.size(); j++) {
			
				column2 = columnNodes.get(j);
				column2Name = column2.getColumnName();
				column2SemanticTypes = column2.getTopKSuggestions(k);
				
				logger.info("Extract relationships between columns: " + column1Name + "," + column2Name);
				
				if (column1SemanticTypes == null || 
						column2SemanticTypes == null)
					continue;
				
				for (SemanticType st1 : column1SemanticTypes) {
					class1Uri = st1.getDomain().getUri();
					for (SemanticType st2: column2SemanticTypes) {
						class2Uri = st2.getDomain().getUri();
						getRelationships(class1Uri, class2Uri);
					}
				}
				
			}
		}
	}
	
	private void getRelationships(String class1Uri, String class2Uri) {
		
		logger.info("Finding properties between: " + class1Uri + "," + class2Uri);

		try {
			   RepositoryConnection con = tripleRespository.getConnection();
			   try {
				   //add data from file
				   //con.add(file, baseURI, RDFFormat.RDFXML);
				   
				   //execute SPARQL query on data
				   String queryString = "SELECT ?x ?y ?z\n"+ 
						   				"WHERE {\n" + 
						   				"?x ?y ?z.\n" + 
						   				"?x rdf:type <" + class1Uri +">.\n" +
						   				"?z rdf:type <" + class2Uri +">.\n" + 
						   				"} LIMIT 10";
				   logger.info("Query String = \n" + queryString);
				   // union the properties from y to x
				   
				   TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				   TupleQueryResult result = tupleQuery.evaluate();
				   try {
					   while (result.hasNext()) {
						   BindingSet bindingSet  = result.next();
						   Value valueOfX = bindingSet.getValue("x");
						   Value valueOfY = bindingSet.getValue("y");	
						   Value valueOfZ = bindingSet.getValue("z");	
						   logger.info("===========================================================================");
						   logger.info("x:" + valueOfX.toString());
						   logger.info("y:" + valueOfY.toString());
						   logger.info("z:" + valueOfZ.toString());
					   }
				   }
				   finally {
					   result.close();
				   }
				   
			   } finally {
				      con.close();
				   }
		}
		catch (OpenRDFException e) {
		   // handle exception
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void main(String[] args) throws Exception {

//		String outputPath = Params.OUTPUT_DIR;
//		String graphPath = Params.GRAPHS_DIR;
		
		List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

//		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();
		
//		OntologyManager ontologyManager = new OntologyManager();
//		File ff = new File(Params.ONTOLOGY_DIR);
//		File[] files = ff.listFiles();
//		for (File f : files) {
//			ontologyManager.doImport(f, "UTF-8");
//		}
//		ontologyManager.updateCache();  

		LinkedDataResearch ld = new LinkedDataResearch(null, null);

		String class1Uri = "http://www.openarchives.org/ore/terms/Proxy";
		String class2Uri = "http://purl.org/collections/nl/am/Person";
		
		ld.initializeRepository();
		ld.getRelationships(class1Uri, class2Uri);
		
//		for (int i = 0; i < semanticModels.size(); i++) {
//		for (int i = 0; i <= 10; i++) {
		int i = 0; {
			ld.initializeRepository();
			ld.extractRelationshipsFromLOD(semanticModels.get(i).getColumnNodes());
		}

	}
	
}
