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

package edu.isi.karma.kr2rml;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.common.HttpMethods;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatter;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.template.StringTemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;

public class KR2RMLMappingWriter {
	
	private Repository myRepository;
	
	// Internal instance variables
	private RepositoryConnection con;
	private ValueFactory f;
	private Map<String, URI> repoURIs;
	private static Logger logger = LoggerFactory
			.getLogger(KR2RMLMappingWriter.class);
	
	public KR2RMLMappingWriter()
			throws RepositoryException {

		initializeTripleStore();
		initializeURIs();
	}

	private Resource addKR2RMLMappingResource(Worksheet worksheet, KR2RMLMapping mapping)
			throws RepositoryException {
		/** Create resource for the mapping as a blank node **/
		Resource mappingRes = f.createBNode();
		con.add(mappingRes, RDF.TYPE, repoURIs.get(Uris.KM_R2RML_MAPPING_URI));
		Value srcNameVal = f.createLiteral(worksheet.getTitle());
		con.add(mappingRes, repoURIs.get(Uris.KM_SOURCE_NAME_URI), srcNameVal);
		
		// Add the timestamp
		con.add(mappingRes, repoURIs.get(Uris.KM_MODEL_PUBLICATION_TIME_URI), f.createLiteral(new Date().getTime()));
		String inputColumns = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(
				Property.inputColumns);
		String outputColumns = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(
				Property.outputColumns);
		String oldHistory = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(
				Property.oldCommandHistory);
		String graphLabel = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(
				Property.graphLabel);
		String baseURI = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(
				Property.baseURI);
		// Add the version
		con.add(mappingRes, repoURIs.get(Uris.KM_MODEL_VERSION_URI), f.createLiteral(KR2RMLVersion.getCurrent().toString()));
		if (inputColumns != null && !inputColumns.trim().isEmpty())
			con.add(mappingRes, repoURIs.get(Uris.KM_HAS_INPUTCOLUMNS), f.createLiteral(inputColumns));
		if (outputColumns != null && !outputColumns.trim().isEmpty())
			con.add(mappingRes, repoURIs.get(Uris.KM_HAS_OUTPUTCOLUMNS), f.createLiteral(outputColumns));
		if (graphLabel != null && !graphLabel.trim().isEmpty())
			con.add(mappingRes, repoURIs.get(Uris.KM_HAS_MODELLABEL), f.createLiteral(graphLabel));	
		if (baseURI != null && !baseURI.trim().isEmpty())
			con.add(mappingRes, repoURIs.get(Uris.KM_HAS_BASEURI), f.createLiteral(baseURI));
		if (oldHistory != null && !oldHistory.trim().isEmpty())
			con.add(mappingRes, repoURIs.get(Uris.KM_HAS_OLDHISTORY), f.createLiteral(oldHistory));
		addWorksheetProperties(worksheet, mappingRes);
		addCompleteWorksheetHistory(mapping, mappingRes);
		return mappingRes;
	}

	private void initializeTripleStore() throws RepositoryException {
		/** Initialize an in-memory sesame triple store **/
		myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		con = myRepository.getConnection();
		f = myRepository.getValueFactory();
	}
	
	protected void initializeURIs()
	{
		repoURIs = new HashMap<>();
		for (String uri : Uris.Uris) {
			repoURIs.put(uri, f.createURI(uri));
		}				
		
	}

	public boolean addR2RMLMapping(KR2RMLMapping mapping, Worksheet worksheet, Workspace workspace)
			throws RepositoryException, JSONException {

		try {

			Resource mappingRes = addKR2RMLMappingResource(worksheet, mapping);
			addTripleMaps(mapping, mappingRes, worksheet, workspace);
			addPrefixes(mapping);
			
		} catch (OpenRDFException e) {
			logger.error("Error occured while generating RDF representation of R2RML data " +
					"structures.", e);
		}
		return true;
	}

	public void writeR2RMLMapping(PrintWriter writer)  {
		try {
			con.export(new TurtleWriter(writer));
		} catch (RepositoryException e) {
			logger.error("Error occured while outputing R2RML mapping");
		} catch (RDFHandlerException e) {
			logger.error("Error occured while outputing R2RML mapping");
		}
	}

