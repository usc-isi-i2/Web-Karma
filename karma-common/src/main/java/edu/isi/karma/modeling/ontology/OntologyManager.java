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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.util.EncodingDetector;

public class OntologyManager  {
	
	static Logger logger = LoggerFactory.getLogger(OntologyManager.class.getName());

	private OntologyHandler ontHandler = null;
	private OntologyCache ontCache = null;
	private List<OntologyUpdateListener> ontUpdateListeners; 
	private String contextId; 
	public OntologyManager(String contextId) {
		this.contextId = contextId;
		ontHandler = new OntologyHandler();
		ontCache = new OntologyCache(ontHandler, contextId);
		ontUpdateListeners = new ArrayList<>();	
	}
	
	public String getContextId()
	{
		return contextId;
	}

	public boolean isEmpty() {
		return ontHandler.getOntModel().isEmpty();
	}
	
	public boolean isClass(String uri) {

		return this.ontCache.getClasses().containsKey(uri);
	}

	public boolean isProperty(String uri) {

		return this.ontCache.getProperties().containsKey(uri);
	}

	public boolean isDataProperty(String uri) {

		return this.ontCache.getDataProperties().containsKey(uri);
	}

	public boolean isObjectProperty(String uri) {

		return this.ontCache.getObjectProperties().containsKey(uri);
	}
	
	public void subscribeListener(OntologyUpdateListener ontUpdateListener) {
		ontUpdateListeners.add(ontUpdateListener);
	}
	
	public void unsubscribeListener(OntologyUpdateListener ontUpdateListener) {
		ontUpdateListeners.remove(ontUpdateListener);
	}
	
	private void notifyListeners() {
		for (OntologyUpdateListener o : ontUpdateListeners)
			o.ontologyModelUpdated();
	}
	
	public boolean doImportAndUpdateCache(File sourceFile, String encoding) throws IOException {

		if (sourceFile == null) {
			logger.debug("input file is null.");
			return false;
		}
		
		logger.debug("Importing " + sourceFile.getName() + " OWL Ontology ...");

		if(!sourceFile.exists()){
			logger.error("file does not exist  " + sourceFile.getAbsolutePath());
			return false;
		}
		
		
		InputStreamReader s = EncodingDetector.getInputStreamReader(sourceFile, encoding);
		if (Files.getFileExtension(sourceFile.getName()).equalsIgnoreCase("N3"))
			ontHandler.getOntModel().read(s, null, "N3");
		else if (Files.getFileExtension(sourceFile.getName()).equalsIgnoreCase("TTL"))
			ontHandler.getOntModel().read(s, null, "TURTLE");
		else
			ontHandler.getOntModel().read(s, null); // default lang = "RDF/XML"
		
		
		// update the cache
		ontCache = new OntologyCache(ontHandler, contextId);
		ontCache.init();
		
		// notify listeners
		this.notifyListeners();
		
		logger.debug("done.");
		return true;
	}
	
	public boolean doImport(File sourceFile, String encoding) throws IOException {

		if (sourceFile == null) {
			logger.debug("input file is null.");
			return false;
		}
		
		logger.debug("Importing " + sourceFile.getName() + " OWL Ontology ...");

		if(!sourceFile.exists()){
			logger.error("file does not exist  " + sourceFile.getAbsolutePath());
			return false;
		}
		
		/***
		 *  values for lang are 
			"RDF/XML", 
			"N-TRIPLE", 
			"TURTLE" (or "TTL") and 
			"N3". 
			null represents the default language, "RDF/XML". 
			"RDF/XML-ABBREV" is a synonym for "RDF/XML". 
		 */
		
		InputStreamReader s = EncodingDetector.getInputStreamReader(sourceFile, encoding);
		if (Files.getFileExtension(sourceFile.getName()).equalsIgnoreCase("N3"))
			ontHandler.getOntModel().read(s, null, "N3");
		else if (Files.getFileExtension(sourceFile.getName()).equalsIgnoreCase("TTL"))
			ontHandler.getOntModel().read(s, null, "TURTLE");
		else if (Files.getFileExtension(sourceFile.getName()).equalsIgnoreCase("NT"))
			ontHandler.getOntModel().read(s, null, "N-TRIPLE");
		else
			ontHandler.getOntModel().read(s, null); // default lang = "RDF/XML"
		
		
		// notify listeners
		this.notifyListeners();

		logger.debug("done.");
		return true;
	}
	
