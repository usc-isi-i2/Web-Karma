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
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.alignment.Label;

public class OntologyCache {
	
	static Logger logger = LoggerFactory.getLogger(OntologyCache.class.getName());
	
	private OntologyHandler ontHandler = null;

	private HashMap<String, Label> classes;
	private HashMap<String, Label> properties;
	private HashMap<String, Label> dataProperties;
	private HashMap<String, Label> objectProperties;

	private HashMap<String, Label> dataPropertiesWithoutDomain;
	private HashMap<String, Label> objectPropertiesWithOnlyDomain;
	private HashMap<String, Label> objectPropertiesWithOnlyRange;
	private HashMap<String, Label> objectPropertiesWithoutDomainAndRange;
	
//	private OntologyTreeNode classHierarchy;
//	private OntologyTreeNode objectPropertyHierarchy;
//	private OntologyTreeNode dataPropertyHierarchy;
	
	// hashmap: class -> subclasses
	private HashMap<String, HashMap<String, Label>> directSubClasses;
	private HashMap<String, HashMap<String, Label>> indirectSubClasses;
	// hashmap: class -> superclasses
	private HashMap<String, HashMap<String, Label>> directSuperClasses;
	private HashMap<String, HashMap<String, Label>> indirectSuperClasses;
	// hashmap: class1 + class2 -> boolean (if c1 is subClassOf c2)
	private HashSet<String> directSubClassCheck;
	private HashSet<String> indirectSubClassCheck;
	// List <subclass, superclass> pairs
//	private List<SubclassSuperclassPair> directSubclassSuperclassPairs;
//	private List<SubclassSuperclassPair> indirectSubclassSuperclassPairs;

	// hashmap: class -> subproperties
	private HashMap<String, HashMap<String, Label>> directSubProperties;
	private HashMap<String, HashMap<String, Label>> indirectSubProperties;
	// hashmap: class -> superproperties
	private HashMap<String, HashMap<String, Label>> directSuperProperties;
	private HashMap<String, HashMap<String, Label>> indirectSuperProperties;
	// hashmap: property1 + property2 -> boolean (if p1 is subPropertyOf p2)
	private HashSet<String> directSubPropertyCheck;
	private HashSet<String> indirectSubPropertyCheck;
	
	// hashmap: property -> property inverse and inverseOf
	private HashMap<String, Label> propertyInverse;
	private HashMap<String, Label> propertyInverseOf;

	// hashmap: property -> direct domains
	private HashMap<String, HashSet<String>> propertyDirectDomains;
	private HashMap<String, HashSet<String>> propertyIndirectDomains;
	// hashmap: property -> direct ranges
	private HashMap<String, HashSet<String>> propertyDirectRanges;
	private HashMap<String, HashSet<String>> propertyIndirectRanges;

	// hashmap: class -> properties whose domain(direct) includes this class 
	private HashMap<String, HashSet<String>> directOutDataProperties; 
	private HashMap<String, HashSet<String>> indirectOutDataProperties; 
	private HashMap<String, HashSet<String>> directOutObjectProperties; 
	private HashMap<String, HashSet<String>> indirectOutObjectProperties; 
	// hashmap: class -> properties whose range(direct) includes this class 
	private HashMap<String, HashSet<String>> directInObjectProperties; 
	private HashMap<String, HashSet<String>> indirectInObjectProperties;
	
	// hashmap: domain+range -> object properties
	private HashMap<String, HashSet<String>> domainRangeToDirectProperties;
	private HashMap<String, HashSet<String>> domainRangeToIndirectProperties;
//	private HashMap<String, List<String>> domainRangeToDomainlessProperties;
//	private HashMap<String, List<String>> domainRangeToRangelessProperties;

	// hashmap: objectproperty -> <domain, range> pairs
//	private HashMap<String, List<DomainRangePair>> domainRangePairsOfDirectProperties;
//	private HashMap<String, List<DomainRangePair>> domainRangePairsOfIndirectProperties;
//	private HashMap<String, List<DomainRangePair>> domainRangePairsOfDomainlessProperties;
//	private HashMap<String, List<DomainRangePair>> domainRangePairsOfRangelessProperties;
	
	// hashmap: class1 + class2 -> boolean (if c1 is connected to c2)
//	private HashSet<String> connectedByDirectProperties;
//	private HashSet<String> connectedByIndirectProperties;
//	private HashSet<String> connectedByDomainlessProperties;
//	private HashSet<String> connectedByRangelessProperties;

	// public methods
	
	public OntologyCache(OntologyHandler ontHandler) {
		this.ontHandler = ontHandler;

		allocateDataStructures();
	}

	public void init() {

		logger.debug("start building the ontology cache ...");
		
		
		long start = System.currentTimeMillis();
		
		// create a list of classes and properties of the model
		this.loadClasses();
		this.loadProperties();
		
		logger.info("number of classes:" + classes.size());
		logger.info("number of all properties:" + properties.size());
		logger.info("number of data properties:" + dataProperties.size() );
		logger.info("number of object properties:" + objectProperties.size() );
		// A = number of all properties including rdf:Property 
		// B = number of properties defined as Data Property
		// C = number of properties defined as Object Property
		// properties = A
		// dataproperties = A - C
		// objectproperties = A - B
		logger.info("number of properties explicitly defined as owl:DatatypeProperty:" + (properties.size() - objectProperties.size()) );
		logger.info("number of properties explicitly defined as owl:ObjectProperty:" + (properties.size() - dataProperties.size()) );

		if (ModelingConfiguration.getManualAlignment()) {
			float elapsedTimeSec = (System.currentTimeMillis() - start)/1000F;
			logger.info("time to build the ontology cache (manual alignment): " + elapsedTimeSec);
			return;
		}
			
		// build hashmaps for indirect subclass and subproperty relationships
		logger.info("build subclass hashmaps ...");
		this.buildSubClassesMaps();
		logger.info("build superclass hashmaps ...");
		this.buildSuperClassesMaps();
		logger.info("build subproperty hashmaps ...");
		this.buildSubPropertiesMaps();
		logger.info("build superproperty hashmaps ...");
		this.buildSuperPropertiesMaps();

		// create a hierarchy of classes and properties of the model
		logger.info("build class hierarchy ...");
//		this.buildClassHierarchy(classHierarchy);
//		logger.info("build object property hierarchy ...");
//		this.buildDataPropertyHierarchy(dataPropertyHierarchy);
//		logger.info("build data property hierarchy ...");
//		this.buildObjectPropertyHierarchy(objectPropertyHierarchy);
		
		
		// build hashmaps to include inverse(Of) properties
		logger.info("build inverse property hashmap ...");
		this.buildInverseProperties();
		
		// build some hashmaps that will be used in alignment
		logger.info("build data property hashmaps ...");
		this.buildDataPropertiesMaps();
		logger.info("build object property hashmaps ...");
		this.buildObjectPropertiesMaps();
		// update hashmaps to include the subproperty relations  
		logger.info("apply subproperty definitions ...");
		this.updateMapsWithSubpropertyDefinitions();
		
		// classify different types of properties
		logger.info("classify properties ...");
		this.classifyProperties();

		// build connectivity hashmaps
//		logger.info("build connectivity hashmaps ...");
//		this.buildConnectivityMaps();
		
		// build hashmaps to speed up adding links to the graph
//		this.buildObjectPropertyDomainRangeMap();
		
		// add some common properties like rdfs:label, rdfs:comment, ...
//		logger.info("add rdfs label/comment/value ...");
//		this.addPropertiesOfRDFVocabulary();
		
		float elapsedTimeSec = (System.currentTimeMillis() - start)/1000F;
		logger.info("time to build the ontology cache: " + elapsedTimeSec);
	}

