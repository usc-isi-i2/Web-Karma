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

package edu.isi.karma.kr2rml.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.ObjectMap;
import edu.isi.karma.kr2rml.Predicate;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapLink;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.StringTemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetBuilder;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;

@Deprecated
public class WorksheetR2RMLSesameModelParser {
	private Repository myRepository;
	private RepositoryConnection con;
	private ValueFactory f;
	
	// Internal data structures required
	private Map<String, SubjectMap> subjectMapIndex;
	private Map<String, TriplesMap> triplesMapIndex;
	private KR2RMLMappingAuxillaryInformation auxInfo;
	private R2RMLMapping r2rmlMapping;
	private int predicateIdCounter = 1;
	private int objectMapCounter = 1;
	private static Logger logger = LoggerFactory.getLogger(WorksheetR2RMLSesameModelParser.class);
	
	public WorksheetR2RMLSesameModelParser(Worksheet worksheet, RepFactory factory, R2RMLMappingIdentifier id) 
			throws RepositoryException, RDFParseException, IOException, JSONException {
		this.r2rmlMapping = new R2RMLMapping(id);
		this.auxInfo = new KR2RMLMappingAuxillaryInformation();
		this.subjectMapIndex = new HashMap<String, SubjectMap>();
		this.triplesMapIndex = new HashMap<String, TriplesMap>();
		/** Initialize the repository **/
		myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		con = myRepository.getConnection();
		f = con.getValueFactory();
		con.add(id.getLocation(), "", RDFFormat.TURTLE);
		
		// Generate TriplesMap for each InternalNode in the tree
		createSubjectMaps();
		
		// Identify the object property links
		createPredicateObjectMaps();
		
		// Calculate the nodes covered by each InternalNode
		calculateColumnNodesCoveredByBlankNodes();
		
		con.close();
		myRepository.shutDown();
	}
	
	public KR2RMLMappingAuxillaryInformation getAuxInfo() {
		return auxInfo;
	}

	public R2RMLMapping getR2rmlMapping() {
		return r2rmlMapping;
	}

	private void createPredicateObjectMaps() 
			throws RepositoryException, JSONException {
		URI trTypeUri = f.createURI(Uris.RR_TRIPLESMAP_CLASS_URI);
		
		// Get all the triple maps
		RepositoryResult<Statement> tripleMapsStmts = con.getStatements(null, RDF.TYPE, 
				trTypeUri, false);
		while (tripleMapsStmts.hasNext()) {
			Statement st = tripleMapsStmts.next();
			Resource trMapRes = st.getSubject();
			
			// Add the predicate object maps
			addPredicateObjectMapsForTripleMap(trMapRes);
		}
	}

	private void createSubjectMaps() throws RepositoryException, 
			JSONException {
		URI trTypeUri = f.createURI(Uris.RR_TRIPLESMAP_CLASS_URI);
		
		// Get all the triple maps
		RepositoryResult<Statement> tripleMapsStmts = con.getStatements(null, RDF.TYPE, 
				trTypeUri, false);
		while (tripleMapsStmts.hasNext()) {
			Statement st = tripleMapsStmts.next();
			Resource trMapRes = st.getSubject();
			
			SubjectMap subjMap = addSubjectMapForTripleMap(trMapRes);
			// Add the Triples map
			TriplesMap trMap = new TriplesMap(trMapRes.stringValue(), subjMap);
			this.triplesMapIndex.put(trMapRes.stringValue(), trMap);
			this.r2rmlMapping.addTriplesMap(trMap);
		}
	}


