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

package edu.isi.karma.model.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.model.Argument;
import edu.isi.karma.rep.model.ArgumentType;
import edu.isi.karma.rep.model.Atom;
import edu.isi.karma.rep.model.ClassAtom;
import edu.isi.karma.rep.model.IndividualPropertyAtom;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.AttributeRequirement;
import edu.isi.karma.rep.sources.DataSource;
import edu.isi.karma.rep.sources.IOType;
import edu.isi.karma.rep.sources.Source;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class DataSourceLoader extends SourceLoader {

	private static Logger logger = LoggerFactory.getLogger(DataSourceLoader.class);
	private static DataSourceLoader instance = null;
	private static final int DEFAULT_SOURCE_RESULTS_SIZE = 10;

	protected DataSourceLoader() {
		      // Exists only to defeat instantiation.
	}
		   
	public static DataSourceLoader getInstance() {
		if (instance == null) {
			instance = new DataSourceLoader();
		}
		return instance;
	}
	
    @Override
	public DataSource getSourceByUri(String uri) {
		
		Model m = Repository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		DataSource source = importSourceFromJenaModel(m);
		return source;
	}

    @Override
	public void deleteSourceByUri(String uri) {
		Repository.Instance().clearNamedModel(uri);
		
		String source_id = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 1);
		//TODO this is not the right way to get the context parameters
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		String dir = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
							Repository.Instance().SOURCE_REPOSITORY_REL_DIR;
		String fileName = source_id + Repository.Instance().getFileExtension(Repository.Instance().LANG);
		File f = new File(dir + fileName);
		
		try {
		if (f.exists()) {
			if (!f.delete())
				logger.debug("The file " + fileName + " cannot be deleted from " + dir);
			else
				logger.debug("The file " + fileName + " has been deleted from " + dir);
		} else
			logger.debug("The file " + fileName + " does not exist in " + dir);
		} catch (Throwable t) {
			logger.debug("cannot delete the file " + fileName + " from " + dir + " because " + t.getMessage());
		}

	}
	
	
	/**
	 * returns the source id, name, address of all sources in the repository
	 * @param limit : maximum number of results, null value means all the sources
	 * @return
	 */
    @Override
	public List<Source> getSourcesAbstractInfo(Integer sourceLimit) {
		
		List<Source> sourceList = new ArrayList<>();
		
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
					sourceList.add(new DataSource(source_id, source_name));
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
    @Override
	public List<Source> getSourcesDetailedInfo(Integer sourceLimit) {

		List<Source> sourceList = getSourcesAbstractInfo(sourceLimit);
		List<Source> sourceListCompleteInfo = new ArrayList<>();
		for (Source s : sourceList) {
			sourceListCompleteInfo.add(getSourceByUri(s.getUri()));
		}
		return sourceListCompleteInfo;
	}
	
	/**
	 * Searches the repository to find the sources whose model matches the semantic model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the source input or source output will be tested for matching
	 * @param sourceLimit maximum number of sources that will be fetched
	 * @return a hashmap of all found sources and a mapping from the found source parameters to the model parameters. 
	 * This help us later to how to join the model's corresponding source and the matched source 
	 */
	public Map<DataSource, Map<String, String>> getDataSourcesByIOPattern(edu.isi.karma.rep.model.Model semanticModel, 
			Integer sourceLimit) {
		
		if (semanticModel == null || semanticModel.getAtoms() == null 
				|| semanticModel.getAtoms().isEmpty()) {
			logger.info("The input model is nul or it does not have any atom");
			return null;
		}
		
		Map<DataSource, Map<String, String>> sourcesAndMappings =
				new HashMap<>();
		
		Map<String, Map<String, String>> sourceIdsAndMappings =
			semanticModel.findInJenaModel(Repository.Instance().getModel(), sourceLimit);
		
		if (sourceIdsAndMappings == null)
			return null;
		
		for (Map.Entry<String, Map<String, String>> stringMapEntry : sourceIdsAndMappings.entrySet()) {
			Model m = Repository.Instance().getNamedModel(stringMapEntry.getKey());
			if (m != null)
				sourcesAndMappings.put(importSourceFromJenaModel(m), stringMapEntry.getValue());
		}
		
		return sourcesAndMappings;
	}
	
	/**
	 * Searches the repository to find the sources whose model is contained in the semantic model parameter.
	 * Note that the services in the return list only include the operations that match the model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the service input or service output will be tested for matching.
	 * @param operationsLimit maximum number of operations that will be fetched
	 * @return a hashmap of all found sources and a mapping from the found source parameters to the model parameters. 
	 * This help us later to how to join the model's corresponding source and the matched service 
	 */
	public Map<DataSource, Map<String, String>> getDataSourcesContainedInModel(edu.isi.karma.rep.model.Model semanticModel, 
			Integer sourceLimit) {
		
		List<Source> sourceList = getSourcesDetailedInfo(sourceLimit);
		
		Map<DataSource, Map<String, String>> sourcesAndMappings =
				new HashMap<>();

		Model jenaModel = semanticModel.getJenaModel();
		for (Source source : sourceList) {
			
			if (!(source instanceof DataSource))
				continue;
			
			edu.isi.karma.rep.model.Model m = ((DataSource)source).getModel();

			if (m == null)
				continue;
			
			Map<String, Map<String, String>> sourceIdsAndMappings =
				m.findInJenaModel(jenaModel, null);
			
			if (sourceIdsAndMappings == null)
				continue;
			
			Iterator<String> itr = sourceIdsAndMappings.keySet().iterator();
			if (itr.hasNext()) {
				String key = itr.next();
				sourcesAndMappings.put((DataSource)source, sourceIdsAndMappings.get(key));
			}
		}
		
		return sourcesAndMappings;
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
    @Override
	public DataSource importSourceFromJenaModel(Model model) {
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
		
		DataSource source = new DataSource(source_id);
		source.setName(source_name);
		source.setVariables(getVariables(model, source_resource));
	 	source.setAttributes(getAttributes(model, source_resource));
	 	source.setModel(getSemanticModel(model, source_resource));
		
		return source;
	}
	
	private List<String> getVariables(Model model, Resource source_resource) {
		
		Property has_variable_property = model.getProperty(Namespaces.KARMA + "hasVariable");
	
		List<String> variables = new ArrayList<>();
		NodeIterator nodeIterator = null;
		RDFNode node = null;

		// hasAttribute
		nodeIterator = model.listObjectsOfProperty(source_resource, has_variable_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasAttribute property is not a resource.");
				continue;
			}
			
			variables.add(node.asResource().getLocalName());
		}
		
		return variables;

	}
	
	private List<Attribute> getAttributes(Model model, Resource source_resource) {
		
		Property has_attribute_property = model.getProperty(Namespaces.KARMA + "hasAttribute");
	
		List<Attribute> attList = new ArrayList<>();
		
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
	
	private Attribute getSingleAttribute(Model model, Resource att_resource) {
		
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
	
	private edu.isi.karma.rep.model.Model getSemanticModel(Model model, Resource source_resource) {
		
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

		edu.isi.karma.rep.model.Model semanticModel = 
			new edu.isi.karma.rep.model.Model(modelNode.asResource().getLocalName());
		List<Atom> atoms = new ArrayList<>();
		

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
	
	private Atom getAtom(Model model, Resource atom_resource) {
		
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
	
	private ClassAtom getClassAtom(Model model, Resource atom_resource) {
		
		String predicateUri = null;
		String predicatePrefix = null;
		String predicateNs = null;
		
		String argument1Id = null;
		String argument1Type = null;
		
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
		
		Label predicateName = new Label(predicateUri, predicateNs, predicatePrefix);
		Argument arg1 = new Argument(argument1Id, argument1Id, argument1Type);
		
		ClassAtom classAtom = new ClassAtom(predicateName, arg1);

		return classAtom;

	}
	
	private IndividualPropertyAtom getPropertyAtom(Model model, Resource atom_resource) {
		
		String predicateUri = null;
		String predicatePrefix = null;
		String predicateNs = null;
		
		String argument1Id = null;
		String argument2Id = null; 

		String argument1Type = null;
		String argument2Type = null; 
		
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
		
		Label predicateName = new Label(predicateUri, predicateNs, predicatePrefix);
		Argument arg1 = new Argument(argument1Id, argument1Id, argument1Type);
		Argument arg2 = new Argument(argument2Id, argument2Id, argument2Type);
		
		IndividualPropertyAtom propertyAtom = new IndividualPropertyAtom(predicateName, arg1, arg2);

		return propertyAtom;	
	}
	
	private boolean isInstanceOfTheClass(Resource resource, Resource class_resource) {
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
		DataSource source = (DataSource) DataSourceLoader.getInstance().getSourceByUri(uri);
		if (source != null) source.print();
	}
	private static void testGetAllSources() {
		List<Source> sourceList = DataSourceLoader.getInstance().getSourcesDetailedInfo(null);
		for (Source s : sourceList) {
			if (s != null) s.print();
		}
	}
	private static void testGetSourcesByIOPattern() {
		edu.isi.karma.rep.model.Model semanticModel = new edu.isi.karma.rep.model.Model(null);

		String geonamesOntology = "http://www.geonames.org/ontology#";
		String wgs84Ontology = "http://www.w3.org/2003/01/geo/wgs84_pos#";
		
		Label featurePredicatName = new Label(geonamesOntology + "Feature", geonamesOntology, "gn");
		Label latPredicatName = new Label(wgs84Ontology + "lat", wgs84Ontology, "wgs84");
		Label lngPredicatName = new Label(wgs84Ontology + "long", wgs84Ontology, "wgs84");
		
		ClassAtom c1 = new ClassAtom(featurePredicatName, new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE));
		IndividualPropertyAtom p1 = new IndividualPropertyAtom(latPredicatName,
				new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE), 
				new Argument("arg2", "arg2", ArgumentType.ATTRIBUTE));
		IndividualPropertyAtom p2 = new IndividualPropertyAtom(lngPredicatName,
				new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE), 
				new Argument("arg3", "arg3", ArgumentType.ATTRIBUTE));
//		ClassAtom c2 = new ClassAtom(featurePredicatName, new Argument("arg2", "arg2", ArgumentType.ATTRIBUTE));

		semanticModel.getAtoms().add(c1);
		semanticModel.getAtoms().add(p1);
		semanticModel.getAtoms().add(p2);
//		semanticModel.getAtoms().add(c2);
		
		Map<DataSource, Map<String, String>> sourcesAndMappings = 
				DataSourceLoader.getInstance().getDataSourcesByIOPattern(semanticModel, null);

		if (sourcesAndMappings == null)
			return;
		
		for (Source s : sourcesAndMappings.keySet()) {
			if (s != null) s.print();
		}
		
		System.out.println("Mappings from matched source to model arguments:");
		for (Map.Entry<DataSource, Map<String, String>> dataSourceMapEntry : sourcesAndMappings.entrySet()) {
			System.out.println("Source: " + dataSourceMapEntry.getKey().getId());
			if (dataSourceMapEntry.getValue() == null)
				continue;
			for (String str : dataSourceMapEntry.getValue().keySet())
				System.out.println(str + "-------" + dataSourceMapEntry.getValue().get(str));
		}

	}
	private static void testDeleteSourceByUri() {
		String uri = "http://isi.edu/integration/karma/sources/AEEA5A9A-744C-8096-B372-836ACC820D5A#";
		DataSourceLoader.getInstance().deleteSourceByUri(uri);
	}
	public static void main(String[] args) {

		boolean test1 = true, test2 = false, test3 = false, test4 = false;
		if (test1) testGetSourceByUri();
		if (test2) testDeleteSourceByUri();
		if (test3) testGetSourcesByIOPattern();
		if (test4) testGetAllSources();


	}

}