	private void allocateDataStructures() {
		this.classes = new HashMap<String, Label>();
		this.properties = new HashMap<String, Label>();
		this.dataProperties = new HashMap<String, Label>();
		this.objectProperties = new HashMap<String, Label>();
		
		this.dataPropertiesWithoutDomain = new HashMap<String, Label>();
		this.objectPropertiesWithOnlyDomain = new HashMap<String, Label>();
		this.objectPropertiesWithOnlyRange = new HashMap<String, Label>();
		this.objectPropertiesWithoutDomainAndRange = new HashMap<String, Label>();
		
//		this.classHierarchy = new OntologyTreeNode(new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL), null, null);
//		this.dataPropertyHierarchy = new OntologyTreeNode(new Label("Data Properties"), null, null);
//		this.objectPropertyHierarchy = new OntologyTreeNode(new Label("Object Properties"), null, null);

		this.directSubClasses = new HashMap<String, HashMap<String, Label>>();
		this.indirectSubClasses = new HashMap<String, HashMap<String, Label>>();
		this.directSuperClasses = new HashMap<String, HashMap<String, Label>>();
		this.indirectSuperClasses = new HashMap<String, HashMap<String, Label>>();
		this.directSubClassCheck = new HashSet<String>();
		this.indirectSubClassCheck = new HashSet<String>();
//		this.directSubclassSuperclassPairs = new ArrayList<SubclassSuperclassPair>();
//		this.indirectSubclassSuperclassPairs = new ArrayList<SubclassSuperclassPair>();
		
		this.directSubProperties = new HashMap<String, HashMap<String, Label>>();
		this.indirectSubProperties = new HashMap<String, HashMap<String, Label>>();
		this.directSuperProperties = new HashMap<String, HashMap<String, Label>>();
		this.indirectSuperProperties = new HashMap<String, HashMap<String, Label>>();
		this.directSubPropertyCheck = new HashSet<String>();
		this.indirectSubPropertyCheck = new HashSet<String>();
		
		this.propertyInverse = new HashMap<String, Label>();
		this.propertyInverseOf = new HashMap<String, Label>();
		
		this.propertyDirectDomains = new HashMap<String, HashSet<String>>();
		this.propertyIndirectDomains = new HashMap<String, HashSet<String>>();
		this.propertyDirectRanges = new HashMap<String, HashSet<String>>();
		this.propertyIndirectRanges = new HashMap<String, HashSet<String>>();
		
		this.directOutDataProperties = new HashMap<String, HashSet<String>>();
		this.indirectOutDataProperties = new HashMap<String, HashSet<String>>();
		this.directOutObjectProperties = new HashMap<String, HashSet<String>>();
		this.indirectOutObjectProperties = new HashMap<String, HashSet<String>>();
		this.directInObjectProperties = new HashMap<String, HashSet<String>>();
		this.indirectInObjectProperties = new HashMap<String, HashSet<String>>();
		
		this.domainRangeToDirectProperties = new HashMap<String, HashSet<String>>();
		this.domainRangeToIndirectProperties = new HashMap<String, HashSet<String>>();
//		this.domainRangeToDomainlessProperties = new HashMap<String, List<String>>();
//		this.domainRangeToRangelessProperties = new HashMap<String, List<String>>();
		
//		this.domainRangePairsOfDirectProperties = new HashMap<String, List<DomainRangePair>>();
//		this.domainRangePairsOfIndirectProperties = new HashMap<String, List<DomainRangePair>>();
//		this.domainRangePairsOfDomainlessProperties = new HashMap<String, List<DomainRangePair>>();
//		this.domainRangePairsOfRangelessProperties = new HashMap<String, List<DomainRangePair>>();

//		this.connectedByDirectProperties = new HashSet<String>();
//		this.connectedByIndirectProperties = new HashSet<String>();
//		this.connectedByDomainlessProperties = new HashSet<String>();
//		this.connectedByRangelessProperties = new HashSet<String>();
	}
	
	public HashMap<String, Label> getClasses() {
		return classes;
	}

	public HashMap<String, Label> getProperties() {
		return properties;
	}

