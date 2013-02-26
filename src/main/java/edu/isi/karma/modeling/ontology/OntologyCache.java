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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.alignment.Label;

class OntologyCache {
	
	static Logger logger = Logger.getLogger(OntologyCache.class.getName());
	
	private OntologyHandler ontHandler = null;

	private HashMap<String, Label> classes;
	private HashMap<String, Label> properties;
	private HashMap<String, Label> dataProperties;
	private HashMap<String, Label> objectProperties;

	private HashMap<String, Label> dataPropertiesWithoutDomain;
	private HashMap<String, Label> objectPropertiesWithOnlyDomain;
	private HashMap<String, Label> objectPropertiesWithOnlyRange;
	private HashMap<String, Label> objectPropertiesWithoutDomainAndRange;
	
	private OntologyTreeNode classHierarchy;
	private OntologyTreeNode objectPropertyHierarchy;
	private OntologyTreeNode dataPropertyHierarchy;
	
	// hashmap: class -> subclasses
	private HashMap<String, HashMap<String, Label>> directSubClasses;
	private HashMap<String, HashMap<String, Label>> indirectSubClasses;
	// hashmap: class -> superclasses
	private HashMap<String, HashMap<String, Label>> directSuperClasses;
	private HashMap<String, HashMap<String, Label>> indirectSuperClasses;
	// hashmap: class1 + class2 -> boolean (if c1 is subClassOf c2)
	private HashMap<String, Boolean> directSubClassCheck;
	private HashMap<String, Boolean> indirectSubClassCheck;
	// List <subclass, superclass> pairs
	private List<SubclassSuperclassPair> directSubclassSuperclassPairs;
	private List<SubclassSuperclassPair> indirectSubclassSuperclassPairs;

	// hashmap: class -> subproperties
	private HashMap<String, HashMap<String, Label>> directSubProperties;
	private HashMap<String, HashMap<String, Label>> indirectSubProperties;
	// hashmap: class -> superproperties
	private HashMap<String, HashMap<String, Label>> directSuperProperties;
	private HashMap<String, HashMap<String, Label>> indirectSuperProperties;
	// hashmap: property1 + property2 -> boolean (if p1 is subPropertyOf p2)
	private HashMap<String, Boolean> directSubPropertyCheck;
	private HashMap<String, Boolean> indirectSubPropertyCheck;
	
	// hashmap: property -> property inverse and inverseOf
	private HashMap<String, Label> propertyInverse;
	private HashMap<String, Label> propertyInverseOf;

	// hashmap: property -> direct domains
	private HashMap<String, List<String>> propertyDirectDomains;
	private HashMap<String, List<String>> propertyIndirectDomains;
	// hashmap: property -> direct ranges
	private HashMap<String, List<String>> propertyDirectRanges;
	private HashMap<String, List<String>> propertyIndirectRanges;

	// hashmap: class -> properties whose domain(direct) includes this class 
	private HashMap<String, List<String>> directOutDataProperties; 
	private HashMap<String, List<String>> indirectOutDataProperties; 
	private HashMap<String, List<String>> directOutObjectProperties; 
	private HashMap<String, List<String>> indirectOutObjectProperties; 
	// hashmap: class -> properties whose range(direct) includes this class 
	private HashMap<String, List<String>> directInObjectProperties; 
	private HashMap<String, List<String>> indirectInObjectProperties;
	
	// hashmap: domain+range -> object properties
	private HashMap<String, List<String>> domainRangeToDirectProperties;
	private HashMap<String, List<String>> domainRangeToIndirectProperties;
	private HashMap<String, List<String>> domainRangeToDomainlessProperties;
	private HashMap<String, List<String>> domainRangeToRangelessProperties;

	// hashmap: objectproperty -> <domain, range> pairs
	private HashMap<String, List<DomainRangePair>> domainRangePairsOfDirectProperties;
	private HashMap<String, List<DomainRangePair>> domainRangePairsOfIndirectProperties;
	private HashMap<String, List<DomainRangePair>> domainRangePairsOfDomainlessProperties;
	private HashMap<String, List<DomainRangePair>> domainRangePairsOfRangelessProperties;
	
	// hashmap: class1 + class2 -> boolean (if c1 is connected to c2)
	private HashMap<String, Boolean> connectedByDirectProperties;
	private HashMap<String, Boolean> connectedByIndirectProperties;
	private HashMap<String, Boolean> connectedByDomainlessProperties;
	private HashMap<String, Boolean> connectedByRangelessProperties;

	// public methods
	
	public OntologyCache(OntologyHandler ontHandler) {
		this.ontHandler = ontHandler;
	}

	public void init() {

		logger.info("start building the ontology cache ...");
		
		this.classes = new HashMap<String, Label>();
		this.properties = new HashMap<String, Label>();
		this.dataProperties = new HashMap<String, Label>();
		this.objectProperties = new HashMap<String, Label>();
		
		this.dataPropertiesWithoutDomain = new HashMap<String, Label>();
		this.objectPropertiesWithOnlyDomain = new HashMap<String, Label>();
		this.objectPropertiesWithOnlyRange = new HashMap<String, Label>();
		this.objectPropertiesWithoutDomainAndRange = new HashMap<String, Label>();
		
		this.classHierarchy = new OntologyTreeNode(new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL), null, null);
		this.dataPropertyHierarchy = new OntologyTreeNode(new Label("Data Properties"), null, null);
		this.objectPropertyHierarchy = new OntologyTreeNode(new Label("Object Properties"), null, null);

