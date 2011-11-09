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
	private void getParents(OntResource r, List<OntResource> resources, boolean recursive) {
		
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
	private void getChildren(OntResource r, List<OntResource> resources, boolean recursive) {
		
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
	private void getMembers(OntResource r, List<OntResource> resources, boolean recursive) {

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
	private List<String> getResourcesURIs(List<OntResource> resources) {
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
		List<OntResource> classes = new ArrayList<OntResource>();
		DatatypeProperty dp = ontModel.getDatatypeProperty(propertyUri);
		if (dp != null) {
			ExtendedIterator<? extends OntResource> itrDomains = dp.listDomain();
			while (itrDomains.hasNext()) {
				OntResource r = itrDomains.next();
				getMembers(r, classes, recursive);
			}
		}

		return getResourcesURIs(classes);
	}

	/**
	 * This method takes @param rangeClassUri and for object properties whose ranges includes this parameter, 
	 * returns all of their domains.
	 * returns the domains of all properties whose domains include all domains of that property.
	 * If @param recursive is true, it also returns the children of the domains.
	 * @param rangeClassUri
	 * @param recursive
	 * @return
	 */
	public List<String> getDomainsGivenRange(String rangeClassUri, boolean recursive) {
		// should add all subclasses to the results
		List<OntResource> domains = new ArrayList<OntResource>();
		List<OntResource> ranges = new ArrayList<OntResource>();
		
		ExtendedIterator<ObjectProperty> itrOP = ontModel.listObjectProperties();
		OntResource r;
		OntResource domain;
		while (itrOP.hasNext()) {
			ranges.clear();
			ObjectProperty op = itrOP.next();
			
			ExtendedIterator<? extends OntResource> itrRanges = op.listRange();
			while (itrRanges.hasNext()) {
				OntResource range = itrRanges.next();
				getMembers(range, ranges, false);
			}
			
			for (int i = 0; i < ranges.size(); i++) {
				r = ranges.get(i);
				if (rangeClassUri.equalsIgnoreCase(r.getNameSpace() + r.getLocalName())) {
					
					List<OntResource> thisPropertyDomains = new ArrayList<OntResource>();
					ExtendedIterator<? extends OntResource> itrDomains = op.listDomain();
					while (itrDomains.hasNext()) {
						domain = itrDomains.next();
						getMembers(domain, thisPropertyDomains, false);
					}
					
					for (int j = 0; j < thisPropertyDomains.size(); j++) {
						domain = thisPropertyDomains.get(j);
						domains.add(domain);
						getChildren(domain, domains, recursive);
					}
					
					break;
				}
			}
		}
		
		return getResourcesURIs(domains);
	}
	
	/**
	 * This function takes a class and a data property and says if the class is in domain of that data property or not.
	 * If @param includeinheritance is true, it also returns the data properties inherited from parents, for example, 
	 * if A is superclass of B, and we have a datatype property P from A to xsd:int, then calling this function with 
	 * (B, P, false) returns nothing, but calling with (B, P, true) returns the property P.
	 * @param domainClassUri
	 * @param propertyUri
	 * @param recursive
	 * @return
	 */
	public List<String> getDataProperties(String domainClassUri, String propertyUri, boolean includeInheritance) {
		
		List<OntResource> properties = new ArrayList<OntResource>();
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		
		ExtendedIterator<DatatypeProperty> itrDP = ontModel.listDatatypeProperties();
		OntResource r;
		while (itrDP.hasNext()) {
			
			directDomains.clear();
			allDomains.clear();
			
			DatatypeProperty dp = itrDP.next();
			
			if (!propertyUri.equalsIgnoreCase(dp.getNameSpace() + dp.getLocalName()))
				continue;
			
			ExtendedIterator<? extends OntResource> itrDomains = dp.listDomain();
			while (itrDomains.hasNext()) {
				OntResource d = itrDomains.next();
				getMembers(d, directDomains, false);
			}

			for (int i = 0; i < directDomains.size(); i++) {
				allDomains.add(directDomains.get(i));
				if (includeInheritance)
					getChildren(directDomains.get(i), allDomains, true);
			}
			
			for (int i = 0; i < allDomains.size(); i++) {
				r = allDomains.get(i);
				if (domainClassUri.equalsIgnoreCase(r.getNameSpace() + r.getLocalName())) {
					properties.add(dp);
					break;
				}
			}
		}
		
		return getResourcesURIs(properties);
	}
	
	/**
	 * This method extracts all the object properties between two classes (object properties 
	 * who have @param domainClassUri in their domain and @param rangeClassUri in their range).
	 * If @param includeinheritance is true, it also returns the object properties inherited from parents.
	 * @param domainClassUri
	 * @param rangeClassUri
	 * @param recursive
	 * @return
	 */
	public List<String> getObjectProperties(String domainClassUri, String rangeClassUri, boolean includeInheritance) {
		
		List<OntResource> properties = new ArrayList<OntResource>();
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		
		ExtendedIterator<ObjectProperty> itrOP = ontModel.listObjectProperties();
		OntResource d;
		OntResource r;
		
		while (itrOP.hasNext()) {
			
			directDomains.clear();
			allDomains.clear();
			directRanges.clear();
			allRanges.clear();
			
			ObjectProperty op = itrOP.next();

			// getting domains and subclasses
			ExtendedIterator<? extends OntResource> itrDomains = op.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				getMembers(d, directDomains, false);
			}

			for (int i = 0; i < directDomains.size(); i++) {
				allDomains.add(directDomains.get(i));
				if (includeInheritance)
					getChildren(directDomains.get(i), allDomains, true);
			}
			
			// getting ranges and subclasses
			ExtendedIterator<? extends OntResource> itrRanges = op.listRange();
			while (itrRanges.hasNext()) {
				r = itrRanges.next();
				getMembers(r, directRanges, false);
			}

			for (int i = 0; i < directRanges.size(); i++) {
				allRanges.add(directRanges.get(i));
				if (includeInheritance)
					getChildren(directRanges.get(i), allRanges, true);
			}
			
			boolean found = false;
			for (int i = 0; i < allDomains.size(); i++) {
				d = allDomains.get(i);
				if (domainClassUri.equalsIgnoreCase(d.getNameSpace() + d.getLocalName())) {
					for (int j = 0; j < allRanges.size(); j++) {
						r = allRanges.get(j);
						if (rangeClassUri.equalsIgnoreCase(r.getNameSpace() + r.getLocalName())) {
							properties.add(op);
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
			}
		}
		
		return getResourcesURIs(properties);
	}
	
	/**
	 * This function takes a class uri and returns the datatype properties who have this class in their domain. 
	 * If second parameter set to True, it also returns the datatype properties inherited from parents of the given class.
	 * @param domainClassUri
	 * @param includeInheritedProperties
	 * @return
	 */
	public List<String> getDataPropertiesOfClass(String domainClassUri, boolean includeInheritedProperties) {
		
		List<OntResource> properties = new ArrayList<OntResource>();
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();

		ExtendedIterator<DatatypeProperty> itrDP = ontModel.listDatatypeProperties();
		OntResource r;
		while (itrDP.hasNext()) {
			
			directDomains.clear();
			allDomains.clear();
			
			DatatypeProperty dp = itrDP.next();
			
			ExtendedIterator<? extends OntResource> itrDomains = dp.listDomain();
			while (itrDomains.hasNext()) {
				OntResource d = itrDomains.next();
				getMembers(d, directDomains, false);
			}

			for (int i = 0; i < directDomains.size(); i++) {
				allDomains.add(directDomains.get(i));
				if (includeInheritedProperties)
					getChildren(directDomains.get(i), allDomains, true);
			}
			
			for (int i = 0; i < allDomains.size(); i++) {
				r = allDomains.get(i);
				if (domainClassUri.equalsIgnoreCase(r.getNameSpace() + r.getLocalName())) {
					properties.add(dp);
					break;
				}
			}
		}
		
		return getResourcesURIs(properties);
	}

	/**
	 * This function takes a class uri and returns the object properties who have this class in their domain. 
	 * If second parameter set to True, it also returns the object properties inherited from parents of the given class.
	 * @param domainClassUri
	 * @param includeInheritedProperties
	 * @return
	 */
	public List<String> getObjectPropertiesOfClass(String domainClassUri, boolean includeInheritedProperties) {
		
		List<OntResource> properties = new ArrayList<OntResource>();
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();

		ExtendedIterator<ObjectProperty> itrOP = ontModel.listObjectProperties();
		OntResource r;
		while (itrOP.hasNext()) {
			
			directDomains.clear();
			allDomains.clear();
			
			ObjectProperty op = itrOP.next();
			
			ExtendedIterator<? extends OntResource> itrDomains = op.listDomain();
			while (itrDomains.hasNext()) {
				OntResource d = itrDomains.next();
				getMembers(d, directDomains, false);
			}

			for (int i = 0; i < directDomains.size(); i++) {
				allDomains.add(directDomains.get(i));
				if (includeInheritedProperties)
					getChildren(directDomains.get(i), allDomains, true);
			}
			
			for (int i = 0; i < allDomains.size(); i++) {
				r = allDomains.get(i);
				if (domainClassUri.equalsIgnoreCase(r.getNameSpace() + r.getLocalName())) {
					properties.add(op);
					break;
				}
			}
		}
		
		return getResourcesURIs(properties);
	}

}
