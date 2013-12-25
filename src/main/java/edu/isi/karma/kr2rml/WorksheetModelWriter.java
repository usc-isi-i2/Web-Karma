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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.http.HttpMethod;
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
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatter;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatterFactory;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.FileUtil;

public class WorksheetModelWriter {
	
	private KR2RMLColumnNameFormatter columnNameFormatter;
	private PrintWriter writer;
	private RepFactory factory;
	private OntologyManager ontMgr;
	private String worksheetName;
	private Repository myRepository;
	
	// Internal instance variables
	private RepositoryConnection con;
	private ValueFactory f;
	// Add a blank node of R2RML mapping
	private Resource mappingRes;
	private static Logger logger = LoggerFactory
			.getLogger(WorksheetModelWriter.class);
	
	public WorksheetModelWriter(PrintWriter writer, RepFactory factory, OntologyManager ontMgr, 
			Worksheet worksheet)
			throws RepositoryException {
		this.writer = writer;
		this.factory = factory;
		this.ontMgr = ontMgr;
		
		/** Initialize an in-memory sesame triple store **/
		myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		con = myRepository.getConnection();
		f = myRepository.getValueFactory();
		
		/** Create resource for the mapping as a blank node **/
		URI r2rmlMapUri = f.createURI(Uris.KM_R2RML_MAPPING_URI);
		URI sourceNameUri = f.createURI(Uris.KM_SOURCE_NAME_URI);
		URI modelVersion = f.createURI(Uris.KM_MODEL_VERSION_URI);
		mappingRes = f.createBNode();
		con.add(mappingRes, RDF.TYPE, r2rmlMapUri);
		this.worksheetName = worksheet.getTitle();
		Value srcNameVal = f.createLiteral(worksheetName);
		con.add(mappingRes, sourceNameUri, srcNameVal);
		
		// Add the timestamp
		URI pubTime = f.createURI(Uris.KM_MODEL_PUBLICATION_TIME_URI);
		con.add(mappingRes, pubTime, f.createLiteral(new Date().getTime()));
		
		// Add the version
		con.add(mappingRes, modelVersion, f.createLiteral(KR2RMLVersion.getCurrent().toString()));
		
		// Find source type
		String sourceType = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.sourceType);
		columnNameFormatter = KR2RMLColumnNameFormatterFactory.getFormatter(SourceTypes.valueOf(sourceType));
		URI sourceTypeURI = f.createURI(Uris.KM_SOURCE_TYPE_URI);
		con.add(mappingRes, sourceTypeURI, f.createLiteral(sourceType));
	}
	
	public boolean writeR2RMLMapping(OntologyManager ontManager, KR2RMLMappingGenerator mappingGen)
			throws RepositoryException, JSONException {
		/** Get the required data structures of R2RML **/
		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
		KR2RMLMappingAuxillaryInformation auxInfo = mapping.getAuxInfo();
		List<TriplesMap> triplesMapList = mapping.getTriplesMapList();
		
		try {
			
			URI trTypeUri = f.createURI(Uris.RR_TRIPLESMAP_CLASS_URI);
			URI templateUri = f.createURI(Uris.RR_TEMPLATE_URI);
			URI subjMapUri = f.createURI(Uris.RR_SUBJECTMAP_URI);
			URI predUri = f.createURI(Uris.RR_PREDICATE_URI);
			URI objectMapUri = f.createURI(Uris.RR_OBJECTMAP_URI);
			URI columnUri = f.createURI(Uris.RR_COLUMN_URI);
			URI rdfLiteralTypeUri = f.createURI(Uris.RR_DATATYPE_URI);
			URI rfObjClassUri = f.createURI(Uris.RR_REF_OBJECT_MAP_URI);
			URI parentTriplesMapUri = f.createURI(Uris.RR_PARENT_TRIPLE_MAP_URI);
			URI predObjMapMapUri = f.createURI(Uris.RR_PRED_OBJ_MAP_URI);
			URI blankNodeUri = f.createURI(Uris.RR_BLANK_NODE_URI);
			URI termTypeUri = f.createURI(Uris.RR_TERM_TYPE_URI);
			URI logicalTableUri = f.createURI(Uris.RR_LOGICAL_TABLE_URI);
			URI tableNameUri = f.createURI(Uris.RR_TABLENAME_URI);
			URI classUri = f.createURI(Uris.RR_CLASS_URI);
			
			URI bnNamePrefixUri = f.createURI(Uris.KM_BLANK_NODE_PREFIX_URI);
			URI nodeIdUri = f.createURI(Uris.KM_NODE_ID_URI);
			URI steinerTreeRootNodeUri = f.createURI(Uris.KM_STEINER_TREE_ROOT_NODE);
			URI hasTrMapUri = f.createURI(Uris.KM_HAS_TRIPLES_MAP_URI);
			
			
			/** Add all the triple maps **/
			for (TriplesMap trMap:triplesMapList) {
				URI trMapUri = f.createURI(Namespaces.KARMA_DEV + trMap.getId());
				// Add the triples map type statement
				con.add(trMapUri, RDF.TYPE, trTypeUri);
				// Associate it with the source mapping URI
				con.add(mappingRes, hasTrMapUri, trMapUri);
				
				// Add the Logical table information
				BNode logTableBNode = f.createBNode();
				con.add(logTableBNode, tableNameUri, f.createLiteral(worksheetName));
				con.add(trMapUri, logicalTableUri, logTableBNode);
				
				// Add the subject map statements
				SubjectMap sjMap = trMap.getSubject();
				BNode sjBlankNode = f.createBNode();
				con.add(trMapUri, subjMapUri, sjBlankNode);

				// Add the node id for the subject
				Value nodeIdVal = f.createLiteral(sjMap.getId());
				con.add(sjBlankNode, nodeIdUri, nodeIdVal);
				
				// Add the type for subject maps
				List<TemplateTermSet> rdfsTypes = sjMap.getRdfsType();
				for (TemplateTermSet typeTermSet:rdfsTypes) {
					if (typeTermSet.isSingleUriString()) {
						URI sjTypeUri = f.createURI(typeTermSet.getR2rmlTemplateString(factory, columnNameFormatter));
						con.add(sjBlankNode, classUri, sjTypeUri);
					} else {
						if (typeTermSet.isSingleColumnTerm()) {
							BNode typeBlankNode = f.createBNode();
							String colRepr  = typeTermSet.getR2rmlTemplateString(factory, columnNameFormatter);
							con.add(typeBlankNode, templateUri, f.createLiteral(colRepr));
							con.add(sjBlankNode, classUri, typeBlankNode);
						}
					}
				}
				
				// Check if the subject map is a blank node
				if (sjMap.isBlankNode()) {
					con.add(sjBlankNode, termTypeUri, blankNodeUri);
					
					// Add the prefix name for the blank node
					String prefix = auxInfo.getBlankNodesUriPrefixMap().get(sjMap.getId());
					Value prefixVal = f.createLiteral(prefix);
					con.add(sjBlankNode, bnNamePrefixUri, prefixVal);
				}
				else
				{
					// Print out the template for anything that isn't a blank node
					Value templVal = f.createLiteral(sjMap.getTemplate()
							.getR2rmlTemplateString(factory, columnNameFormatter));
					con.add(sjBlankNode, templateUri, templVal);
				}
				
				// Mark as Steiner tree root node if required
				if (sjMap.isSteinerTreeRootNode()) {
					con.add(sjBlankNode, RDF.TYPE, steinerTreeRootNodeUri);
				}
				
				// Add the predicate object maps
				for (PredicateObjectMap pom:trMap.getPredicateObjectMaps()) {
					BNode pomBlankNode = f.createBNode();
					// Add the predicate
					TemplateTermSet predTermSet = pom.getPredicate().getTemplate();
					if (predTermSet.isSingleUriString()) {
						URI predValUri = f.createURI(predTermSet
								.getR2rmlTemplateString(factory, columnNameFormatter));
						
						// Skip the class instance special meta property
						if (predValUri.stringValue().equals(Uris.CLASS_INSTANCE_LINK_URI)) continue;
						
						con.add(pomBlankNode, predUri, predValUri);
					} else {
						Value predValLiteratl = f.createLiteral(predTermSet.
								getR2rmlTemplateString(factory, columnNameFormatter));
						con.add(pomBlankNode, predUri, predValLiteratl);
					}
					
					// Add the object: Could be RefObjectMap or simple object with column values
					if (pom.getObject().hasRefObjectMap()) {
						RefObjectMap rfMap = pom.getObject().getRefObjectMap();
						URI rfUri = f.createURI(Namespaces.KARMA_DEV + rfMap.getId());
						con.add(rfUri, RDF.TYPE, rfObjClassUri);
						
						TriplesMap prMap = rfMap.getParentTriplesMap();
						URI prMapUri = f.createURI(Namespaces.KARMA_DEV + prMap.getId());
						con.add(rfUri, parentTriplesMapUri, prMapUri);
						
						// Add the RefObjectMap as the object map of current POMap
						con.add(pomBlankNode, objectMapUri, rfUri);
					} else {
						TemplateTermSet objTermSet = pom.getObject().getTemplate();
						TemplateTermSet rdfLiteralTypeTermSet = pom.getObject().getRdfLiteralType();
							
						if (objTermSet.isSingleColumnTerm()) {
							BNode cnBnode = f.createBNode();
							Value cnVal = f.createLiteral(objTermSet.
									getColumnNameR2RMLRepresentation(factory, columnNameFormatter));
							con.add(cnBnode, columnUri, cnVal);

							if (rdfLiteralTypeTermSet != null && rdfLiteralTypeTermSet.isSingleUriString()) {
								Value cnRdfLiteralType = f.createLiteral(rdfLiteralTypeTermSet.
									getR2rmlTemplateString(factory));
								con.add(cnBnode, rdfLiteralTypeUri, cnRdfLiteralType);

							}
							
							// Add the link b/w blank node and object map
							con.add(pomBlankNode, objectMapUri, cnBnode);
						}
					}
					con.add(trMapUri, predObjMapMapUri, pomBlankNode);
				}
			}
			
			Map<String, String> prefixMap = ontMgr.getPrefixMap(); 
			for (String ns:prefixMap.keySet()) {
				String prefix = prefixMap.get(ns);
				con.setNamespace(prefix, ns);
			}
			con.setNamespace(Prefixes.RR, Namespaces.RR);
			con.setNamespace(Prefixes.KARMA_DEV, Namespaces.KARMA_DEV);
			
			RDFHandler rdfxmlWriter = new TurtleWriter(writer);
			con.export(rdfxmlWriter);
			
		} catch (OpenRDFException e) {
			logger.error("Error occured while generating RDF representation of R2RML data " +
					"structures.", e);
		} finally {
			con.close();
		}
		return true;
	}

	public void close() throws RepositoryException {
		con.close();
		myRepository.shutDown();
	}

	public void writeCompleteWorksheetHistory(String historyFilePath) 
			throws RepositoryException {
		File historyFile = new File(historyFilePath);
		URI hasWorksheetHistoryUri = f.createURI(Uris.KM_HAS_WORKSHEET_HISTORY_URI);
		if (!historyFile.exists()) {
			logger.error("Worksheet history file not found! Can't write worksheet history " +
					"into R2RML model. Path:" + historyFile.getAbsolutePath());
			return;
		}
		try {
			String encoding = EncodingDetector.detect(historyFile);
			String historyJsonStr = FileUtil.readFileContentsToString(historyFile, encoding);
			Value historyLiteral = f.createLiteral(historyJsonStr);
			con.add(mappingRes, hasWorksheetHistoryUri, historyLiteral);
		} catch (IOException e) {
			logger.error("IO Exception occured while writing worksheet history into R2RML model", e);
			return;
		}
	}
	
	public void writeWorksheetProperties(Worksheet worksheet) throws RepositoryException {
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
			
			if (props.getPropertyValue(Property.serviceRequestMethod).equals(HttpMethod.POST.name())) {
				// POST method related option triple
				URI postMethodUri = f.createURI(Uris.KM_SERVICE_POST_METHOD_TYPE_URI);
				Value methodUrl = f.createLiteral(props.getPropertyValue(Property.serviceDataPostMethod));
				con.add(mappingRes, postMethodUri, methodUrl);
			}
		}
	}
}
