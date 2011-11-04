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
                if (superP.isURIResource())
                	resources.add(superP);
                else {
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
                if (subC.isURIResource())
                	resources.add(subC);
                else {
            		List<OntResource> members = new ArrayList<OntResource>();
                	getMembers(subC, members, false);
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
	
	public boolean isSuperClass(String superClassUri, String subClassUri, boolean recursive) {
		
		List<String> superClasses = getSuperClasses(subClassUri, recursive);
		
		for (int i = 0; i < superClasses.size(); i++) {
			if (superClassUri.equalsIgnoreCase(superClasses.get(i).toString() )) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isSubClass(String subClassUri, String superClassUri, boolean recursive) {
		
		List<String> subClasses = getSubClasses(superClassUri, recursive);
		
		for (int i = 0; i < subClasses.size(); i++) {
			if (subClassUri.equalsIgnoreCase(subClasses.get(i).toString() )) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getSubClasses(String classUri, boolean recursive) {

		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntClass(classUri);
		getChildren(r, resources, recursive);
		return getResourcesURIs(resources);
	}
	
	public List<String> getSuperClasses(String classUri, boolean recursive) {
		
		List<OntResource> resources = new ArrayList<OntResource>();
		OntResource r = ontModel.getOntClass(classUri);
		getParents(r, resources, recursive);
		return getResourcesURIs(resources);
	}
	
	private List<String> getResourcesURIs(List<OntResource> resources) {
		List<String> resourcesNameSet = new ArrayList<String>();
		if (resources != null)
			for (OntResource r: resources) {
				resourcesNameSet.add(r.getURI());
			}
		return resourcesNameSet;
	}
	
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
	
	public List<String> getDataProperties(String domainClassUri, String propertyUri, boolean recursive) {
		
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
	
	public List<String> getObjectProperties(String domainClassUri, String rangeClassUri, boolean recursive) {
		
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
	

}