	public Label getPropertyLabel(String property) {
		return properties.get(property);
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

//	public OntologyTreeNode getClassHierarchy() {
//		return classHierarchy;
//	}
//
//	public OntologyTreeNode getObjectPropertyHierarchy() {
//		return objectPropertyHierarchy;
//	}
//
//	public OntologyTreeNode getDataPropertyHierarchy() {
//		return dataPropertyHierarchy;
//	}

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

	public HashSet<String> getDirectSubClassCheck() {
		return directSubClassCheck;
	}

	public HashSet<String> getIndirectSubClassCheck() {
		return indirectSubClassCheck;
	}
	
//	public List<SubclassSuperclassPair> getDirectSubclassSuperclassPairs() {
//		return directSubclassSuperclassPairs;
//	}
//
//	public List<SubclassSuperclassPair> getIndirectSubclassSuperclassPairs() {
//		return indirectSubclassSuperclassPairs;
//	}

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

	public HashSet<String> getDirectSubPropertyCheck() {
		return directSubPropertyCheck;
	}

	public HashSet<String> getIndirectSubPropertyCheck() {
		return indirectSubPropertyCheck;
	}

	public HashMap<String, Label> getPropertyInverse() {
		return propertyInverse;
	}

	public HashMap<String, Label> getPropertyInverseOf() {
		return propertyInverseOf;
	}

	public HashMap<String, HashSet<String>> getPropertyDirectDomains() {
		return propertyDirectDomains;
	}

	public HashMap<String, HashSet<String>> getPropertyIndirectDomains() {
		return propertyIndirectDomains;
	}

	public HashMap<String, HashSet<String>> getPropertyDirectRanges() {
		return propertyDirectRanges;
	}

	public HashMap<String, HashSet<String>> getPropertyIndirectRanges() {
		return propertyIndirectRanges;
	}

	public HashMap<String, HashSet<String>> getDirectOutDataProperties() {
		return directOutDataProperties;
	}

	public HashMap<String, HashSet<String>> getIndirectOutDataProperties() {
		return indirectOutDataProperties;
	}

	public HashMap<String, HashSet<String>> getDirectOutObjectProperties() {
		return directOutObjectProperties;
	}

	public HashMap<String, HashSet<String>> getIndirectOutObjectProperties() {
		return indirectOutObjectProperties;
	}

	public HashMap<String, HashSet<String>> getDirectInObjectProperties() {
		return directInObjectProperties;
	}

	public HashMap<String, HashSet<String>> getIndirectInObjectProperties() {
		return indirectInObjectProperties;
	}

	public HashMap<String, HashSet<String>> getDomainRangeToDirectProperties() {
		return domainRangeToDirectProperties;
	}

	public HashMap<String, HashSet<String>> getDomainRangeToIndirectProperties() {
		return domainRangeToIndirectProperties;
	}

//	public HashMap<String, List<String>> getDomainRangeToDomainlessProperties() {
//		return domainRangeToDomainlessProperties;
//	}
//
//	public HashMap<String, List<String>> getDomainRangeToRangelessProperties() {
//		return domainRangeToRangelessProperties;
//	}
//
//	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfDirectProperties() {
//		return domainRangePairsOfDirectProperties;
//	}
//
//	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfIndirectProperties() {
//		return domainRangePairsOfIndirectProperties;
//	}
//
//	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfDomainlessProperties() {
//		return domainRangePairsOfDomainlessProperties;
//	}
//
//	public HashMap<String, List<DomainRangePair>> getDomainRangePairsOfRangelessProperties() {
//		return domainRangePairsOfRangelessProperties;
//	}

//	public HashSet<String> getConnectedByDirectProperties() {
//		return connectedByDirectProperties;
//	}
//
//	public HashSet<String> getConnectedByIndirectProperties() {
//		return connectedByIndirectProperties;
//	}
//
//	public HashSet<String> getConnectedByDomainlessProperties() {
//		return connectedByDomainlessProperties;
//	}
//
//	public HashSet<String> getConnectedByRangelessProperties() {
//		return connectedByRangelessProperties;
//	}

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
		
		Property rdfType = this.ontHandler.getOntModel().createProperty(Uris.RDF_TYPE_URI);
		Resource classNode = this.ontHandler.getOntModel().createResource(Uris.RDFS_CLASS_URI);
		ResIterator itr = ontHandler.getOntModel().listSubjectsWithProperty(rdfType, classNode);
		
		while (itr.hasNext()) {
			
			Resource r = itr.next();
			
			if (!r.isURIResource())
				continue;
			
			if (!classes.containsKey(r.getURI()))
				classes.put(r.getURI(), ontHandler.getResourceLabel(r));

		}

	}

