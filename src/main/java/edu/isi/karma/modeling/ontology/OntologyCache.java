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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.rep.alignment.Label;

class OntologyCache {
	
	static Logger logger = Logger.getLogger(OntologyCache.class.getName());
	
	private OntologyHandler ontHandler = null;

	private HashMap<String, Label> classes;
	private HashMap<String, Label> properties;
	private HashMap<String, Label> dataProperties;
	private HashMap<String, Label> objectProperties;
	
	private OntologyTreeNode classHierarchy;
	private OntologyTreeNode objectPropertyHierarchy;
	private OntologyTreeNode dataPropertyHierarchy;
	
	// hashmap: class -> properties whose domain(direct) includes this class 
	private HashMap<String, List<String>> directOutDataProperties; 
	private HashMap<String, List<String>> indirectOutDataProperties; 
	private HashMap<String, List<String>> directOutObjectProperties; 
	private HashMap<String, List<String>> indirectOutObjectProperties; 
	// hashmap: class -> properties whose range(direct) includes this class 
	private HashMap<String, List<String>> directInObjectProperties; 
	private HashMap<String, List<String>> indirectInObjectProperties;
	
	// hashmap: property -> direct domains
	private HashMap<String, List<String>> propertyDirectDomains;
	private HashMap<String, List<String>> propertyIndirectDomains;
	// hashmap: property -> direct ranges
	private HashMap<String, List<String>> propertyDirectRanges;
	private HashMap<String, List<String>> propertyIndirectRanges;
	
	// hashmap: domain+range -> object properties
	private HashMap<String, List<String>> directDomainRangeProperties;
	private HashMap<String, List<String>> indirectDomainRangeProperties;

	// hashmap: class -> subclasses
	private HashMap<String, List<String>> directSubClasses;
	private HashMap<String, List<String>> indirectSubClasses;

	// hashmap: class -> subproperties
	private HashMap<String, List<String>> directSubProperties;
	private HashMap<String, List<String>> indirectSubProperties;

	// hashmap: class1 + class2 -> boolean (if c1 is subClassOf c2)
	private HashMap<String, Boolean> directSubClassCheck;
	// hashmap: property1 + property2 -> boolean (if p1 is subPropertyOf p2)
	private HashMap<String, Boolean> directSubPropertyCheck;
	
	private HashMap<String, Label> propertyInverse;
	private HashMap<String, Label> propertyInverseOf;
	
	// hashmap: objectproperty -> <domain, range> pairs
	private HashMap<String, List<DomainRangePair>> objectPropertyDirectDomainRangePairs;
	private HashMap<String, List<DomainRangePair>> objectPropertyIndirectDomainRangePairs;
	private HashMap<String, List<DomainRangePair>> objectPropertyNotDirectDomainRangePairs;
	
	// List <subclass, superclass> pairs
	private List<SubclassSuperclassPair> directSubclassSuperclassPairs;
	private List<SubclassSuperclassPair> indirectSubclassSuperclassPairs;
	
	public OntologyCache(OntologyHandler ontHandler) {
		this.ontHandler = ontHandler;
	}
	
	public HashMap<String, Label> getClasses() {
		return classes;
	}

	public HashMap<String, Label> getProperties() {
		return properties;
	}

	public HashMap<String, Label> getDataProperties() {
		return dataProperties;
	}

	public HashMap<String, Label> getObjectProperties() {
		return objectProperties;
	}

	public OntologyTreeNode getClassHierarchy() {
		return classHierarchy;
	}

	public OntologyTreeNode getObjectPropertyHierarchy() {
		return objectPropertyHierarchy;
	}

	public OntologyTreeNode getDataPropertyHierarchy() {
		return dataPropertyHierarchy;
	}

	public HashMap<String, List<String>> getDirectOutDataProperties() {
		return directOutDataProperties;
	}

	public HashMap<String, List<String>> getIndirectOutDataProperties() {
		return indirectOutDataProperties;
	}

	public HashMap<String, List<String>> getDirectOutObjectProperties() {
		return directOutObjectProperties;
	}

	public HashMap<String, List<String>> getIndirectOutObjectProperties() {
		return indirectOutObjectProperties;
	}