	public void updateCache() {
		ontCache = new OntologyCache(ontHandler, contextId);
		ontCache.init();
	}
	
	public HashMap<String, Label> getClasses() {
		return this.ontCache.getClasses();
	}

	public HashMap<String, Label> getProperties() {
		return this.ontCache.getProperties();
	}

	public HashMap<String, Label> getDataProperties() {
		return this.ontCache.getDataProperties();
	}

	public HashMap<String, Label> getObjectProperties() {
		return this.ontCache.getObjectProperties();
	}

	public HashMap<String, Label> getDataPropertiesWithoutDomain() {
		return this.ontCache.getDataPropertiesWithoutDomain();
	}

	public HashMap<String, Label> getObjectPropertiesWithOnlyDomain() {
		return this.ontCache.getObjectPropertiesWithOnlyDomain();
	}

	public HashMap<String, Label> getObjectPropertiesWithOnlyRange() {
		return this.ontCache.getObjectPropertiesWithOnlyRange();
	}

	public HashMap<String, Label> getObjectPropertiesWithoutDomainAndRange() {
		return this.ontCache.getObjectPropertiesWithoutDomainAndRange();
	}
	
	public ObjectPropertyType getObjectPropertyType(String sourceUri, String targetUri, String linkUri) {
		if (getObjectPropertiesDirect(sourceUri, targetUri) != null &&
			getObjectPropertiesDirect(sourceUri, targetUri).contains(linkUri))
				return ObjectPropertyType.Direct;
		else if (getObjectPropertiesIndirect(sourceUri, targetUri) != null &&
			getObjectPropertiesIndirect(sourceUri, targetUri).contains(linkUri))
				return ObjectPropertyType.Indirect;
		else if (getObjectPropertiesWithOnlyDomain(sourceUri) != null &&
			getObjectPropertiesWithOnlyDomain(sourceUri).contains(linkUri))
				return ObjectPropertyType.WithOnlyDomain;
		else if (getObjectPropertiesWithOnlyRange(targetUri) != null &&
			getObjectPropertiesWithOnlyRange(targetUri).contains(linkUri))
				return ObjectPropertyType.WithOnlyRange;
		else if (getObjectPropertiesWithoutDomainAndRange() != null &&
			getObjectPropertiesWithoutDomainAndRange().keySet().contains(linkUri))
				return ObjectPropertyType.WithoutDomainAndRange;
		else
			return ObjectPropertyType.None;
	}
	
//	public OntologyTreeNode getClassHierarchy() {
//		return this.ontCache.getClassHierarchy();
//	}

//	public OntologyTreeNode getObjectPropertyHierarchy() {
//		return this.ontCache.getObjectPropertyHierarchy();
//	}
//
//	public OntologyTreeNode getDataPropertyHierarchy() {
//		return this.ontCache.getDataPropertyHierarchy();
//	}
	
	public Label getUriLabel(String uri) {
		return this.ontCache.getUriLabel(uri);
	}
	