	private void loadProperties() {

		this.properties.put(Uris.RDF_TYPE_URI, new Label(Uris.RDF_TYPE_URI, Namespaces.RDF, Prefixes.RDF));
		this.properties.put(Uris.RDF_VALUE_URI, new Label(Uris.RDF_VALUE_URI, Namespaces.RDF, Prefixes.RDF));
		this.properties.put(Uris.RDFS_SUBCLASS_URI, new Label(Uris.RDFS_SUBCLASS_URI, Namespaces.RDFS, Prefixes.RDFS));
		this.properties.put(Uris.RDFS_LABEL_URI, new Label(Uris.RDFS_LABEL_URI, Namespaces.RDFS, Prefixes.RDFS));
		this.properties.put(Uris.RDFS_COMMENT_URI, new Label(Uris.RDFS_COMMENT_URI, Namespaces.RDFS, Prefixes.RDFS));

		this.objectProperties.put(Uris.RDF_TYPE_URI, new Label(Uris.RDF_TYPE_URI, Namespaces.RDF, Prefixes.RDF));
		this.objectProperties.put(Uris.RDF_VALUE_URI, new Label(Uris.RDF_VALUE_URI, Namespaces.RDF, Prefixes.RDF));
		this.objectProperties.put(Uris.RDFS_SUBCLASS_URI, new Label(Uris.RDFS_SUBCLASS_URI, Namespaces.RDFS, Prefixes.RDFS));
		this.dataProperties.put(Uris.RDFS_LABEL_URI, new Label(Uris.RDFS_LABEL_URI, Namespaces.RDFS, Prefixes.RDFS));
		this.dataProperties.put(Uris.RDFS_COMMENT_URI, new Label(Uris.RDFS_COMMENT_URI, Namespaces.RDFS, Prefixes.RDFS));

		this.properties.put(Uris.CLASS_INSTANCE_LINK_URI, new Label(Uris.CLASS_INSTANCE_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV));
		this.properties.put(Uris.COLUMN_SUBCLASS_LINK_URI, new Label(Uris.COLUMN_SUBCLASS_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV));
		this.properties.put(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI, new Label(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV));
		this.properties.put(Uris.OBJECTPROPERTY_SPECIALIZATION_LINK_URI, new Label(Uris.OBJECTPROPERTY_SPECIALIZATION_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV));
		this.properties.put(Uris.DEFAULT_LINK_URI, new Label(Uris.DEFAULT_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV));
		
		ExtendedIterator<OntProperty> itrP = ontHandler.getOntModel().listAllOntProperties();
		
		while (itrP.hasNext()) {
			
			OntProperty p = itrP.next();
			
			if (!p.isURIResource())
				continue;
			
//			if (p.isAnnotationProperty())
//				continue;

			if (!properties.containsKey(p.getURI()))
				properties.put(p.getURI(), ontHandler.getResourceLabel(p));
			
			if (p.isDatatypeProperty() || !p.isObjectProperty())
			{
				if (!dataProperties.containsKey(p.getURI()))
					dataProperties.put(p.getURI(), ontHandler.getResourceLabel(p));
			}

			if (p.isObjectProperty() || !p.isDatatypeProperty())
			{
				if (p.getRange() != null && p.getRange().hasURI(Namespaces.RDFS + "Literal")) { 
					if (!dataProperties.containsKey(p.getURI())) {
						dataProperties.put(p.getURI(), ontHandler.getResourceLabel(p));
					}
				} else if (!objectProperties.containsKey(p.getURI()))
					objectProperties.put(p.getURI(), ontHandler.getResourceLabel(p));
			}
		}
	}
	
//	private boolean isTopLevelClass(String c) {
//		
//		// returns TRUE if the class s is an immediate node after THING in the class hierarchy
//		
////		if (c.equalsIgnoreCase(Uris.THING_URI))
////			return true;
//		
//		Set<String> superClasses = this.directSuperClasses.get(c).keySet();
//
//		if (superClasses == null || superClasses.isEmpty())
//			return true;
//		
//		if (superClasses.size() == 1 &&
//				superClasses.iterator().next().equalsIgnoreCase(Uris.THING_URI))
//			return true;
//		
//		for (String s : superClasses)
//			if (this.classes.containsKey(s))
//				return false;
//		
//		return true;
//	}
//	
//	private void buildClassHierarchy(OntologyTreeNode node) {
//		
//		List<OntologyTreeNode> children = new ArrayList<OntologyTreeNode>();
//		if (node.getParent() == null) {
//			for (String s : this.classes.keySet()) {
////				if (s.equalsIgnoreCase(Uris.THING_URI)) continue;
//				if (isTopLevelClass(s)) {
//					Label label = this.classes.get(s);
//					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
//					buildClassHierarchy(childNode);
//					children.add(childNode);
//				}
//			}
//		} else {
//			HashMap<String, Label> subClasses = 
//					this.directSubClasses.get(node.getLabel().getUri());
//
//			for (String s : subClasses.keySet()) {
//				Label label = subClasses.get(s);
//				OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
//				
//				buildClassHierarchy(childNode);
//				children.add(childNode);
//			}
//		}
//		node.setChildren(children);
//	}
//	
//	private boolean isTopLevelDataProperty(String property) {
//		
//		Set<String> superProperties = this.directSuperProperties.get(property).keySet();
//
//		if (superProperties == null || superProperties.isEmpty())
//			return true;
//		
//		for (String s : superProperties)
//			if (this.dataProperties.containsKey(s))
//				return false;
//		
//		return true;
//	}
	
//	private void buildDataPropertyHierarchy(OntologyTreeNode node) {
//		List<OntologyTreeNode> children = new ArrayList<OntologyTreeNode>();
//		if (node.getParent() == null) {
//			for (String s : this.dataProperties.keySet()) {
//				if (isTopLevelDataProperty(s)) {
//					Label label = this.dataProperties.get(s);
//					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
//					buildDataPropertyHierarchy(childNode);
//					children.add(childNode);
//				}
//			}
//		} else {
//			HashMap<String, Label> subProperties = 
//					this.directSubProperties.get(node.getLabel().getUri());
//
//			if (subProperties != null)
//				for (String s : subProperties.keySet()) {
//					Label label = subProperties.get(s);
//					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
//					
//					buildDataPropertyHierarchy(childNode);
//					children.add(childNode);
//				}
//		}
//		node.setChildren(children);	
//	}
	
//	private boolean isTopLevelObjectProperty(String property) {
//		
//		Set<String> superProperties = this.directSuperProperties.get(property).keySet();
//
//		if (superProperties == null || superProperties.isEmpty())
//			return true;
//		
//		for (String s : superProperties)
//			if (this.objectProperties.containsKey(s))
//				return false;
//		
//		return true;
//	}
//	
//	private void buildObjectPropertyHierarchy(OntologyTreeNode node) {
//		
//		List<OntologyTreeNode> children = new ArrayList<OntologyTreeNode>();
//		if (node.getParent() == null) {
//			for (String s : this.objectProperties.keySet()) {
//				if (isTopLevelObjectProperty(s)) {
//					Label label = this.objectProperties.get(s);
//					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
//					buildObjectPropertyHierarchy(childNode);
//					children.add(childNode);
//				}
//			}
//		} else {
//			HashMap<String, Label> subProperties = 
//					this.directSubProperties.get(node.getLabel().getUri());
//
//			if (subProperties != null)
//				for (String s : subProperties.keySet()) {
//					Label label = subProperties.get(s);
//					OntologyTreeNode childNode = new OntologyTreeNode(label, node, null);
//					
//					buildObjectPropertyHierarchy(childNode);
//					children.add(childNode);
//				}
//		}
//		node.setChildren(children);	
//	}

