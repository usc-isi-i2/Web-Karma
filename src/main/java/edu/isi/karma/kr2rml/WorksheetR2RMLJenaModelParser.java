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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class WorksheetR2RMLJenaModelParser {
	private Model model;
	private String sourceName;
	
	private Worksheet worksheet;
	private Workspace workspace;
	private RepFactory factory;
	
	// Internal data structures required
	private Map<String, SubjectMap> subjectMapIndex;
	private Map<String, TriplesMap> triplesMapIndex;
	private KR2RMLMappingAuxillaryInformation auxInfo;
	private R2RMLMapping r2rmlMapping;
	private int predicateIdCounter = 1;
	private int objectMapCounter = 1;
	private List<Resource> subjectMapResources;
	private static Logger logger = LoggerFactory.getLogger(WorksheetR2RMLJenaModelParser.class);
	
	
	public WorksheetR2RMLJenaModelParser(Worksheet worksheet, Workspace workspace, R2RMLMappingIdentifier id) throws IOException, JSONException, KarmaException {
		
		this.model = loadSourceModelIntoJenaModel(id.getLocation());
		this.sourceName = id.getName();
		
		this.worksheet = worksheet;
		this.workspace = workspace;
		this.factory = workspace.getFactory();
		
		
		this.r2rmlMapping = new R2RMLMapping(id);
		this.auxInfo = new KR2RMLMappingAuxillaryInformation();
		this.subjectMapIndex = new HashMap<String, SubjectMap>();
		this.triplesMapIndex = new HashMap<String, TriplesMap>();
		this.subjectMapResources = new ArrayList<Resource>();
		
		// Capture the main mapping resource that corresponds to the source name
		Resource mappingResource = getMappingResourceFromSourceName();
		if (mappingResource == null) {
			throw new KarmaException("Resource not found in model for the source: " + sourceName);
		}
		
		// Perform any transformations on the worksheet if required
		performTransformations(mappingResource);
		
		// Generate TriplesMap for each InternalNode in the tree
		createSubjectMaps(mappingResource);
		
		// Identify the object property links
		createPredicateObjectMaps(mappingResource);
		
		// Calculate the nodes covered by each InternalNode
		calculateColumnNodesCoveredByBlankNodes();
	}

    private static Model loadSourceModelIntoJenaModel(URL modelURL) throws IOException {
        // Create an empty Model
        Model model = ModelFactory.createDefaultModel();
        InputStream s = modelURL.openStream();
        model.read(s, null, "TURTLE");
        return model;
    }
	private Resource getMappingResourceFromSourceName() throws KarmaException {
		Property sourceNameProp = model.getProperty(Uris.KM_SOURCE_NAME_URI);
		RDFNode node = model.createLiteral(sourceName);
		ResIterator res = model.listResourcesWithProperty(sourceNameProp, node);
		List<Resource> resList = res.toList();
		
		if (resList.size() > 1) {
			throw new KarmaException("More than one resource exists with source name: " + sourceName);
		} else if (resList.size() == 1) {
			return resList.get(0);
		} else {
			//If we didnt find the sourceName in the model, maybe it is a different source with the
			//same schema.
			//Maybe we need to substitute the sourceName in the model with this one
			NodeIterator sourceObjectIter = model.listObjectsOfProperty(sourceNameProp);
			List<RDFNode> sourceObjects = sourceObjectIter.toList();
			
			if(sourceObjects.size() > 1) {
				throw new KarmaException("More than one resource exists with source name: " + sourceName);
			} else if(sourceObjects.size() == 1) {
				RDFNode prevSourceObject = sourceObjects.get(0);
				
				//We got the previous source object, now get the Subject Node for this
				ResIterator prevSourceSubjectsIter = model.listResourcesWithProperty(sourceNameProp, prevSourceObject);
				List<Resource> prevSourceSubjects = prevSourceSubjectsIter.toList();
				
				if (prevSourceSubjects.size() == 1) {
					Resource subject = prevSourceSubjects.get(0);
					model.remove(subject, sourceNameProp, prevSourceObject);
					model.add(subject, sourceNameProp, node);
					return subject;
				} else if(prevSourceSubjects.size() > 1) {
					throw new KarmaException("More than one resource exists with model source name: " + prevSourceObject.toString());
				}
			}
			return null;
		}
	}
	
	private void performTransformations(Resource mappingResource) throws JSONException {
		JSONArray normalizedCommandsJSON = getWorksheetHistory(mappingResource);
		WorksheetCommandHistoryExecutor wchr = new WorksheetCommandHistoryExecutor(worksheet.getId(), workspace);
		try
		{
			List<CommandTag> tags = new ArrayList<CommandTag>();
			tags.add(CommandTag.Transformation);
			wchr.executeCommandsByTags(tags, normalizedCommandsJSON);
		}
		catch (CommandException | KarmaException e)
		{
			logger.error("Unable to execute column transformations", e);
		}
	}

	private JSONArray getWorksheetHistory(Resource mappingResource) throws JSONException {
		Property hasTransformation = model.getProperty(Uris.KM_HAS_WORKSHEET_HISTORY_URI);
		NodeIterator transItr = model.listObjectsOfProperty(mappingResource, hasTransformation);
		while (transItr.hasNext()) {
			String commands = transItr.next().toString();
			return new JSONArray(commands);
		}
		return new JSONArray();
	}

	private void createPredicateObjectMaps(Resource mappingResource) throws JSONException {
		Property hasTrMapUri = model.getProperty(Uris.KM_HAS_TRIPLES_MAP_URI);
		
		// Get all the triple maps
		NodeIterator trMapsResItr = model.listObjectsOfProperty(mappingResource, hasTrMapUri);
		while (trMapsResItr.hasNext()) {
			// Add the predicate object maps
			addPredicateObjectMapsForTripleMap(trMapsResItr.next().asResource());
		}
	}

	private void createSubjectMaps(Resource mappingResource) throws JSONException {
		Property hasTrMapUri = model.getProperty(Uris.KM_HAS_TRIPLES_MAP_URI);
		
		// Get all the triple maps
		NodeIterator trMapsResItr = model.listObjectsOfProperty(mappingResource, hasTrMapUri);
		while (trMapsResItr.hasNext()) {
			Resource trMapRes = trMapsResItr.next().asResource();
			SubjectMap subjMap = addSubjectMapForTripleMap(trMapRes);
			
			// Add the Triples map
			TriplesMap trMap = new TriplesMap(trMapRes.getURI(), subjMap);
			this.triplesMapIndex.put(trMapRes.getURI(), trMap);
			this.r2rmlMapping.addTriplesMap(trMap);
		}
	}


	private void addPredicateObjectMapsForTripleMap(Resource trMapRes) throws  JSONException {
		Property predObjMapProp = model.getProperty(Uris.RR_PRED_OBJ_MAP_URI);
		Property predProp = model.getProperty(Uris.RR_PREDICATE_URI);
		Property objectMapProp = model.getProperty(Uris.RR_OBJECTMAP_URI);
		Property columnProp = model.getProperty(Uris.RR_COLUMN_URI);
		Property rdfLiteralTypeProp = model.getProperty(Uris.RR_DATATYPE_URI);
		Resource rfObjClassUri = model.getResource(Uris.RR_REF_OBJECT_MAP_URI);
		Property parentTriplesMapProp = model.getProperty(Uris.RR_PARENT_TRIPLE_MAP_URI);
		Property rdfTypeProp = model.getProperty(Uris.RDF_TYPE_URI);
		
		TriplesMap trMap = this.triplesMapIndex.get(trMapRes.getURI());
		if (trMap == null) {
			logger.error("No Triples Map found for resource: " + trMapRes.getURI());
			return;
		}
		NodeIterator predObjItr = model.listObjectsOfProperty(trMapRes, predObjMapProp);
		while (predObjItr.hasNext()) {
			Resource pomBlankNode = predObjItr.next().asResource();
			// Create the PredicateObjectMap object for current POM
			PredicateObjectMap pom = new PredicateObjectMap(trMap);
			
			// Get the predicate for the POM
			Predicate pred = null;
			NodeIterator pomPredItr = model.listObjectsOfProperty(pomBlankNode, predProp); 
			while (pomPredItr.hasNext()) {
				RDFNode pomPredNode = pomPredItr.next();
				pred = new Predicate(pomPredNode.toString() + "-" + getNewPredicateId());
				
				// Check if the predicate value is a URI or a literal (such as column name)
				if (pomPredNode instanceof Resource) {
					pred.getTemplate().addTemplateTermToSet(
							new StringTemplateTerm(((Resource) pomPredNode).getURI(), true));
				} else {
					pred.setTemplate(TemplateTermSetBuilder.
							constructTemplateTermSetFromR2rmlTemplateString(
									pomPredNode.toString(), worksheet, factory));
				}
			}
			pom.setPredicate(pred);
			
			// Get the object for the POM
			ObjectMap objMap = null;
			NodeIterator pomObjItr = model.listObjectsOfProperty(pomBlankNode, objectMapProp);
			
			while (pomObjItr.hasNext()) {
				Resource objNode = pomObjItr.next().asResource();
				
				/** Check if objBlankNode is a RefObjectMap or a normal object map with column **/
				if (model.contains(objNode, rdfTypeProp, rfObjClassUri)) {
					NodeIterator parentTripleMapItr = model.listObjectsOfProperty(objNode, 
							parentTriplesMapProp);
					while (parentTripleMapItr.hasNext()) {
						Resource parentTripleRes = parentTripleMapItr.next().asResource();
						TriplesMap parentTM = this.triplesMapIndex.get(parentTripleRes.getURI());
						
						// Create a RefObjectMap
						RefObjectMap rfMap = new RefObjectMap(objNode.getURI(), parentTM);
						objMap = new ObjectMap(getNewObjectMapId(), rfMap);
						
						// Add the link between triple maps in the auxInfo
						TriplesMapLink link = new TriplesMapLink(trMap, parentTM, pom);  
						this.auxInfo.getTriplesMapGraph().addLink(link);
					}
				} else {
					NodeIterator objMapColStmts = model.listObjectsOfProperty(objNode, columnProp);
					
					// RDF Literal Type
					Statement objMapRdfLiteralTypeStmt = model.getProperty(objNode, rdfLiteralTypeProp);
					TemplateTermSet rdfLiteralTypeTermSet = null;
					if (objMapRdfLiteralTypeStmt != null && objMapRdfLiteralTypeStmt.getObject().isLiteral()) {
						StringTemplateTerm rdfLiteralTypeTerm = 
								new StringTemplateTerm(objMapRdfLiteralTypeStmt.getObject().toString(), true);
						rdfLiteralTypeTermSet = new TemplateTermSet();
						rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);
					}
					while (objMapColStmts.hasNext()) {
						RDFNode colNode = objMapColStmts.next(); 
						objMap = new ObjectMap(getNewObjectMapId(), 
								TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlColumnString(
										colNode.toString(), worksheet, factory), rdfLiteralTypeTermSet);
					}
					// Check if anything needs to be added to the hNodeIdToPredicateObjectMap Map
					addHNodeIdToPredObjectMapLink(objMap, pom);
				}
			}
			pom.setObject(objMap);
			trMap.addPredicateObjectMap(pom);
		}
		
		// If there are no poms, let's see if we can translate a template into a POM
		// TODO clean this up.
		if(trMap.getPredicateObjectMaps().isEmpty())
		{
			TemplateTermSet subjTemplTermSet = trMap.getSubject().getTemplate();
			List<TemplateTerm> terms = subjTemplTermSet.getAllTerms();
			if(terms != null && terms.size() == 1 && terms.get(0) instanceof ColumnTemplateTerm)
			{
				PredicateObjectMap pom = new PredicateObjectMap(trMap);
				Predicate pred = new Predicate(Uris.CLASS_INSTANCE_LINK_URI + "-" + getNewPredicateId());
				pred.getTemplate().addTemplateTermToSet(
						new StringTemplateTerm(Uris.CLASS_INSTANCE_LINK_URI, true));
				pom.setPredicate(pred);
				StringTemplateTerm rdfLiteralTypeTerm = new StringTemplateTerm("", true);
				TemplateTermSet rdfLiteralTypeTermSet = new TemplateTermSet();
				rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);
				ObjectMap objMap = new ObjectMap(getNewObjectMapId(), 
						subjTemplTermSet, rdfLiteralTypeTermSet);
				pom.setObject(objMap);
				trMap.addPredicateObjectMap(pom);
				addHNodeIdToPredObjectMapLink(objMap, pom);
				
			}
		}
	}
	
	private void addHNodeIdToPredObjectMapLink(ObjectMap objMap, PredicateObjectMap pom) {
		TemplateTermSet objTermSet = objMap.getTemplate();
		if(objTermSet == null)
		{
			logger.error("No matching object term set");
			return;
		}
		for (TemplateTerm term:objTermSet.getAllTerms()) {
			if (term instanceof ColumnTemplateTerm) {
				String hNodeId = term.getTemplateTermValue();
				List<PredicateObjectMap> existingPomList = this.auxInfo.
						getHNodeIdToPredObjLinks().get(hNodeId);  
				if (existingPomList == null) {
					existingPomList = new ArrayList<PredicateObjectMap>();
				}
				existingPomList.add(pom);
				this.auxInfo.getHNodeIdToPredObjLinks().put(hNodeId, existingPomList);
			}
		}
	}

	private int getNewPredicateId() {
		return predicateIdCounter++;
	}
	
	private String getNewObjectMapId() {
		return "ObjectMap" + objectMapCounter++;
	}

	private SubjectMap addSubjectMapForTripleMap(Resource trMapRes) throws  JSONException {
		SubjectMap subjMap = null;
		Property subjMapProp = model.getProperty(Uris.RR_SUBJECTMAP_URI);
		Property templateProp = model.getProperty(Uris.RR_TEMPLATE_URI);
		Property rdfTypeProp = model.getProperty(Uris.RDF_TYPE_URI);
		Property rrClassProp = model.getProperty(Uris.RR_CLASS_URI);
		Resource steinerTreeRootNodeRes = model.getResource(Uris.KM_STEINER_TREE_ROOT_NODE);
		
		NodeIterator subjMapsItr = model.listObjectsOfProperty(trMapRes, subjMapProp);
		while (subjMapsItr.hasNext()){
			Resource subjMapBlankRes = subjMapsItr.next().asResource();
			subjectMapResources.add(subjMapBlankRes);
			
			String subjMapId = subjMapBlankRes.getId().getLabelString();
			subjMap = new SubjectMap(subjMapId);
			this.subjectMapIndex.put(subjMapId, subjMap);
			
			// Get the subject template
			NodeIterator templateItr = model.listObjectsOfProperty(subjMapBlankRes, templateProp);
			TemplateTermSet subjTemplTermSet = null;
			while (templateItr.hasNext()) {
				RDFNode templNode = templateItr.next();
				String template = templNode.toString();
				subjTemplTermSet = TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlTemplateString(
						template, worksheet, factory);
				
			}
			subjMap.setTemplate(subjTemplTermSet);
			
			// Get the subject type
			NodeIterator rdfTypesItr = model.listObjectsOfProperty(subjMapBlankRes, rrClassProp);
			while (rdfTypesItr.hasNext()) {
				RDFNode typeNode = rdfTypesItr.next();
				
				if (typeNode.isAnon()) {
					NodeIterator typeTemplItr = model.listObjectsOfProperty(typeNode.asResource(),
							templateProp);
					
					while (typeTemplItr.hasNext()) {
						RDFNode templNode = typeTemplItr.next();
						String template = templNode.toString();
						TemplateTermSet typeTermSet = TemplateTermSetBuilder.
								constructTemplateTermSetFromR2rmlTemplateString(
								template, worksheet, factory);
						subjMap.addRdfsType(typeTermSet);
					}
					continue;
				}
				
				if (typeNode instanceof Resource) {
					// Skip the steiner tree root type
					if(((Resource) typeNode).getURI().equals(Uris.KM_STEINER_TREE_ROOT_NODE))
						continue;
					
					StringTemplateTerm uriTerm = new StringTemplateTerm(
							((Resource) typeNode).getURI(), true);
					TemplateTermSet typeTermSet = new TemplateTermSet();
					typeTermSet.addTemplateTermToSet(uriTerm);
					subjMap.addRdfsType(typeTermSet);
				} else {
					TemplateTermSet typeTermSet = TemplateTermSetBuilder.
							constructTemplateTermSetFromR2rmlTemplateString(
							typeNode.toString(), worksheet, factory);
					subjMap.addRdfsType(typeTermSet);
				}
			}
			
			// Check if it is as the Steiner tree root node
			if (model.contains(subjMapBlankRes, rdfTypeProp, steinerTreeRootNodeRes)) {
				subjMap.setAsSteinerTreeRootNode(true);
			}
		}
		return subjMap;
	}

	private void calculateColumnNodesCoveredByBlankNodes() throws JSONException {
		Property termTypeProp = model.getProperty(Uris.RR_TERM_TYPE_URI);
		Resource blankNodeRes = model.getResource(Uris.RR_BLANK_NODE_URI);
		Property kmBnodePrefixProp = model.getProperty(Uris.KM_BLANK_NODE_PREFIX_URI);
		ResIterator blankNodeSubjMapItr = model.listResourcesWithProperty(termTypeProp, blankNodeRes);
		
		for (Resource subjMapRes:subjectMapResources) {
			
			if (model.contains(subjMapRes, termTypeProp, blankNodeRes)) {
				List<String> columnsCoveredHnodeIds = new ArrayList<String>();
				Resource blankNodeSubjRes = blankNodeSubjMapItr.next();
				SubjectMap subjMap = this.subjectMapIndex.get(blankNodeSubjRes.getId().getLabelString());
				subjMap.setAsBlankNode(true);
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
				
			
				logger.debug("Adding columns for blank node" + subjMap.getId() + " List: " + 
						columnsCoveredHnodeIds);
				this.auxInfo.getBlankNodesColumnCoverage().put(subjMap.getId(), columnsCoveredHnodeIds);
				
				// Get the blank node prefix
				NodeIterator bnodePrefixItr = model.listObjectsOfProperty(blankNodeSubjRes, kmBnodePrefixProp);
				while (bnodePrefixItr.hasNext()) {
					this.auxInfo.getBlankNodesUriPrefixMap().put(subjMap.getId(), 
							bnodePrefixItr.next().toString());
				}
			}
		}
	}	

	public KR2RMLMappingAuxillaryInformation getAuxInfo() {
		return auxInfo;
	}

	public R2RMLMapping getR2rmlMapping() {
		return r2rmlMapping;
	}
}