	public HashMap<String, List<String>> getDirectInObjectProperties() {
		return directInObjectProperties;
	}

	public HashMap<String, List<String>> getIndirectInObjectProperties() {
		return indirectInObjectProperties;
	}

	public HashMap<String, List<String>> getPropertyDirectDomains() {
		return propertyDirectDomains;
	}

	public HashMap<String, List<String>> getPropertyIndirectDomains() {
		return propertyIndirectDomains;
	}

	public HashMap<String, List<String>> getPropertyDirectRanges() {
		return propertyDirectRanges;
	}

	public HashMap<String, List<String>> getPropertyIndirectRanges() {
		return propertyIndirectRanges;
	}
	
	public HashMap<String, List<String>> getDirectDomainRangeProperties() {
		return directDomainRangeProperties;
	}

	public HashMap<String, List<String>> getIndirectDomainRangeProperties() {
		return indirectDomainRangeProperties;
	}
	
	public HashMap<String, List<String>> getDirectSubClasses() {
		return directSubClasses;
	}

	public HashMap<String, List<String>> getIndirectSubClasses() {
		return indirectSubClasses;
	}

	public HashMap<String, List<String>> getDirectSubProperties() {
		return directSubProperties;
	}

	public HashMap<String, List<String>> getIndirectSubProperties() {
		return indirectSubProperties;
	}

	public HashMap<String, Boolean> getDirectSubClassCheck() {
		return directSubClassCheck;
	}
	
	public HashMap<String, Boolean> getDirectSubPropertyCheck() {
		return directSubPropertyCheck;
	}

	public HashMap<String, Label> getPropertyInverse() {
		return propertyInverse;
	}

	public HashMap<String, Label> getPropertyInverseOf() {
		return propertyInverseOf;
	}

	public HashMap<String, List<DomainRangePair>> getObjectPropertyDirectDomainRangePairs() {
		return objectPropertyDirectDomainRangePairs;
	}

	public HashMap<String, List<DomainRangePair>> getObjectPropertyIndirectDomainRangePairs() {
		return objectPropertyIndirectDomainRangePairs;
	}

	public HashMap<String, List<DomainRangePair>> getObjectPropertyNotDirectDomainRangePairs() {
		return objectPropertyNotDirectDomainRangePairs;
	}

	public List<SubclassSuperclassPair> getDirectSubclassSuperclassPairs() {
		return directSubclassSuperclassPairs;
	}

	public List<SubclassSuperclassPair> getIndirectSubclassSuperclassPairs() {
		return indirectSubclassSuperclassPairs;
	}