	private void buildSubClassesMaps() {
		
		HashMap<String, Label> allClassesExceptThing = new HashMap<String, Label>();
		for (Entry<String, Label> entry : this.classes.entrySet())
			if (!entry.getKey().equalsIgnoreCase(Uris.THING_URI))
				allClassesExceptThing.put(entry.getKey(), entry.getValue());

		HashMap<String, Label> directSubClassesLocal;
		HashMap<String, Label> indirectSubClassesLocal;
		HashMap<String, Label> allSubClassesLocal;
		
		for (String c : this.classes.keySet()) {
			
			// Thing node
			if (c.equalsIgnoreCase(Uris.THING_URI)) {
				directSubClassesLocal = allClassesExceptThing;
				indirectSubClassesLocal = allClassesExceptThing;
			} else {
				
				directSubClassesLocal = this.ontHandler.getSubClasses(c, false);
				allSubClassesLocal = this.ontHandler.getSubClasses(c, true);
				indirectSubClassesLocal = new HashMap<String, Label>();
				for (Entry<String, Label> entry : allSubClassesLocal.entrySet())
					if (!directSubClassesLocal.containsKey(entry.getKey()))
						indirectSubClassesLocal.put(entry.getKey(), entry.getValue());
			
			}

			
			this.directSubClasses.put(c, directSubClassesLocal);
			this.indirectSubClasses.put(c, indirectSubClassesLocal);

			for (String s : directSubClassesLocal.keySet())
				this.directSubClassCheck.add(s + c);
			
			for (String s : indirectSubClassesLocal.keySet())
				this.indirectSubClassCheck.add(s + c);
		}
		
//		for (String superclass : this.directSubClasses.keySet()) {
//			Set<String> subClasses = this.directSubClasses.get(superclass).keySet();
//			if (subClasses != null)
//			for (String subclass : subClasses)
//				this.directSubclassSuperclassPairs.add(new SubclassSuperclassPair(subclass, superclass));
//		}
//		
//		for (String superclass : this.indirectSubClasses.keySet()) {
//			Set<String> subClasses = this.indirectSubClasses.get(superclass).keySet();
//			if (subClasses != null)
//			for (String subclass : subClasses)
//				this.indirectSubclassSuperclassPairs.add(new SubclassSuperclassPair(subclass, superclass));
//		}
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
			if (superClasses == null) {
				superClasses = new HashMap<String, Label>();
				this.indirectSuperClasses.put(c, superClasses); 
			}
			if (!c.equalsIgnoreCase(Uris.THING_URI) && !superClasses.containsKey(Uris.THING_URI))
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
				this.directSubPropertyCheck.add(s + p);

			for (String s : indirectSubPropertiesLocal.keySet())
				this.indirectSubPropertyCheck.add(s + p);
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
		
		HashSet<OntResource> directDomains;
		HashSet<String> directDomainsUris;
		HashSet<String> indirectDomainsUris;
		HashSet<OntResource> allDomains;
		HashSet<String> allDomainsUris;
		HashSet<OntResource> directRanges;
		HashSet<String> directRangesUris;
		HashSet<String> indirectRangesUris;
		HashSet<OntResource> allRanges;
		HashSet<String> allRangesUris;
		HashSet<String> temp; 
		
		OntResource d;
		OntResource r;
		
		for (String propertyUri : this.dataProperties.keySet()) {
			
			directDomains = new HashSet<OntResource>();
			directDomainsUris = new HashSet<String>();
			indirectDomainsUris = new HashSet<String>();
			allDomains = new HashSet<OntResource>();
			allDomainsUris = new HashSet<String>();
			directRanges = new HashSet<OntResource>();
			directRangesUris = new HashSet<String>();
			indirectRangesUris = new HashSet<String>();
			allRanges = new HashSet<OntResource>();
			allRangesUris = new HashSet<String>();
			
			OntProperty property = this.ontHandler.getOntModel().getOntProperty(propertyUri);
			if (property == null || !property.isURIResource())
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
					temp = new HashSet<String>();
					directOutDataProperties.put(domain.getURI(), temp);
				}
				temp.add(property.getURI());
			}

			// all domains
			if (directDomainsUris.contains(Uris.THING_URI))
				allDomainsUris = new HashSet<String>(this.classes.keySet());
			else {
				for (OntResource domain : directDomains) {
					allDomains.add(domain);
					ontHandler.getChildren(domain, allDomains, true);
				}
				allDomainsUris = ontHandler.getResourcesUris(allDomains);
			}

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
					temp = new HashSet<String>();
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
		
		HashSet<OntResource> directDomains;
		HashSet<String> directDomainsUris;
		HashSet<String> indirectDomainsUris;
		HashSet<OntResource> allDomains;
		HashSet<String> allDomainsUris;
		HashSet<OntResource> directRanges;
		HashSet<String> directRangesUris;
		HashSet<String> indirectRangesUris;
		HashSet<OntResource> allRanges;
		HashSet<String> allRangesUris;
		HashSet<String> temp; 
		
		OntResource d;
		OntResource r;
		
//		int count = 0;
		for (String propertyUri : this.objectProperties.keySet()) {

			OntProperty property = this.ontHandler.getOntModel().getOntProperty(propertyUri);
			if (property == null || !property.isURIResource())
				continue;
						
			directDomains = new HashSet<OntResource>();
			directDomainsUris = new HashSet<String>();
			indirectDomainsUris = new HashSet<String>();
			allDomains = new HashSet<OntResource>();
			allDomainsUris = new HashSet<String>();
			directRanges = new HashSet<OntResource>();
			directRangesUris = new HashSet<String>();
			indirectRangesUris = new HashSet<String>();
			allRanges = new HashSet<OntResource>();
			allRangesUris = new HashSet<String>();
			
//			count ++;
//			if (count % 1000 == 0)
//				System.gc();

			
			// direct domain
			ExtendedIterator<? extends OntResource> itrDomains = property.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontHandler.getMembers(d, directDomains, false);
			}
			directDomainsUris = ontHandler.getResourcesUris(directDomains);
			
			if (directDomainsUris != null && directDomainsUris.contains(Uris.THING_URI))
				directDomainsUris.remove(Uris.THING_URI);
			
			temp  = propertyDirectDomains.get(property.getURI());
			if (temp == null)
				propertyDirectDomains.put(property.getURI(), directDomainsUris);
			else 
				temp.addAll(directDomainsUris);
			
			for (OntResource domain : directDomains) {
				temp = directOutObjectProperties.get(domain.getURI());
				if (temp == null) {
					temp = new HashSet<String>();
					directOutObjectProperties.put(domain.getURI(), temp);
				}
				temp.add(property.getURI());
			}

			// all domains
			if (directDomainsUris.contains(Uris.THING_URI))
				allDomainsUris = new HashSet<String>(this.classes.keySet());
			else {
				for (OntResource domain : directDomains) {
					allDomains.add(domain);
					ontHandler.getChildren(domain, allDomains, true);
				}
				allDomainsUris = ontHandler.getResourcesUris(allDomains);
			}

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
					temp = new HashSet<String>();
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
			
			if (directRangesUris != null && directRangesUris.contains(Uris.THING_URI))
				directRangesUris.remove(Uris.THING_URI);

			temp  = propertyDirectRanges.get(property.getURI());
			if (temp == null)
				propertyDirectRanges.put(property.getURI(), directRangesUris);
			else 
				temp.addAll(directRangesUris);
			