	private void addPredicateObjectMapsForTripleMap(Resource trMapRes) 
			throws RepositoryException, JSONException {
		URI predObjMapMapUri = f.createURI(Uris.RR_PRED_OBJ_MAP_URI);
		URI predUri = f.createURI(Uris.RR_PREDICATE_URI);
		URI objectMapUri = f.createURI(Uris.RR_OBJECTMAP_URI);
		URI columnUri = f.createURI(Uris.RR_COLUMN_URI);
		URI rdfLiteralTypeUri = f.createURI(Uris.RR_DATATYPE_URI);
		URI rfObjClassUri = f.createURI(Uris.RR_REF_OBJECT_MAP_URI);
		URI parentTriplesMapUri = f.createURI(Uris.RR_PARENT_TRIPLE_MAP_URI);
		
		RepositoryResult<Statement> predObjStmts = con.getStatements(trMapRes, predObjMapMapUri, 
				null, false);
		TriplesMap trMap = this.triplesMapIndex.get(trMapRes.stringValue());
		if (trMap == null) {
			logger.error("No Triples Map found for resource: " + trMapRes.stringValue());
			return;
		}
		while (predObjStmts.hasNext()) {
			Statement pomStmt = predObjStmts.next();
			Resource pomBlankNode = (Resource) pomStmt.getObject();
			
			// Create the PredicateObjectMap object for current POM
			PredicateObjectMap pom = new PredicateObjectMap(trMap);
			
			// Get the predicate for the POM
			Predicate pred = null;
			RepositoryResult<Statement> predStmts = con.getStatements(pomBlankNode, 
					predUri, null, false);
			while (predStmts.hasNext()) {
				Statement predStmt = predStmts.next();
				Value predVal = predStmt.getObject();
				
				pred = new Predicate(predVal.stringValue() + "-" + getNewPredicateId());
				// Check if the predicate value is a URI or a literal (such as column name)
				if (predVal instanceof Resource) {
					pred.getTemplate().addTemplateTermToSet(
							new StringTemplateTerm(predVal.stringValue(), true));
				} else {
					pred.setTemplate(TemplateTermSetBuilder.
							constructTemplateTermSetFromR2rmlTemplateString(
									predVal.stringValue()));
				}
			}
			pom.setPredicate(pred);
			
			// Get the object for the POM
			ObjectMap objMap = null;
			RepositoryResult<Statement> objMapStmts = con.getStatements(pomBlankNode, 
					objectMapUri, null, false);
			while (objMapStmts.hasNext()) {
				Statement objMapStmt = objMapStmts.next();
				Resource objNode = (Resource) objMapStmt.getObject();
				/** Check if objBlankNode is a RefObjectMap or a normal object map with column **/
				if (con.hasStatement(objNode, RDF.TYPE, rfObjClassUri, false)) {
					RepositoryResult<Statement> parentTripleMapStmts = con.getStatements(objNode, 
							parentTriplesMapUri, null, false);
					while (parentTripleMapStmts.hasNext()) {
						Statement parentTripleMapStmt = parentTripleMapStmts.next();
						Resource parentTripleRes = (Resource) parentTripleMapStmt.getObject();
						TriplesMap parentTM = this.triplesMapIndex.get(parentTripleRes.stringValue());
						
						// Create a RefObjectMap
						RefObjectMap rfMap = new RefObjectMap(objNode.stringValue(), parentTM);
						objMap = new ObjectMap(getNewObjectMapId(), rfMap);
						
						// Add the link between triple maps in the auxInfo
						TriplesMapLink link = new TriplesMapLink(trMap, parentTM, pom);  
						this.auxInfo.getTriplesMapGraph().addLink(link);
					}
				} else {
					RepositoryResult<Statement> objMapColStmts = con.getStatements(objNode, 
							columnUri, null, false);
					
					// RDF Literal Type
					RepositoryResult<Statement> objMapRdfLiteralTypeStmt = con.getStatements(objNode, 
							rdfLiteralTypeUri, null, false);
					TemplateTermSet rdfLiteralTypeTermSet = null;
					if (objMapRdfLiteralTypeStmt != null && objMapRdfLiteralTypeStmt.hasNext()) {
						StringTemplateTerm rdfLiteralTypeTerm = 
								new StringTemplateTerm(objMapRdfLiteralTypeStmt.next().getObject().stringValue(), true);
						rdfLiteralTypeTermSet = new TemplateTermSet();
						rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);
					}
					while (objMapColStmts.hasNext()) {
						Statement objMapColStmt = objMapColStmts.next(); 
						Value colVal = objMapColStmt.getObject();
						objMap = new ObjectMap(getNewObjectMapId(), 
								TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlColumnString(
										colVal.stringValue()), rdfLiteralTypeTermSet);
					}
					// Check if anything needs to be added to the hNodeIdToPredicateObjectMap Map
					addHNodeIdToPredObjectMapLink(objMap, pom);
				}
			}
			pom.setObject(objMap);
			trMap.addPredicateObjectMap(pom);
		}
	}
	
	private void addHNodeIdToPredObjectMapLink(ObjectMap objMap, PredicateObjectMap pom) {
		TemplateTermSet objTermSet = objMap.getTemplate();
		for (TemplateTerm term:objTermSet.getAllTerms()) {
			if (term instanceof ColumnTemplateTerm) {
				String hNodeId = term.getTemplateTermValue();
				List<PredicateObjectMap> existingPomList = this.auxInfo.
						getColumnNameToPredObjLinks().get(hNodeId);  
				if (existingPomList == null) {
					existingPomList = new ArrayList<PredicateObjectMap>();
				}
				existingPomList.add(pom);
				this.auxInfo.getColumnNameToPredObjLinks().put(hNodeId, existingPomList);
			}
		}
	}

	private int getNewPredicateId() {
		return predicateIdCounter++;
	}
	
	private String getNewObjectMapId() {
		return "ObjectMap" + objectMapCounter++;
	}

	private SubjectMap addSubjectMapForTripleMap(Resource trMapRes) 
			throws RepositoryException, JSONException {
		SubjectMap subjMap = null;
		URI subjMapUri = f.createURI(Uris.RR_SUBJECTMAP_URI);
		URI templateUri = f.createURI(Uris.RR_TEMPLATE_URI);
		URI steinerTreeRootNodeUri = f.createURI(Uris.KM_STEINER_TREE_ROOT_NODE);
		
		RepositoryResult<Statement> subjMapStmts = con.getStatements(trMapRes, subjMapUri, 
				null, false);
		while (subjMapStmts.hasNext()) {
			Statement subjMapStmt = subjMapStmts.next();
			Resource subjMapBlankNode = (Resource) subjMapStmt.getObject();
			String subjMapId = subjMapBlankNode.stringValue();
			subjMap = new SubjectMap(subjMapId);
			this.subjectMapIndex.put(subjMapId, subjMap);
			
			// Get the subject template
			TemplateTermSet subjTemplTermSet = null;
			RepositoryResult<Statement> templates = con.getStatements(subjMapBlankNode, 
					templateUri, null, false);
			while (templates.hasNext()) {
				Statement templStmt = templates.next();
				logger.debug("Template: " + templStmt.getObject().stringValue());
				subjTemplTermSet = TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlTemplateString(
						templStmt.getObject().stringValue());
			}
			subjMap.setTemplate(subjTemplTermSet);
			
			// Get the subject type
			RepositoryResult<Statement> rdfTypes = con.getStatements(subjMapBlankNode, 
					RDF.TYPE, null, false);
			while (rdfTypes.hasNext()) {
				Statement typeStmt = rdfTypes.next();
				if (typeStmt.getObject() instanceof Resource) {
					// Skip the steiner tree root type
					if(typeStmt.getObject().stringValue().equals(Uris.KM_STEINER_TREE_ROOT_NODE))
						continue;
					
					StringTemplateTerm uriTerm = new StringTemplateTerm(
							typeStmt.getObject().stringValue(), true);
					TemplateTermSet typeTermSet = new TemplateTermSet();
					typeTermSet.addTemplateTermToSet(uriTerm);
					subjMap.addRdfsType(typeTermSet);
				} else {
					TemplateTermSet typeTermSet = TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlTemplateString(
							typeStmt.getObject().stringValue());
					subjMap.addRdfsType(typeTermSet);
				}
				
			}
			
			// Check if it is as the Steiner tree root node
			if (con.hasStatement(subjMapBlankNode, RDF.TYPE, steinerTreeRootNodeUri, false)) {
				subjMap.setAsSteinerTreeRootNode(true);
			}
		}
		return subjMap;
	}

	private void calculateColumnNodesCoveredByBlankNodes() 
			throws RepositoryException, JSONException {
		URI termTypeUri = f.createURI(Uris.RR_TERM_TYPE_URI);
		URI blankNodeUri = f.createURI(Uris.RR_BLANK_NODE_URI);
		URI kmBnodePrefixUri = f.createURI(Uris.KM_BLANK_NODE_PREFIX_URI);
		
		RepositoryResult<Statement> blankNodeSubjectMapStmts = con.getStatements(null, termTypeUri, 
				blankNodeUri, false);
		while (blankNodeSubjectMapStmts.hasNext()) {
			Resource blankNodeSubjRes = blankNodeSubjectMapStmts.next().getSubject();
			SubjectMap subjMap = this.subjectMapIndex.get(blankNodeSubjRes.stringValue());
			subjMap.setAsBlankNode(true);
			
			List<String> columnsCoveredHnodeIds = new ArrayList<String>();
			TriplesMap mytm = null;
			for(TriplesMap tm : r2rmlMapping.getTriplesMapList())
			{
				if(tm.getSubject().getId().equalsIgnoreCase(subjMap.getId()))
				{
					mytm = tm;
					
					List<PredicateObjectMap> poms = mytm.getPredicateObjectMaps();
					for(PredicateObjectMap pom : poms )
					{
						TemplateTermSet templateTermSet = pom.getObject().getTemplate();
						if(templateTermSet != null)
						{
							TemplateTerm term = templateTermSet.getAllTerms().get(0);
							if(term!= null)
							{
								columnsCoveredHnodeIds.add(term.getTemplateTermValue());
							}
						}
					}
					break;
				}
			}
			logger.debug("Adding columns for blank node" + subjMap.getId() + " List: " + columnsCoveredHnodeIds);
			this.auxInfo.getBlankNodesColumnCoverage().put(subjMap.getId(), columnsCoveredHnodeIds);
			
			// Get the blank node prefix
			RepositoryResult<Statement> bnodePrefixStmts = con.getStatements(blankNodeSubjRes, 
					kmBnodePrefixUri, null, false);
			while (bnodePrefixStmts.hasNext()) {
				this.auxInfo.getBlankNodesUriPrefixMap().put(subjMap.getId(), 
						bnodePrefixStmts.next().getObject().stringValue());
			}
		}
	}

}
