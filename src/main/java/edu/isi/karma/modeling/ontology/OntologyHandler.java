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

import java.util.ArrayList;
import java.util.HashMap;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.rep.alignment.Label;

class OntologyHandler {
	
	static Logger logger = Logger.getLogger(OntologyHandler.class.getName());

	private static OntModel ontModel = null;
	
	public OntologyHandler() {
		
		OntDocumentManager mgr = new OntDocumentManager();
		mgr.setProcessImports(false);
		OntModelSpec s = new OntModelSpec( OntModelSpec.OWL_MEM );
		s.setDocumentManager( mgr );
		ontModel = ModelFactory.createOntologyModel(s);
	}

	public OntModel getOntModel() {
		return ontModel;
	}

	public Label getUriLabel(String uriString) {
		Resource r = ontModel.getResource(uriString);
		if (r == null || !ontModel.containsResource(r)) {
			logger.debug("Could not find the resource " + uriString + " in the ontology model.");
			return null;
		}
		String ns = r.getNameSpace();
		if (ns != null && ns.trim().length() == 0) ns = null;
		String prefix = ontModel.getNsURIPrefix(r.getNameSpace());
		if (prefix != null && prefix.trim().length() == 0) prefix = null;
		
		OntResource ontR = null;
		try { ontR = (OntResource)r;} catch(Exception e) {}
		if (ontR == null)
			return new Label(r.getURI(), ns, prefix);
		
		String rdfsLabel = ontR.getLabel(null);
		String rdfsComment = ontR.getComment(null);
		
		return new Label(r.getURI(), ns, prefix, rdfsLabel, rdfsComment);
	}
	
	public Label getResourceLabel(Resource r) {
		if (r == null || !ontModel.containsResource(r)) {
			logger.debug("Could not find the resource in the ontology model.");
			return null;
		}
		String ns = r.getNameSpace();
		if (ns != null && ns.trim().length() == 0) ns = null;
		String prefix = ontModel.getNsURIPrefix(r.getNameSpace());
		if (prefix != null && prefix.trim().length() == 0) prefix = null;
		
		OntResource ontR = null;
		try { ontR = (OntResource)r;} catch(Exception e) {}
		if (ontR == null)
			return new Label(r.getURI(), ns, prefix);
		
		String rdfsLabel = ontR.getLabel(null);
		String rdfsComment = ontR.getComment(null);
		
		return new Label(r.getURI(), ns, prefix, rdfsLabel, rdfsComment);
	}
	
	
	/**
	 * returns URIs of given resources.
	 * @param resources
	 * @return
	 */
	public List<String> getResourcesUris(List<OntResource> resources) {
		List<String> resourcesURIs = new ArrayList<String>();
		if (resources != null)
			for (OntResource r: resources) {
				if (resourcesURIs.indexOf(r.getURI()) == -1)
					resourcesURIs.add(r.getURI());
			}
		return resourcesURIs;
	}
	
	/**
	 * returns URI and Label of given resources.
	 * @param resources
	 * @return
	 */
	public HashMap<String, Label> getResourcesLabels(List<OntResource> resources) {
		HashMap<String, Label> resourcesLabels = new HashMap<String, Label>();
		if (resources != null)
			for (OntResource r: resources) {
				if (!resourcesLabels.containsKey(r.getURI()))
					resourcesLabels.put(r.getURI(), getResourceLabel(r));
			}
		return resourcesLabels;
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
		
	public boolean isProperty(String label) {

		Property p = ontModel.getProperty(label);
		if (p != null)
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
		
		if (c != null) { // && c.hasSuperClass()) {
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
		
		if (c != null) {
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
                		getChildren(subP, resources, recursive);
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
	 * returns URIs of all subclasses of @param classUri (also considers indirect subclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSubClasses(String classUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntClass(classUri);
		if (r == null) return new HashMap<String, Label>();
		getChildren(r, resources, recursive);
		return getResourcesLabels(resources);
	}
	
	/**
	 * returns URIs of all superclasses of @param classUri (also considers indirect superclasses if second parameter is true).
	 * @param classUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSuperClasses(String classUri, boolean recursive) {
		
		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntClass(classUri);
		if (r == null) return new HashMap<String, Label>();
		getParents(r, resources, recursive);
		return getResourcesLabels(resources);
	}
	
	/**
	 * returns URIs of all sub-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSubProperties(String propertyUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntProperty(propertyUri);
		if (r == null) return new HashMap<String, Label>();
		getChildren(r, resources, recursive);
		return getResourcesLabels(resources);
	}
	
	/**
	 * returns URIs of all super-properties of @param propertyUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public HashMap<String, Label> getSuperProperties(String propertyUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntProperty(propertyUri);
		if (r == null) return new HashMap<String, Label>();
		getParents(r, resources, recursive);
		return getResourcesLabels(resources);
	}
}
