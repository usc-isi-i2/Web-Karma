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

package edu.isi.karma.model.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class Repository {
	
	public final String REPOSITORY_REL_DIR = "repository/";
	public final String SERVICE_REPOSITORY_REL_DIR = "repository/services/";
	public final String SOURCE_REPOSITORY_REL_DIR = "repository/sources/";
	public final String TRIPLE_DATASET_REL_DIR =  "repository/dataset/";
	
//	@param LANG: The language in which to write the model is specified by the lang argument.
//	* Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3". 
//	* The default value, represented by null is "RDF/XML".
	public final String LANG = SerializationLang.N3;
	
	private static Logger logger = LoggerFactory.getLogger(Repository.class);

	private Dataset dataset;

	//TODO this should not be static!
	private static Repository _InternalInstance = null;
	public static Repository Instance()
	{
		if (_InternalInstance == null)
		{
			_InternalInstance = new Repository();
			ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
			File repository = new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + _InternalInstance.REPOSITORY_REL_DIR);
			if (!repository.exists())
				repository.mkdir();
			
			File tripleStore = new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + _InternalInstance.TRIPLE_DATASET_REL_DIR);
			if (!tripleStore.exists())
				tripleStore.mkdir();
			
			File serviceRepository = new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + _InternalInstance.SERVICE_REPOSITORY_REL_DIR);
			if (!serviceRepository.exists())
				serviceRepository.mkdir();
			
			File sourceRepository = new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + _InternalInstance.SOURCE_REPOSITORY_REL_DIR);
			if (!sourceRepository.exists())
				sourceRepository.mkdir();
			
			_InternalInstance.createRepository();
		}
		return _InternalInstance;
	}
	
	public void createRepository() {
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		Location location = new Location(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + this.TRIPLE_DATASET_REL_DIR);
		this.dataset = TDBFactory.createDataset(location);
//		TDB.getContext().set(TDB.symUnionDefaultGraph, true);
	}
	
	public void addModel(Model m, String name) {
		if (name == null) {
			logger.info("cannot add the model because the given name is null.");
			return;
		}
		
		Model namedModel = this.dataset.getNamedModel(name); 
		namedModel.removeAll();
		namedModel.add(m.listStatements());
		namedModel.setNsPrefixes(m.getNsPrefixMap());
		namedModel.commit();
		TDB.sync(this.dataset);
	}
	
	/**
	 * returns the union of all named models.
	 * @return
	 */
	public Model getModel() {

//		1) tdbDataset.getNamedModel("urn:x-arq:UnionGraph") does always return the union graph (even when
//		TDB.getContext().set(TDB.symUnionDefaultGraph, true) is NOT set).
//		2) tdbDataset.getDefaultModel() does always return the 'real' default graph which is different/independent of (1).
//		3) so only in context of a SPARQL query the'default graph' contains the union of all named graphs.
				
		return this.dataset.getNamedModel("urn:x-arq:UnionGraph") ;
	}
	
	public Model getNamedModel(String name) {
		if (name == null) {
			logger.info("cannot get the model because the given name is null.");
			return null;
		}
		
		if (!this.dataset.containsNamedModel(name)) {
			logger.info("The model: " + name + " does not exist in the repository.");
			return null;
		}
		return this.dataset.getNamedModel(name);
	}
	
	public void clearNamedModel(String name) {
		if (name == null) {
			logger.info("cannot clear the model because the given name is null.");
			return;
		}
		
		if (!this.dataset.containsNamedModel(name)) {
			logger.info("The model " + name + " does not exist in the repository.");
			return;
		}
		this.dataset.getNamedModel(name).removeAll();
		this.dataset.getNamedModel(name).commit();
	}
	
	/**
	 * imports the named model from a directory or a file
	 * @param lang The language of the file specified by the lang argument. 
	 * Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3". 
	 * The default value, represented by null is "RDF/XML".
	 * @param file
	 */
	public void importModel(File file, String lang) {
		if (!file.exists()) {
			logger.error("cannot find the file/dir at " + file.getAbsolutePath());
			return;
		}
		
		if (file.isFile()) {
			importModelFromSingleFile(file, lang);
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				importModelFromSingleFile(f, lang);
			}
		}
		

	}
	
	private void importModelFromSingleFile(File file, String lang) {
		Model m = ModelFactory.createDefaultModel();
		
		try {
			InputStream s = new FileInputStream(file);
			m.read(s, null);
			
			this.addModel(m, file.getName());
			
			logger.info("The model " + file.getPath() + " successfully imported to repository");
			
		} catch (Throwable t) {
			logger.error("Error reading the model file!", t);
			return;
		}		
	}
	
	public String getFileExtension(String lang) {
		String ext = ".rdf";
		if (lang.equalsIgnoreCase("RDF/XML")) ext = ".rdf";
		else if (lang.equalsIgnoreCase("RDF/XML-ABBREV")) ext = ".rdf";
		else if (lang.equalsIgnoreCase("N-TRIPLE")) ext = ".ntriple";
		else if (lang.equalsIgnoreCase("TURTLE")) ext = ".turtle";
		else if (lang.equalsIgnoreCase("TTL")) ext = ".ttl";
		else if (lang.equalsIgnoreCase("N3")) ext = ".n3";
		return ext;
	}

}