			for (OntResource range : directRanges) {
				temp = directInObjectProperties.get(range.getURI());
				if (temp == null) {
					temp = new HashSet<String>();
					directInObjectProperties.put(range.getURI(), temp);
				}
				temp.add(property.getURI());
			}
			
			// all ranges
			if (directRangesUris.contains(Uris.THING_URI))
				allRangesUris = new HashSet<String>(this.classes.keySet());
			else {
				for (OntResource range : directRanges) {
					allRanges.add(range);
					ontHandler.getChildren(range, allRanges, true);
				}
				allRangesUris = ontHandler.getResourcesUris(allRanges);
			}
			
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
					temp = new HashSet<String>();
					indirectInObjectProperties.put(rangeUri, temp);
				}
				temp.add(property.getURI());
			}
				
			for (String domain : directDomainsUris) {
				for (String range : directRangesUris) {
					temp = 
						domainRangeToDirectProperties.get(domain + range);
					if (temp == null) {
						temp = new HashSet<String>();
						domainRangeToDirectProperties.put(domain + range, temp);
					}
					temp.add(property.getURI());
				}
			}

			for (String domain : allDomainsUris) {
				for (String range : allRangesUris) {
					if (directDomainsUris.contains(domain) && directRangesUris.contains(range)) continue;
					temp = domainRangeToIndirectProperties.get(domain + range);
					if (temp == null) {
						temp = new HashSet<String>();
						domainRangeToIndirectProperties.put(domain + range, temp);
					}
					temp.add(property.getURI());
				}
			}

		}	

	}
	
//	private void buildObjectPropertyDomainRangeMap() {
//		
//		for (String op : this.objectProperties.keySet()) {
//			
//			List<DomainRangePair> directDomainRangePairs = new ArrayList<DomainRangePair>();
//			List<DomainRangePair> indirectDomainRangePairs = new ArrayList<DomainRangePair>();
//			
//			this.domainRangePairsOfDirectProperties.put(op, directDomainRangePairs);
//			this.domainRangePairsOfIndirectProperties.put(op, indirectDomainRangePairs);
//			
//			List<String> directDomains = this.propertyDirectDomains.get(op);
//			List<String> directRanges = this.propertyDirectRanges.get(op);
//
//			List<String> indirectDomains = this.propertyIndirectDomains.get(op);
//			List<String> indirectRanges = this.propertyIndirectRanges.get(op);
//
//			// direct
//			if (directDomains != null && directRanges != null) { 
//				for (String d : directDomains)
//					for (String r : directRanges)
//						directDomainRangePairs.add(new DomainRangePair(d, r));
//			}
//			
//			// indirect
//			if (indirectDomains != null && indirectRanges != null) { 
//				for (String d : indirectDomains)
//					for (String r : indirectRanges) {
//						if (directDomains.contains(d) && directRanges.contains(r)) continue;
//						indirectDomainRangePairs.add(new DomainRangePair(d, r));
//					}
//			}
//
//		}
//	}
	
//	private void addPropertiesOfRDFVocabulary() {
//		
//		List<String> uris = new ArrayList<String>();
//		
//		uris.add(Uris.RDFS_LABEL_URI);
//		uris.add(Uris.RDFS_COMMENT_URI);
//		uris.add(Uris.RDF_VALUE_URI);
//		
//		HashSet<String> temp;
//		HashSet<String> temp1;
//		HashSet<String> temp2;
//		HashSet<String> temp3;
//		
//		ontHandler.getOntModel().setNsPrefix(Prefixes.RDF, Namespaces.RDF);
//		ontHandler.getOntModel().setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
//		
//		ontHandler.getOntModel().createDatatypeProperty(Uris.RDFS_COMMENT_URI);
//		ontHandler.getOntModel().createDatatypeProperty(Uris.RDFS_LABEL_URI);
//		ontHandler.getOntModel().createObjectProperty(Uris.RDF_VALUE_URI);
//
//		// add label, value, comment property to the properties of all the classes
//		for (String s : this.classes.keySet()) {
//			temp1 = indirectOutDataProperties.get(s);
//			temp2 = indirectOutObjectProperties.get(s);
//			temp3 = indirectInObjectProperties.get(s);
//			if (temp1 == null) {
//				temp1 = new HashSet<String>();
//				indirectOutDataProperties.put(s, temp1);
//			}
//			if (temp2 == null) {
//				temp2 = new HashSet<String>();
//				indirectOutObjectProperties.put(s, temp2);
//			}
//			if (temp3 == null) {
//				temp3 = new HashSet<String>();
//				indirectInObjectProperties.put(s, temp3);
//			}
//			temp1.add(Uris.RDFS_LABEL_URI);
//			temp1.add(Uris.RDFS_COMMENT_URI);
//			temp2.add(Uris.RDF_VALUE_URI);
//			temp3.add(Uris.RDF_VALUE_URI);
//		}
//
//		// add uris to properties hashmap
//		for (String uri : uris) {
//			temp = propertyIndirectDomains.get(uri);
//			if (temp == null) {
//				temp = new HashSet<String>();
//				propertyIndirectDomains.put(uri, temp);
//			}
//			for (String s : this.classes.keySet())
//				temp.add(s);
//		}
//
//		temp = propertyIndirectRanges.get(Uris.RDF_VALUE_URI);
//		if (temp == null) {
//			temp = new HashSet<String>();
//			propertyIndirectRanges.put(Uris.RDF_VALUE_URI, temp);
//		}
//		for (String s : this.classes.keySet())
//			temp.add(s);
//		
//	}
	
	private void updateMapsWithSubpropertyDefinitions() {
		
		
		Set<String> allSuperPropertiesLocal;

		// iterate over all properties
		for (String p : this.properties.keySet()) {
			
			allSuperPropertiesLocal = new HashSet<String>();
			
			Set<String> directSuperPropertiesLocal = this.directSuperProperties.get(p).keySet();
			Set<String> indirectSuperPropertiesLocal = this.indirectSuperProperties.get(p).keySet();
			if (directSuperPropertiesLocal != null) allSuperPropertiesLocal.addAll(directSuperPropertiesLocal);
			if (indirectSuperPropertiesLocal != null) allSuperPropertiesLocal.addAll(indirectSuperPropertiesLocal);
			
			if (allSuperPropertiesLocal.size() == 0) continue;
			
			HashSet<String> temp = null;
			
			HashSet<String> directDomains = this.propertyDirectDomains.get(p);
			HashSet<String> indirectDomains = this.propertyIndirectDomains.get(p);
			HashSet<String> allDomains = new HashSet<String>();
			if (directDomains != null) allDomains.addAll(directDomains);
			if (indirectDomains != null) allDomains.addAll(indirectDomains);

			HashSet<String> directRanges = this.propertyDirectRanges.get(p);
			HashSet<String> indirectRanges = this.propertyIndirectRanges.get(p);
			HashSet<String> allRanges = new HashSet<String>();
			if (directRanges != null) allRanges.addAll(directRanges);
			if (indirectRanges != null) allRanges.addAll(indirectRanges);

			for (String d : allDomains) {
				temp = indirectOutObjectProperties.get(d);
				if (temp == null) {
					temp = new HashSet<String>();
					indirectOutObjectProperties.put(d,  temp);
				}
				for (String superP : allSuperPropertiesLocal) {
					temp.add(superP);
				}
			}

			for (String r : allRanges) {
				temp = indirectInObjectProperties.get(r);
				if (temp == null) {
					temp = new HashSet<String>();
					indirectInObjectProperties.put(r,  temp);
				}
				for (String superP : allSuperPropertiesLocal) {
					temp.add(superP);
				}
			}

			for (String domain : allDomains) {
				for (String range : allRanges) {
					temp = domainRangeToIndirectProperties.get(domain + range);
					if (temp == null) {
						temp = new HashSet<String>();
						domainRangeToIndirectProperties.put(domain + range, temp);
					}
					for (String superP : allSuperPropertiesLocal) {
						if (superP.compareTo(p) != 0)
							temp.add(superP);
					}
				}
			}
		}

	}
	