	public void init() {

		logger.info("start building the ontology cache ...");
		
		this.classes = new HashMap<String, Label>();
		this.properties = new HashMap<String, Label>();
		this.dataProperties = new HashMap<String, Label>();
		this.objectProperties = new HashMap<String, Label>();
		
		this.classHierarchy = new OntologyTreeNode(new Label("Classes"), null, null);
		this.dataPropertyHierarchy = new OntologyTreeNode(new Label("Data Properties"), null, null);
		this.objectPropertyHierarchy = new OntologyTreeNode(new Label("Object Properties"), null, null);

		this.directSubClasses = new HashMap<String, List<String>>();
		this.indirectSubClasses = new HashMap<String, List<String>>();
		
		this.directSubProperties = new HashMap<String, List<String>>();
		this.indirectSubProperties = new HashMap<String, List<String>>();
		
		this.directSubClassCheck = new HashMap<String, Boolean>();
		this.directSubPropertyCheck = new HashMap<String, Boolean>();
		
		this.directOutDataProperties = new HashMap<String, List<String>>();
		this.indirectOutDataProperties = new HashMap<String, List<String>>();
		this.directOutObjectProperties = new HashMap<String, List<String>>();
		this.indirectOutObjectProperties = new HashMap<String, List<String>>();
		this.directInObjectProperties = new HashMap<String, List<String>>();
		this.indirectInObjectProperties = new HashMap<String, List<String>>();
		
		this.propertyDirectDomains = new HashMap<String, List<String>>();
		this.propertyIndirectDomains = new HashMap<String, List<String>>();
		this.propertyDirectRanges = new HashMap<String, List<String>>();
		this.propertyIndirectRanges = new HashMap<String, List<String>>();
		
		this.directDomainRangeProperties = new HashMap<String, List<String>>();
		this.indirectDomainRangeProperties = new HashMap<String, List<String>>();
		
		this.propertyInverse = new HashMap<String, Label>();
		this.propertyInverseOf = new HashMap<String, Label>();
		
		this.objectPropertyDirectDomainRangePairs = new HashMap<String, List<DomainRangePair>>();
		this.objectPropertyIndirectDomainRangePairs = new HashMap<String, List<DomainRangePair>>();
		this.objectPropertyNotDirectDomainRangePairs = new HashMap<String, List<DomainRangePair>>();
		this.directSubclassSuperclassPairs = new ArrayList<SubclassSuperclassPair>();
		this.indirectSubclassSuperclassPairs = new ArrayList<SubclassSuperclassPair>();
		
		long start = System.currentTimeMillis();
		
		// create a list of classes and properties of the model
		this.loadClasses();
		this.loadProperties();
		
		logger.info("number of classes:" + classes.size());
		logger.info("number of all properties:" + properties.size());
		// A = number of all properties including rdf:Property 
		// B = number of properties defined as Data Property
		// C = number of properties defined as Object Property
		// properties = A
		// dataproperties = A - C
		// objectproperties = A - B
		logger.info("number of data properties:" + (properties.size() - objectProperties.size()) );
		logger.info("number of object properties:" + (properties.size() - dataProperties.size()) );

		// create a hierarchy of classes and properties of the model
		this.buildClassHierarchy(classHierarchy);
		this.buildDataPropertyHierarchy(dataPropertyHierarchy);
		this.buildObjectPropertyHierarchy(objectPropertyHierarchy);
		
		// build some hashmaps that will be used in alignment
		this.buildDataPropertiesHashMaps();
		this.buildObjectPropertiesHashMaps();
		
		// build hashmaps for indirect subclass and subproperty relationships
		this.buildIndirectSubClassesHashMap();
		this.buildIndirectSubPropertiesHashMap();
		
		// build hashmaps to include inverse(Of) properties
		this.buildInverseProperties();
		
		// build hashmaps to speed up adding links to the graph
		this.buildObjectPropertyDomainRangeMap();
		this.buildSubclassSuperclassPairs();
		
		// update hashmaps to include the subproperty relations  
		this.updateMapsWithSubpropertyDefinitions(true);
		
		// add some common properties like rdfs:label, rdfs:comment, ...
		this.addPropertiesOfRDFVocabulary();
		
//		classHierarchy.print();
//		dataPropertyHierarchy.print();
//		objectPropertyHierarchy.print();
		
		float elapsedTimeSec = (System.currentTimeMillis() - start)/1000F;
		logger.info("time to build the ontology cache: " + elapsedTimeSec);
	}

	private void loadClasses() {
		
		ExtendedIterator<OntClass> itrC = ontHandler.getOntModel().listNamedClasses();
		
		while (itrC.hasNext()) {
			
			OntClass c = itrC.next();
			
			if (!c.isURIResource())
				continue;
			
			if (!classes.containsKey(c.getURI()))
				classes.put(c.getURI(), ontHandler.getResourceLabel(c));

		}

//		List<OntClass> namedRoots = OntTools.namedHierarchyRoots(ontController.getOntModel());
//		for (OntClass c : namedRoots) {
//			if (c.isURIResource() && rootClasses.indexOf(c.getURI()) == -1)
//				rootClasses.add(c.getURI());
//		}
	}

	private void loadProperties() {
		
		ExtendedIterator<OntProperty> itrP = ontHandler.getOntModel().listAllOntProperties();
		
		while (itrP.hasNext()) {
			
			OntProperty p = itrP.next();
			
			if (!p.isURIResource())
				continue;
			
			if (!properties.containsKey(p.getURI()))
				properties.put(p.getURI(), ontHandler.getResourceLabel(p));
			
			if (p.isDatatypeProperty() || !p.isObjectProperty())
			{
				if (!dataProperties.containsKey(p.getURI()))
					dataProperties.put(p.getURI(), ontHandler.getResourceLabel(p));
			}

			if (p.isObjectProperty() || !p.isDatatypeProperty())
			{
				if (!objectProperties.containsKey(p.getURI()))
					objectProperties.put(p.getURI(), ontHandler.getResourceLabel(p));
			}
		}
	}
	
