package edu.isi.karma.modeling.ontology;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class OntologyManager {
	
	static Logger logger = Logger.getLogger(OntologyManager.class.getName());

	private static OntModel ontModel = null;

	private static OntologyManager _InternalInstance = null;
	public static OntologyManager Instance()
	{
		if (_InternalInstance == null)
		{
			OntDocumentManager mgr = new OntDocumentManager();
			mgr.setProcessImports(false);
			OntModelSpec s = new OntModelSpec( OntModelSpec.OWL_MEM );
			s.setDocumentManager( mgr );
			ontModel = ModelFactory.createOntologyModel(s);
			_InternalInstance = new OntologyManager();
		}
		return _InternalInstance;
	}
	
	public OntModel getOntModel() {
		return ontModel;
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
			ExtendedIterator<OntClass> i;
			if (recursive)
				i = c.listSuperClasses(false);
			else
				i = c.listSuperClasses(true);
            for (; i.hasNext();) {
                OntClass superC = (OntClass) i.next();
                if (superC.isURIResource())
                	resources.add(superC);
                else {
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
			ExtendedIterator<? extends OntProperty> i;
			if (recursive)
				i = p.listSuperProperties(false);
			else
				i = p.listSuperProperties(true);
			
            for (; i.hasNext();) {
                OntProperty superP = (OntProperty) i.next();
                if (superP.isURIResource()) {
                	resources.add(superP);
                	if (recursive)
                		getParents(superP, resources, recursive);
                } else {
            		List<OntResource> members = new ArrayList<OntResource>();
                	getMembers(superP, members, false);
                	for (int j = 0; j < members.size(); j++) {
                		resources.add(members.get(j));
                		if (recursive)
                			getParents(members.get(j), resources, recursive);
                	}
                }
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
			ExtendedIterator<OntClass> i;
			if (recursive)
				i = c.listSubClasses(false);
			else
				i = c.listSubClasses(true);
            for (; i.hasNext();) {
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
			ExtendedIterator<? extends OntProperty> i;
			if (recursive)
				i = p.listSubProperties(false);
			else
				i = p.listSubProperties(true);
			
            for (; i.hasNext();) {
                OntProperty subP = (OntProperty) i.next();
                if (subP.isURIResource())
                	resources.add(subP);
                else {
            		List<OntResource> members = new ArrayList<OntResource>();
                	getMembers(subP, members, false);
                	for (int j = 0; j < members.size(); j++) {
                		resources.add(members.get(j));
                		if (recursive)
                			getParents(members.get(j), resources, recursive);
                	}
                }
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
			if (superClassUri.equalsIgnoreCase(superClasses.get(i).toString() )) {
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
			if (subClassUri.equalsIgnoreCase(subClasses.get(i).toString() )) {
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
			results = OntologyCache.Instance().getPropertyDirectDomains().get(propertyUri);
		else
			results = OntologyCache.Instance().getPropertyIndirectDomains().get(propertyUri);
		
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
		
		List<String> objectProperties = OntologyCache.Instance().getDirectInObjectProperties().get(rangeClassUri);
		List<String> domains = new ArrayList<String>();
		List<String> temp;
		
		if (objectProperties == null)
			return domains;
		
		for (int i = 0; i < objectProperties.size(); i++) {
			if (!recursive) 
				temp = OntologyCache.Instance().getPropertyDirectDomains().get(objectProperties.get(i));
			else
				temp = OntologyCache.Instance().getPropertyIndirectDomains().get(objectProperties.get(i));
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
			propertyDomains = OntologyCache.Instance().getPropertyDirectDomains().get(propertyUri);
		else
			propertyDomains = OntologyCache.Instance().getPropertyIndirectDomains().get(propertyUri);
		
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
			results = OntologyCache.Instance().getDirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		else
			results = OntologyCache.Instance().getIndirectDomainRangeProperties().get(domainClassUri+rangeClassUri);
		
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
			results = OntologyCache.Instance().getDirectOutDataProperties().get(domainClassUri);
		else
			results = OntologyCache.Instance().getIndirectOutDataProperties().get(domainClassUri);
		
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
			results = OntologyCache.Instance().getDirectOutObjectProperties().get(domainClassUri);
		else
			results = OntologyCache.Instance().getIndirectOutObjectProperties().get(domainClassUri);
		
		if (results == null)
			return new ArrayList<String>();
		
		return results;
	}

}