// 	Please don't remove this commented method. We had this before to implement SubProperty inference, but later we changed our interpretation of SubProperty.
// 	It is better to keep that for a while.
//	/**
//	 * If the inheritance is true, it adds all the sub-classes of the domain and range of super-properties too.
//	 * @param inheritance
//	 */
//	private void updateMapsWithSubpropertyDefinitions(boolean inheritance) {
//		
//		
//		Set<String> allSuperPropertiesLocal;
//
//		// iterate over all properties
//		for (String p : this.properties.keySet()) {
//			
//			allSuperPropertiesLocal = new HashSet<String>();
//			
//			Set<String> directSuperPropertiesLocal = this.directSuperProperties.get(p).keySet();
//			Set<String> indirectSuperPropertiesLocal = this.indirectSuperProperties.get(p).keySet();
//			if (directSuperPropertiesLocal != null) allSuperPropertiesLocal.addAll(directSuperPropertiesLocal);
//			if (indirectSuperPropertiesLocal != null) allSuperPropertiesLocal.addAll(indirectSuperPropertiesLocal);
//			
//			if (allSuperPropertiesLocal.size() == 0) continue;
//			
//			HashSet<String> temp = null;
//			
//			// domains 
//			HashSet<String> seuperDirectDomains = new HashSet<String>();
//			HashSet<String> seuperIndirectDomains = new HashSet<String>();
//			List<String> superAllDomains = new ArrayList<String>();
//			for (String superP : allSuperPropertiesLocal) {
//				seuperDirectDomains = this.propertyDirectDomains.get(superP);
//				seuperIndirectDomains = this.propertyIndirectDomains.get(superP);
//				if (seuperDirectDomains != null) superAllDomains.addAll(seuperDirectDomains);
//				if (seuperIndirectDomains != null && inheritance) superAllDomains.addAll(seuperIndirectDomains);
//			}
//
//			HashSet<String> indirectDomains = propertyIndirectDomains.get(p);
//			if (indirectDomains == null) {
//				indirectDomains = new HashSet<String>();
//				propertyIndirectDomains.put(p, indirectDomains);
//			}
//			
//			for (String d : superAllDomains) {
//				if (!indirectDomains.contains(d))
//					indirectDomains.add(d);
//			}
//			
//			if (this.objectProperties.containsKey(p)) 
//				for (String d : superAllDomains) {
//					temp = indirectOutObjectProperties.get(d);
//					if (temp != null)
//						temp.add(p);
//				}
//
//			if (this.dataProperties.containsKey(p)) 
//				for (String d : superAllDomains) {
//					temp = indirectOutDataProperties.get(d);
//					if (temp != null)
//						temp.add(p);
//				}
//
//			// ranges
//			HashSet<String> seuperDirectRanges = new HashSet<String>();
//			HashSet<String> seuperIndirectRanges = new HashSet<String>();
//			HashSet<String> superAllRanges = new HashSet<String>();
//			for (String superP : allSuperPropertiesLocal) {
//				seuperDirectRanges = this.propertyDirectRanges.get(superP);
//				seuperIndirectRanges = this.propertyIndirectRanges.get(superP);
//				if (seuperDirectRanges != null) superAllRanges.addAll(seuperDirectRanges);
//				if (seuperIndirectRanges != null && inheritance) superAllRanges.addAll(seuperIndirectRanges);
//			}
//
//			HashSet<String> indirectRanges = propertyIndirectRanges.get(p);
//			if (indirectRanges == null) {  
//				indirectRanges = new HashSet<String>();
//				propertyIndirectRanges.put(p, indirectRanges);
//			}
//			
//			for (String r : superAllRanges) {
//				if (!indirectRanges.contains(r))
//					indirectRanges.add(r);
//			}
//			
//			if (this.objectProperties.containsKey(p)) 
//				for (String r : superAllRanges) {
//					temp = indirectInObjectProperties.get(r);
//					if (temp != null)
//						temp.add(p);
//				}
//				
//			HashSet<String> directDomains = this.propertyDirectDomains.get(p);
//			HashSet<String> allDomains = new HashSet<String>();
//			HashSet<String> directRanges = this.propertyDirectRanges.get(p);
//			HashSet<String> allRanges = new HashSet<String>();
//			
//			if (directDomains != null) allDomains.addAll(directDomains);
//			if (indirectDomains != null) allDomains.addAll(indirectDomains);
//
//			if (directRanges != null) allRanges.addAll(directRanges);
//			if (indirectRanges != null) allRanges.addAll(indirectRanges);
//			
//			for (String domain : allDomains) {
//				for (String range : allRanges) {
//					if (directDomains.contains(domain) && directRanges.contains(range)) continue;
//					temp = domainRangeToIndirectProperties.get(domain + range);
//					if (temp == null) {
//						temp = new HashSet<String>();
//						domainRangeToIndirectProperties.put(domain + range, temp);
//					}
//					temp.add(p);
//				}
//			}
//		}
//
//	}

	private void classifyProperties() {

		boolean haveDomain;
		boolean haveRange;
		Label label;
		
		HashSet<String> directDomains;
		HashSet<String> indirectDomains;
		HashSet<String> directRanges;
		HashSet<String> indirectRanges;
		
		for (String p : this.dataProperties.keySet()) {
			
			label = this.dataProperties.get(p);
			
			directDomains = propertyDirectDomains.get(p);
			indirectDomains = propertyIndirectDomains.get(p);

			haveDomain = true;
			
			if ((directDomains == null || directDomains.size() == 0) &&
					(indirectDomains == null || indirectDomains.size() == 0))
				haveDomain = false;
			
			if (directDomains != null && directDomains.size() == 1 &&
					directDomains.iterator().next().equalsIgnoreCase(Uris.THING_URI))
				haveDomain = false;
			
			if (!haveDomain)
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
			
			if ((directRanges == null || directRanges.size() == 0) &&
					(indirectRanges == null || indirectRanges.size() == 0))
				haveRange = false;
			
			if (haveDomain && !haveRange) 
				this.objectPropertiesWithOnlyDomain.put(p, label);
			else if (!haveDomain && haveRange) {
				this.objectPropertiesWithOnlyRange.put(p, label);
			}
			else if (!haveDomain && !haveRange) {
				if (!p.startsWith(Namespaces.RDF) && !p.startsWith(Namespaces.RDFS))
					this.objectPropertiesWithoutDomainAndRange.put(p, label);
			}
		}
}
	