	public HashSet<String> getPossibleUris(String sourceUri, String targetUri) {

		HashSet<String> linkUris =
				new HashSet<>();

		HashSet<String> objectPropertiesDirect;
		HashSet<String> objectPropertiesIndirect;
		HashSet<String> objectPropertiesWithOnlyDomain;
		HashSet<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange = 
				getObjectPropertiesWithoutDomainAndRange();
							
//		if (targetUri.endsWith("Person") && sourceUri.endsWith("Organisation"))
//			logger.debug("debug");
		
//		if (sourceUri.endsWith("Vehicle") && targetUri.endsWith("Observation") ||
//		targetUri.endsWith("Vehicle") && sourceUri.endsWith("Observation"))
//				logger.debug("debug");

		objectPropertiesDirect = getObjectPropertiesDirect(sourceUri, targetUri);
		if (objectPropertiesDirect != null) {
			for (String s : objectPropertiesDirect)
			linkUris.add(s);
		}

		objectPropertiesIndirect = getObjectPropertiesIndirect(sourceUri, targetUri);
		if (objectPropertiesIndirect != null) {
			for (String s : objectPropertiesIndirect)
				linkUris.add(s);
		}		

		objectPropertiesWithOnlyDomain = getObjectPropertiesWithOnlyDomain(sourceUri);
		if (objectPropertiesWithOnlyDomain != null) {
			for (String s : objectPropertiesWithOnlyDomain)
				linkUris.add(s);
		}	
	
		objectPropertiesWithOnlyRange = getObjectPropertiesWithOnlyRange(targetUri);
		if (objectPropertiesWithOnlyRange != null) {
			for (String s : objectPropertiesWithOnlyRange)
				linkUris.add(s);
		}	

		if (isSubClass(sourceUri, targetUri, true)) 
			linkUris.add(Uris.RDFS_SUBCLASS_URI);
		
		if (objectPropertiesWithoutDomainAndRange != null) {
			for (String s : objectPropertiesWithoutDomainAndRange.keySet())
				linkUris.add(s);
		}

		return linkUris;
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
		
		if (ontCache.getDirectSubClassCheck().contains(subClassUri + superClassUri))
			return true;
		else if (recursive) 
			return ontCache.getIndirectSubClassCheck().contains(subClassUri + superClassUri);
		
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
		
		if (ontCache.getDirectSubPropertyCheck().contains(subPropertyUri + superPropertyUri))
			return true;
		else if (recursive) 
			return ontCache.getIndirectSubPropertyCheck().contains(subPropertyUri + superPropertyUri);
		
		return false;
	}
		
	
	/**
	 * This method takes a property URI and returns domains of that property.
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashSet<String> getDomainsOfProperty(String propertyUri, boolean recursive) {

		HashSet<String> results = new HashSet<>();
		HashSet<String> direct = null;
		HashSet<String> indirect = null;
		
		direct = ontCache.getPropertyDirectDomains().get(propertyUri);
		if (direct != null) results.addAll(direct);
		if (recursive) indirect = ontCache.getPropertyIndirectDomains().get(propertyUri);
		if (indirect != null) results.addAll(indirect);
		
		return results;

	}

	/**
	 * This method takes a property URI and returns ranges of that property.
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashSet<String> getRangesOfProperty(String propertyUri, boolean recursive) {

		HashSet<String> results = new HashSet<>();
		HashSet<String> direct = null;
		HashSet<String> indirect = null;
		
		direct = ontCache.getPropertyDirectRanges().get(propertyUri);		
		if (direct != null) results.addAll(direct);
		if (recursive) indirect = ontCache.getPropertyIndirectRanges().get(propertyUri);
		if (indirect != null) results.addAll(indirect);
		
		return results;

	}
	
	/**
	 * This method takes @param rangeClassUri and for object properties whose ranges includes this parameter, 
	 * returns all of their domains.
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param rangeUri
	 * @param recursive
	 * @return
	 */
	public HashSet<String> getDomainsGivenRange(String rangeUri, boolean recursive) {
		
		HashSet<String> objectProperties = ontCache.getDirectInObjectProperties().get(rangeUri);
		HashSet<String> results = new HashSet<>();
		HashSet<String> direct = null;
		HashSet<String> indirect = null;
		
		if (objectProperties == null)
			return results;
		
		for (String op : objectProperties) {
			direct = ontCache.getPropertyDirectDomains().get(op);
			if (direct != null) results.addAll(direct);
			if (recursive) indirect = ontCache.getPropertyIndirectDomains().get(op);
			if (indirect != null) results.addAll(indirect);
		}
		
		return results;
	}
	