	private void addPrefixes(KR2RMLMapping mapping) throws RepositoryException {
		for (Prefix p : mapping.getPrefixes())
		{
			con.setNamespace(p.getPrefix(), p.getNamespace());
		}
		con.setNamespace(Prefixes.RR, Namespaces.RR);
		con.setNamespace(Prefixes.KARMA_DEV, Namespaces.KARMA_DEV);
	}

	private void addTripleMaps(KR2RMLMapping mapping, Resource mappingRes, Worksheet worksheet, Workspace workspace)
			throws RepositoryException {
		/** Get the required data structures of R2RML **/

		List<TriplesMap> triplesMapList = mapping.getTriplesMapList();
		/** Add all the triple maps **/
		for (TriplesMap trMap:triplesMapList) {
			addTripleMap(mapping, mappingRes, worksheet, workspace,
					trMap);
		}
	}

	private void addTripleMap(KR2RMLMapping mapping, Resource mappingRes,
			Worksheet worksheet, Workspace workspace, TriplesMap trMap) throws RepositoryException {
		URI trMapUri = f.createURI(Namespaces.KARMA_DEV + trMap.getId());
		addTripleMapMetadata(mapping, mappingRes, worksheet, trMap, trMapUri);
		
		addSubjectMap(mapping, mappingRes, trMap, trMapUri, workspace);
		
		addPredicateObjectMaps(mapping, mappingRes, trMap, trMapUri, workspace, worksheet);
	}

