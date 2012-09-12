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

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class OntologyCache {
	
	static Logger logger = Logger.getLogger(OntologyCache.class.getName());
	
	private OntologyManager ontologyManager = null;

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
	
	public OntologyCache() {
	}

	public void init(OntologyManager ontologyManager) {

		this.ontologyManager = ontologyManager;
		
		directOutDataProperties = new HashMap<String, List<String>>();
		indirectOutDataProperties = new HashMap<String, List<String>>();
		directOutObjectProperties = new HashMap<String, List<String>>();
		indirectOutObjectProperties = new HashMap<String, List<String>>();
		directInObjectProperties = new HashMap<String, List<String>>();
		indirectInObjectProperties = new HashMap<String, List<String>>();
		
		propertyDirectDomains = new HashMap<String, List<String>>();
		propertyIndirectDomains = new HashMap<String, List<String>>();
		propertyDirectRanges = new HashMap<String, List<String>>();
		propertyIndirectRanges = new HashMap<String, List<String>>();
		
		directDomainRangeProperties = new HashMap<String, List<String>>();
		indirectDomainRangeProperties = new HashMap<String, List<String>>();
		
		long start = System.currentTimeMillis();
		fillDataPropertiesHashMaps();
		fillObjectPropertiesHashMaps();
		updateMapsWithSubpropertyDefinitions(false);
		addPropertiesOfRDFVocabulary();
		float elapsedTimeSec = (System.currentTimeMillis() - start)/1000F;
		logger.info("time to build ontology cache: " + elapsedTimeSec);
	}

	
	private void fillObjectPropertiesHashMaps() {
		
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		List<String> temp; 
		
		ExtendedIterator<OntProperty> itrOP = ontologyManager.getOntModel().listAllOntProperties();
//		ExtendedIterator<ObjectProperty> itrOP = ontologyManager.getOntModel().listObjectProperties();
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
//			System.out.println("OP:" + op.getURI());
			
			// getting domains and subclasses
			ExtendedIterator<? extends OntResource> itrDomains = op.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontologyManager.getMembers(d, directDomains, false);
			}

			temp  = propertyDirectDomains.get(op.getURI());
			if (temp == null)
				propertyDirectDomains.put(op.getURI(), ontologyManager.getResourcesURIs(directDomains));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(directDomains));
			
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
				ontologyManager.getChildren(directDomains.get(i), allDomains, true);
			}

			temp  = propertyIndirectDomains.get(op.getURI());
			if (temp == null)
				propertyIndirectDomains.put(op.getURI(), ontologyManager.getResourcesURIs(allDomains));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(allDomains));
			
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
				ontologyManager.getMembers(r, directRanges, false);
			}

			temp  = propertyDirectRanges.get(op.getURI());
			if (temp == null)
				propertyDirectRanges.put(op.getURI(), ontologyManager.getResourcesURIs(directRanges));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(directRanges));
			
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
				ontologyManager.getChildren(directRanges.get(i), allRanges, true);
			}
			
			temp  = propertyIndirectRanges.get(op.getURI());
			if (temp == null)
				propertyIndirectRanges.put(op.getURI(), ontologyManager.getResourcesURIs(allRanges));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(allRanges));
			
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
	
	private void fillDataPropertiesHashMaps() {
		
		List<OntResource> directDomains = new ArrayList<OntResource>();
		List<OntResource> allDomains = new ArrayList<OntResource>();
		List<OntResource> directRanges = new ArrayList<OntResource>();
		List<OntResource> allRanges = new ArrayList<OntResource>();
		List<String> temp; 
		
		ExtendedIterator<OntProperty> itrDP = ontologyManager.getOntModel().listAllOntProperties();
//		ExtendedIterator<DatatypeProperty> itrDP = ontologyManager.getOntModel().listDatatypeProperties();
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
//			System.out.println("DP:" + dp.getURI());

			// getting domains and subclasses
			ExtendedIterator<? extends OntResource> itrDomains = dp.listDomain();
			while (itrDomains.hasNext()) {
				d = itrDomains.next();
				ontologyManager.getMembers(d, directDomains, false);
			}

			temp  = propertyDirectDomains.get(dp.getURI());
			if (temp == null)
				propertyDirectDomains.put(dp.getURI(), ontologyManager.getResourcesURIs(directDomains));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(directDomains));
			
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
				ontologyManager.getChildren(directDomains.get(i), allDomains, true);
			}

			temp  = propertyIndirectDomains.get(dp.getURI());
			if (temp == null)
				propertyIndirectDomains.put(dp.getURI(), ontologyManager.getResourcesURIs(allDomains));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(allDomains));
			
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
				ontologyManager.getMembers(r, directRanges, false);
			}

			temp  = propertyDirectRanges.get(dp.getURI());
			if (temp == null)
				propertyDirectRanges.put(dp.getURI(), ontologyManager.getResourcesURIs(directRanges));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(directRanges));
			
			for (int i = 0; i < directRanges.size(); i++) {
				allRanges.add(directRanges.get(i));
				ontologyManager.getChildren(directRanges.get(i), allRanges, true);
			}
			
			temp  = propertyIndirectRanges.get(dp.getURI());
			if (temp == null)
				propertyIndirectRanges.put(dp.getURI(), ontologyManager.getResourcesURIs(allRanges));
			else 
				temp.addAll(ontologyManager.getResourcesURIs(allRanges));
			
		}		
	}
	
	private void addPropertiesOfRDFVocabulary() {
		String labelUri = "http://www.w3.org/2000/01/rdf-schema#label";
		String commentUri = "http://www.w3.org/2000/01/rdf-schema#comment";
		List<String> temp;
		
		ontologyManager.getOntModel().setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		ontologyManager.getOntModel().createDatatypeProperty(labelUri);
		ontologyManager.getOntModel().createDatatypeProperty(commentUri);

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

	}
	
	/**
	 * If the inheritance is true, it adds all the sub-classes of the domain and range of super-properties too.
	 * @param inheritance
	 */
	private void updateMapsWithSubpropertyDefinitions(boolean inheritance) {
		
		
		// iterate over all properties
		for (String p : propertyDirectDomains.keySet()) {
			
			List<String> superProperties = ontologyManager.getSuperProperties(p, true);
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
			
			if (ontologyManager.isObjectProperty(p)) 
				for (String d : allDomains) {
					temp = indirectOutObjectProperties.get(d);
					if (temp != null && temp.indexOf(p) == -1)
						temp.add(p);
				}

			if (ontologyManager.isDataProperty(p)) 
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
			
			if (ontologyManager.isObjectProperty(p)) 
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
