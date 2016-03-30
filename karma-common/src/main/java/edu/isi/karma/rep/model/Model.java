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

package edu.isi.karma.rep.model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.model.serialization.Repository;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.rep.sources.IOType;

public class Model {
	
	private static Logger logger = LoggerFactory.getLogger(Model.class);

	private String id;
	private String baseUri;
	//private String type;
	
	private List<Atom> atoms;

	public Model(String id) {
		this.id = id;
		atoms = new ArrayList<>();
	}
	
//	public Model(String id, String type) {
//		this.id = id;
//		this.type = type;
//		atoms = new ArrayList<Atom>();
//	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getUri() {
		String uri = "";
		if (getBaseUri() != null) uri += getBaseUri();
		if (getId() != null) uri += getId();
		return uri;
	}

	public List<Atom> getAtoms() {
		return atoms;
	}

	public void setAtoms(List<Atom> atoms) {
		this.atoms = atoms;
	}
	
	public com.hp.hpl.jena.rdf.model.Model getJenaModel() {
		
		com.hp.hpl.jena.rdf.model.Model model = ModelFactory.createDefaultModel();
		
		if (this.baseUri != null)
			model.setNsPrefix("", this.baseUri);
		
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		Resource model_resource = model.createResource(Namespaces.KARMA + "Model");
		Resource class_atom_resource = model.createResource(Namespaces.SWRL + "ClassAtom");
		Resource individual_property_atom_resource = model.createResource(Namespaces.SWRL + "IndividualPropertyAtom");
		//Resource input_resource = model.createResource(Namespaces.KARMA + "Input");
		//Resource output_resource = model.createResource(Namespaces.KARMA + "Output");

		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_atom = model.createProperty(Namespaces.KARMA, "hasAtom");
		//Property has_model = model.createProperty(Namespaces.KARMA, "hasModel");

		Property class_predicate = model.createProperty(Namespaces.SWRL, "classPredicate");
		Property property_predicate = model.createProperty(Namespaces.SWRL, "propertyPredicate");
		Property has_argument1 = model.createProperty(Namespaces.SWRL, "argument1");
		Property has_argument2 = model.createProperty(Namespaces.SWRL, "argument2");

		Resource my_model = model.createResource(getUri());
		my_model.addProperty(rdf_type, model_resource);

		Resource r = null;

		for (Atom atom : this.getAtoms()) {
			if (atom instanceof ClassAtom) {
				ClassAtom classAtom = (ClassAtom)atom;
				
				r = model.createResource();
				r.addProperty(rdf_type, class_atom_resource);
				
				if (classAtom.getClassPredicate().getPrefix() != null && classAtom.getClassPredicate().getNs() != null)
					model.setNsPrefix(classAtom.getClassPredicate().getPrefix(), classAtom.getClassPredicate().getNs());
				Resource className = model.createResource(classAtom.getClassPredicate().getUri());
				r.addProperty(class_predicate, className);
				
				Resource arg1 = model.getResource(this.baseUri + classAtom.getArgument1().getAttOrVarId());
				r.addProperty(has_argument1, arg1);
				
				my_model.addProperty(has_atom, r);
			}
			else if (atom instanceof IndividualPropertyAtom) {
				IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;
				
				r = model.createResource();
				r.addProperty(rdf_type, individual_property_atom_resource);
				
				if (propertyAtom.getPropertyPredicate().getPrefix() != null && propertyAtom.getPropertyPredicate().getNs() != null)
					model.setNsPrefix(propertyAtom.getPropertyPredicate().getPrefix(), propertyAtom.getPropertyPredicate().getNs());
				Resource propertyName = model.createResource(propertyAtom.getPropertyPredicate().getUri());
				r.addProperty(property_predicate, propertyName);
				
				Resource arg1 = model.getResource(this.baseUri + propertyAtom.getArgument1().getAttOrVarId());
				r.addProperty(has_argument1, arg1);
				
				Resource arg2 = model.getResource(this.baseUri + propertyAtom.getArgument2().getAttOrVarId());
				r.addProperty(has_argument2, arg2);
				
				my_model.addProperty(has_atom, r);
			}
		}
		return model;
	}

	public Map<String, Map<String, String>> findInServiceInputs(
			com.hp.hpl.jena.rdf.model.Model serviceJenaModel, Integer serviceLimit) {
		return findSemanticModelInJenaModel(serviceJenaModel, IOType.INPUT, serviceLimit);
	}
	public Map<String, Map<String, String>> findInServiceOutputs(
			com.hp.hpl.jena.rdf.model.Model serviceJenaModel, Integer serviceLimit) {
		return findSemanticModelInJenaModel(serviceJenaModel, IOType.OUTPUT, serviceLimit);
	}
	public Map<String, Map<String, String>> findInJenaModel(
			com.hp.hpl.jena.rdf.model.Model jenaModel, Integer limit) {
		return findSemanticModelInJenaModel(jenaModel, IOType.NONE, limit);
	}
	
	/**
	 * Returns a hash map of all sources/services in the jena model whose input/output matches this object model.
	 * @param jenaModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the service input or service output will be tested for matching
	 * @return a hashmap of all IDs of found sources/services and a mapping from the model parameters to query params. 
	 * This help us later to join the model's corresponding source and the matched service 
	 */
	private Map<String, Map<String, String>> findSemanticModelInJenaModel( 
			com.hp.hpl.jena.rdf.model.Model jenaModel, String ioType, Integer limit) {

		Map<String, Map<String, String>> IdsAndMappings =
				new HashMap<>();
		
		List<String> argList = new ArrayList<>();
		String queryString = "";
		if (ioType.equalsIgnoreCase(IOType.INPUT)) 
			queryString = this.getSparqlToMatchServiceInputs(argList);
		else if (ioType.equalsIgnoreCase(IOType.OUTPUT)) 
			queryString = this.getSparqlToMatchServiceOutputs(argList);
		else 
			queryString = this.getSparql(argList);

		if (limit != null) {
			queryString += "LIMIT " + String.valueOf(limit.intValue() + "\n");
		}
		
//		logger.debug("query= \n" + queryString);
		
		Query query = QueryFactory.create(queryString);
//		// Execute the query and obtain results
		QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel);

		try {
			ResultSet results = qexec.execSelect() ;
			
			if (!results.hasNext()) {
				logger.info("query does not return any answer.");
				return null;
			}

//			ResultSetFormatter.out(System.out, results, query) ;
			 
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				
				RDFNode m = soln.get("model") ;       // Get a result variable by name.

				if (m == null) {
					logger.info("model uri is null.");
					continue;
				}

				String uri = m.asResource().getNameSpace();
				logger.debug("service uri: " + uri);
				if (IdsAndMappings.get(uri) == null) {
					IdsAndMappings.put(uri, new HashMap<String, String>());
				}
				
				for (String arg : argList) {
					RDFNode argNode = soln.get(arg) ;
					if (argNode != null && argNode.isResource()) {
						String retrieved_id = argNode.asResource().getLocalName();
						IdsAndMappings.get(uri).put(arg, retrieved_id);
					}
				}
			}
			
			return IdsAndMappings;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		} finally { 
			qexec.close() ; 
		}
	}
	
	public List<Map<String, String>> findModelDataInJenaData( 
			com.hp.hpl.jena.rdf.model.Model jenaModel, Integer limit) {

		List<Map<String, String>> attValueList =
				new ArrayList<>();
		
		List<String> argList = new ArrayList<>();
		String queryString = "";
		queryString = this.getSparqlDataQuery(argList);
		
//		System.out.println(queryString);

		if (limit != null) {
			queryString += "LIMIT " + String.valueOf(limit.intValue() + "\n");
		}
		
//		logger.debug("query= \n" + queryString);
		
		Query query = QueryFactory.create(queryString);
//		// Execute the query and obtain results
		QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel);

		try {
			ResultSet results = qexec.execSelect() ;
			
			if (!results.hasNext()) {
				logger.info("query does not return any answer.");
				return null;
			}

//			ResultSetFormatter.out(System.out, results, query) ;
			 
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				Map<String, String> attValues =
						new HashMap<>();
				
				for (String arg : argList) {
					RDFNode argNode = soln.get(arg) ;
					if (argNode != null) {
						String value = argNode.toString();
						attValues.put(arg, value);
					}
				}
				
				attValueList.add(attValues);
			}
			
			return attValueList;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		} finally { 
			qexec.close() ; 
		}
	}
	
	public void writeJenaModelToFile(String path, String lang) throws FileNotFoundException {
		com.hp.hpl.jena.rdf.model.Model model = getJenaModel();
		String source_desc_file = path + Repository.Instance().getFileExtension(lang);
		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(source_desc_file));
		model.write(output,lang);		
		
	}
	
	private static String getSparqlHeader(Map<String, String> nsToPrefixMapping) {
		String prefixHeader = "";
		
		for (Map.Entry<String, String> stringStringEntry : nsToPrefixMapping.entrySet()) {
			String prefix = stringStringEntry.getValue();
			if (prefix != null)
				prefixHeader += "PREFIX " + prefix + ": <" + stringStringEntry.getKey() + "> \n";
		}

		return prefixHeader;
	}
	
	public String getSparqlDataQuery(List<String> argList) {

		String queryString = "";
		
		// map of NS --> Prefix
		Map<String, String> nsToPrefixMapping = new HashMap<>();
		nsToPrefixMapping.put(Namespaces.RDF, Prefixes.RDF);
		
		String select_header = "SELECT \n";
		String select_where =
			"WHERE { \n" ;
		
		String predicateUri = "";
		String argument1 = "";
		String argument2 = "";
		String argument1Var = "";
		String argument2Var = "";

		if (argList == null) 
			argList = new ArrayList<>();
		
		for (int i = 0; i < this.getAtoms().size(); i++) {
			Atom atom = this.getAtoms().get(i);
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = (ClassAtom)atom;
					
					if (classAtom.getClassPredicate().getPrefix() != null &&
							classAtom.getClassPredicate().getNs() != null) { 
						
						nsToPrefixMapping.put(classAtom.getClassPredicate().getNs(), 
						classAtom.getClassPredicate().getPrefix());
						predicateUri = classAtom.getClassPredicate().getDisplayName();
						
					} else {
						predicateUri = "<" + classAtom.getClassPredicate().getUri() + ">";
					}
					
					argument1 = classAtom.getArgument1().getId();
					argument1Var = "?" + argument1;
					
					if (argList.indexOf(argument1) == -1) {
						argList.add(argument1);
						select_header += "  " + argument1Var;
					}
						
					select_where += 
						"      " + argument1Var + " rdf:type " + predicateUri + " . \n";
				}
				else if (atom instanceof IndividualPropertyAtom) {
					IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;
					
					if (propertyAtom.getPropertyPredicate().getPrefix() != null &&
							propertyAtom.getPropertyPredicate().getNs() != null) { 
						
						nsToPrefixMapping.put(propertyAtom.getPropertyPredicate().getNs(), 
						propertyAtom.getPropertyPredicate().getPrefix());
						predicateUri = propertyAtom.getPropertyPredicate().getDisplayName();
						
					} else {
						predicateUri = "<" + propertyAtom.getPropertyPredicate().getUri() + ">";
					}

					argument1 = propertyAtom.getArgument1().getId();
					argument2 = propertyAtom.getArgument2().getId();
					argument1Var = "?" + argument1;
					argument2Var = "?" + argument2;
					
					if (argList.indexOf(argument1) == -1) {
						argList.add(argument1);
						select_header += "  " + argument1Var;
					}					
					if (argList.indexOf(argument2) == -1) {
						argList.add(argument2);
						select_header += "  " + argument2Var;
					}
			
					select_where += 
						"      " + argument1Var + " " + predicateUri + " " + argument2Var + " . \n";
				}			
			}
		}		
		select_header += "\n";
		select_where += "} \n";
 
		String prefix = getSparqlHeader(nsToPrefixMapping);
		String select = select_header + select_where;

		queryString = prefix + select;
		
		logger.debug("query= \n" + queryString);

		return queryString;
	}


	public String getSparqlConstructQuery(List<String> argList) {

		String queryString = "";
		
		// map of NS --> Prefix
		Map<String, String> nsToPrefixMapping = new HashMap<>();
		nsToPrefixMapping.put(Namespaces.RDF, Prefixes.RDF);
		
		String construct_header = "CONSTRUCT { \n";
		String construct_where =
			"WHERE { \n" ;
		
		String predicateUri = "";
		String argument1 = "";
		String argument2 = "";
		String argument1Var = "";
		String argument2Var = "";

		if (argList == null) 
			argList = new ArrayList<>();
		
		for (int i = 0; i < this.getAtoms().size(); i++) {
			Atom atom = this.getAtoms().get(i);
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = (ClassAtom)atom;
					
					if (classAtom.getClassPredicate().getPrefix() != null &&
							classAtom.getClassPredicate().getNs() != null) { 
						
						nsToPrefixMapping.put(classAtom.getClassPredicate().getNs(), 
						classAtom.getClassPredicate().getPrefix());
						predicateUri = classAtom.getClassPredicate().getDisplayName();
						
					} else {
						predicateUri = "<" + classAtom.getClassPredicate().getUri() + ">";
					}
					
					argument1 = classAtom.getArgument1().getId();
					argument1Var = "?" + argument1;
					
					if (argList.indexOf(argument1) == -1) 
						argList.add(argument1);

					construct_header += 
						"      " + argument1Var + " rdf:type " + predicateUri + " . \n";
						
					construct_where += 
						"      " + argument1Var + " rdf:type " + predicateUri + " . \n";
				}
				else if (atom instanceof IndividualPropertyAtom) {
					IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;
					
					if (propertyAtom.getPropertyPredicate().getPrefix() != null &&
							propertyAtom.getPropertyPredicate().getNs() != null) { 
						
						nsToPrefixMapping.put(propertyAtom.getPropertyPredicate().getNs(), 
						propertyAtom.getPropertyPredicate().getPrefix());
						predicateUri = propertyAtom.getPropertyPredicate().getDisplayName();
						
					} else {
						predicateUri = "<" + propertyAtom.getPropertyPredicate().getUri() + ">";
					}

					argument1 = propertyAtom.getArgument1().getId();
					argument2 = propertyAtom.getArgument2().getId();
					argument1Var = "?" + argument1;
					argument2Var = "?" + argument2;
					
					if (argList.indexOf(argument1) == -1) 
						argList.add(argument1);
					if (argList.indexOf(argument2) == -1) 
						argList.add(argument2);
					
					construct_header += 
						"      " + argument1Var + " " + predicateUri + " " + argument2Var + " . \n";
						
					construct_where += 
						"      " + argument1Var + " " + predicateUri + " " + argument2Var + " . \n";
				}			
			}
		}		
		construct_header += "} \n";
		construct_where += "} \n";
 
		String prefix = getSparqlHeader(nsToPrefixMapping);
		String select = construct_header + construct_where;

		queryString = prefix + select;
		
		logger.debug("query= \n" + queryString);

		return queryString;
	}

	public String getSparqlToMatchServiceInputs(List<String> argList) {
		return getSparqlQuery(IOType.INPUT, argList);
	}
	public String getSparqlToMatchServiceOutputs(List<String> argList) {
		return getSparqlQuery(IOType.OUTPUT, argList);
	}
	public String getSparql(List<String> argList) {
		return getSparqlQuery(IOType.NONE, argList);
	}
	
	private String getSparqlQuery(String ioType, List<String> argList) {

		Model m = this;
		
		String queryString = "";
		
		// map of NS --> Prefix
		Map<String, String> nsToPrefixMapping = new HashMap<>();
		nsToPrefixMapping.put(Namespaces.KARMA, Prefixes.KARMA);
		nsToPrefixMapping.put(Namespaces.SWRL, Prefixes.SWRL);
		nsToPrefixMapping.put(Namespaces.HRESTS, Prefixes.HRESTS);
		
		String io_class = "";
		if (ioType.equalsIgnoreCase(IOType.OUTPUT))
			io_class = "Output";
		else if (ioType.equalsIgnoreCase(IOType.INPUT))
			io_class =  "Input";

		String select_header = "SELECT ?model ";
		String select_where =
			"WHERE { \n" ;
		
		if (io_class.trim().length() > 0) {
			select_where += "      ?x a " + Prefixes.KARMA + ":" + io_class + " . \n" + 
							"      ?x " + Prefixes.KARMA + ":hasModel ?model . \n"; 
		}
		
		select_where += 
			"      ?model a " + Prefixes.KARMA + ":Model . \n";
		
		String atomVar = "";
		String predicateUri = "";
		String argument1 = "";
		String argument2 = "";
		String argument1Var = "";
		String argument2Var = "";

		if (argList == null) 
			argList = new ArrayList<>();

		for (int i = 0; i < m.getAtoms().size(); i++) {
			Atom atom = m.getAtoms().get(i);
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = (ClassAtom)atom;
					atomVar = "?atom" + String.valueOf(i+1);
					predicateUri = classAtom.getClassPredicate().getUri();
					argument1 = classAtom.getArgument1().getId();
					argument1Var = "?" + argument1;
					
					if (argList.indexOf(argument1) == -1) {
						argList.add(argument1);
						select_header += argument1Var + " ";
					}

					select_where += 
						"      ?model " + Prefixes.KARMA + ":hasAtom " + atomVar + " . \n" +
						"      " + atomVar + " a " + Prefixes.SWRL + ":ClassAtom . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":classPredicate <" + predicateUri + "> . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument1 " + argument1Var + " . \n";
				}
				else if (atom instanceof IndividualPropertyAtom) {
					IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;
					atomVar = "?atom" + String.valueOf(i+1);
					predicateUri = propertyAtom.getPropertyPredicate().getUri();
					argument1 = propertyAtom.getArgument1().getId();
					argument2 = propertyAtom.getArgument2().getId();
					argument1Var = "?" + argument1;
					argument2Var = "?" + argument2;
					
					if (argList.indexOf(argument1) == -1) {
						argList.add(argument1);
						select_header += argument1Var + " ";
					}
					if (argList.indexOf(argument2) == -1) {
						argList.add(argument2);
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
 
		String prefix = getSparqlHeader(nsToPrefixMapping);
		String select = select_header + " \n " + select_where;

		queryString = prefix + select;
		
		logger.debug("query= \n" + queryString);

		return queryString;
	}
	
	public String getLogicalForm() {
		String logicalForm = "";
		String separator = " /\\ ";
		for (Atom atom : atoms) {
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = (ClassAtom)atom;
					logicalForm += classAtom.getClassPredicate().getLocalName();
					logicalForm += "(";
					logicalForm += classAtom.getArgument1().getId();
					logicalForm += ")";
					logicalForm += separator;				
				}
				else if (atom instanceof IndividualPropertyAtom) {
					IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;
					logicalForm += propertyAtom.getPropertyPredicate().getLocalName();
					logicalForm += "(";
					logicalForm += propertyAtom.getArgument1().getId();
					logicalForm += ",";
					logicalForm += propertyAtom.getArgument2().getId();
					logicalForm += ")";
					logicalForm += separator;				
				}			
			}
		}		
		int index = logicalForm.lastIndexOf(separator);
		if (index != -1)
			logicalForm = logicalForm.substring(0, index);
		
		return logicalForm;
	}

	public void print() {
//		System.out.println("model id=" + this.getId());
		System.out.println(getLogicalForm());
//		for (Atom atom : atoms) {
//			System.out.println("@@@@@@@@@@@@@@@");
//			if (atom != null) atom.print();
//		}
	}
}