		this.directSubClasses = new HashMap<String, HashMap<String, Label>>();
		this.indirectSubClasses = new HashMap<String, HashMap<String, Label>>();
		this.directSuperClasses = new HashMap<String, HashMap<String, Label>>();
		this.indirectSuperClasses = new HashMap<String, HashMap<String, Label>>();
		this.directSubClassCheck = new HashMap<String, Boolean>();
		this.indirectSubClassCheck = new HashMap<String, Boolean>();
		this.directSubclassSuperclassPairs = new ArrayList<SubclassSuperclassPair>();
		this.indirectSubclassSuperclassPairs = new ArrayList<SubclassSuperclassPair>();
		
		this.directSubProperties = new HashMap<String, HashMap<String, Label>>();
		this.indirectSubProperties = new HashMap<String, HashMap<String, Label>>();
		this.directSuperProperties = new HashMap<String, HashMap<String, Label>>();
		this.indirectSuperProperties = new HashMap<String, HashMap<String, Label>>();
		this.directSubPropertyCheck = new HashMap<String, Boolean>();
		this.indirectSubPropertyCheck = new HashMap<String, Boolean>();
		
		this.propertyInverse = new HashMap<String, Label>();
		this.propertyInverseOf = new HashMap<String, Label>();
		
		this.propertyDirectDomains = new HashMap<String, List<String>>();
		this.propertyIndirectDomains = new HashMap<String, List<String>>();
		this.propertyDirectRanges = new HashMap<String, List<String>>();
		this.propertyIndirectRanges = new HashMap<String, List<String>>();
		
		this.directOutDataProperties = new HashMap<String, List<String>>();
		this.indirectOutDataProperties = new HashMap<String, List<String>>();
		this.directOutObjectProperties = new HashMap<String, List<String>>();
		this.indirectOutObjectProperties = new HashMap<String, List<String>>();
		this.directInObjectProperties = new HashMap<String, List<String>>();
		this.indirectInObjectProperties = new HashMap<String, List<String>>();
		
		this.domainRangeToDirectProperties = new HashMap<String, List<String>>();
		this.domainRangeToIndirectProperties = new HashMap<String, List<String>>();
		this.domainRangeToDomainlessProperties = new HashMap<String, List<String>>();
		this.domainRangeToRangelessProperties = new HashMap<String, List<String>>();
		
		this.domainRangePairsOfDirectProperties = new HashMap<String, List<DomainRangePair>>();
		this.domainRangePairsOfIndirectProperties = new HashMap<String, List<DomainRangePair>>();
		this.domainRangePairsOfDomainlessProperties = new HashMap<String, List<DomainRangePair>>();
		this.domainRangePairsOfRangelessProperties = new HashMap<String, List<DomainRangePair>>();

		this.connectedByDirectProperties = new HashMap<String, Boolean>();
		this.connectedByIndirectProperties = new HashMap<String, Boolean>();
		this.connectedByDomainlessProperties = new HashMap<String, Boolean>();
		this.connectedByRangelessProperties = new HashMap<String, Boolean>();
		
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
		
		// build hashmaps for indirect subclass and subproperty relationships
		this.buildSubClassesMaps();
		this.buildSuperClassesMaps();
		this.buildSubPropertiesMaps();
		this.buildSuperPropertiesMaps();

		// build hashmaps to include inverse(Of) properties
		this.buildInverseProperties();
		
		// build some hashmaps that will be used in alignment
		this.buildDataPropertiesMaps();
		this.buildObjectPropertiesMaps();
		// update hashmaps to include the subproperty relations  
		this.updateMapsWithSubpropertyDefinitions(true);
		
		// build hashmaps to speed up adding links to the graph
		this.buildObjectPropertyDomainRangeMap();
		
		// add some common properties like rdfs:label, rdfs:comment, ...
		this.addPropertiesOfRDFVocabulary();
		
		// update hashmaps to include ralations with Thing
		this.buildMapsForPropertiesWithoutDomainOrRange();

		// build connectivity hashmaps
		this.buildConnectivityMaps();
		
		float elapsedTimeSec = (System.currentTimeMillis() - start)/1000F;
		logger.info("time to build the ontology cache: " + elapsedTimeSec);
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

	public HashMap<String, Label> getDataPropertiesWithoutDomain() {
		return dataPropertiesWithoutDomain;
	}

	public HashMap<String, Label> getObjectPropertiesWithOnlyDomain() {
		return objectPropertiesWithOnlyDomain;
	}

	public HashMap<String, Label> getObjectPropertiesWithOnlyRange() {
		return objectPropertiesWithOnlyRange;
	}

