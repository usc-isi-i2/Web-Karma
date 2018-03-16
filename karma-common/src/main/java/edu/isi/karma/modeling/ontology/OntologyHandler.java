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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger logger = LoggerFactory.getLogger(OntologyHandler.class.getName());

	private static OntModel ontModel = null;
	
	public OntologyHandler() {
		
		OntDocumentManager mgr = new OntDocumentManager();
		mgr.setProcessImports(false);
		OntModelSpec s = new OntModelSpec( OntModelSpec.OWL_MEM );
		s.setDocumentManager( mgr );
		ontModel = ModelFactory.createOntologyModel(s);
		ontModel.setStrictMode(false);
	}

	public OntModel getOntModel() {
		return ontModel;
	}

	public Label getUriLabel(String uri) {
		return getResourceLabel(ontModel.getResource(uri));
	}
	
	public Label getResourceLabel(Resource r) {
		if (r == null || !ontModel.containsResource(r)) {
			logger.debug("Could not find the resource in the ontology model.");
			return null;
		}
		String ns = r.getNameSpace();
		if (ns == null || ns.trim().length() == 0) ns = null;
		String prefix = ontModel.getNsURIPrefix(r.getNameSpace());
		if (prefix == null || prefix.trim().length() == 0) prefix = null;
		
		OntResource ontR = null;
		try { ontR = (OntResource)r;} catch(Exception e) {}
		if (ontR == null) {
			logger.error("No rdfs:label and rdfs:comment for resource:" + r.toString());
			return new Label(r.getURI(), ns, prefix);
		}
		//Get the rdfs:label and comment in English
		//If one is not available in English, then try and get one in any other language
		String rdfsLabel;
		try {
			rdfsLabel = ontR.getLabel("EN");
			if(rdfsLabel == null)
				rdfsLabel = ontR.getLabel(null);
		} catch(Exception e) {
			logger.error("No rdfs:label for resource:" + r.toString());
			rdfsLabel = "";
		}
		String rdfsComment;
		try {
			rdfsComment = ontR.getComment("EN");
			if(rdfsComment == null)
				rdfsComment = ontR.getComment(null);
		} catch(Exception e) {
			logger.error("No Comment for resource:" + r.toString());
			rdfsComment = "";
		}
		return new Label(r.getURI(), ns, prefix, rdfsLabel, rdfsComment);
	}
	
	
	/**
	 * returns URIs of given resources.
	 * @param resources
	 * @return
	 */
	public HashSet<String> getResourcesUris(HashSet<OntResource> resources) {
		HashSet<String> resourcesURIs = new HashSet<>();
		if (resources != null)
			for (OntResource r: resources) {
				resourcesURIs.add(r.getURI());
			}
		return resourcesURIs;
	}
	
	/**
	 * returns URI and Label of given resources.
	 * @param resources
	 * @return
	 */
	public HashMap<String, Label> getResourcesLabels(HashSet<OntResource> resources) {
		HashMap<String, Label> resourcesLabels = new HashMap<>();
		if (resources != null)
			for (OntResource r: resources) {
				resourcesLabels.put(r.getURI(), getResourceLabel(r));
			}
		return resourcesLabels;
	}
	
	public boolean isClass(String uri) {
		
		OntClass c = ontModel.getOntClass(uri);
		if (c != null)
			return true;
		
		return false;
	}
	
	public boolean isDataProperty(String uri) {

		DatatypeProperty dp = ontModel.getDatatypeProperty(uri);
		if (dp != null)
			return true;
		
		return false;
	}
		
	public boolean isProperty(String uri) {

		Property p = ontModel.getProperty(uri);
		if (p != null)
			return true;
		
		return false;
	}
	
	public boolean isObjectProperty(String uri) {

		ObjectProperty op = ontModel.getObjectProperty(uri);
		if (op != null)
			return true;
		
		return false;
	}
	
	/**
	 * Returns the inverse property of the property with given URI
	 * @param uri
	 * @return
	 */
	public Label getInverseProperty(String uri) {
		ObjectProperty op = ontModel.getObjectProperty(uri);
		if (op == null)
			return null;
		OntProperty inverseProp = null; 
		try {
			inverseProp = op.getInverse();
		} catch (ConversionException e) {
			logger.error(e.getMessage());
		}
		if (inverseProp != null) {
			return getUriLabel(inverseProp.getURI());
		}
		else
			return null;
	}
	
	/**
	 * Returns the inverseOf property of the property with given URI
	 * @param uri
	 * @return
	 */
	public Label getInverseOfProperty(String uri) {
		ObjectProperty op = ontModel.getObjectProperty(uri);
		if (op == null)
			return null;
		OntProperty inverseOfProp = null;
		try {
			inverseOfProp = op.getInverse();
		} catch (ConversionException e) {
			logger.error(e.getMessage());
		}
		if (inverseOfProp != null) {
			return getUriLabel(inverseOfProp.getURI());
		}
		else
			return null;
	}
	
	/**
	 * This method gets a resource (class or property) and adds the parents of the resource to the second parameter.
	 * If third parameter is set to true, it adds the parents recursively.
	 * @param r
	 * @param resources
	 * @param recursive
	 */
	public void getParents(OntResource r, HashSet<OntResource> resources, boolean recursive) {
		
		OntClass c = null;
		OntProperty p = null;
		
		if (resources == null) 
			resources = new HashSet<>();

		if (r == null)
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
                	HashSet<OntResource> members = new HashSet<>();
                	getMembers(superC, members, false);
                	for (OntResource or : members) {
                		resources.add(or);
                		if (recursive)
                			getParents(or, resources, recursive);
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
	public void getChildren(OntResource r, HashSet<OntResource> resources, boolean recursive) {
		
		OntClass c = null;
		OntProperty p = null;
		
		if (resources == null) 
			resources = new HashSet<>();

		if (r == null)
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
                	HashSet<OntResource> members = new HashSet<>();
                	getMembers(subC, members, false);
                	for (OntResource or : members) {
                		resources.add(or);
                		if (recursive)
                			getChildren(or, resources, recursive);
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
	public void getMembers(OntResource r, HashSet<OntResource> resources, boolean recursive) {

		if (resources == null) 
			resources = new HashSet<>();

		if (r == null)
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
				      try {
				    	  OntResource op = i.next();
//				    	  OntClass op = (OntClass) i.next();
				    	  getMembers(op, resources, recursive);
				      } catch (ConversionException e) {
				    	  logger.error(e.getMessage());
				      }
				  }
			
			// intersectionOf
			} else if (c.isIntersectionClass()) {
				IntersectionClass ic = c.asIntersectionClass();
				  for (Iterator<? extends OntClass> i = ic.listOperands(); i.hasNext(); ) {
				      try {
				    	  OntResource op = i.next();
//				    	  OntClass op = (OntClass) i.next();
				    	  getMembers(op, resources, recursive);
				      } catch (ConversionException e) {
				    	  logger.error(e.getMessage());
				      }
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

		HashSet<OntResource> resources = new HashSet<>();
		OntResource r = ontModel.getOntClass(classUri);
		if (r == null) return new HashMap<>();
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
		
		HashSet<OntResource> resources = new HashSet<>();
		OntResource r = ontModel.getOntClass(classUri);
		if (r == null) return new HashMap<>();
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

		HashSet<OntResource> resources = new HashSet<>();
		OntResource r = ontModel.getOntProperty(propertyUri);
		if (r == null) return new HashMap<>();
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

		HashSet<OntResource> resources = new HashSet<>();
		OntResource r = ontModel.getOntProperty(propertyUri);
		if (r == null) return new HashMap<>();
		getParents(r, resources, recursive);
		return getResourcesLabels(resources);
	}
}