	private void buildClassHierarchy(OntologyTreeNode node) {
		
		List<OntologyTreeNode> children = new ArrayList<OntologyTreeNode>();
		if (node.getParent() == null) {
//			for (String s : rootClasses) {
			for (String s : this.classes.keySet()) {
				Set<String> superClasses = this.ontHandler.getSuperClasses(s, false).keySet();
				if (superClasses == null || superClasses.size() == 0) {
					Label label = this.classes.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					buildClassHierarchy(childNode);
					children.add(childNode);
				}
			}
		} else {
			HashMap<String, Label> subClasses = 
					this.ontHandler.getSubClasses(node.getUri().getUri(), false);
			this.directSubClasses.put(node.getUri().getUri(), new ArrayList<String>(subClasses.keySet()));
			for (String s : subClasses.keySet()) {
				Label label = subClasses.get(s);
				OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
				
				// update direct subClass map
				this.directSubClassCheck.put(childNode.getUri().getUri() + node.getUri().getUri(), true);
				
				buildClassHierarchy(childNode);
				children.add(childNode);
			}
		}
		node.setChildren(children);
	}
	
	private void buildDataPropertyHierarchy(OntologyTreeNode node) {
		
		List<OntologyTreeNode> children = new ArrayList<OntologyTreeNode>();
		if (node.getParent() == null) {
			for (String s : this.dataProperties.keySet()) {
				Set<String> superProperties = this.ontHandler.getSuperProperties(s, false).keySet();
				if (superProperties == null || superProperties.size() == 0) {
					Label label = this.dataProperties.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					buildDataPropertyHierarchy(childNode);
					children.add(childNode);
				}
			}
		} else {
			HashMap<String, Label> subProperties = 
					this.ontHandler.getSubProperties(node.getUri().getUri(), false);
			this.directSubProperties.put(node.getUri().getUri(), new ArrayList<String>(subProperties.keySet()));
			if (subProperties != null)
				for (String s : subProperties.keySet()) {
					Label label = subProperties.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					
					// update direct subProperty map
					this.directSubPropertyCheck.put(childNode.getUri().getUri() + node.getUri().getUri(), true);
					
					buildDataPropertyHierarchy(childNode);
					children.add(childNode);
				}
		}
		node.setChildren(children);	
	}
	
	private void buildObjectPropertyHierarchy(OntologyTreeNode node) {
		
		List<OntologyTreeNode> children = new ArrayList<OntologyTreeNode>();
		if (node.getParent() == null) {
			for (String s : this.objectProperties.keySet()) {
				Set<String> superProperties = this.ontHandler.getSuperProperties(s, false).keySet();
				if (superProperties == null || superProperties.size() == 0) {
					Label label = this.objectProperties.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					buildObjectPropertyHierarchy(childNode);
					children.add(childNode);
				}
			}
		} else {
			HashMap<String, Label> subProperties = 
					this.ontHandler.getSubProperties(node.getUri().getUri(), false);
			this.directSubProperties.put(node.getUri().getUri(), new ArrayList<String>(subProperties.keySet()));
			if (subProperties != null)
				for (String s : subProperties.keySet()) {
					Label label = subProperties.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					
					// update direct subProperty map
					this.directSubPropertyCheck.put(childNode.getUri().getUri() + node.getUri().getUri(), true);
					
					buildObjectPropertyHierarchy(childNode);
					children.add(childNode);
				}
		}
		node.setChildren(children);	
	}

	private void buildIndirectSubPropertiesHashMap() {

		for (String property : this.properties.keySet()) {
			List<String> subProperties = 
					new ArrayList<String>(this.ontHandler.getSubProperties(property, true).keySet());
			this.indirectSubProperties.put(property, subProperties);
		}
	}

