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
import java.util.Set;

import org.apache.log4j.Logger;

import edu.isi.karma.rep.alignment.Label;

public class OntologyManager {
	
	static Logger logger = Logger.getLogger(OntologyManager.class.getName());

	private OntologyHandler ontHandler = null;
	private OntologyCache ontCache = null;
	private List<OntologyUpdateListener> ontUpdateListeners; 
	
	public OntologyManager() {
		ontHandler = new OntologyHandler();
		ontCache = new OntologyCache(ontHandler);
		ontUpdateListeners = new ArrayList<OntologyUpdateListener>();	
	}

	public boolean isEmpty() {
		return ontHandler.getOntModel().isEmpty();
	}
	
	private void notifyListeners() {
		for (OntologyUpdateListener o : ontUpdateListeners)
			o.ontologyModelUpdated();
	}
	
	public boolean doImportAndUpdateCache(File sourceFile) {

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
			ontHandler.getOntModel().read(s, null);
		} catch (Throwable t) {
			logger.error("Error reading the OWL ontology file!", t);
			return false;
		}
		
		// update the cache
		ontCache = new OntologyCache(ontHandler);
		ontCache.init();
		
		// notify listeners
		this.notifyListeners();
		
		logger.debug("done.");
		return true;
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
			ontHandler.getOntModel().read(s, null);
		} catch (Throwable t) {
			logger.error("Error reading the OWL ontology file!", t);
			return false;
		}
		
		// notify listeners
		this.notifyListeners();

		logger.debug("done.");
		return true;
	}
	
	public void updateCache() {
		ontCache = new OntologyCache(ontHandler);
		ontCache.init();
	}
	
	public HashMap<String, Label> getClasses() {
		return ontCache.getClasses();
	}

	public HashMap<String, Label> getProperties() {
		return ontCache.getProperties();
	}

	public HashMap<String, Label> getDataProperties() {
		return ontCache.getDataProperties();
	}

	public HashMap<String, Label> getObjectProperties() {
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
	
	public HashMap<String, List<String>> getDirectOutDataProperties() {
		return ontCache.getDirectOutDataProperties();
	}

	public HashMap<String, List<String>> getIndirectOutDataProperties() {
		return ontCache.getIndirectOutDataProperties();
	}

	public HashMap<String, List<String>> getDirectOutObjectProperties() {
		return ontCache.getDirectOutObjectProperties();
	}

	public HashMap<String, List<String>> getIndirectOutObjectProperties() {
		return ontCache.getIndirectOutObjectProperties();
	}

	public HashMap<String, List<String>> getDirectInObjectProperties() {
		return ontCache.getDirectInObjectProperties();
	}

	public HashMap<String, List<String>> getIndirectInObjectProperties() {
		return ontCache.getIndirectInObjectProperties();
	}

	public HashMap<String, List<String>> getPropertyDirectDomains() {
		return ontCache.getPropertyDirectDomains();
	}

	public HashMap<String, List<String>> getPropertyIndirectDomains() {
		return ontCache.getPropertyIndirectDomains();
	}

	public HashMap<String, List<String>> getPropertyDirectRanges() {
		return ontCache.getPropertyDirectRanges();
	}

	public HashMap<String, List<String>> getPropertyIndirectRanges() {
		return ontCache.getPropertyIndirectRanges();
	}
	
	public HashMap<String, List<String>> getDirectDomainRangeProperties() {
		return ontCache.getDirectDomainRangeProperties();
	}

	public HashMap<String, List<String>> getIndirectDomainRangeProperties() {
		return ontCache.getIndirectDomainRangeProperties();
	}

	public HashMap<String, List<DomainRangePair>> getObjectPropertyDirectDomainRangePairs() {
		return ontCache.getObjectPropertyDirectDomainRangePairs();
	}

	public HashMap<String, List<DomainRangePair>> getObjectPropertyIndirectDomainRangePairs() {
		return ontCache.getObjectPropertyIndirectDomainRangePairs();
	}

	public HashMap<String, List<DomainRangePair>> getObjectPropertyNotDirectDomainRangePairs() {
		return ontCache.getObjectPropertyNotDirectDomainRangePairs();
	}

	public List<SubclassSuperclassPair> getDirectSubclassSuperclassPairs() {
		return ontCache.getDirectSubclassSuperclassPairs();
	}

	public List<SubclassSuperclassPair> getIndirectSubclassSuperclassPairs() {
		return ontCache.getIndirectSubclassSuperclassPairs();
	}
	

	public HashMap<String, Boolean> getDirectConnectivityCheck() {
		return ontCache.getDirectConnectivityCheck();
	}

	public HashMap<String, Boolean> getIndirectConnectivityCheck() {
		return ontCache.getIndirectConnectivityCheck();
	}
	
	public Label getUriLabel(String uriString) {
		return this.ontHandler.getUriLabel(uriString);
	}
	
	public boolean isClass(String uri) {
		return this.ontHandler.isClass(uri);
	}
	
	public boolean isProperty(String uri) {
		return this.ontHandler.isProperty(uri);
	}	
	
	public boolean isDataProperty(String uri) {
		return this.ontHandler.isDataProperty(uri);
	}
	
	public boolean isObjectProperty(String uri) {
		return this.ontHandler.isObjectProperty(uri);
	}
	
	/**
	 * Returns the inverse property of the property with given URI
	 * @param uri
	 * @return
	 */
	public Label getInverseProperty(String uri) {
		return this.ontCache.getPropertyInverse().get(uri);
	}
	
	/**
	 * Returns the inverseOf property of the property with given URI
	 * @param uri
	 * @return
	 */
	public Label getInverseOfProperty(String uri) {
		return this.ontCache.getPropertyInverseOf().get(uri);		
	}
	
	/**
	 * If @param superClassUri is a superclass of @param subClassUri, it returns true; otherwise, false.
	 * If third parameter is set to true, it also considers indirect superclasses.
	 * @param superClassUri
	 * @param subClassUri
	 * @param recursive
	 * @return
	 */
	public boolean isSubClass(String subClassUri, String superClassUri, boolean recursive) {
		
		if (!recursive) { 
			if (ontCache.getDirectSubClassCheck().containsKey(subClassUri + superClassUri))
				return true;
		} else {
			HashMap<String, Label> map = ontCache.getIndirectSubClasses().get(superClassUri);
			if (map == null) return false;
			Set<String> indirectSubClasses = map.keySet();
			if (indirectSubClasses != null && indirectSubClasses.contains(subClassUri))
				return true;
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
			if (ontCache.getDirectSubPropertyCheck().containsKey(subPropertyUri + superPropertyUri))
				return true;
		} else {
			HashMap<String, Label> map = ontCache.getIndirectSubProperties().get(superPropertyUri);
			if (map == null) return false;
			Set<String> indirectSubProperties = map.keySet();
			if (indirectSubProperties != null && indirectSubProperties.contains(subPropertyUri))
				return true;
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
		List<String> results = null;

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
		List<String> temp = null;
		
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

		List<String> propertyDomains = null;
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
		
		List<String> results = null;

		if (!inheritance)
			results = ontCache.getDirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		else
			results = ontCache.getIndirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;	
	}
	
	/**
	 * This method extracts just the inherited object properties between two classes (object properties 
	 * who have @param domainClassUri in their domain and @param rangeClassUri in their range).
	 * If @param inheritance is true, it also returns the object properties inherited from parents.
	 * @param domainClassUri
	 * @param rangeClassUri
	 * @param inheritance
	 * @return
	 */
	public List<String> getOnlyInheritedObjectProperties(String domainClassUri, String rangeClassUri) {
		
		List<String> results = null;

		results = ontCache.getNotDirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;	
	}
	
	/**
	 * This function takes a class uri and returns the datatype properties who have this class in their domain. 
	 * If second parameter set to True, it also returns the datatype properties inherited from parents of the given class.
	 * @param domainClassUri
	 * @param inheritance
	 * @return
	 */
	public List<String> getDataPropertiesOfClass(String domainClassUri, boolean inheritance) {
		
		List<String> results = null;

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
		
		List<String> results = null;

		if (!inheritance)
			results = ontCache.getDirectOutObjectProperties().get(domainClassUri);
		else
			results = ontCache.getIndirectOutObjectProperties().get(domainClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;
	}
	
	public Map<String, String> getPrefixMap () {
		Map<String, String> nsMap = ontHandler.getOntModel().getNsPrefixMap();
		Map<String, String> prefixMap = new HashMap<String, String>();
		
		for(String ns: nsMap.keySet()) {
			if (!ns.equals("") && !prefixMap.containsKey(nsMap.get(ns)))
				prefixMap.put(nsMap.get(ns), ns);
		}
		return prefixMap;
	}
	
	/**
	 * returns URIs of all subclasses of @param classUri (also considers indirect subclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSubClasses(String classUri, boolean recursive) {

		if (!recursive)
			return ontCache.getDirectSubClasses().get(classUri);
		else
			return ontCache.getIndirectSubClasses().get(classUri);
	}
	
	/**
	 * returns URIs of all superclasses of @param classUri (also considers indirect superclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSuperClasses(String classUri, boolean recursive) {
		
		if (!recursive)
			return ontCache.getDirectSuperClasses().get(classUri);
		else
			return ontCache.getIndirectSuperClasses().get(classUri);	
	}
	
	/**
	 * returns URIs of all sub-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSubProperties(String propertyUri, boolean recursive) {

		if (!recursive)
			return ontCache.getDirectSubProperties().get(propertyUri);
		else
			return ontCache.getIndirectSubProperties().get(propertyUri);
	}
	
	/**
	 * returns URIs of all super-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSuperProperties(String propertyUri, boolean recursive) {

		if (!recursive)
			return ontCache.getDirectSuperProperties().get(propertyUri);
		else
			return ontCache.getIndirectSuperProperties().get(propertyUri);
	}
}