	/**
	 * This method takes @param domainUri and rangeClassUri and for returns all links/properties with this domain & range
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param domainUri
	 * @param rangeUri
	 * @param recursive
	 * @return
	 */
	public Map<String, Label> getObjectPropertiesByDomainRange(String domainUri, String rangeUri, boolean recursive) {
		logger.debug("getObjectPropertiesByDomainRange:" + domainUri + "," + rangeUri + "," + ontCache);
		if(domainUri == null || domainUri.length() == 0)
			return this.getObjectPropertiesByRange(rangeUri, recursive);
		
		if(rangeUri == null || rangeUri.length() == 0)
			return this.getObjectPropertiesByDomain(domainUri, recursive);
		
		HashSet<String> objectProperties = ontCache.getDirectInObjectProperties().get(rangeUri);
		if(recursive) {
			HashSet<String> propRecursive = ontCache.getIndirectInObjectProperties().get(rangeUri);
			if(propRecursive != null) {
				if(objectProperties != null)
					objectProperties.addAll(propRecursive);
				else
					objectProperties = propRecursive;
			}
		}
		HashMap<String, Label> results = new HashMap<>();
		HashSet<String> direct = null;
		HashSet<String> indirect = null;
		
		logger.debug("Got object properties:" + objectProperties);
		if (objectProperties == null)
			return results;
		
		for (String op : objectProperties) {
			boolean propAdded = false;
			direct = ontCache.getPropertyDirectDomains().get(op);
			if (direct != null) {
				for(String directDomain : direct) {
					if(directDomain.equals(domainUri)) {
						results.put(op, ontCache.getPropertyLabel(op));
						propAdded = true;
						break;
					}
				}
				//results.addAll(direct);
			}
			if(propAdded)
				continue;
			if (recursive) indirect = ontCache.getPropertyIndirectDomains().get(op);
			if (indirect != null) {
				//results.addAll(indirect);
				for(String indirectDomain : indirect) {
					if(indirectDomain.equals(domainUri)) {
						results.put(op, ontCache.getPropertyLabel(op));
						propAdded = true;
						break;
					}
				}
			}
		}
		
		logger.info("got back " + results.size() + " results");
		return results;
	}
	
	
	public Map<String, Label> getObjectPropertiesByDomain(String domainUri, boolean recursive) {
		HashSet<String> all = new HashSet<>();
		HashSet<String> objectProperties = ontCache.getDirectOutObjectProperties().get(domainUri);
		if(objectProperties != null)
			all.addAll(objectProperties);
		if(recursive) {
			HashSet<String> propRecursive = ontCache.getIndirectOutObjectProperties().get(domainUri);
			if(propRecursive != null)
				all.addAll(propRecursive);
		}
		HashMap<String, Label> results = new HashMap<>();
		
		for (String op : all) {
			results.put(op, ontCache.getPropertyLabel(op));
		}
		
		return results;
	}
	
	public Map<String, Label> getObjectPropertiesByRange(String rangeUri, boolean recursive) {
		HashSet<String> all = new HashSet<>();
		HashSet<String> objectProperties = ontCache.getDirectInObjectProperties().get(rangeUri);
		if(objectProperties != null)
			all.addAll(objectProperties);
		if(recursive) {
			HashSet<String> propRecursive = ontCache.getIndirectInObjectProperties().get(rangeUri);
			if(propRecursive != null)
				all.addAll(propRecursive);
		}
		HashMap<String, Label> results = new HashMap<>();
		
		for (String op : all) {
			results.put(op, ontCache.getPropertyLabel(op));
		}
		
		return results;
	}
	
	public Map<String, Label> getDataPropertiesByDomain(String domainUri, boolean recursive) {
		HashSet<String> all = new HashSet<>();
		HashSet<String> dataProperties = ontCache.getDirectOutDataProperties().get(domainUri);
		if(dataProperties != null)
			all.addAll(dataProperties);
		if(recursive) {
			HashSet<String> propRecursive = ontCache.getIndirectOutDataProperties().get(domainUri);
			if(propRecursive != null)
				all.addAll(propRecursive);
		}
		HashMap<String, Label> results = new HashMap<>();
		
		for (String op : all) {
			results.put(op, ontCache.getPropertyLabel(op));
		}
		
		return results;
	}
	