	private void buildIndirectSubClassesHashMap() {
		
		for (String c : this.classes.keySet()) {
			List<String> subClasses = 
					new ArrayList<String>(this.ontHandler.getSubClasses(c, true).keySet());
			this.indirectSubClasses.put(c, subClasses);
		}

	}

	private void buildInverseProperties() {
		for (String op : this.objectProperties.keySet()) {
			this.propertyInverse.put(op, this.ontHandler.getInverseProperty(op));
			this.propertyInverseOf.put(op, this.ontHandler.getInverseOfProperty(op));
		}
	}
	
	private void buildObjectPropertyDomainRangeMap() {
		
		for (String op : this.objectProperties.keySet()) {
			
			List<DomainRangePair> directDomainRangePairs = new ArrayList<DomainRangePair>();
			List<DomainRangePair> indirectDomainRangePairs = new ArrayList<DomainRangePair>();
			List<DomainRangePair> notDirectDomainRangePairs = new ArrayList<DomainRangePair>();
			
			this.objectPropertyDirectDomainRangePairs.put(op, directDomainRangePairs);
			this.objectPropertyIndirectDomainRangePairs.put(op, indirectDomainRangePairs);
			this.objectPropertyNotDirectDomainRangePairs.put(op, notDirectDomainRangePairs);
			
			List<String> directDomains = this.propertyDirectDomains.get(op);
			List<String> directRanges = this.propertyDirectRanges.get(op);

			List<String> indirectDomains = this.propertyIndirectDomains.get(op);
			List<String> indirectRanges = this.propertyIndirectRanges.get(op);

			// direct
			if (directDomains != null && directRanges != null) { 
				for (String d : directDomains)
					for (String r : directRanges)
						directDomainRangePairs.add(new DomainRangePair(d, r));
			}
			
			// indirect
			if (indirectDomains != null && indirectRanges != null) { 
				for (String d : indirectDomains)
					for (String r : indirectRanges)
						indirectDomainRangePairs.add(new DomainRangePair(d, r));
			}
			
			// not direct
			if (indirectDomains != null && indirectRanges != null) { 
				for (String d : indirectDomains) {
					for (String r : indirectRanges) {
						if (directDomains.contains(d) && directRanges.contains(r)) continue;
						notDirectDomainRangePairs.add(new DomainRangePair(d, r));
					}
				}
			}
		}
	}
	
	private void buildSubclassSuperclassPairs() {
		
		for (String superclass : this.directSubClasses.keySet()) {
			List<String> subClasses = this.directSubClasses.get(superclass);
			for (String subclass : subClasses)
				this.directSubclassSuperclassPairs.add(new SubclassSuperclassPair(subclass, superclass));
		}
		
		for (String superclass : this.indirectSubClasses.keySet()) {
			List<String> subClasses = this.indirectSubClasses.get(superclass);
			for (String subclass : subClasses)
				this.indirectSubclassSuperclassPairs.add(new SubclassSuperclassPair(subclass, superclass));
		}
	}
	
