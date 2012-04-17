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

package edu.isi.karma.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.isi.karma.modeling.alignment.Name;

public class SourceLoader {

	private static final int DEFAULT_SOURCE_RESULTS_SIZE = 10;

	static Logger logger = Logger.getLogger(SourceLoader.class);

	
	public static void deleteSourceByUri(String uri) {
		Repository.Instance().clearNamedModel(uri);
	}
	
	public static Source getSourceByUri(String uri) {
		
		Model m = Repository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		Source source = getSourceFromJenaModel(m);
		return source;
	}
	
	/**
	 * returns the source id, name, address of all sources in the repository
	 * @param limit : maximum number of results, null value means all the sources
	 * @return
	 */
	public static List<Source> getAllSources(Integer sourceLimit) {
		
		List<Source> sourceList = new ArrayList<Source>();
		
		Model model = Repository.Instance().getModel();
		
		String source_id = "";
		String source_name = "";
		
		// Create a new query
		String queryString =
			"PREFIX " + Prefixes.KARMA + ": <" + Namespaces.KARMA + "> \n" +
			"SELECT ?s ?name \n" +
			"WHERE { \n" +
			"      ?s a " + Prefixes.KARMA + ":Source . \n" +
			"      OPTIONAL {?s " + Prefixes.KARMA + ":hasName ?name .} \n" +
			"      } \n";
		
		if (sourceLimit != null) {
			if (sourceLimit.intValue() < 0) sourceLimit = DEFAULT_SOURCE_RESULTS_SIZE;
			queryString += "LIMIT " + String.valueOf(sourceLimit.intValue() + "\n");
		}
		
		logger.debug("query= \n" + queryString);
		
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet results = qexec.execSelect() ;
			
			if (!results.hasNext())
				logger.info("query does not return any answer.");

//			ResultSetFormatter.out(System.out, results, query) ;
			 
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				
				RDFNode s = soln.get("s") ;       // Get a result variable by name.
				RDFNode name = soln.get("name") ;       // Get a result variable by name.

				if (s == null) {
					logger.info("source id is null.");
					continue;
				}

				String source_uri = s.toString();
				source_id = source_uri.substring(source_uri.lastIndexOf("/") + 1, source_uri.length() - 1);

				logger.debug("source uri: " + source_uri);
				logger.debug("source id: " + source_id);
				if (name != null && name.isLiteral()) source_name = name.asLiteral().getString();
				logger.debug("source name: " + source_name);
				
				if (source_id.trim().length() > 0)
					sourceList.add(new Source(source_id, source_name));
				else
					logger.info("length of source id is zero.");
			}
			