	public Map<String, String> getPrefixMap () {
		Map<String, String> nsMap = ontHandler.getOntModel().getNsPrefixMap();
		Map<String, String> prefixMap = new HashMap<>();
		
		for(Map.Entry<String, String> stringStringEntry : nsMap.entrySet()) {
			if (!stringStringEntry.getKey().equals("") && !prefixMap.containsKey(stringStringEntry.getValue()))
				prefixMap.put(stringStringEntry.getValue(), stringStringEntry.getKey());
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
		
		HashMap<String, Label> direct = ontCache.getDirectSubClasses().get(classUri);
		if (!recursive) return direct;
		
		HashMap<String, Label> all = new HashMap<>();
		HashMap<String, Label> indirect = ontCache.getIndirectSubClasses().get(classUri);
		if (direct != null) all.putAll(direct);
		if (indirect != null) all.putAll(indirect);
		return all;
	}
	
	/**
	 * returns URIs of all superclasses of @param classUri (also considers indirect superclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSuperClasses(String classUri, boolean recursive) {
		
		HashMap<String, Label> direct = ontCache.getDirectSuperClasses().get(classUri);
		if (!recursive) return direct;
		
		HashMap<String, Label> all = new HashMap<>();
		HashMap<String, Label> indirect = ontCache.getIndirectSuperClasses().get(classUri);
		if (direct != null) all.putAll(direct);
		if (indirect != null) all.putAll(indirect);
		return all;
	}
	
	/**
	 * returns URIs of all sub-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSubProperties(String propertyUri, boolean recursive) {

		HashMap<String, Label> direct = ontCache.getDirectSubProperties().get(propertyUri);
		if (!recursive) return direct;
		
		HashMap<String, Label> all = new HashMap<>();
		HashMap<String, Label> indirect = ontCache.getIndirectSubProperties().get(propertyUri);
		if (direct != null) all.putAll(direct);
		if (indirect != null) all.putAll(indirect);
		return all;

	}
	
	/**
	 * returns URIs of all super-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSuperProperties(String propertyUri, boolean recursive) {

		HashMap<String, Label> direct = ontCache.getDirectSuperProperties().get(propertyUri);
		if (!recursive) return direct;
		
		HashMap<String, Label> all = new HashMap<>();
		HashMap<String, Label> indirect = ontCache.getIndirectSuperProperties().get(propertyUri);
		if (direct != null) all.putAll(direct);
		if (indirect != null) all.putAll(indirect);
		return all;

	}

//	/**
//	 * This function takes a class uri and returns the datatype properties who have this class in their domain. 
//	 * If second parameter set to True, it also returns the datatype properties inherited from parents of the given class.
//	 * @param domainUri
//	 * @param inheritance
//	 * @return
//	 */
//	public HashSet<String> getDataPropertiesOfClass(String domainUri, boolean inheritance) {
//
//		HashSet<String> direct = ontCache.getDirectOutDataProperties().get(domainUri);
//		if (!inheritance) return direct;
//		
//		HashSet<String> all = new HashSet<String>();
//		HashSet<String> indirect = ontCache.getIndirectOutDataProperties().get(domainUri);
//		if (direct != null) all.addAll(direct);
//		if (indirect != null) all.addAll(indirect);
//		return all;
//
//	}
//
//	/**
//	 * This function takes a class uri and returns the object properties who have this class in their domain. 
//	 * If second parameter set to True, it also returns the object properties inherited from parents of the given class.
//	 * @param domainUri
//	 * @param inheritance
//	 * @return
//	 */
//	public HashSet<String> getObjectPropertiesOfClass(String domainUri, boolean inheritance) {
//
//		HashSet<String> direct = ontCache.getDirectOutObjectProperties().get(domainUri);
//		if (!inheritance) return direct;
//		
//		HashSet<String> all = new HashSet<String>();
//		HashSet<String> indirect = ontCache.getIndirectOutObjectProperties().get(domainUri);
//		if (direct != null) all.addAll(direct);
//		if (indirect != null) all.addAll(indirect);
//		return all;
//	}
	
	public HashSet<String> getObjectPropertiesDirect(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return null;
		return this.ontCache.getDomainRangeToDirectProperties().get(sourceUri + targetUri);
	}

	public HashSet<String> getObjectPropertiesIndirect(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return null;
		return this.ontCache.getDomainRangeToIndirectProperties().get(sourceUri + targetUri);
	}

	public HashSet<String> getObjectPropertiesWithOnlyDomain(String domainUri) {
		
		if (domainUri == null) return null;
		
		HashSet<String> directOutProperties;
		HashSet<String> indirectOutProperties;
		HashSet<String> results = new HashSet<>();

		directOutProperties = this.ontCache.getDirectOutObjectProperties().get(domainUri);
		if (directOutProperties != null)
			for (String s : directOutProperties) 
				if (this.ontCache.getObjectPropertiesWithOnlyDomain().containsKey(s)) 
					results.add(s);

		indirectOutProperties = this.ontCache.getIndirectOutObjectProperties().get(domainUri);
		if (indirectOutProperties != null) 
			for (String s : indirectOutProperties) 
				if (this.ontCache.getObjectPropertiesWithOnlyDomain().containsKey(s)) 
					results.add(s);
		
		return results;
	}

	public HashSet<String> getObjectPropertiesWithOnlyRange(String rangeUri) {
		
		if (rangeUri == null) return null;
		
		HashSet<String> directInProperties;
		HashSet<String> indirectInProperties;
		HashSet<String> results = new HashSet<>();
		
		directInProperties = this.ontCache.getDirectInObjectProperties().get(rangeUri);
		if (directInProperties != null)
			for (String s : directInProperties) 
				if (this.ontCache.getObjectPropertiesWithOnlyRange().containsKey(s)) 
					results.add(s);

		indirectInProperties = this.ontCache.getIndirectInObjectProperties().get(rangeUri);
		if (indirectInProperties != null) 
			for (String s : indirectInProperties) 
				if (this.ontCache.getObjectPropertiesWithOnlyRange().containsKey(s)) 
					results.add(s);
		
		return results;
	}

	public boolean isConnectedByDirectProperty(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return false;
//		return this.ontCache.getConnectedByDirectProperties().contains(sourceUri + targetUri);
		HashSet<String> directProperties = this.ontCache.getDomainRangeToDirectProperties().get(sourceUri+targetUri);
		if (directProperties != null && !directProperties.isEmpty()) { 
			return true;
		}
		return false;
	}

	public boolean isConnectedByIndirectProperty(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return false;
//		return this.ontCache.getConnectedByIndirectProperties().contains(sourceUri + targetUri);
		HashSet<String> indirectProperties = this.ontCache.getDomainRangeToIndirectProperties().get(sourceUri+targetUri);
		if (indirectProperties != null && !indirectProperties.isEmpty()) { 
			return true;
		}
		return false;
	}

	public boolean isConnectedByDomainlessProperty(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return false;
//		return this.ontCache.getConnectedByDomainlessProperties().contains(sourceUri + targetUri);
		HashSet<String> directInProperties = this.ontCache.getDirectInObjectProperties().get(targetUri);
		if (directInProperties != null) {
			if (!Sets.intersection(this.ontCache.getObjectPropertiesWithOnlyRange().keySet(), directInProperties).isEmpty())
				return true;
		}
		HashSet<String> indirectInProperties = this.ontCache.getIndirectInObjectProperties().get(targetUri);
		if (indirectInProperties != null) {
			if (!Sets.intersection(this.ontCache.getObjectPropertiesWithOnlyRange().keySet(), indirectInProperties).isEmpty())
				return true;
		}
		return false;
	}

	public boolean isConnectedByRangelessProperty(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return false;
//		return this.ontCache.getConnectedByRangelessProperties().contains(sourceUri + targetUri);
		HashSet<String> directOutProperties = this.ontCache.getDirectOutObjectProperties().get(sourceUri);
		if (directOutProperties != null) {
			if (!Sets.intersection(this.ontCache.getObjectPropertiesWithOnlyDomain().keySet(), directOutProperties).isEmpty())
				return true;
		}
		HashSet<String> indirectOutProperties = this.ontCache.getIndirectOutObjectProperties().get(sourceUri);
		if (indirectOutProperties != null) {
			if (!Sets.intersection(this.ontCache.getObjectPropertiesWithOnlyDomain().keySet(), indirectOutProperties).isEmpty())
				return true;
		}
		return false;
	}
	
	public boolean isConnectedByDomainlessAndRangelessProperty(String sourceUri, String targetUri) {
		
		if (sourceUri == null || targetUri == null) return false;
		return (!this.ontCache.getObjectPropertiesWithoutDomainAndRange().isEmpty());
	}
	
//	public static void main(String[] args) throws IOException {
//		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().registerByKarmaHome("/Users/mohsen/karma-rodi/");
//		contextParameters.setParameterValue(ContextParameter.USER_CONFIG_DIRECTORY, "/Users/mohsen/karma-rodi/config");
//		OntologyManager ontManager = new OntologyManager(contextParameters.getId());
//		ontManager.doImport(new File("/Users/mohsen/karma-rodi/preloaded-ontologies/schema.nt"), "UTF-8");
//		ontManager.updateCache();
//		System.out.println("done.");
//	}
}

