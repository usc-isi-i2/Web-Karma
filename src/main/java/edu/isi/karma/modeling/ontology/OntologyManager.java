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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.modeling.alignment.URI;

public class OntologyManager {
	
	static Logger logger = Logger.getLogger(OntologyManager.class.getName());

	private static OntModel ontModel = null;
	private static OntologyCache ontCache = null;
	
	public OntologyManager() {
		OntDocumentManager mgr = new OntDocumentManager();
		mgr.setProcessImports(false);
		OntModelSpec s = new OntModelSpec( OntModelSpec.OWL_MEM );
		s.setDocumentManager( mgr );
		ontModel = ModelFactory.createOntologyModel(s);
		ontCache = new OntologyCache();
	}

	public OntModel getOntModel() {
		return ontModel;
	}

	public OntologyCache getOntCache() {
		return ontCache;
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
			ontModel.read(s, null);
			
			// Store the new namespaces information in the namespace map maintained in OntologyGraphManager
//			String baseNS = m.getNsPrefixURI("");
//			m.setNsPrefix("dv" + ontologyNSIndex++, baseNS);	
			
			//System.out.println("Prefix map:" + ontologyModel.getNsPrefixMap());
		} catch (Throwable t) {
			logger.error("Error reading the OWL ontology file!", t);
			return false;
		}
		