	private void addTripleMapMetadata(KR2RMLMapping mapping, Resource mappingRes, Worksheet worksheet,
			TriplesMap trMap, URI trMapUri) throws RepositoryException {
		
		// Add the triples map type statement
		con.add(trMapUri, RDF.TYPE, repoURIs.get(Uris.RR_TRIPLESMAP_CLASS_URI));
		// Associate it with the source mapping URI
		con.add(mappingRes, repoURIs.get(Uris.KM_HAS_TRIPLES_MAP_URI), trMapUri);
		con.add(trMapUri, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
		
		addLogicalSource(mapping, worksheet, mappingRes, trMapUri);
	}

	private void addLogicalSource(KR2RMLMapping mapping, Worksheet worksheet, Resource mappingRes, URI trMapUri)
			throws RepositoryException {
		// Add the Logical table information
		BNode logTableBNode = f.createBNode();
		
		con.add(logTableBNode, repoURIs.get(Uris.RR_TABLENAME_URI), f.createLiteral(worksheet.getTitle()));
		con.add(logTableBNode, RDF.TYPE, repoURIs.get(Uris.RR_LOGICAL_TABLE_CLASS_URI));
		con.add(logTableBNode, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
		con.add(mappingRes, repoURIs.get(Uris.KM_HAS_LOGICAL_TABLE_URI), logTableBNode);
		con.add(trMapUri, repoURIs.get(Uris.RR_LOGICAL_TABLE_URI), logTableBNode);

	}
	
	private void addSubjectMap(KR2RMLMapping mapping,
			Resource mappingRes, TriplesMap trMap,
			URI trMapUri, Workspace workspace) throws RepositoryException {
		
		KR2RMLColumnNameFormatter columnNameFormatter = mapping.getColumnNameFormatter();
		RepFactory factory = workspace.getFactory();
		// Add the subject map statements
		SubjectMap sjMap = trMap.getSubject();
		BNode sjBlankNode = f.createBNode();
		con.add(trMapUri, repoURIs.get(Uris.RR_SUBJECTMAP_URI), sjBlankNode);
		con.add(mappingRes, repoURIs.get(Uris.KM_HAS_SUBJECT_MAP_URI), sjBlankNode);
		con.add(sjBlankNode, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
		// Add the subject map type statement
		con.add(sjBlankNode, RDF.TYPE, repoURIs.get(Uris.RR_SUBJECTMAP_CLASS_URI));
		
		// Add the node id for the subject
		Value nodeIdVal = f.createLiteral(sjMap.getId());
		con.add(sjBlankNode, repoURIs.get(Uris.KM_NODE_ID_URI), nodeIdVal);
		
		// Add the type for subject maps
		List<TemplateTermSet> rdfsTypes = sjMap.getRdfsType();
		for (TemplateTermSet typeTermSet:rdfsTypes) {
			if (typeTermSet.isSingleUriString()) {
				URI sjTypeUri = f.createURI(typeTermSet.getR2rmlTemplateString(factory, columnNameFormatter));
				con.add(sjBlankNode, repoURIs.get(Uris.RR_CLASS_URI), sjTypeUri);
			} else {
				if (typeTermSet.isSingleColumnTerm()) {
					BNode typeBlankNode = f.createBNode();
					String colRepr  = typeTermSet.getR2rmlTemplateString(factory, columnNameFormatter);
					con.add(typeBlankNode, repoURIs.get(Uris.RR_TEMPLATE_URI), f.createLiteral(colRepr));
					con.add(sjBlankNode, repoURIs.get(Uris.RR_CLASS_URI), typeBlankNode);
				}
			}
		}
		
		// Check if the subject map is a blank node
		if (sjMap.isBlankNode()) {
			con.add(sjBlankNode, repoURIs.get(Uris.RR_TERM_TYPE_URI), repoURIs.get(Uris.RR_BLANK_NODE_URI));
			
			// Add the prefix name for the blank node
			String prefix = mapping.getAuxInfo().getBlankNodesUriPrefixMap().get(sjMap.getId());
			Value prefixVal = f.createLiteral(prefix);
			con.add(sjBlankNode, repoURIs.get(Uris.KM_BLANK_NODE_PREFIX_URI), prefixVal);
		}
		else
		{
			// Print out the template for anything that isn't a blank node
			Value templVal = f.createLiteral(sjMap.getTemplate()
					.getR2rmlTemplateString(factory, columnNameFormatter));
			con.add(sjBlankNode, repoURIs.get(Uris.RR_TEMPLATE_URI), templVal);
		}
		
		// Mark as Steiner tree root node if required
		if (sjMap.isSteinerTreeRootNode()) {
			con.add(sjBlankNode, RDF.TYPE, repoURIs.get(Uris.KM_STEINER_TREE_ROOT_NODE));
		}
	}


	private void addPredicateObjectMaps(
			KR2RMLMapping mapping, Resource mappingRes, TriplesMap trMap,
			URI trMapUri, Workspace workspace, Worksheet worksheet) throws RepositoryException {

		// Add the predicate object maps
		for (PredicateObjectMap pom:trMap.getPredicateObjectMaps()) {
			addPredicateObjectMap(mapping, mappingRes, trMapUri, workspace, pom, worksheet);
		}
	}

	private void addPredicateObjectMap(KR2RMLMapping mapping, Resource mappingRes, URI trMapUri,
			Workspace workspace, PredicateObjectMap pom, Worksheet worksheet)
			throws RepositoryException {
		KR2RMLColumnNameFormatter columnNameFormatter = mapping.getColumnNameFormatter();
		RepFactory factory = workspace.getFactory();
		URI pomUri = f.createURI(Namespaces.KARMA_DEV + pom.getId());
		//BNode pomBlankNode = f.createBNode();
		
		boolean usablePredicate = addPredicate(pom, columnNameFormatter, factory, pomUri);
		if(!usablePredicate)
		{
			return;
		}
		
		addObject(mappingRes, pom, columnNameFormatter, factory, pomUri, worksheet);
		con.add(trMapUri, repoURIs.get(Uris.RR_PRED_OBJ_MAP_URI), pomUri);
		// Add the predicate object map type statement
		con.add(pomUri, RDF.TYPE, repoURIs.get(Uris.RR_PREDICATEOBJECTMAP_CLASS_URI));
		con.add(pomUri, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
		con.add(mappingRes, repoURIs.get(Uris.KM_HAS_PREDICATE_OBJECT_MAP_URI), pomUri);
		
	}
	
	private boolean addPredicate(PredicateObjectMap pom,
			KR2RMLColumnNameFormatter columnNameFormatter, RepFactory factory,
			URI pomUri) throws RepositoryException {
		// Add the predicate
		TemplateTermSet predTermSet = pom.getPredicate().getTemplate();
		if (predTermSet.isSingleUriString()) {
			URI predValUri = f.createURI(predTermSet
					.getR2rmlTemplateString(factory, columnNameFormatter));
			
			// Skip the class instance special meta property
			if (predValUri.stringValue().equals(Uris.CLASS_INSTANCE_LINK_URI))
				return false;
			
			con.add(pomUri, repoURIs.get(Uris.RR_PREDICATE_URI), predValUri);
		} else {
			Value predValLiteratl = f.createLiteral(predTermSet.
					getR2rmlTemplateString(factory, columnNameFormatter));
			con.add(pomUri, repoURIs.get(Uris.RR_PREDICATE_URI), predValLiteratl);
		}
		return true;
	}

	private void addObject(Resource mappingRes, PredicateObjectMap pom,
			KR2RMLColumnNameFormatter columnNameFormatter, RepFactory factory,
			URI pomUri, Worksheet worksheet) throws RepositoryException {
		// Add the object: Could be RefObjectMap or simple object with column values
		if (pom.getObject().hasRefObjectMap()) {
			RefObjectMap rfMap = pom.getObject().getRefObjectMap();
			URI rfUri = f.createURI(Namespaces.KARMA_DEV + rfMap.getId());
			con.add(rfUri, RDF.TYPE, repoURIs.get(Uris.RR_REF_OBJECT_MAP_CLASS_URI));
			con.add(rfUri, RDF.TYPE, repoURIs.get(Uris.RR_OBJECTMAP_CLASS_URI));
			
			TriplesMap prMap = rfMap.getParentTriplesMap();
			URI prMapUri = f.createURI(Namespaces.KARMA_DEV + prMap.getId());
			con.add(rfUri, repoURIs.get(Uris.RR_PARENT_TRIPLE_MAP_URI), prMapUri);
			con.add(rfUri, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
			con.add(mappingRes, repoURIs.get(Uris.KM_HAS_OBJECT_MAP_URI), rfUri);
			// Add the RefObjectMap as the object map of current POMap
			con.add(pomUri, repoURIs.get(Uris.RR_OBJECTMAP_URI), rfUri);
		} else {
			TemplateTermSet objTermSet = pom.getObject().getTemplate();
			TemplateTermSet rdfLiteralTypeTermSet = pom.getObject().getRdfLiteralType();
			TemplateTermSet languageTermSet = pom.getObject().getLanguage();
			
			if (objTermSet.isSingleColumnTerm()) {
				BNode cnBnode = f.createBNode();
				Value cnVal = f.createLiteral(objTermSet.
						getColumnNameR2RMLRepresentation(factory, columnNameFormatter));
				
				con.add(cnBnode, repoURIs.get(Uris.RR_COLUMN_URI), cnVal);
				con.add(cnBnode, RDF.TYPE, repoURIs.get(Uris.RR_OBJECTMAP_CLASS_URI));
				if (rdfLiteralTypeTermSet != null && rdfLiteralTypeTermSet.isSingleUriString()) {
					String rdfLiteralTypeString = rdfLiteralTypeTermSet.
							getR2rmlTemplateString(factory);
					if(!rdfLiteralTypeString.isEmpty())
					{
						Value cnRdfLiteralType = f.createLiteral(rdfLiteralTypeString);
						con.add(cnBnode, repoURIs.get(Uris.RR_DATATYPE_URI), cnRdfLiteralType);
					}

				}
				if(languageTermSet != null) {
					String languageString = languageTermSet.getR2rmlTemplateString(factory);
					if(!languageString.isEmpty()) {
						Value cnLanguage = f.createLiteral(languageString);
						con.add(cnBnode, repoURIs.get(Uris.RR_LANGUAGE_URI), cnLanguage);
					}
				}
				con.add(cnBnode, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
				con.add(mappingRes, repoURIs.get(Uris.KM_HAS_OBJECT_MAP_URI), cnBnode);
				
				// Add the link b/w blank node and object map
				con.add(pomUri, repoURIs.get(Uris.RR_OBJECTMAP_URI), cnBnode);
			}
			else if(!objTermSet.isEmpty())
			{
				BNode cnBnode = f.createBNode();
				// Print out the template for anything that isn't a blank node
				boolean isUri = false;
				
				for (TemplateTerm term:objTermSet.getAllTerms()) {
					if (term instanceof StringTemplateTerm && ((StringTemplateTerm)term).hasFullUri()) {
						isUri = true;
					}
				}
						
				String value = objTermSet
						.getR2rmlTemplateString(factory, columnNameFormatter);
				Value templVal;
				if(isUri) {
					try {
						templVal = f.createURI(value);
					}catch(IllegalArgumentException e) {
						String baseURI = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(
								Property.baseURI);
						templVal = f.createURI(baseURI + value);
					}
				} else {
					templVal = f.createLiteral(value);
				}
				con.add(cnBnode, repoURIs.get(Uris.RR_CONSTANT), templVal);
				if (rdfLiteralTypeTermSet != null && rdfLiteralTypeTermSet.isSingleUriString()) {
					String rdfLiteralTypeString = rdfLiteralTypeTermSet.
							getR2rmlTemplateString(factory);
					if(!rdfLiteralTypeString.isEmpty())
					{
						Value cnRdfLiteralType = f.createLiteral(rdfLiteralTypeString);
						con.add(cnBnode, repoURIs.get(Uris.RR_DATATYPE_URI), cnRdfLiteralType);
					}

				}
				if(languageTermSet != null) {
					String languageString = languageTermSet.getR2rmlTemplateString(factory);
					if(!languageString.isEmpty()) {
						Value cnLanguage = f.createLiteral(languageString);
						con.add(cnBnode, repoURIs.get(Uris.RR_LANGUAGE_URI), cnLanguage);
					}
				}
				//con.add(cnBnode, repoURIs.get(Uris.RR_TERM_TYPE_URI), repoURIs.get(Uris.RR_LITERAL_URI));
				con.add(cnBnode, RDF.TYPE, repoURIs.get(Uris.RR_OBJECTMAP_CLASS_URI));
				con.add(cnBnode, repoURIs.get(Uris.KM_IS_PART_OF_MAPPING_URI), mappingRes);
				con.add(mappingRes, repoURIs.get(Uris.KM_HAS_OBJECT_MAP_URI), cnBnode);
				
				//Add the link b/w blank node and object map
				con.add(pomUri, repoURIs.get(Uris.RR_OBJECTMAP_URI), cnBnode);
				
			}
		}
	}


	public void close() throws RepositoryException {
		con.close();
		myRepository.shutDown();
	}

	public void addCompleteWorksheetHistory(KR2RMLMapping mapping, Resource mappingRes) 
			throws RepositoryException {
		URI hasWorksheetHistoryUri = f.createURI(Uris.KM_HAS_WORKSHEET_HISTORY_URI);
		
			Value historyLiteral = f.createLiteral(mapping.getWorksheetHistory().toString(4));
			con.add(mappingRes, hasWorksheetHistoryUri, historyLiteral);
		
	}

	public void addWorksheetProperties(Worksheet worksheet, Resource mappingRes) throws RepositoryException {
		WorksheetProperties props = worksheet.getMetadataContainer().getWorksheetProperties();
		if (props == null) {
			return;
		}
		
		// Service options (if present)
		if (props.hasServiceProperties()) {
			if (props.getPropertyValue(Property.serviceUrl) == null) {
				return;
			}
			
			// Request method triple
			URI reqMethodUri = f.createURI(Uris.KM_SERVICE_REQ_METHOD_URI);
			Value method = f.createLiteral(props.getPropertyValue(Property.serviceRequestMethod));
			con.add(mappingRes, reqMethodUri, method);
			
			// Service Url triple
			URI serUrlUri = f.createURI(Uris.KM_SERVICE_URL_URI);
			Value servUrl = f.createLiteral(props.getPropertyValue(Property.serviceUrl));
			con.add(mappingRes, serUrlUri, servUrl);
			
			if (props.getPropertyValue(Property.serviceRequestMethod).equals(HttpMethods.POST.name())) {
				// POST method related option triple
				URI postMethodUri = f.createURI(Uris.KM_SERVICE_POST_METHOD_TYPE_URI);
				Value methodUrl = f.createLiteral(props.getPropertyValue(Property.serviceDataPostMethod));
				con.add(mappingRes, postMethodUri, methodUrl);
			}
		}
	}
}