	public HashMap<String, Label> getObjectPropertiesWithoutDomainAndRange() {
		return objectPropertiesWithoutDomainAndRange;
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

	public HashMap<String, HashMap<String, Label>> getDirectSubClasses() {
		return directSubClasses;
	}

	public HashMap<String, HashMap<String, Label>> getIndirectSubClasses() {
		return indirectSubClasses;
	}

	public HashMap<String, HashMap<String, Label>> getDirectSuperClasses() {
		return directSuperClasses;
	}

	public HashMap<String, HashMap<String, Label>> getIndirectSuperClasses() {
		return indirectSuperClasses;
	}

	public HashMap<String, Boolean> getDirectSubClassCheck() {
		return directSubClassCheck;
	}

	public HashMap<String, Boolean> getIndirectSubClassCheck() {
		return indirectSubClassCheck;
	}
	
	public List<SubclassSuperclassPair> getDirectSubclassSuperclassPairs() {
		return directSubclassSuperclassPairs;
	}

	public List<SubclassSuperclassPair> getIndirectSubclassSuperclassPairs() {
		return indirectSubclassSuperclassPairs;
	}

	public HashMap<String, HashMap<String, Label>> getDirectSubProperties() {
		return directSubProperties;
	}

	public HashMap<String, HashMap<String, Label>> getIndirectSubProperties() {
		return indirectSubProperties;
	}

	public HashMap<String, HashMap<String, Label>> getDirectSuperProperties() {
		return directSuperProperties;
	}

	public HashMap<String, HashMap<String, Label>> getIndirectSuperProperties() {
		return indirectSuperProperties;
	}

	public HashMap<String, Boolean> getDirectSubPropertyCheck() {
		return directSubPropertyCheck;
	}

	public HashMap<String, Boolean> getIndirectSubPropertyCheck() {
		return indirectSubPropertyCheck;
	}

	public HashMap<String, Label> getPropertyInverse() {
		return propertyInverse;
	}

	public HashMap<String, Label> getPropertyInverseOf() {
		return propertyInverseOf;
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

	public HashMap<String, List<String>> getDomainRangeToDirectProperties() {
		return domainRangeToDirectProperties;
	}

	public HashMap<String, List<String>> getDomainRangeToIndirectProperties() {
		return domainRangeToIndirectProperties;
	}

	public HashMap<String, List<String>> getDomainRangeToDomainlessProperties() {
		return domainRangeToDomainlessProperties;
	}

	public HashMap<String, List<String>> getDomainRangeToRangelessProperties() {
		return domainRangeToRangelessProperties;
	}

	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfDirectProperties() {
		return domainRangePairsOfDirectProperties;
	}

	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfIndirectProperties() {
		return domainRangePairsOfIndirectProperties;
	}

	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfDomainlessProperties() {
		return domainRangePairsOfDomainlessProperties;
	}

	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfRangelessProperties() {
		return domainRangePairsOfRangelessProperties;
	}

	public HashMap<String, Boolean> getConnectedByDirectProperties() {
		return connectedByDirectProperties;
	}

	public HashMap<String, Boolean> getConnectedByIndirectProperties() {
		return connectedByIndirectProperties;
	}

	public HashMap<String, Boolean> getConnectedByDomainlessProperties() {
		return connectedByDomainlessProperties;
	}

	public HashMap<String, Boolean> getConnectedByRangelessProperties() {
		return connectedByRangelessProperties;
	}

	public Label getUriLabel(String uri) {
		Label label = this.classes.get(uri);
		if (label == null) label = this.properties.get(uri);
		if (label == null) label = this.ontHandler.getUriLabel(uri);
		return label;
	}
	
	// private methods 
	
	private void loadClasses() {
		
		this.classes.put(Uris.THING_URI, new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL));

		ExtendedIterator<OntClass> itrC = ontHandler.getOntModel().listNamedClasses();
		
		while (itrC.hasNext()) {
			
			OntClass c = itrC.next();
			
			if (!c.isURIResource())
				continue;
			
			if (!classes.containsKey(c.getURI()))
				classes.put(c.getURI(), ontHandler.getResourceLabel(c));

		}
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
					this.ontHandler.getSubClasses(node.getLabel().getUri(), false);

			for (String s : subClasses.keySet()) {
				Label label = subClasses.get(s);
				OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
				
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
					this.ontHandler.getSubProperties(node.getLabel().getUri(), false);

			if (subProperties != null)
				for (String s : subProperties.keySet()) {
					Label label = subProperties.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					
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
					this.ontHandler.getSubProperties(node.getLabel().getUri(), false);

			if (subProperties != null)
				for (String s : subProperties.keySet()) {
					Label label = subProperties.get(s);
					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
					
					buildObjectPropertyHierarchy(childNode);
					children.add(childNode);
				}
		}
		node.setChildren(children);	
	}

	private void buildSubClassesMaps() {
		
		HashMap<String, Label> allClassesExceptThing = new HashMap<String, Label>();
		for (Entry<String, Label> entry : this.classes.entrySet())
			if (!entry.getKey().equalsIgnoreCase(Uris.THING_URI))
				allClassesExceptThing.put(entry.getKey(), entry.getValue());

		HashMap<String, Label> directSubClassesLocal;
		HashMap<String, Label> indirectSubClassesLocal;
		HashMap<String, Label> allSubClassesLocal;
		
		for (String c : this.classes.keySet()) {
			
			directSubClassesLocal = this.ontHandler.getSubClasses(c, false);
			allSubClassesLocal = this.ontHandler.getSubClasses(c, true);
			indirectSubClassesLocal = new HashMap<String, Label>();
			for (Entry<String, Label> entry : allSubClassesLocal.entrySet())
				if (!directSubClassesLocal.containsKey(entry.getKey()))
					indirectSubClassesLocal.put(entry.getKey(), entry.getValue());
			
			// Thing node
			if (c.equalsIgnoreCase(Uris.THING_URI)) {
				List<OntologyTreeNode> thingDirectChildren = this.classHierarchy.getChildren();
				if (thingDirectChildren != null) 
					for (OntologyTreeNode node : thingDirectChildren) 
						if (node.getLabel() != null && node.getLabel().getUri() != null)
							directSubClassesLocal.put(node.getLabel().getUri(), node.getLabel());
				
				indirectSubClassesLocal = allClassesExceptThing;
			}
			
			this.directSubClasses.put(c, directSubClassesLocal);
			this.indirectSubClasses.put(c, indirectSubClassesLocal);

			for (String s : directSubClassesLocal.keySet())
				this.directSubClassCheck.put(s + c, true);
			
			for (String s : indirectSubClassesLocal.keySet())
				this.indirectSubClassCheck.put(s + c, true);
		}
		
		for (String superclass : this.directSubClasses.keySet()) {
			Set<String> subClasses = this.directSubClasses.get(superclass).keySet();
			for (String subclass : subClasses)
				this.directSubclassSuperclassPairs.add(new SubclassSuperclassPair(subclass, superclass));
		}
		
		for (String superclass : this.indirectSubClasses.keySet()) {
			Set<String> subClasses = this.indirectSubClasses.get(superclass).keySet();
			for (String subclass : subClasses)
				this.indirectSubclassSuperclassPairs.add(new SubclassSuperclassPair(subclass, superclass));
		}
	}
	
	private void buildSuperClassesMaps() {
		
		HashMap<String, Label> directSuperClassesLocal;
		HashMap<String, Label> indirectSuperClassesLocal;
		HashMap<String, Label> allSuperClassesLocal;
		
		for (String c : this.classes.keySet()) {
			
			directSuperClassesLocal = this.ontHandler.getSuperClasses(c, false);
			
			this.directSuperClasses.put(c, directSuperClassesLocal);
			
			allSuperClassesLocal = this.ontHandler.getSuperClasses(c, true);
			indirectSuperClassesLocal = new HashMap<String, Label>();
			for (Entry<String, Label> entry : allSuperClassesLocal.entrySet())
				if (!directSuperClassesLocal.containsKey(entry.getKey()))
					indirectSuperClassesLocal.put(entry.getKey(), entry.getValue());
			
			this.indirectSuperClasses.put(c, indirectSuperClassesLocal);
		}		
		
		for (String c : this.indirectSuperClasses.keySet()) {
			
			HashMap<String, Label> superClasses = this.indirectSuperClasses.get(c);
			if (superClasses != null && !superClasses.containsKey(Uris.THING_URI))
				superClasses.put(Uris.THING_URI, new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL));
		}
		
	}
	
	private void buildSubPropertiesMaps() {

		HashMap<String, Label> directSubPropertiesLocal; 
		HashMap<String, Label> indirectSubPropertiesLocal;
		HashMap<String, Label> allSubPropertiesLocal;
		
		for (String p : this.properties.keySet()) { 

			directSubPropertiesLocal = this.ontHandler.getSubProperties(p, false);
			
			allSubPropertiesLocal = this.ontHandler.getSubProperties(p, true);
			indirectSubPropertiesLocal = new HashMap<String, Label>();
			for (Entry<String, Label> entry : allSubPropertiesLocal.entrySet())
				if (!directSubPropertiesLocal.containsKey(entry.getKey()))
					indirectSubPropertiesLocal.put(entry.getKey(), entry.getValue());
			
			this.directSubProperties.put(p, directSubPropertiesLocal);
			this.indirectSubProperties.put(p, indirectSubPropertiesLocal);

			for (String s : directSubPropertiesLocal.keySet())
				this.directSubPropertyCheck.put(s + p, true);

			for (String s : indirectSubPropertiesLocal.keySet())
				this.indirectSubPropertyCheck.put(s + p, true);
		}
	}

	private void buildSuperPropertiesMaps() {

		HashMap<String, Label> directSuperPropertiesLocal;
		HashMap<String, Label> indirectSuperPropertiesLocal;
		HashMap<String, Label> allSuperPropertiesLocal;
		
		for (String p : this.properties.keySet()) { 

			directSuperPropertiesLocal = this.ontHandler.getSuperProperties(p, false);
			
			this.directSuperProperties.put(p, directSuperPropertiesLocal);
			
			allSuperPropertiesLocal = this.ontHandler.getSuperProperties(p, true);
			indirectSuperPropertiesLocal = new HashMap<String, Label>();
			for (Entry<String, Label> entry : allSuperPropertiesLocal.entrySet())
				if (!directSuperPropertiesLocal.containsKey(entry.getKey()))
					indirectSuperPropertiesLocal.put(entry.getKey(), entry.getValue());
			
			this.indirectSuperProperties.put(p, indirectSuperPropertiesLocal);
		}		
	}
	
	private void buildInverseProperties() {
		for (String op : this.objectProperties.keySet()) {
			this.propertyInverse.put(op, this.ontHandler.getInverseProperty(op));
			this.propertyInverseOf.put(op, this.ontHandler.getInverseOfProperty(op));
		}
	}
	
	private void buildDataPropertiesMaps() {
		
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<String> directDomainsUris = new ArrayList<String>();
		List<String> indirectDomainsUris = new ArrayList<String>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<String> allDomainsUris = new ArrayList<String>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<String> directRangesUris = new ArrayList<String>();
		List<String> indirectRangesUris = new ArrayList<String>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		List<String> allRangesUris = new ArrayList<String>();
		List<String> temp; 
		
		OntResource d;
		OntResource r;
		
		for (String propertyUri : this.dataProperties.keySet()) {
			
			directDomains.clear();
			directDomainsUris.clear();
			indirectDomainsUris.clear();
			allDomains.clear();
			allDomainsUris.clear();
			directRanges.clear();
			directRangesUris.clear();
			indirectRangesUris.clear();
			allRanges.clear();
			allRangesUris.clear();
			
			OntProperty property = this.ontHandler.getOntModel().getOntProperty(propertyUri);
			if (!property.isURIResource())
				continue;
			
			// direct domain
			ExtendedIterator<? extends OntResource> itrDomains = property.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontHandler.getMembers(d, directDomains, false);
			}
			directDomainsUris = ontHandler.getResourcesUris(directDomains);
			
			temp  = propertyDirectDomains.get(property.getURI());
			if (temp == null)
				propertyDirectDomains.put(property.getURI(), directDomainsUris);
			else 
				temp.addAll(directDomainsUris);
			
			for (OntResource domain : directDomains) {
				temp = directOutDataProperties.get(domain.getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					directOutDataProperties.put(domain.getURI(), temp);
				}
				temp.add(property.getURI());
			}

			// all domains
			for (OntResource domain : directDomains) {
				allDomains.add(domain);
				ontHandler.getChildren(domain, allDomains, true);
			}
			allDomainsUris = ontHandler.getResourcesUris(allDomains);

			// indirect domains
			for (String domainUri : allDomainsUris) {
				if (!directDomainsUris.contains(domainUri))
					indirectDomainsUris.add(domainUri);
			}

			temp  = propertyIndirectDomains.get(property.getURI());
			if (temp == null)
				propertyIndirectDomains.put(property.getURI(), indirectDomainsUris);
			else 
				temp.addAll(indirectDomainsUris);
			
			for (String domainUri : indirectDomainsUris) {
				temp = indirectOutDataProperties.get(domainUri);
				if (temp == null) { 
					temp = new ArrayList<String>();
					indirectOutDataProperties.put(domainUri, temp);
				}
				temp.add(property.getURI());
			}
			
			// direct ranges
			ExtendedIterator<? extends OntResource> itrRanges = property.listRange();
			while (itrRanges.hasNext()) {
				r = itrRanges.next();
				ontHandler.getMembers(r, directRanges, false);
			}
			directRangesUris = ontHandler.getResourcesUris(directRanges);

			temp  = propertyDirectRanges.get(property.getURI());
			if (temp == null)
				propertyDirectRanges.put(property.getURI(), directRangesUris);
			else 
				temp.addAll(directRangesUris);
			
			// all ranges
			for (OntResource range : directRanges) {
				allRanges.add(range);
				ontHandler.getChildren(range, allRanges, true);
			}
			allRangesUris = ontHandler.getResourcesUris(allRanges);
			
			// indirect ranges
			for (String rangeUri : allRangesUris) {
				if (!directRangesUris.contains(rangeUri))
					indirectRangesUris.add(rangeUri);
			}
			
			temp  = propertyIndirectRanges.get(property.getURI());
			if (temp == null)
				propertyIndirectRanges.put(property.getURI(), indirectRangesUris);
			else 
				temp.addAll(indirectRangesUris);
			
		}	
	}
	
