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

package edu.isi.karma.imp.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.ConfigUtil;
import edu.isi.karma.service.Repository;

public class testRepository {

	/**
	 * @param args
	 */
	private  Dataset dataset;
	private JSONArray confArr = new JSONArray();
	private String url1 = "http://www.semanticweb.org/yingzhang/ontologies/2013/1/untitled-ontology-48#:name";
	private String url2 = "http://www.semanticweb.org/yingzhang/ontologies/2013/1/untitled-ontology-48#:latitude";	
	private String url3 = "http://www.semanticweb.org/yingzhang/ontologies/2013/1/untitled-ontology-48#:longitude";
	
    public testRepository(){
	    
		File file = new File("config/b.json");
		if (!file.exists()) {
			throw new IllegalArgumentException("file name "
					+ file.getAbsolutePath() + " does not exist.");
		}

		ConfigUtil util = new ConfigUtil();
		// util.loadConstants();
		this.confArr = util.loadConfig(file);
    }
	
	
	public void setupDirectoryModelfrom(String DATASET_PATH,
			String[] SOURCE_PATH) {
		this.dataset = TDBFactory.createDataset(DATASET_PATH);
		
		int i=0;
		for(String source_path:SOURCE_PATH){
			Model model =dataset.getNamedModel("http://buildings/simple"+(i+1));
			FileManager.get().readModel(model, source_path);
            model.close();
			 //importModelFromSingleFile(source_path,"modeName_"+i) ;
			i++;
		}
		
		dataset.close();
	} 
	
	
	public List<String> sparqlQueryLiteral(String DATASET_PATH, String query, List<String> literal) {
		//make all the named graphs be accessed
		TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
	
		Dataset dataset = TDBFactory.createDataset(DATASET_PATH);
	    com.hp.hpl.jena.query.Query querying = QueryFactory.create(query);
	 
	    // Execute the query and obtain results 
	    QueryExecution qe = QueryExecutionFactory.create(querying, dataset);
	    ResultSet results = qe.execSelect();
	 

	    //get elements from ResultSet;
	    List<String> ls=new ArrayList<String>();
		while(results.hasNext()){
			QuerySolution qs=(QuerySolution) results.next();
			for(String lit:literal){
				
				if(qs.contains(lit)){
					ls.add(qs.getLiteral(lit).toString());
				}
			}
		}

	    qe.close();
	         dataset.close();
	         return ls;
	}
	
	
	public List<String> sparqlQueryLiteral(String DATASET_PATH, String query, String literal) {
		//make all the named graphs be accessed
		TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
	
		Dataset dataset = TDBFactory.createDataset(DATASET_PATH);
	    com.hp.hpl.jena.query.Query querying = QueryFactory.create(query);
	 
	    // Execute the query and obtain results 
	    QueryExecution qe = QueryExecutionFactory.create(querying, dataset);
	    ResultSet results = qe.execSelect();
	 

	    //get elements from ResultSet;
	    List<String> ls=new ArrayList<String>();
		while(results.hasNext()){
			QuerySolution qs=(QuerySolution) results.next();
			ls.add(qs.getLiteral(literal).toString());
		}

		for(String str:ls){
			System.out.println("inner ls="+str);
		}
	    qe.close();
	         dataset.close();
	         return ls;
	}
	
	
	
	public List<String> sparqlQuery(String DATASET_PATH, String query, String literal) {
		//make all the named graphs be accessed
		TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
	
		Dataset dataset = TDBFactory.createDataset(DATASET_PATH);
	    com.hp.hpl.jena.query.Query querying = QueryFactory.create(query);
	 
	    // Execute the query and obtain results 
	    QueryExecution qe = QueryExecutionFactory.create(querying, dataset);
	    ResultSet results = qe.execSelect();
	 

	    //get elements from ResultSet;
	    List<String> ls=new ArrayList<String>();
		while(results.hasNext()){
			QuerySolution qs=(QuerySolution) results.next();

			ls.add(qs.getResource(literal).toString());
		}
	    qe.close();
	         dataset.close();
	         return ls;
	}

}