		ontCache.init(this);
		/* Record the operation */
		logger.debug("done.");
		return true;
	}

	public URI getURIFromString(String uri) {
		Resource r = ontModel.getResource(uri);
		if (r == null || !ontModel.containsResource(r)) {
			logger.error("Could not find the resource " + uri + " in the ontology model.");
			return null;
		}
		String ns = r.getNameSpace();
		if (ns != null && ns.trim().length() == 0) ns = null;
		String prefix = ontModel.getNsURIPrefix(r.getNameSpace());
		if (prefix != null && prefix.trim().length() == 0) prefix = null;
		return new URI(r.getURI(), ns, prefix);
	}
	
	public boolean isClass(String label) {
		
		OntClass c = ontModel.getOntClass(label);
		if (c != null)
			return true;
		
		return false;
	}
	
	public boolean isDataProperty(String label) {

		DatatypeProperty dp = ontModel.getDatatypeProperty(label);
		if (dp != null)
			return true;
		
		return false;
	}
		
	public boolean isObjectProperty(String label) {

		ObjectProperty op = ontModel.getObjectProperty(label);
		if (op != null)
			return true;
		
		return false;
	}
	
	/**
	 * This method gets a resource (class or property) and adds the parents of the resource to the second parameter.
	 * If third parameter is set to true, it adds the parents recursively.
	 * @param r
	 * @param resources
	 * @param recursive
	 */
	public void getParents(OntResource r, List<OntResource> resources, boolean recursive) {
		
		OntClass c = null;
		OntProperty p = null;
		
		if (r == null || resources == null)
			return;

		if (r.isClass())
			c = r.asClass();
		else if  (r.isProperty())
			p = r.asProperty();
		else
			return;
		
		if (c != null && c.hasSuperClass()) {
			ExtendedIterator<OntClass> i = null;
			try {
//				if (recursive)
//					i = c.listSuperClasses(false);
//				else
					i = c.listSuperClasses(true);
			} catch (ConversionException e) {
				logger.debug(e.getMessage());
			}
            for (; i != null && i.hasNext();) {
                OntClass superC = (OntClass) i.next();
                if (superC.isURIResource()) {
                	resources.add(superC);
                	if (recursive)
                		getParents(superC, resources, recursive);
                } else {
            		List<OntResource> members = new ArrayList<OntResource>();
                	getMembers(superC, members, false);
                	for (int j = 0; j < members.size(); j++) {
                		resources.add(members.get(j));
                		if (recursive)
                			getParents(members.get(j), resources, recursive);
                	}
                }
            }
		}

		if (p != null) {
			ExtendedIterator<? extends OntProperty> i = null;
			try {
	//			if (recursive)
	//				i = p.listSuperProperties(false);
	//			else
					i = p.listSuperProperties(true);
			} catch (ConversionException e) {
				logger.debug(e.getMessage());
			}
            for (; i != null && i.hasNext();) {
                
            	OntResource superP = i.next();
            	//if (superP.a)
                if (superP.isURIResource()) {
                	resources.add(superP);
                	if (recursive)
                		getParents(superP, resources, recursive);
                } 
//                else {
//            		List<OntResource> members = new ArrayList<OntResource>();
//                	getMembers(superP, members, false);
//                	for (int j = 0; j < members.size(); j++) {
//                		resources.add(members.get(j));
//                		if (recursive)
//                			getParents(members.get(j), resources, recursive);
//                	}
//                }
            }
		}
	}

	/**
	 * This method gets a resource (class or property) and adds the children of the resource to the second parameter.
	 * If third parameter is set to true, it adds the children recursively.
	 * @param r
	 * @param resources
	 * @param recursive
	 */
	public void getChildren(OntResource r, List<OntResource> resources, boolean recursive) {
		
		OntClass c = null;
		OntProperty p = null;
		
		if (r == null || resources == null)
			return;

		if (r.isClass())
			c = r.asClass();
		else if  (r.isProperty())
			p = r.asProperty();
		else
			return;
		
		if (c != null && c.hasSubClass()) {
			ExtendedIterator<OntClass> i = null;
			try {
//				if (recursive)
//					i = c.listSubClasses(false);
//				else
					i = c.listSubClasses(true);
			} catch (ConversionException e) {
				logger.debug(e.getMessage());
			}
            for (; i != null && i.hasNext();) {
                OntClass subC = (OntClass) i.next();
                if (subC.isURIResource()) {
                	resources.add(subC);
                	if (recursive)
                		getChildren(subC, resources, recursive);
                } else {
            		List<OntResource> members = new ArrayList<OntResource>();
                	getMembers(subC, members, false);
                	for (int j = 0; j < members.size(); j++) {
                		resources.add(members.get(j));
                		if (recursive)
                			getChildren(members.get(j), resources, recursive);
                	}
                }
            }
		}

		if (p != null) {
			ExtendedIterator<? extends OntProperty> i = null;
			try {
//				if (recursive)
//					i = p.listSubProperties(false);
//				else
					i = p.listSubProperties(true);
			} catch (ConversionException e) {
				logger.debug(e.getMessage());
			}
            for (; i != null && i.hasNext();) {
                OntProperty subP = (OntProperty) i.next();
                if (subP.isURIResource()) {
                	resources.add(subP);
                	if (recursive)
                		getParents(subP, resources, recursive);
                }
//                else {
//            		List<OntResource> members = new ArrayList<OntResource>();
//                	getMembers(subP, members, false);
//                	for (int j = 0; j < members.size(); j++) {
//                		resources.add(members.get(j));
//                		if (recursive)
//                			getParents(members.get(j), resources, recursive);
//                	}
//                }
            }
		}
	}

	/**
	 * This method gets a resource and adds the members of the resource to the second parameter.
	 * For example, for a resource like "A or (B and C)", it extracts three elements A, B, C.
	 * If third parameter is set to true, it also adds the children of each member.
	 * @param r
	 * @param resources
	 * @param recursive
	 */
	public void getMembers(OntResource r, List<OntResource> resources, boolean recursive) {

		if (r == null || resources == null)
			return;

		if (r.isClass()) { 
			OntClass c = r.asClass();
			
			// simple class
			if (c.isURIResource()) {
				resources.add(c);
				if (recursive)
					getChildren(c, resources, true);
				return;
			}
			
			// unionOf
			else if (c.isUnionClass()) { // in form of unionOf or intersectionOf
				UnionClass uc = c.asUnionClass();
				  for (Iterator<? extends OntClass> i = uc.listOperands(); i.hasNext(); ) {
				      OntClass op = (OntClass) i.next();
			    	  getMembers(op, resources, recursive);
				  }
			
			// intersectionOf
			} else if (c.isIntersectionClass()) {
				IntersectionClass ic = c.asIntersectionClass();
				  for (Iterator<? extends OntClass> i = ic.listOperands(); i.hasNext(); ) {
				      OntClass op = (OntClass) i.next();
			    	  getMembers(op, resources, recursive);
				  }
			}
		}
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
		
		List<String> superClasses = getSuperClasses(subClassUri, recursive);
		
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
		
		List<String> subClasses = getSubClasses(superClassUri, recursive);
		
		for (int i = 0; i < subClasses.size(); i++) {
			if (subClassUri.equalsIgnoreCase(subClasses.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * returns URIs of all subclasses of @param classUri (also considers indirect subclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public List<String> getSubClasses(String classUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntClass(classUri);
		if (r == null) return null;
		getChildren(r, resources, recursive);
		return getResourcesURIs(resources);
	}
	
	/**
	 * returns URIs of all superclasses of @param classUri (also considers indirect superclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public List<String> getSuperClasses(String classUri, boolean recursive) {
		
		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntClass(classUri);
		if (r == null) return null;
		getParents(r, resources, recursive);
		return getResourcesURIs(resources);
	}

	/**
	 * returns URIs of all sub-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public List<String> getSubProperties(String propertyUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntProperty(propertyUri);
		if (r == null) return null;
		getChildren(r, resources, recursive);
		return getResourcesURIs(resources);
	}
	
	/**
	 * returns URIs of all super-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public List<String> getSuperProperties(String propertyUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntProperty(propertyUri);
		if (r == null) return null;
		getParents(r, resources, recursive);
		return getResourcesURIs(resources);
	}
	
	/**
	 * returns URIs of given resources.
	 * @param resources
	 * @return
	 */
	public List<String> getResourcesURIs(List<OntResource> resources) {
		List<String> resourcesURIs = new ArrayList<String>();
		if (resources != null)
			for (OntResource r: resources) {
				if (resourcesURIs.indexOf(r.getURI()) == -1)
					resourcesURIs.add(r.getURI());
			}
		return resourcesURIs;
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

}