//	private void buildConnectivityMaps() {
//		
//		List<String> classList = new ArrayList<String>(this.classes.keySet());
//		
//		HashSet<String> directProperties;
//		HashSet<String> indirectProperties;
//		HashSet<String> directOutProperties;
//		HashSet<String> directInProperties;
//		HashSet<String> indirectOutProperties;
//		HashSet<String> indirectInProperties;
//		boolean foundDomainlessProperty;
//		boolean foundRangelessProperty;
//		
//		int count = 0;
//		for (int i = 0; i < classList.size(); i++) {
//			String c1 = classList.get(i);
//			for (int j = 0; j < classList.size(); j++) {
//				String c2 = classList.get(j);
//				
//				count ++;
//				if (count % 1000000 == 0 ) {
//					System.gc();
//				}
////				if (c1.equals(c2))
////					continue;
//				
//				directProperties = this.domainRangeToDirectProperties.get(c1+c2);
//				if (directProperties != null && directProperties.size() > 0) { 
//					this.connectedByDirectProperties.add(c1+c2);
//				}
//				indirectProperties = this.domainRangeToIndirectProperties.get(c1+c2);
//				if (indirectProperties != null && indirectProperties.size() > 0) { 
//					this.connectedByIndirectProperties.add(c1+c2);
//				}
//				
//				// domainless property
//				
//				foundDomainlessProperty = false;
//				directInProperties = this.directInObjectProperties.get(c1);
//				if (directInProperties != null) {
//					for (String s : directInProperties) 
//						if (this.objectPropertiesWithOnlyRange.containsKey(s)) {
//							foundDomainlessProperty = true;
//							break;
//						}
//				}
//				if (!foundDomainlessProperty) {
//					directInProperties = this.directInObjectProperties.get(c2);
//					if (directInProperties != null)
//						for (String s : directInProperties) 
//							if (this.objectPropertiesWithOnlyRange.containsKey(s)) {
//								foundDomainlessProperty = true;
//								break;
//							}			
//				}
//				if (!foundDomainlessProperty) {
//					indirectInProperties = this.indirectInObjectProperties.get(c1);
//					if (indirectInProperties != null)
//						for (String s : indirectInProperties) 
//							if (this.objectPropertiesWithOnlyRange.containsKey(s)) {
//								foundDomainlessProperty = true;
//								break;
//							}
//				}
//				if (!foundDomainlessProperty) {
//					indirectInProperties = this.indirectInObjectProperties.get(c2);
//					if (indirectInProperties != null)
//						for (String s : indirectInProperties) 
//							if (this.objectPropertiesWithOnlyRange.containsKey(s)) {
//								foundDomainlessProperty = true;
//								break;
//							}
//				}
//				
//				// rangeless property
//				
//				foundRangelessProperty = false;
//				directOutProperties = this.directOutObjectProperties.get(c1);
//				if (directOutProperties != null) {
//					for (String s : directOutProperties) 
//						if (this.objectPropertiesWithOnlyDomain.containsKey(s)) {
//							foundRangelessProperty = true;
//							break;
//						}
//				}
//				if (!foundRangelessProperty) {
//					directOutProperties = this.directOutObjectProperties.get(c2);
//					if (directOutProperties != null)
//						for (String s : directOutProperties) 
//							if (this.objectPropertiesWithOnlyDomain.containsKey(s)) {
//								foundRangelessProperty = true;
//								break;
//							}
//				}
//				if (!foundRangelessProperty) {
//					indirectOutProperties = this.indirectOutObjectProperties.get(c1);
//					if (indirectOutProperties != null)
//						for (String s : indirectOutProperties) 
//							if (this.objectPropertiesWithOnlyDomain.containsKey(s)) {
//								foundRangelessProperty = true;
//								break;
//							}
//				}
//				if (!foundRangelessProperty) {
//					indirectOutProperties = this.indirectOutObjectProperties.get(c2);
//					if (indirectOutProperties != null)
//						for (String s : indirectOutProperties) 
//							if (this.objectPropertiesWithOnlyDomain.containsKey(s)) {
//								foundRangelessProperty = true;
//								break;
//							}
//				}				
//				
//				if (foundRangelessProperty) { 
//					this.connectedByRangelessProperties.add(c1+c2);
//				}
//				if (foundDomainlessProperty) { 
//					this.connectedByDomainlessProperties.add(c1+c2);
//				}
//			}
//		}
//	}
}