	private void buildDataPropertiesHashMaps() {
		
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		List<String> temp; 
		
		ExtendedIterator<OntProperty> itrDP = ontHandler.getOntModel().listAllOntProperties();
//		ExtendedIterator<DatatypeProperty> itrDP = ontController.getOntModel().listDatatypeProperties();
		OntResource d;
		OntResource r;
		
		while (itrDP.hasNext()) {
			
			directDomains.clear();
			allDomains.clear();
			directRanges.clear();
			allRanges.clear();

//			DatatypeProperty dp = itrDP.next();
			
			OntProperty dp = itrDP.next();
			if (dp.isObjectProperty() && !dp.isDatatypeProperty())
				continue;
			
			if (!dp.isURIResource())
				continue;
//			System.out.println("DP:" + dp.getURI());

			// getting domains and subclasses
			ExtendedIterator<? extends OntResource> itrDomains = dp.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontHandler.getMembers(d, directDomains, false);
			}

			temp  = propertyDirectDomains.get(dp.getURI());
			if (temp == null)
				propertyDirectDomains.put(dp.getURI(), ontHandler.getResourcesUris(directDomains));
			else 
				temp.addAll(ontHandler.getResourcesUris(directDomains));
			
			for (int i = 0; i < directDomains.size(); i++) {
				temp = directOutDataProperties.get(directDomains.get(i).getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					directOutDataProperties.put(directDomains.get(i).getURI(), temp);
				}
				temp.add(dp.getURI());
			}

			for (int i = 0; i < directDomains.size(); i++) {
				allDomains.add(directDomains.get(i));
				ontHandler.getChildren(directDomains.get(i), allDomains, true);
			}

			temp  = propertyIndirectDomains.get(dp.getURI());
			if (temp == null)
				propertyIndirectDomains.put(dp.getURI(), ontHandler.getResourcesUris(allDomains));
			else 
				temp.addAll(ontHandler.getResourcesUris(allDomains));
			
			for (int i = 0; i < allDomains.size(); i++) {
				temp = indirectOutDataProperties.get(allDomains.get(i).getURI());
				if (temp == null) { 
					temp = new ArrayList<String>();
					indirectOutDataProperties.put(allDomains.get(i).getURI(), temp);
				}
				temp.add(dp.getURI());
			}
			
			// getting ranges and subclasses
			ExtendedIterator<? extends OntResource> itrRanges = dp.listRange();
			while (itrRanges.hasNext()) {
				r = itrRanges.next();
				ontHandler.getMembers(r, directRanges, false);
			}

			temp  = propertyDirectRanges.get(dp.getURI());
			if (temp == null)
				propertyDirectRanges.put(dp.getURI(), ontHandler.getResourcesUris(directRanges));
			else 
				temp.addAll(ontHandler.getResourcesUris(directRanges));
			
			for (int i = 0; i < directRanges.size(); i++) {
				allRanges.add(directRanges.get(i));
				ontHandler.getChildren(directRanges.get(i), allRanges, true);
			}
			
			temp  = propertyIndirectRanges.get(dp.getURI());
			if (temp == null)
				propertyIndirectRanges.put(dp.getURI(), ontHandler.getResourcesUris(allRanges));
			else 
				temp.addAll(ontHandler.getResourcesUris(allRanges));
			
		}		
	}
	
	private void buildObjectPropertiesHashMaps() {
		
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		List<String> temp; 
		
		ExtendedIterator<OntProperty> itrOP = ontHandler.getOntModel().listAllOntProperties();
//		ExtendedIterator<ObjectProperty> itrOP = ontController.getOntModel().listObjectProperties();
		OntResource d;
		OntResource r;
		
		while (itrOP.hasNext()) {
			
			directDomains.clear();
			allDomains.clear();
			directRanges.clear();
			allRanges.clear();
			
//			ObjectProperty op = itrOP.next();
			
			OntProperty op = itrOP.next();
			if (op.isDatatypeProperty() && !op.isObjectProperty())
				continue;
			
			if (!op.isURIResource())
				continue;
			
//			System.out.println("OP:" + op.getURI());
			
			// getting domains and subclasses
			ExtendedIterator<? extends OntResource> itrDomains = op.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontHandler.getMembers(d, directDomains, false);
			}

			temp  = propertyDirectDomains.get(op.getURI());
			if (temp == null)
				propertyDirectDomains.put(op.getURI(), ontHandler.getResourcesUris(directDomains));
			else 
				temp.addAll(ontHandler.getResourcesUris(directDomains));
			
			for (int i = 0; i < directDomains.size(); i++) {
				temp = directOutObjectProperties.get(directDomains.get(i).getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					directOutObjectProperties.put(directDomains.get(i).getURI(), temp);
				}
				temp.add(op.getURI());
			}

			for (int i = 0; i < directDomains.size(); i++) {
				allDomains.add(directDomains.get(i));
				ontHandler.getChildren(directDomains.get(i), allDomains, true);
			}

			temp  = propertyIndirectDomains.get(op.getURI());
			if (temp == null)
				propertyIndirectDomains.put(op.getURI(), ontHandler.getResourcesUris(allDomains));
			else 
				temp.addAll(ontHandler.getResourcesUris(allDomains));
			
			for (int i = 0; i < allDomains.size(); i++) {
				temp = indirectOutObjectProperties.get(allDomains.get(i).getURI());
				if (temp == null) { 
					temp = new ArrayList<String>();
					indirectOutObjectProperties.put(allDomains.get(i).getURI(), temp);
				}
				temp.add(op.getURI());
			}
			
			// getting ranges and subclasses
			ExtendedIterator<? extends OntResource> itrRanges = op.listRange();
			while (itrRanges.hasNext()) {
				r = itrRanges.next();
				ontHandler.getMembers(r, directRanges, false);
			}

			temp  = propertyDirectRanges.get(op.getURI());
			if (temp == null)
				propertyDirectRanges.put(op.getURI(), ontHandler.getResourcesUris(directRanges));
			else 
				temp.addAll(ontHandler.getResourcesUris(directRanges));
			
			for (int i = 0; i < directRanges.size(); i++) {
				temp = directInObjectProperties.get(directRanges.get(i).getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					directInObjectProperties.put(directRanges.get(i).getURI(), temp);
				}
				temp.add(op.getURI());
			}
			
			for (int i = 0; i < directRanges.size(); i++) {
				allRanges.add(directRanges.get(i));
				ontHandler.getChildren(directRanges.get(i), allRanges, true);
			}
			
			temp  = propertyIndirectRanges.get(op.getURI());
			if (temp == null)
				propertyIndirectRanges.put(op.getURI(), ontHandler.getResourcesUris(allRanges));
			else 
				temp.addAll(ontHandler.getResourcesUris(allRanges));
			
			for (int i = 0; i < allRanges.size(); i++) {
				temp = indirectInObjectProperties.get(allRanges.get(i).getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					indirectInObjectProperties.put(allRanges.get(i).getURI(), temp);
				}
				temp.add(op.getURI());
			}
			
			for (int i = 0; i < directDomains.size(); i++) {
				for (int j = 0; j < directRanges.size(); j++) {
					temp = 
						directDomainRangeProperties.get(directDomains.get(i).toString() + directRanges.get(j).toString());
					if (temp == null) {
						temp = new ArrayList<String>();
						directDomainRangeProperties.put(directDomains.get(i).toString() + directRanges.get(j).toString(), temp);
					}
					if (temp.indexOf(op.getURI()) == -1)
						temp.add(op.getURI());
				}
			}
			
			for (int i = 0; i < allDomains.size(); i++) {
				for (int j = 0; j < allRanges.size(); j++) {
					temp = 
						indirectDomainRangeProperties.get(allDomains.get(i).toString() + allRanges.get(j).toString());
					if (temp == null) {
						temp = new ArrayList<String>();
						indirectDomainRangeProperties.put(allDomains.get(i).toString() + allRanges.get(j).toString(), temp);
					}
					if (temp.indexOf(op.getURI()) == -1)
						temp.add(op.getURI());
				}
			}

		}	

	}
	
	private void addPropertiesOfRDFVocabulary() {
		String labelUri = "http://www.w3.org/2000/01/rdf-schema#label";
		String commentUri = "http://www.w3.org/2000/01/rdf-schema#comment";
		String valueUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
		List<String> temp;
		
		ontHandler.getOntModel().setNsPrefix("rdfs", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		ontHandler.getOntModel().setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		ontHandler.getOntModel().createDatatypeProperty(labelUri);
		ontHandler.getOntModel().createDatatypeProperty(commentUri);
		ontHandler.getOntModel().createDatatypeProperty(valueUri);

		// collect all the existing classes
		List<String> classes = new ArrayList<String>();
		for (String s : indirectOutDataProperties.keySet()) 
			if (classes.indexOf(s) == -1) classes.add(s);
		for (String s : indirectOutObjectProperties.keySet()) 
			if (classes.indexOf(s) == -1) classes.add(s);
		for (String s : indirectInObjectProperties.keySet()) 
			if (classes.indexOf(s) == -1) classes.add(s);
		
		// add label and comment property to the properties of all the classes
		for (String s : classes) {
			temp = indirectOutDataProperties.get(s);
			if (temp == null) {
				temp = new ArrayList<String>();
				indirectOutDataProperties.put(s, temp);
			}
			temp.add(labelUri);
			temp.add(commentUri);
			temp.add(valueUri);
		}

		// add label to properties hashmap
		temp = propertyIndirectDomains.get(labelUri);
		if (temp == null) {
			temp = new ArrayList<String>();
			propertyIndirectDomains.put(labelUri, temp);
		}
		for (String s : classes)
			if (temp.indexOf(s) == -1)
				temp.add(s);
		
		// add comment to properties hashmap
		temp = propertyIndirectDomains.get(commentUri);
		if (temp == null) {
			temp = new ArrayList<String>();
			propertyIndirectDomains.put(commentUri, temp);
		}
		for (String s : classes)
			if (temp.indexOf(s) == -1)
				temp.add(s);

		// add value to properties hashmap
		temp = propertyIndirectDomains.get(valueUri);
		if (temp == null) {
			temp = new ArrayList<String>();
			propertyIndirectDomains.put(valueUri, temp);
		}
		for (String s : classes)
			if (temp.indexOf(s) == -1)
				temp.add(s);
	}
	
	/**
	 * If the inheritance is true, it adds all the sub-classes of the domain and range of super-properties too.
	 * @param inheritance
	 */
	private void updateMapsWithSubpropertyDefinitions(boolean inheritance) {
		
		
		// iterate over all properties
		for (String p : propertyDirectDomains.keySet()) {
			
//			logger.debug("*********************************" + p);
			Set<String> superProperties = ontHandler.getSuperProperties(p, true).keySet();
			
//			System.out.println("*****************" + p);
//			for (String s : superProperties)
//				System.out.println(">>>>>>>>>>>>>>>>>>" + s);
			List<String> temp = null;
			
			List<String> allDomains = new ArrayList<String>();
			for (String superP : superProperties) {
				List<String> domains = null;
				if (inheritance)
					domains = propertyIndirectDomains.get(superP);
				else
					domains = propertyDirectDomains.get(superP);
				if (domains == null) continue;
				allDomains.addAll(domains);
			}

			List<String> indirectDomains = propertyIndirectDomains.get(p);
			if (indirectDomains == null) {
				indirectDomains = new ArrayList<String>();
				propertyIndirectDomains.put(p, indirectDomains);
			}
			
			for (String d : allDomains) {
				if (indirectDomains.indexOf(d) == -1)
					indirectDomains.add(d);
			}
			
			if (ontHandler.isObjectProperty(p)) 
				for (String d : allDomains) {
					temp = indirectOutObjectProperties.get(d);
					if (temp != null && temp.indexOf(p) == -1)
						temp.add(p);
				}

			if (ontHandler.isDataProperty(p)) 
				for (String d : allDomains) {
					temp = indirectOutDataProperties.get(d);
					if (temp != null && temp.indexOf(p) == -1)
						temp.add(p);
				}

			List<String> allRanges = new ArrayList<String>();
			for (String superP : superProperties) {
				List<String> ranges = null;
				if (inheritance)
					ranges = propertyIndirectRanges.get(superP);
				else
					ranges = propertyDirectRanges.get(superP);
				if (ranges == null) continue;
				allRanges.addAll(ranges);
			}

			List<String> indirectRanges = propertyIndirectRanges.get(p);
			if (indirectRanges == null) {  
				indirectRanges = new ArrayList<String>();
				propertyIndirectRanges.put(p, indirectRanges);
			}
			
			for (String r : allRanges) {
				if (indirectRanges.indexOf(r) == -1)
					indirectRanges.add(r);
			}
			
			if (ontHandler.isObjectProperty(p)) 
				for (String r : allRanges) {
					temp = indirectInObjectProperties.get(r);
					if (temp != null && temp.indexOf(p) == -1)
						temp.add(p);
				}
				
			allDomains.addAll(indirectDomains);
			allRanges.addAll(indirectRanges);
			
			for (int i = 0; i < allDomains.size(); i++) {
				for (int j = 0; j < allRanges.size(); j++) {
					temp = 
						indirectDomainRangeProperties.get(allDomains.get(i).toString() + allRanges.get(j).toString());
					if (temp == null) {
						temp = new ArrayList<String>();
						indirectDomainRangeProperties.put(allDomains.get(i).toString() + allRanges.get(j).toString(), temp);
					}
					if (temp.indexOf(p) == -1)
						temp.add(p);
				}
			}
		}

	}

}
