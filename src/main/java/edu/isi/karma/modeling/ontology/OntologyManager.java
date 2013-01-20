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
package edu.isi.karma.modeling.ontology;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class OntologyManager {
	
	static Logger logger = Logger.getLogger(OntologyManager.class.getName());

	private static OntologyController ontController = null;
	private static OntologyCache ontCache = null;
	
	public OntologyManager() {
		ontController = new OntologyController();
		ontCache = new OntologyCache(ontController);
	}

	public boolean isEmpty() {
		return ontController.getOntModel().isEmpty();
	}
	
	public boolean doImport(File sourceFile) {

		if (sourceFile == null) {
			logger.debug("input file is null.");
			return false;
		}
		
		logger.debug("Importing " + sourceFile.getName() + " OWL Ontology ...");

		if(!sourceFile.exists()){
			logger.error("file does not exist  " + sourceFile.getAbsolutePath());
			return false;
		}
		
		try {
			InputStream s = new FileInputStream(sourceFile);
			ontController.getOntModel().read(s, null);
		} catch (Throwable t) {
			logger.error("Error reading the OWL ontology file!", t);
			return false;
		}
		
		ontCache = new OntologyCache(ontController);
		ontCache.init();
		/* Record the operation */
		logger.debug("done.");
		return true;
	}
	
	public boolean doImportWithoutCacheUpdate(File sourceFile) {

		if (sourceFile == null) {
			logger.debug("input file is null.");
			return false;
		}
		
		logger.debug("Importing " + sourceFile.getName() + " OWL Ontology ...");

		if(!sourceFile.exists()){
			logger.error("file does not exist  " + sourceFile.getAbsolutePath());
			return false;
		}
		
		try {
			InputStream s = new FileInputStream(sourceFile);
			ontController.getOntModel().read(s, null);
		} catch (Throwable t) {
			logger.error("Error reading the OWL ontology file!", t);
			return false;
		}
		
		/* Record the operation */
		logger.debug("done.");
		return true;
	}
	
	public void updateCache() {
		ontCache = new OntologyCache(ontController);
		ontCache.init();
	}
	
	public List<String> getClasses() {
		return ontCache.getClasses();
	}

	public List<String> getProperties() {
		return ontCache.getProperties();
	}

	public List<String> getDataProperties() {
		return ontCache.getDataProperties();
	}

	public List<String> getObjectProperties() {
		return ontCache.getObjectProperties();
	}

	public OntologyTreeNode getClassHierarchy() {
		return ontCache.getClassHierarchy();
	}

	public OntologyTreeNode getObjectPropertyHierarchy() {
		return ontCache.getObjectPropertyHierarchy();
	}

	public OntologyTreeNode getDataPropertyHierarchy() {
		return ontCache.getDataPropertyHierarchy();
	}
	
	/**
	 * If @param superClassUri is a superclass of @param subClassUri, it returns true; otherwise, false.
	 * If third parameter is set to true, it also considers indirect superclasses.
	 * @param superClassUri
	 * @param subClassUri
	 * @param recursive
	 * @return
	 */
	public boolean isSuperClass(String superClassUri, String subClassUri, boolean recursive) {
		
		if (!recursive) {
			if (ontCache.getSubClassMap().containsKey(subClassUri + superClassUri))
				return true;
			else
				return false;
		}
		
		List<String> superClasses = ontController.getSuperClasses(subClassUri, recursive);
		for (int i = 0; i < superClasses.size(); i++) {
			if (superClassUri.equalsIgnoreCase(superClasses.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * If @param subClassUri is a subclass of @param superClassUri, it returns true; otherwise, false.
	 * If third parameter is set to true, it also considers indirect subclaases.
	 * @param subClassUri
	 * @param superClassUri
	 * @param recursive
	 * @return
	 */
	public boolean isSubClass(String subClassUri, String superClassUri, boolean recursive) {
		
		if (!recursive) {
			if (ontCache.getSubClassMap().containsKey(subClassUri + superClassUri))
				return true;
			else
				return false;
		}
		
		List<String> subClasses = ontController.getSubClasses(superClassUri, recursive);
		for (int i = 0; i < subClasses.size(); i++) {
			if (subClassUri.equalsIgnoreCase(subClasses.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	


	/**
	 * If @param superPropertyUri is a superProperty of @param subPropertyUri, it returns true; otherwise, false.
	 * If third parameter is set to true, it also considers indirect superproperties.
	 * @param superPropertyUri
	 * @param subPropertyUri
	 * @param recursive
	 * @return
	 */
	public boolean isSuperProperty(String superPropertyUri, String subPropertyUri, boolean recursive) {
		
		if (!recursive) {
			if (ontCache.getSubPropertyMap().containsKey(subPropertyUri + superPropertyUri))
				return true;
			else
				return false;
		}
		
		List<String> superProperties = ontController.getSuperProperties(subPropertyUri, recursive);
		for (int i = 0; i < superProperties.size(); i++) {
			if (superPropertyUri.equalsIgnoreCase(superProperties.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * If @param subPropertyUri is a subProperty of @param superPropertyUri, it returns true; otherwise, false.
	 * If third parameter is set to true, it also considers indirect subproperties.
	 * @param subPropertyUri
	 * @param superClassUri
	 * @param recursive
	 * @return
	 */
	public boolean isSubProperty(String subPropertyUri, String superPropertyUri, boolean recursive) {
		
		if (!recursive) {
			if (ontCache.getSubPropertyMap().containsKey(subPropertyUri + superPropertyUri))
				return true;
			else
				return false;
		}
		
		List<String> subProperties = ontController.getSubProperties(superPropertyUri, recursive);
		for (int i = 0; i < subProperties.size(); i++) {
			if (subPropertyUri.equalsIgnoreCase(subProperties.get(i))) {
				return true;
			}
		}
		
		return false;
	}
		
	
	/**
	 * This method takes a property URI and returns all domains of that property.
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public List<String> getDomainsGivenProperty(String propertyUri, boolean recursive) {
		// should add all subclasses to the results
		List<String> results;

		if (!recursive)
			results = ontCache.getPropertyDirectDomains().get(propertyUri);
		else
			results = ontCache.getPropertyIndirectDomains().get(propertyUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;

	}

	/**
	 * This method takes @param rangeClassUri and for object properties whose ranges includes this parameter, 
	 * returns all of their domains.
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param rangeClassUri
	 * @param recursive
	 * @return
	 */
	public List<String> getDomainsGivenRange(String rangeClassUri, boolean recursive) {
		
		List<String> objectProperties = ontCache.getDirectInObjectProperties().get(rangeClassUri);
		List<String> domains = new ArrayList<String>();
		List<String> temp;
		
		if (objectProperties == null)
			return domains;
		
		for (int i = 0; i < objectProperties.size(); i++) {
			if (!recursive) 
				temp = ontCache.getPropertyDirectDomains().get(objectProperties.get(i));
			else
				temp = ontCache.getPropertyIndirectDomains().get(objectProperties.get(i));
			if (temp != null)
				domains.addAll(temp);
		}
		
		return domains;
	}
	
	/**
	 * This function takes a class and a data property and says if the class is in domain of that data property or not.
	 * If @param includeinheritance is true, it also returns the data properties inherited from parents, for example, 
	 * if A is superclass of B, and we have a datatype property P from A to xsd:int, then calling this function with 
	 * (B, P, false) returns nothing, but calling with (B, P, true) returns the property P.
	 * @param domainClassUri
	 * @param propertyUri
	 * @param inheritance
	 * @return
	 */
	public List<String> getDataProperties(String domainClassUri, String propertyUri, boolean inheritance) {

		List<String> propertyDomains;
		List<String> results = new ArrayList<String>();

		if (!inheritance)
			propertyDomains = ontCache.getPropertyDirectDomains().get(propertyUri);
		else
			propertyDomains = ontCache.getPropertyIndirectDomains().get(propertyUri);
		
		if (propertyDomains != null && propertyDomains.indexOf(domainClassUri) != -1)
			results.add(propertyUri);
		
		return results;

	}
	
	/**
	 * This method extracts all the object properties between two classes (object properties 
	 * who have @param domainClassUri in their domain and @param rangeClassUri in their range).
	 * If @param inheritance is true, it also returns the object properties inherited from parents.
	 * @param domainClassUri
	 * @param rangeClassUri
	 * @param inheritance
	 * @return
	 */
	public List<String> getObjectProperties(String domainClassUri, String rangeClassUri, boolean inheritance) {
		
		List<String> results;

		if (!inheritance)
			results = ontCache.getDirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		else
			results = ontCache.getIndirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;	}
	
	/**
	 * This function takes a class uri and returns the datatype properties who have this class in their domain. 
	 * If second parameter set to True, it also returns the datatype properties inherited from parents of the given class.
	 * @param domainClassUri
	 * @param inheritance
	 * @return
	 */
	public List<String> getDataPropertiesOfClass(String domainClassUri, boolean inheritance) {
		
		List<String> results;

		if (!inheritance)
			results = ontCache.getDirectOutDataProperties().get(domainClassUri);
		else
			results = ontCache.getIndirectOutDataProperties().get(domainClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;
	}

	/**
	 * This function takes a class uri and returns the object properties who have this class in their domain. 
	 * If second parameter set to True, it also returns the object properties inherited from parents of the given class.
	 * @param domainClassUri
	 * @param inheritance
	 * @return
	 */
	public List<String> getObjectPropertiesOfClass(String domainClassUri, boolean inheritance) {
		
		List<String> results;

		if (!inheritance)
			results = ontCache.getDirectOutObjectProperties().get(domainClassUri);
		else
			results = ontCache.getIndirectOutObjectProperties().get(domainClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;
	}
	
	public Map<String, String> getPrefixMap () {
		Map<String, String> nsMap = ontController.getOntModel().getNsPrefixMap();
		Map<String, String> prefixMap = new HashMap<String, String>();
		
		for(String ns: nsMap.keySet()) {
			if (!ns.equals("") && !prefixMap.containsKey(nsMap.get(ns)))
				prefixMap.put(nsMap.get(ns), ns);
		}
		return prefixMap;
	}
}