			return sourceList;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		} finally { 
			qexec.close() ; 
		}
	}
	
	/**
	 * returns all the sources in the repository along with their complete information
	 * probably this method is not useful since fetching all the sources with their complete
	 * information takes long time.
	 * @return
	 */
	public static List<Source> getAllSourcesComplete(Integer sourceLimit) {

		List<Source> sourceList = getAllSources(sourceLimit);
		List<Source> sourceListCompleteInfo = new ArrayList<Source>();
		for (Source s : sourceList) {
			sourceListCompleteInfo.add(getSourceByUri(s.getUri()));
		}
		return sourceListCompleteInfo;
	}
	
	/**
	 * Returns all the sources in the repository whose model matches the semantic model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the source input or source output will be tested for matching
	 * @param sourceLimit maximum number of sources that will be fetched
	 * @return
	 */
	public static List<Source> getSourcesByIOPattern(edu.isi.karma.service.Model semanticModel, Integer sourceLimit,
			Map<String, String> modelToMatchedSourceParameterMapping) {
		
		List<String> sourceIds = getSourcesIdsByIOPattern(semanticModel, sourceLimit,
				modelToMatchedSourceParameterMapping);
		
		for (String arg : modelToMatchedSourceParameterMapping.keySet())
			System.out.println(arg + "*****" + modelToMatchedSourceParameterMapping.get(arg));
		
		if (sourceIds == null)
			return null;
		
		List<Source> sourceList = new ArrayList<Source>();
		
		for (String uri : sourceIds) {
			Model m = Repository.Instance().getNamedModel(uri);
			if (m != null)
				sourceList.add(getSourceFromJenaModel(m));
		}
		return sourceList;
	}
	
	/**
	 * Returns a hash map of all sources whose model matches the semantic model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the source input or source output will be tested for matching
	 * @param sourceLimit maximum number of sources that will be fetched
	 * @return
	 */
	private static List<String> getSourcesIdsByIOPattern(edu.isi.karma.service.Model semanticModel, 
			Integer sourceLimit, Map<String, String> modelToMatchedSourceParameterMapping) {

		List<String> sourceIds = new ArrayList<String>();

		if (semanticModel == null || semanticModel.getAtoms() == null 
				|| semanticModel.getAtoms().size() == 0) {
			logger.info("The input model is nul or it does not have any atom");
			return null;
		}
	
		// map of NS --> Prefix
		Map<String, String> nsToPrefixMapping = new HashMap<String, String>();
		nsToPrefixMapping.put(Namespaces.KARMA, Prefixes.KARMA);
		nsToPrefixMapping.put(Namespaces.SWRL, Prefixes.SWRL);
		
		String select_header = "SELECT ?s ";
		String select_where =
			"WHERE { \n" +
			"      ?s a " + Prefixes.KARMA + ":Source . \n" +
			"      ?s " + Prefixes.KARMA + ":hasModel ?model . \n" + 
			"      ?model a " + Prefixes.KARMA + ":Model . \n";
		
		String atomVar = "";
		String predicateUri = "";
		String argument1 = "";
		String argument2 = "";
		String argument1Var = "";
		String argument2Var = "";

		for (int i = 0; i < semanticModel.getAtoms().size(); i++) {
			Atom atom = semanticModel.getAtoms().get(i);
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = ((ClassAtom)atom);
					atomVar = "?atom" + String.valueOf(i+1);
					predicateUri = classAtom.getClassPredicate().getUri();
					argument1 = classAtom.getArgument1().getId();
					argument1Var = "?" + argument1;
					
					if (modelToMatchedSourceParameterMapping.get(argument1) == null) {
						modelToMatchedSourceParameterMapping.put(argument1, "");
						select_header += argument1Var + " ";
					}
					select_where += 
						"      ?model " + Prefixes.KARMA + ":hasAtom " + atomVar + " . \n" +
						"      " + atomVar + " a " + Prefixes.SWRL + ":ClassAtom . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":classPredicate <" + predicateUri + "> . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument1 " + argument1Var + " . \n";
				}
				else if (atom instanceof PropertyAtom) {
					PropertyAtom propertyAtom = ((PropertyAtom)atom);
					atomVar = "?atom" + String.valueOf(i+1);
					predicateUri = propertyAtom.getPropertyPredicate().getUri();
					argument1 = propertyAtom.getArgument1().getId();
					argument2 = propertyAtom.getArgument2().getId();
					argument1Var = "?" + argument1;
					argument2Var = "?" + argument2;

					
					if (modelToMatchedSourceParameterMapping.get(argument1) == null) {
						modelToMatchedSourceParameterMapping.put(argument1, "");
						select_header += argument1Var + " ";
					}
					if (modelToMatchedSourceParameterMapping.get(argument2) == null) {
						modelToMatchedSourceParameterMapping.put(argument2, "");
						select_header += argument2Var + " ";
					}
					
					select_where += 
						"      ?model " + Prefixes.KARMA + ":hasAtom " + atomVar + " . \n" +
						"      " + atomVar + " a " + Prefixes.SWRL + ":IndividualPropertyAtom . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":propertyPredicate <" + predicateUri + "> . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument1 " + argument1Var + " . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument2 " + argument2Var + " . \n";
				}			
			}
		}		
		select_where += 	"      } \n";
 
		String prefix = getSPARQLHeader(nsToPrefixMapping);
		String select = select_header + " \n " + select_where;
		String queryString = prefix + select;
		
		if (sourceLimit != null) {
			if (sourceLimit.intValue() < 0) sourceLimit = DEFAULT_SOURCE_RESULTS_SIZE;
			queryString += "LIMIT " + String.valueOf(sourceLimit.intValue() + "\n");
		}
		
		logger.debug("query= \n" + queryString);
		
		Model model = Repository.Instance().getModel();
		Query query = QueryFactory.create(queryString);
//		// Execute the query and obtain results
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet results = qexec.execSelect() ;
			
			if (!results.hasNext()) {
				logger.info("query does not return any answer.");
				modelToMatchedSourceParameterMapping = null;
				return sourceIds;
			}