	private void buildObjectPropertiesMaps() {
		
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<String> directDomainsUris = new ArrayList<String>();
		List<String> indirectDomainsUris = new ArrayList<String>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<String> allDomainsUris = new ArrayList<String>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<String> directRangesUris = new ArrayList<String>();
		List<String> indirectRangesUris = new ArrayList<String>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		List<String> allRangesUris = new ArrayList<String>();
		List<String> temp; 
		
		OntResource d;
		OntResource r;
		
		for (String propertyUri : this.objectProperties.keySet()) {
			
			directDomains.clear();
			directDomainsUris.clear();
			indirectDomainsUris.clear();
			allDomains.clear();
			allDomainsUris.clear();
			directRanges.clear();
			directRangesUris.clear();
			indirectRangesUris.clear();
			allRanges.clear();
			allRangesUris.clear();
			
			OntProperty property = this.ontHandler.getOntModel().getOntProperty(propertyUri);
			if (!property.isURIResource())
				continue;
			
			// direct domain
			ExtendedIterator<? extends OntResource> itrDomains = property.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontHandler.getMembers(d, directDomains, false);
			}
			directDomainsUris = ontHandler.getResourcesUris(directDomains);
			
			temp  = propertyDirectDomains.get(property.getURI());
			if (temp == null)
				propertyDirectDomains.put(property.getURI(), directDomainsUris);
			else 
				temp.addAll(directDomainsUris);
			
			for (OntResource domain : directDomains) {
				temp = directOutObjectProperties.get(domain.getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					directOutObjectProperties.put(domain.getURI(), temp);
				}
				temp.add(property.getURI());
			}

			// all domains
			for (OntResource domain : directDomains) {
				allDomains.add(domain);
				ontHandler.getChildren(domain, allDomains, true);
			}
			allDomainsUris = ontHandler.getResourcesUris(allDomains);

			// indirect domains
			for (String domainUri : allDomainsUris) {
				if (!directDomainsUris.contains(domainUri))
					indirectDomainsUris.add(domainUri);
			}

			temp  = propertyIndirectDomains.get(property.getURI());
			if (temp == null)
				propertyIndirectDomains.put(property.getURI(), indirectDomainsUris);
			else 
				temp.addAll(indirectDomainsUris);
			
			for (String domainUri : indirectDomainsUris) {
				temp = indirectOutObjectProperties.get(domainUri);
				if (temp == null) { 
					temp = new ArrayList<String>();
					indirectOutObjectProperties.put(domainUri, temp);
				}
				temp.add(property.getURI());
			}
			
			// direct ranges
			ExtendedIterator<? extends OntResource> itrRanges = property.listRange();
			while (itrRanges.hasNext()) {
				r = itrRanges.next();
				ontHandler.getMembers(r, directRanges, false);
			}
			directRangesUris = ontHandler.getResourcesUris(directRanges);

			temp  = propertyDirectRanges.get(property.getURI());
			if (temp == null)
				propertyDirectRanges.put(property.getURI(), directRangesUris);
			else 
				temp.addAll(directRangesUris);
			
			for (OntResource range : directRanges) {
				temp = directInObjectProperties.get(range.getURI());
				if (temp == null) {
					temp = new ArrayList<String>();
					directInObjectProperties.put(range.getURI(), temp);
				}
				temp.add(property.getURI());
			}
			
			// all ranges
			for (OntResource range : directRanges) {
				allRanges.add(range);
				ontHandler.getChildren(range, allRanges, true);
			}
			allRangesUris = ontHandler.getResourcesUris(allRanges);
			
			// indirect ranges
			for (String rangeUri : allRangesUris) {
				if (!directRangesUris.contains(rangeUri))
					indirectRangesUris.add(rangeUri);
			}
			
			temp  = propertyIndirectRanges.get(property.getURI());
			if (temp == null)
				propertyIndirectRanges.put(property.getURI(), indirectRangesUris);
			else 
				temp.addAll(indirectRangesUris);
			
			for (String rangeUri : indirectRangesUris) {
				temp = indirectInObjectProperties.get(rangeUri);
				if (temp == null) {
					temp = new ArrayList<String>();
					indirectInObjectProperties.put(rangeUri, temp);
				}
				temp.add(property.getURI());
			}
			
			for (String domain : directDomainsUris) {
				for (String range : directRangesUris) {
					temp = 
						domainRangeToDirectProperties.get(domain + range);
					if (temp == null) {
						temp = new ArrayList<String>();
						domainRangeToDirectProperties.put(domain + range, temp);
					}
					if (!temp.contains(property.getURI()))
						temp.add(property.getURI());
				}
			}
			
			for (String domain : allDomainsUris) {
				for (String range : allRangesUris) {
					if (directDomainsUris.contains(domain) && directRangesUris.contains(range)) continue;
					temp = domainRangeToIndirectProperties.get(domain + range);
					if (temp == null) {
						temp = new ArrayList<String>();
						domainRangeToIndirectProperties.put(domain + range, temp);
					}
					if (!temp.contains(property.getURI()))
						temp.add(property.getURI());
				}
			}

		}	

	}
	
	private void buildObjectPropertyDomainRangeMap() {
		
		for (String op : this.objectProperties.keySet()) {
			
			List<DomainRangePair> directDomainRangePairs = new ArrayList<DomainRangePair>();
			List<DomainRangePair> indirectDomainRangePairs = new ArrayList<DomainRangePair>();
			
			this.domainRangePairsOfDirectProperties.put(op, directDomainRangePairs);
			this.domainRangePairsOfIndirectProperties.put(op, indirectDomainRangePairs);
			
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
					for (String r : indirectRanges) {
						if (directDomains.contains(d) && directRanges.contains(r)) continue;
						indirectDomainRangePairs.add(new DomainRangePair(d, r));
					}
			}

		}
	}
	
	private void addPropertiesOfRDFVocabulary() {
		
		List<String> uris = new ArrayList<String>();
		
		String labelUri = "http://www.w3.org/2000/01/rdf-schema#label";
		String commentUri = "http://www.w3.org/2000/01/rdf-schema#comment";
		String valueUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
		
		uris.add(labelUri);
		uris.add(commentUri);
		uris.add(valueUri);
		
		List<String> temp;
		
		ontHandler.getOntModel().setNsPrefix("rdfs", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		ontHandler.getOntModel().setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		
		for (String uri : uris) 
			ontHandler.getOntModel().createDatatypeProperty(uri);

		
		// add label and comment property to the properties of all the classes
		for (String s : this.classes.keySet()) {
			temp = indirectOutDataProperties.get(s);
			if (temp == null) {
				temp = new ArrayList<String>();
				indirectOutDataProperties.put(s, temp);
			}
			for (String uri : uris)
				temp.add(uri);
		}

		// add uris to properties hashmap
		for (String uri : uris) {
			temp = propertyIndirectDomains.get(uri);
			if (temp == null) {
				temp = new ArrayList<String>();
				propertyIndirectDomains.put(labelUri, temp);
			}
			for (String s : this.classes.keySet())
				if (!temp.contains(s))
					temp.add(s);
		}
		
	}
	
	/**
	 * If the inheritance is true, it adds all the sub-classes of the domain and range of super-properties too.
	 * @param inheritance
	 */
	private void updateMapsWithSubpropertyDefinitions(boolean inheritance) {
		
		
		// iterate over all properties
		for (String p : this.properties.keySet()) {
			
			Set<String> directSuperPropertiesLocal = this.directSubProperties.get(p).keySet();
			Set<String> indirectSuperPropertiesLocal = this.directSubProperties.get(p).keySet();
			List<String> allSuperPropertiesLocal = new ArrayList<String>();
			if (directSuperPropertiesLocal != null) allSuperPropertiesLocal.addAll(directSuperPropertiesLocal);
			if (indirectSuperPropertiesLocal != null) allSuperPropertiesLocal.addAll(indirectSuperPropertiesLocal);
			

			List<String> temp = null;
			
			// domains 
			List<String> seuperDirectDomains = new ArrayList<String>();
			List<String> seuperIndirectDomains = new ArrayList<String>();
			List<String> superAllDomains = new ArrayList<String>();
			for (String superP : allSuperPropertiesLocal) {
				seuperDirectDomains = this.propertyDirectDomains.get(superP);
				seuperIndirectDomains = this.propertyIndirectDomains.get(superP);
				if (seuperDirectDomains != null) superAllDomains.addAll(seuperDirectDomains);
				if (seuperIndirectDomains != null && inheritance) superAllDomains.addAll(seuperIndirectDomains);
			}

			List<String> indirectDomains = propertyIndirectDomains.get(p);
			if (indirectDomains == null) {
				indirectDomains = new ArrayList<String>();
				propertyIndirectDomains.put(p, indirectDomains);
			}
			
			for (String d : superAllDomains) {
				if (!indirectDomains.contains(d))
					indirectDomains.add(d);
			}
			
			if (this.objectProperties.containsKey(p)) 
				for (String d : superAllDomains) {
					temp = indirectOutObjectProperties.get(d);
					if (temp != null && !temp.contains(p))
						temp.add(p);
				}

			if (this.dataProperties.containsKey(p)) 
				for (String d : superAllDomains) {
					temp = indirectOutDataProperties.get(d);
					if (temp != null && !temp.contains(p))
						temp.add(p);
				}

			// ranges
			List<String> seuperDirectRanges = new ArrayList<String>();
			List<String> seuperIndirectRanges = new ArrayList<String>();
			List<String> superAllRanges = new ArrayList<String>();
			for (String superP : allSuperPropertiesLocal) {
				seuperDirectRanges = this.propertyDirectRanges.get(superP);
				seuperIndirectRanges = this.propertyIndirectRanges.get(superP);
				if (seuperDirectRanges != null) superAllRanges.addAll(seuperDirectRanges);
				if (seuperIndirectRanges != null && inheritance) superAllRanges.addAll(seuperIndirectRanges);
			}

			List<String> indirectRanges = propertyIndirectRanges.get(p);
			if (indirectRanges == null) {  
				indirectRanges = new ArrayList<String>();
				propertyIndirectRanges.put(p, indirectRanges);
			}
			
			for (String r : superAllRanges) {
				if (!indirectRanges.contains(r))
					indirectRanges.add(r);
			}
			
			if (this.objectProperties.containsKey(p)) 
				for (String r : superAllRanges) {
					temp = indirectInObjectProperties.get(r);
					if (temp != null && !temp.contains(p))
						temp.add(p);
				}
				
			List<String> directDomains = this.propertyDirectDomains.get(p);
			List<String> allDomains = new ArrayList<String>();
			List<String> directRanges = this.propertyDirectRanges.get(p);
			List<String> allRanges = new ArrayList<String>();
			
			if (directDomains != null) allRanges.addAll(directDomains);
			if (indirectDomains != null) allDomains.addAll(indirectDomains);

			if (directRanges != null) allDomains.addAll(directRanges);
			if (indirectRanges != null) allRanges.addAll(indirectRanges);
			
			for (String domain : allDomains) {
				for (String range : allRanges) {
					if (directDomains.contains(domain) && directRanges.contains(range)) continue;
					temp = domainRangeToIndirectProperties.get(domain + range);
					if (temp == null) {
						temp = new ArrayList<String>();
						domainRangeToIndirectProperties.put(domain + range, temp);
					}
					if (!temp.contains(p))
						temp.add(p);
				}
			}
		}

	}

	private void buildMapsForPropertiesWithoutDomainOrRange() {

		List<String> temp;
		boolean haveDomain;
		boolean haveRange;
		Label label;
		
		List<String> directDomains;
		List<String> indirectDomains;
		List<String> directRanges;
		List<String> indirectRanges;
		List<String> allDomains;
		List<String> allRanges;
		
		for (String p : this.dataProperties.keySet()) {
			
			label = this.dataProperties.get(p);
			
			directDomains = propertyDirectDomains.get(p);
			indirectDomains = propertyIndirectDomains.get(p);

			haveDomain = false;
			
			if ((directDomains == null || directDomains.size() == 0) &&
					(indirectDomains == null || indirectDomains.size() == 0))
				haveDomain = true;
			
			if (haveDomain)
				this.dataPropertiesWithoutDomain.put(p, label);
		}
		
		for (String p : this.objectProperties.keySet()) {
			
			label = this.objectProperties.get(p);
			
			directDomains = propertyDirectDomains.get(p);
			directRanges = propertyDirectRanges.get(p);

			indirectDomains = propertyIndirectDomains.get(p);
			indirectRanges = propertyIndirectRanges.get(p);

			haveDomain = true;
			haveRange = true;
			
			if ((directDomains == null || directDomains.size() == 0) &&
					(indirectDomains == null || indirectDomains.size() == 0))
				haveDomain = false;
			
			if ((directRanges == null || directRanges.size() == 0) ||
					(indirectRanges == null || indirectRanges.size() == 0))
				haveRange = false;
			
			if (haveDomain && !haveRange) {
				this.objectPropertiesWithOnlyDomain.put(p, label);
				allDomains = new ArrayList<String>();
				if (directDomains != null) allDomains.addAll(directDomains);
				if (indirectDomains != null) allDomains.addAll(indirectDomains);
				
				for (String domain : allDomains) {
					for (String range : this.classes.keySet()) {
						temp = this.domainRangeToRangelessProperties.get(domain + range);
						if (temp == null) {
							temp = new ArrayList<String>();
							domainRangeToRangelessProperties.put(domain + range, temp);
						}
						if (!temp.contains(p)) temp.add(p);					
					}
				}
			} else if (!haveDomain && haveRange) {
				this.objectPropertiesWithOnlyRange.put(p, label);
				allRanges = new ArrayList<String>();
				if (directRanges != null) allRanges.addAll(directRanges);
				if (indirectRanges != null) allRanges.addAll(indirectRanges);

				for (String domain : this.classes.keySet()) {
					for (String range : allRanges) {
						temp = this.domainRangeToDomainlessProperties.get(domain + range);
						if (temp == null) {
							temp = new ArrayList<String>();
							domainRangeToDomainlessProperties.put(domain + range, temp);
						}
						if (!temp.contains(p)) temp.add(p);					
					}
				}
			} else if (!haveDomain && !haveRange) {
				this.objectPropertiesWithoutDomainAndRange.put(p, label);
			}
			
		}

	}
	
	private void buildConnectivityMaps() {
		
		List<String> classList = new ArrayList<String>(this.classes.keySet());
		
		List<String> directProperties;
		List<String> indirectProperties;
		List<String> propertiesWithOnlyDomain;
		List<String> propertiesWithOnlyRange;
		
		for (int i = 0; i < classList.size(); i++) {
			String c1 = classList.get(i);
			for (int j = i+1; j < classList.size(); j++) {
				String c2 = classList.get(j);
				
				directProperties = this.domainRangeToDirectProperties.get(c1+c2);
				if (directProperties != null && directProperties.size() > 0) { 
					this.connectedByDirectProperties.put(c1+c2, true);
					this.connectedByDirectProperties.put(c2+c1, true);
				}
				indirectProperties = this.domainRangeToIndirectProperties.get(c1+c2);
				if (indirectProperties != null && indirectProperties.size() > 0) { 
					this.connectedByIndirectProperties.put(c1+c2, true);
					this.connectedByIndirectProperties.put(c2+c1, true);
				}
				propertiesWithOnlyDomain = this.domainRangeToRangelessProperties.get(c1+c2);
				if (propertiesWithOnlyDomain != null && propertiesWithOnlyDomain.size() > 0) { 
					this.connectedByRangelessProperties.put(c1+c2, true);
					this.connectedByRangelessProperties.put(c2+c1, true);
				}
				propertiesWithOnlyRange = this.domainRangeToDomainlessProperties.get(c1+c2);
				if (propertiesWithOnlyRange != null && propertiesWithOnlyRange.size() > 0) { 
					this.connectedByDomainlessProperties.put(c1+c2, true);
					this.connectedByDomainlessProperties.put(c2+c1, true);
				}
			}
		}
	}
}