//			ResultSetFormatter.out(System.out, results, query) ;
			 
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				
				RDFNode s = soln.get("s") ;       // Get a result variable by name.

				if (s == null) {
					logger.info("source uri is null.");
					continue;
				}

				String source_uri = s.toString();
				logger.debug("source uri: " + source_uri);
				sourceIds.add(source_uri);
				
				for (String arg : modelToMatchedSourceParameterMapping.keySet()) {
					RDFNode argNode = soln.get(arg) ;
					if (argNode != null && argNode.isResource()) {
						String retrieved_id = argNode.asResource().getLocalName();
						modelToMatchedSourceParameterMapping.put(arg, retrieved_id);
					}
				}
			}
			
			return sourceIds;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		} finally { 
			qexec.close() ; 
		}
	}

	private static String getSPARQLHeader(Map<String, String> nsToPrefixMapping) {
		String prefixHeader = "";
		
		for (String ns : nsToPrefixMapping.keySet()) {
			String prefix = nsToPrefixMapping.get(ns);
			if (prefix != null)
				prefixHeader += "PREFIX " + prefix + ": <" + ns + "> \n";
		}

		return prefixHeader;
	}
	
	/**
	 * From the source model, returns the source object
	 * If the list of operation ids is null, it includes all the operations, otherwise it only 
	 * fetches the mentioned operations
	 * If operationLimit=null and operationIds=null returns all operations
	 * If operationLimit=null, it returns the all specified operations
	 * If operationIds=null, it returns the first (opLimit) number of operations
	 * If both of them are not null, it returns the first (opLimit) number of specified operations 
	 * @param model
	 * @param operationIds
	 * @return
	 */
	public static Source getSourceFromJenaModel(Model model) {
		logger.debug("model size: " + model.getGraph().size());
		
		String source_name = "";
		String source_uri = "";
		String source_id = "";
		
		// source id
		source_uri = model.getNsPrefixURI("");
		logger.debug("source uri: " + source_uri);
		
		// source local id
		source_id = source_uri.substring(source_uri.lastIndexOf("/") + 1, source_uri.length() - 1);
		logger.debug("source id: " + source_id);

		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		
		Resource source_resource = model.getResource(source_uri);
		
		NodeIterator nodeIterator = null;
		RDFNode node = null;

		// source name
		nodeIterator = model.listObjectsOfProperty(source_resource, has_name_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			source_name = node.asLiteral().getString();
			logger.debug("source name: " + source_name);
		} else
			logger.debug("source does not have a name.");
		
		Source source = new Source(source_id);
		source.setName(source_name);
	 	source.setAttributes(getAttributes(model, source_resource));
	 	source.setModel(getSemanticModel(model, source_resource));
		
		return source;
	}
	
	private static List<Attribute> getAttributes(Model model, Resource source_resource) {
		
		Property has_attribute_property = model.getProperty(Namespaces.KARMA + "hasAttribute");
	
		List<Attribute> attList = new ArrayList<Attribute>();
		
		NodeIterator nodeIterator = null;
		RDFNode node = null;

		// hasAttribute
		nodeIterator = model.listObjectsOfProperty(source_resource, has_attribute_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasAttribute property is not a resource.");
				continue;
			}
			
			attList.add(getSingleAttribute(model, node.asResource()));
		}
		
		return attList;

	}
	
	private static Attribute getSingleAttribute(Model model, Resource att_resource) {
		
		String att_id = "";
		String att_name = "";

		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		
		// attribute id
		att_id = att_resource.getLocalName();
		logger.debug("attribute id: " + att_id);

		NodeIterator nodeIterator = null;
		RDFNode node = null;
		
		// attribute name
		nodeIterator = model.listObjectsOfProperty(att_resource, has_name_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			att_name = node.asLiteral().getString();
			logger.debug("attribute name: " + att_name);
		} else
			logger.debug("attribute does not have a name.");
		
		Attribute att = new Attribute(att_id, att_resource.getNameSpace(), att_name, IOType.NONE, AttributeRequirement.NONE);
		
		return att;

	}
	
	private static edu.isi.karma.service.Model getSemanticModel(Model model, Resource source_resource) {
		
		Property has_model_property = model.getProperty(Namespaces.KARMA + "hasModel");
		Property has_atom_property = model.getProperty(Namespaces.KARMA + "hasAtom");
		
		NodeIterator nodeIterator = null;
		RDFNode modelNode = null;
		RDFNode atomNode = null;

		// hasModel
		nodeIterator = model.listObjectsOfProperty(source_resource, has_model_property);
		if (!nodeIterator.hasNext() || !(modelNode = nodeIterator.next()).isResource()) {
			logger.info("There is no model resource.");
			return null;
		}

		edu.isi.karma.service.Model semanticModel = 
			new edu.isi.karma.service.Model(modelNode.asResource().getLocalName());
		List<Atom> atoms = new ArrayList<Atom>();
		

		// hasAtom
		nodeIterator = model.listObjectsOfProperty(modelNode.asResource(), has_atom_property);
		while ( nodeIterator.hasNext()) {
			atomNode = nodeIterator.next();
			
			if (!atomNode.isResource()) {
				logger.info("object of the hasAtom property is not a resource.");
				continue;
			}
			
			atoms.add(getAtom(model, atomNode.asResource()));
		}

		semanticModel.setAtoms(atoms);
		return semanticModel;

	}
	
	private static Atom getAtom(Model model, Resource atom_resource) {
		
		Property rdf_type = model.getProperty(Namespaces.RDF + "type");

		NodeIterator nodeIterator = null;
		RDFNode node = null;

		String classAtomUri = Namespaces.SWRL + "ClassAtom";
		String propertyAtomUri = Namespaces.SWRL + "IndividualPropertyAtom";
		
		// atom type
		nodeIterator = model.listObjectsOfProperty(atom_resource, rdf_type);
		if (!nodeIterator.hasNext() || !(node = nodeIterator.next()).isResource()) {
			logger.info("The atom type is not specified.");
			return null;
		}
		
		if (node.asResource().getURI().equalsIgnoreCase(classAtomUri)) {
			logger.debug("The atom is a ClassAtom");
			return getClassAtom(model, atom_resource);
		}
		else if (node.asResource().getURI().equalsIgnoreCase(propertyAtomUri)) {
			logger.debug("The atom is an IndividualPropertyAtom");
			return getPropertyAtom(model, atom_resource);
		}
		
		return null;

	}
	
	private static ClassAtom getClassAtom(Model model, Resource atom_resource) {
		
		String predicateUri = "";
		String predicatePrefix = "";
		String predicateNs = "";
		
		String argument1Id = "";
		String argument1Type = "";
		
		Resource attribute = ResourceFactory.createResource(Namespaces.KARMA + "Attribute");
		Resource variable = ResourceFactory.createResource(Namespaces.SWRL + "Variable");

		Property class_predicate_property = model.getProperty(Namespaces.SWRL + "classPredicate");
		Property argument1_property = model.getProperty(Namespaces.SWRL + "argument1");


		NodeIterator nodeIterator = null;
		RDFNode node = null;

		// atom class predicate
		nodeIterator = model.listObjectsOfProperty(atom_resource, class_predicate_property);
		if (!nodeIterator.hasNext() || !(node = nodeIterator.next()).isResource()) {
			logger.info("The class predicate resource is not specified.");
			return null;
		}
		
		predicateUri = node.asResource().getURI();
		logger.debug("The atom predicate is: " + predicateUri);
		predicateNs = node.asResource().getNameSpace();
		predicatePrefix = model.getNsURIPrefix(predicateNs);
		
		// atom argument1 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument1_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument1Id = node.asResource().getLocalName();
			logger.debug("The atom argument1 is: " + argument1Id);
			
			if (isInstanceOfTheClass(node.asResource(), attribute))
				argument1Type = ArgumentType.ATTRIBUTE;
			else if (isInstanceOfTheClass(node.asResource(), variable))
				argument1Type = ArgumentType.VARIABLE;
			
		} else {
			logger.info("atom does not have an argument1.");
			return null;
		}
		
		Name predicateName = new Name(predicateUri, predicateNs, predicatePrefix);
		Argument arg1 = new Argument(argument1Id, argument1Id, argument1Type);
		
		ClassAtom classAtom = new ClassAtom(predicateName, arg1);

		return classAtom;

	}
	
	private static PropertyAtom getPropertyAtom(Model model, Resource atom_resource) {
		
		String predicateUri = "";
		String predicatePrefix = "";
		String predicateNs = "";
		
		String argument1Id = "";
		String argument2Id = ""; 

		String argument1Type = "";
		String argument2Type = ""; 
		
		Resource attribute = ResourceFactory.createResource(Namespaces.KARMA + "Attribute");
		Resource variable = ResourceFactory.createResource(Namespaces.SWRL + "Variable");

		Property property_predicate_property = model.getProperty(Namespaces.SWRL + "propertyPredicate");
		Property argument1_property = model.getProperty(Namespaces.SWRL + "argument1");
		Property argument2_property = model.getProperty(Namespaces.SWRL + "argument2");

		NodeIterator nodeIterator = null;
		RDFNode node = null;

		// atom class predicate
		nodeIterator = model.listObjectsOfProperty(atom_resource, property_predicate_property);
		if (!nodeIterator.hasNext() || !(node = nodeIterator.next()).isResource()) {
			logger.info("The property predicate resource is not specified.");
			return null;
		}
		
		predicateUri = node.asResource().getURI();
		logger.debug("The atom predicate is: " + predicateUri);
		predicateNs = node.asResource().getNameSpace();
		predicatePrefix = model.getNsURIPrefix(predicateNs);
		
		// atom argument1 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument1_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument1Id = node.asResource().getLocalName();
			logger.debug("The atom argument1 is: " + argument1Id);
			
			if (isInstanceOfTheClass(node.asResource(), attribute))
				argument1Type = ArgumentType.ATTRIBUTE;
			else if (isInstanceOfTheClass(node.asResource(), variable))
				argument1Type = ArgumentType.VARIABLE;
			
		} else {
			logger.info("atom does not have an argument1.");
			return null;
		}

		// atom argument2 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument2_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument2Id = node.asResource().getLocalName();
			logger.debug("The atom argument2 is: " + argument2Id);
			
			if (isInstanceOfTheClass(node.asResource(), attribute))
				argument2Type = ArgumentType.ATTRIBUTE;
			else if (isInstanceOfTheClass(node.asResource(), variable))
				argument2Type = ArgumentType.VARIABLE;
			
		} else {
			logger.info("atom does not have an argument2.");
			return null;
		}
		
		Name predicateName = new Name(predicateUri, predicateNs, predicatePrefix);
		Argument arg1 = new Argument(argument1Id, argument1Id, argument1Type);
		Argument arg2 = new Argument(argument2Id, argument2Id, argument2Type);
		
		PropertyAtom propertyAtom = new PropertyAtom(predicateName, arg1, arg2);

		return propertyAtom;	
	}
	
	private static boolean isInstanceOfTheClass(Resource resource, Resource class_resource) {
		Property type_property = ResourceFactory.createProperty(Namespaces.RDF + "type");
		
		if (resource == null || !resource.isResource())
			return true;
		
		if (resource.hasProperty(type_property, class_resource))
			return true;
		else
			return false;
	}
	
	private static void testGetSourceByUri() {
		String uri = "http://isi.edu/integration/karma/sources/AEEA5A9A-744C-8096-B372-836ACC820D5A#";
//		String uri = "http://isi.edu/integration/karma/sources/940466A8-8733-47B1-2597-11DC112F0437F#";
		Source source = SourceLoader.getSourceByUri(uri);
		if (source != null) source.print();
	}
	private static void testGetAllSources() {
		List<Source> sourceList = getAllSourcesComplete(null);
		for (Source s : sourceList) {
			if (s != null) s.print();
		}
	}
	private static void testGetSourcesByIOPattern() {
		edu.isi.karma.service.Model semanticModel = new edu.isi.karma.service.Model(null);

		String geonamesOntology = "http://www.geonames.org/ontology#";
		String wgs84Ontology = "http://www.w3.org/2003/01/geo/wgs84_pos#";
		
		Name featurePredicatName = new Name(geonamesOntology + "Feature", geonamesOntology, "gn");
		Name latPredicatName = new Name(wgs84Ontology + "lat", wgs84Ontology, "wgs84");
		Name lngPredicatName = new Name(wgs84Ontology + "long", wgs84Ontology, "wgs84");
		
		ClassAtom c1 = new ClassAtom(featurePredicatName, new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE));
		PropertyAtom p1 = new PropertyAtom(latPredicatName,
				new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE), 
				new Argument("arg2", "arg2", ArgumentType.ATTRIBUTE));
		PropertyAtom p2 = new PropertyAtom(lngPredicatName,
				new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE), 
				new Argument("arg3", "arg3", ArgumentType.ATTRIBUTE));
//		ClassAtom c2 = new ClassAtom(featurePredicatName, new Argument("arg2", "arg2", ArgumentType.ATTRIBUTE));

		semanticModel.getAtoms().add(c1);
		semanticModel.getAtoms().add(p1);
		semanticModel.getAtoms().add(p2);
//		semanticModel.getAtoms().add(c2);
		
		Map<String, String> modelToMatchedSourceParameterMapping =
			new HashMap<String, String>();
		List<Source> sourceList = getSourcesByIOPattern(semanticModel, null, modelToMatchedSourceParameterMapping);
		for (Source s : sourceList) {
			if (s != null) s.print();
		}
	}
	private static void testDeleteSourceByUri() {
		String uri = "http://isi.edu/integration/karma/sources/AEEA5A9A-744C-8096-B372-836ACC820D5A#";
		SourceLoader.deleteSourceByUri(uri);
	}
	public static void main(String[] args) {

//		SourceBuilder.main(new String[0]);

		boolean test1 = false, test2 = false, test3 = false, test4 = true;
		if (test1) testGetSourceByUri();
		if (test2) testDeleteSourceByUri();
		if (test3) testGetSourcesByIOPattern();
		if (test4) testGetAllSources();


	}

}
