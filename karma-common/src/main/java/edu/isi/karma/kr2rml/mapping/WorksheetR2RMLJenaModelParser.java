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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
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
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.kr2rml.KR2RMLVersion;
import edu.isi.karma.kr2rml.ObjectMap;
import edu.isi.karma.kr2rml.Predicate;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.Prefix;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatter;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatterFactory;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapLink;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.StringTemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.kr2rml.template.TemplateTermSetBuilder;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.webserver.KarmaException;

public class WorksheetR2RMLJenaModelParser {
	
	private Model model;
	private R2RMLMappingIdentifier id;
	private KR2RMLMapping mapping;
	private static Logger logger = LoggerFactory.getLogger(WorksheetR2RMLJenaModelParser.class);
	
	
	public WorksheetR2RMLJenaModelParser(R2RMLMappingIdentifier id) throws JSONException, KarmaException 
	{
		this.id = id;
	}
	
	public WorksheetR2RMLJenaModelParser(Model model, R2RMLMappingIdentifier id) throws JSONException, KarmaException 
	{
		this.id = id;
	}
	
	public synchronized Model getModel() throws IOException {
		if(this.model == null){
			loadModel();
		}
		return model;
	}
	
	public Model getModelFromCache(){
		return model;
	}

	private void loadModel() throws IOException {
		if (model != null) {
			return;
		}
		synchronized(this)
		{	
			if(model == null)
			{
				this.model = loadSourceModelIntoJenaModel(id);
			}
		}
	}

	public KR2RMLMapping parse() throws IOException, KarmaException, JSONException
	{
		loadModel();
		
		if(null != mapping)
		{
			return mapping;
		}
		synchronized(this)
		{
			if(null != mapping)
			{
				return mapping;
			}
		// Capture the main mapping resource that corresponds to the source name
		Resource mappingResource = getMappingResourceFromSourceName();
		if (mappingResource == null) {
			throw new KarmaException("Resource not found in model for the source: " + id.getName());
		}
		
		Property modelVersionNameProp = model.getProperty(Uris.KM_MODEL_VERSION_URI);
		Statement s = model.getProperty(mappingResource, modelVersionNameProp);
		
		KR2RMLVersion version;
		try 
		{
			version = new KR2RMLVersion(s.getString());
		}
		catch (Exception e)
		{
			version = KR2RMLVersion.unknown;
		}
		KR2RMLMapping kr2rmlMapping = new KR2RMLMapping(id, version);
		Map<String, String> prefixes = model.getNsPrefixMap();
		for(Entry<String, String> prefix : prefixes.entrySet())
		{
			Prefix p = new Prefix(prefix.getKey(), prefix.getValue());
			kr2rmlMapping.addPrefix(p);
		}
		
		SourceTypes sourceType = getSourceType(mappingResource);
		kr2rmlMapping.setColumnNameFormatter(KR2RMLColumnNameFormatterFactory.getFormatter(sourceType));
		// Load any transformations on the worksheet if required
		loadWorksheetHistory(mappingResource, kr2rmlMapping);
		
		// Generate TriplesMap for each InternalNode in the tree
		List<Resource> subjectResources = createSubjectMaps(mappingResource, kr2rmlMapping);
		
		// Identify the object property links
		createPredicateObjectMaps(mappingResource, kr2rmlMapping);
		
		// Calculate the nodes covered by each InternalNode
		calculateColumnNodesCoveredByBlankNodes(kr2rmlMapping, subjectResources);
		createGraphNodeToTriplesNodeMap(kr2rmlMapping);
		return mapping = kr2rmlMapping;
		}
	}
	
	
	private void createGraphNodeToTriplesNodeMap(KR2RMLMapping kr2rmlMapping) throws FileNotFoundException, UnsupportedEncodingException{
		
		StmtIterator itr = model.listStatements(null, model.getProperty(Uris.KM_NODE_ID_URI), (RDFNode)null);
		Resource subject;
		Map<String,String> graphNodeIdToTriplesMapIdMap = kr2rmlMapping.getAuxInfo().getGraphNodeIdToTriplesMapIdMap();
		while (itr.hasNext()) {
			Statement subjMapToNodeIdStmt = itr.next();
			String nodeId = subjMapToNodeIdStmt.getObject().toString();
			subject = subjMapToNodeIdStmt.getSubject();
			if (subject != null) {
				StmtIterator itr2 = model.listStatements(null, model.getProperty(Uris.RR_SUBJECTMAP_URI), subject);
				while (itr2.hasNext()) {
					String triplesMapId = itr2.next().getSubject().toString();
					graphNodeIdToTriplesMapIdMap.put(nodeId, triplesMapId);
				}
				
			}
		}
	}
	
	private SourceTypes getSourceType(Resource mappingResource)
	{
		Property sourceNameProp = model.getProperty(Uris.KM_SOURCE_TYPE_URI);
		Statement s = model.getProperty(mappingResource, sourceNameProp);
		String sourceType;
		if(s != null)
		{
			RDFNode node = s.getObject();
			if(node != null && node.isLiteral())
			{
				sourceType = node.asLiteral().getString();
				return SourceTypes.valueOf(sourceType);
			}
		}
		return SourceTypes.CSV;
		
		
	}
    
	public static Model loadSourceModelIntoJenaModel(R2RMLMappingIdentifier id) throws IOException {
        // Create an empty Model
        Model model = ModelFactory.createDefaultModel();
        InputStream s;
        if(id.getContent() != null) {
        	s = IOUtils.toInputStream(id.getContent());
        } else {
        	URL modelURL = id.getLocation();
        	logger.info("Load model:" + modelURL.toString());
        	s = modelURL.openStream();
        }
        model.read(s, null, "TURTLE");
        return model;
    }
   
	private Resource getMappingResourceFromSourceName() throws KarmaException {
		Property sourceNameProp = model.getProperty(Uris.KM_SOURCE_NAME_URI);
		RDFNode node = model.createLiteral(id.getName());
		ResIterator res = model.listResourcesWithProperty(sourceNameProp, node);
		List<Resource> resList = res.toList();
		
		if (resList.size() > 1) {
			throw new KarmaException("More than one resource exists with source name: " + id.getName());
		} else if (resList.size() == 1) {
			return resList.get(0);
		} else {
			//If we didnt find the sourceName in the model, maybe it is a different source with the
			//same schema.
			//Maybe we need to substitute the sourceName in the model with this one
			NodeIterator sourceObjectIter = model.listObjectsOfProperty(sourceNameProp);
			List<RDFNode> sourceObjects = sourceObjectIter.toList();
			
			if(sourceObjects.size() > 1) {
				throw new KarmaException("More than one resource exists with source name: " + id.getName());
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
	
	private void loadWorksheetHistory(Resource mappingResource, KR2RMLMapping kr2rmlMapping) throws JSONException {
		JSONArray normalizedCommandsJSON = getWorksheetHistory(mappingResource);
		kr2rmlMapping.setWorksheetHistory(normalizedCommandsJSON);
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

	private void createPredicateObjectMaps(Resource mappingResource, KR2RMLMapping kr2rmlMapping) throws JSONException {
		Property hasTrMapUri = model.getProperty(Uris.KM_HAS_TRIPLES_MAP_URI);
		
		// Get all the triple maps
		NodeIterator trMapsResItr = model.listObjectsOfProperty(mappingResource, hasTrMapUri);
		while (trMapsResItr.hasNext()) {
			// Add the predicate object maps
			addPredicateObjectMapsForTripleMap(trMapsResItr.next().asResource(), kr2rmlMapping);
		}
	}

	private List<Resource> createSubjectMaps(Resource mappingResource, KR2RMLMapping kr2rmlMapping) throws JSONException {
		List<Resource> subjectMapResources = new ArrayList<>();
		Property hasTrMapUri = model.getProperty(Uris.KM_HAS_TRIPLES_MAP_URI);
		
		// Get all the triple maps
		NodeIterator trMapsResItr = model.listObjectsOfProperty(mappingResource, hasTrMapUri);
		while (trMapsResItr.hasNext()) {
			Resource trMapRes = trMapsResItr.next().asResource();
			SubjectMap subjMap = addSubjectMapForTripleMap(trMapRes, kr2rmlMapping, subjectMapResources);
			
			// Add the Triples map
			TriplesMap trMap = new TriplesMap(trMapRes.getURI(), subjMap);
			kr2rmlMapping.getTriplesMapIndex().put(trMapRes.getURI(), trMap);
			kr2rmlMapping.addTriplesMap(trMap);
			kr2rmlMapping.getAuxInfo().getTriplesMapGraph().addTriplesMap(trMap);
		}
		return subjectMapResources;
	}


	private void addPredicateObjectMapsForTripleMap(Resource trMapRes, KR2RMLMapping kr2rmlMapping) throws  JSONException {
		int predicateIdCounter = 0;
		int objectMapCounter = 0;
		Property predObjMapProp = model.getProperty(Uris.RR_PRED_OBJ_MAP_URI);
		Property predProp = model.getProperty(Uris.RR_PREDICATE_URI);
		Property objectMapProp = model.getProperty(Uris.RR_OBJECTMAP_URI);
		Property columnProp = model.getProperty(Uris.RR_COLUMN_URI);
		Property rdfLiteralTypeProp = model.getProperty(Uris.RR_DATATYPE_URI);
		Resource rfObjClassUri = model.getResource(Uris.RR_REF_OBJECT_MAP_CLASS_URI);
		Property parentTriplesMapProp = model.getProperty(Uris.RR_PARENT_TRIPLE_MAP_URI);
		Property rdfTypeProp = model.getProperty(Uris.RDF_TYPE_URI);
		Property templateProp = model.getProperty(Uris.RR_TEMPLATE_URI);
		Property constantProp = model.getProperty(Uris.RR_CONSTANT);
		KR2RMLColumnNameFormatter formatter = kr2rmlMapping.getColumnNameFormatter();
		TriplesMap trMap = kr2rmlMapping.getTriplesMapIndex().get(trMapRes.getURI());
		if (trMap == null) {
			logger.error("No Triples Map found for resource: " + trMapRes.getURI());
			return;
		}
		NodeIterator predObjItr = model.listObjectsOfProperty(trMapRes, predObjMapProp);
		while (predObjItr.hasNext()) {
			Resource pomBlankNode = predObjItr.next().asResource();
			// Create the PredicateObjectMap object for current POM
			PredicateObjectMap pom = new PredicateObjectMap(pomBlankNode.getURI(), trMap);
			
			// Get the predicate for the POM
			Predicate pred = null;
			NodeIterator pomPredItr = model.listObjectsOfProperty(pomBlankNode, predProp); 
			while (pomPredItr.hasNext()) {
				RDFNode pomPredNode = pomPredItr.next();
				pred = new Predicate(pomPredNode.toString() + "-" + predicateIdCounter++);
				
				// Check if the predicate value is a URI or a literal (such as column name)
				if (pomPredNode instanceof Resource) {
					pred.getTemplate().addTemplateTermToSet(
							new StringTemplateTerm(((Resource) pomPredNode).getURI(), true));
				} else {
					pred.setTemplate(TemplateTermSetBuilder.
							constructTemplateTermSetFromR2rmlTemplateString(
									pomPredNode.toString(), formatter));
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
						TriplesMap parentTM = kr2rmlMapping.getTriplesMapIndex().get(parentTripleRes.getURI());
						
						// Create a RefObjectMap
						RefObjectMap rfMap = new RefObjectMap(objNode.getURI(), parentTM);
						objMap = new ObjectMap(getNewObjectMapId(objectMapCounter++), rfMap);
						
						// Add the link between triple maps in the auxInfo
						TriplesMapLink link = new TriplesMapLink(trMap, parentTM, pom);  
						kr2rmlMapping.getAuxInfo().getTriplesMapGraph().addLink(link);
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
						objMap = new ObjectMap(getNewObjectMapId(objectMapCounter++), 
								TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlColumnString(
										colNode.toString(), formatter), rdfLiteralTypeTermSet);
					}
					if(objMap == null)
					{
						NodeIterator templateItr = model.listObjectsOfProperty(objNode, templateProp);
						//try a literal/constant node
						if(templateItr == null ||  !templateItr.hasNext()){
							templateItr = model.listObjectsOfProperty(objNode, constantProp);
						}
						TemplateTermSet objTemplTermSet = null;
						while (templateItr.hasNext()) {
							RDFNode templNode = templateItr.next();
							String template = templNode.toString();
							boolean isUri = !templNode.isLiteral();
							objTemplTermSet = TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlTemplateString(
								template, isUri, kr2rmlMapping.getColumnNameFormatter());
						
						}
						objMap = new ObjectMap(getNewObjectMapId(objectMapCounter++), 
								objTemplTermSet, rdfLiteralTypeTermSet);
					}
					// Check if anything needs to be added to the columnNameToPredicateObjectMap Map
					if(objMap != null)
						addColumnNameToPredObjectMapLink(objMap, pom, kr2rmlMapping);
				}
			}
			pom.setObject(objMap);
			trMap.addPredicateObjectMap(pom);
		}
		
	
		// Try to add template to pom
			TemplateTermSet subjTemplTermSet = trMap.getSubject().getTemplate();
			if(subjTemplTermSet != null)
			{
				List<TemplateTerm> terms = subjTemplTermSet.getAllTerms();
				if(isValidTemplate(terms))
				{
					PredicateObjectMap pom = new PredicateObjectMap(PredicateObjectMap.getNewId(),trMap);
					Predicate pred = new Predicate(Uris.CLASS_INSTANCE_LINK_URI + "-" + predicateIdCounter++);
					pred.getTemplate().addTemplateTermToSet(
							new StringTemplateTerm(Uris.CLASS_INSTANCE_LINK_URI, true));
					pom.setPredicate(pred);
					StringTemplateTerm rdfLiteralTypeTerm = new StringTemplateTerm("", true);
					TemplateTermSet rdfLiteralTypeTermSet = new TemplateTermSet();
					rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);
					ObjectMap objMap = new ObjectMap(getNewObjectMapId(objectMapCounter++), 
							subjTemplTermSet, rdfLiteralTypeTermSet);
					pom.setObject(objMap);
					trMap.addPredicateObjectMap(pom);
					addColumnNameToPredObjectMapLink(objMap, pom, kr2rmlMapping);
					
				}
			}
	}

	private boolean isValidTemplate(List<TemplateTerm> terms) {
		if(terms == null || terms.isEmpty())
		{
			return false;
		}
		
		for(TemplateTerm term : terms)
		{
			if(term instanceof ColumnTemplateTerm)
			{
				return true;
			}
		}
		return false;
	}
	
	private void addColumnNameToPredObjectMapLink(ObjectMap objMap, PredicateObjectMap pom, KR2RMLMapping kr2rmlMapping) {
		TemplateTermSet objTermSet = objMap.getTemplate();
		if(objTermSet == null)
		{
			logger.error("No matching object term set");
			return;
		}
		for (TemplateTerm term:objTermSet.getAllTerms()) {
			if (term instanceof ColumnTemplateTerm) {
				String columnName = term.getTemplateTermValue();
				String columnNameWithoutFormatting = kr2rmlMapping.getColumnNameFormatter().getColumnNameWithoutFormatting(columnName);
				List<PredicateObjectMap> existingPomList = kr2rmlMapping.getAuxInfo().
						getColumnNameToPredObjLinks().get(columnNameWithoutFormatting);  
				if (existingPomList == null) {
					existingPomList = new ArrayList<>();
				}
				existingPomList.add(pom);
				kr2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().put(columnNameWithoutFormatting, existingPomList);
			}
		}
	}


	private String getNewObjectMapId(int objectMapCounter) {
		return "ObjectMap" + objectMapCounter;
	}

	private SubjectMap addSubjectMapForTripleMap(Resource trMapRes, KR2RMLMapping kr2rmlMapping, List<Resource> subjectMapResources) throws  JSONException {
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
			kr2rmlMapping.getSubjectMapIndex().put(subjMapId, subjMap);
			
			// Get the subject template
			NodeIterator templateItr = model.listObjectsOfProperty(subjMapBlankRes, templateProp);
			TemplateTermSet subjTemplTermSet = null;
			while (templateItr.hasNext()) {
				RDFNode templNode = templateItr.next();
				String template = templNode.toString();
				subjTemplTermSet = TemplateTermSetBuilder.constructTemplateTermSetFromR2rmlTemplateString(
						template, kr2rmlMapping.getColumnNameFormatter());
				List<String> columnsCovered = new LinkedList<>();
				for(TemplateTerm term : subjTemplTermSet.getAllColumnNameTermElements())
				{
					columnsCovered.add(term.getTemplateTermValue());
				}
				kr2rmlMapping.getAuxInfo().getSubjectMapIdToTemplateAnchor().put(subjMap.getId(), KR2RMLMappingAuxillaryInformation.findSubjectMapTemplateAnchor(columnsCovered));
				
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
								template);
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
							typeNode.toString());
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

	private void calculateColumnNodesCoveredByBlankNodes(KR2RMLMapping kr2rmlMapping, List<Resource> subjectMapResources) throws JSONException, KarmaException {
		Property termTypeProp = model.getProperty(Uris.RR_TERM_TYPE_URI);
		Resource blankNodeRes = model.getResource(Uris.RR_BLANK_NODE_URI);
		Property kmBnodePrefixProp = model.getProperty(Uris.KM_BLANK_NODE_PREFIX_URI);
		ResIterator blankNodeSubjMapItr = model.listResourcesWithProperty(termTypeProp, blankNodeRes);
		
		for (Resource subjMapRes:subjectMapResources) {
			
			if (model.contains(subjMapRes, termTypeProp, blankNodeRes)) {
				List<String> columnsCovered = new ArrayList<>();
				Resource blankNodeSubjRes = blankNodeSubjMapItr.next();
				
				SubjectMap subjMap = kr2rmlMapping.getSubjectMapIndex().get(blankNodeSubjRes.getId().getLabelString());
				subjMap.setAsBlankNode(true);
				NodeIterator bnodePrefixItr = model.listObjectsOfProperty(blankNodeSubjRes, kmBnodePrefixProp);
				while (bnodePrefixItr.hasNext()) {
					kr2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().put(subjMap.getId(), 
							bnodePrefixItr.next().toString());
				}
				
				TriplesMap mytm;
				for(TriplesMap tm : kr2rmlMapping.getTriplesMapList())
				{
					if(tm.getSubject().getId().equalsIgnoreCase(subjMap.getId()))
					{
						mytm = tm;
						
						List<PredicateObjectMap> poms = mytm.getPredicateObjectMaps();
						for(PredicateObjectMap pom : poms )
						{
							ObjectMap objMap = pom.getObject();
							if(objMap == null)
							{
								logger.error("Unable to find object map for pom :" + pom.toString());
							}
								
							TemplateTermSet templateTermSet = pom.getObject().getTemplate();
							if(templateTermSet != null)
							{
								TemplateTerm term = templateTermSet.getAllTerms().get(0);
								if(term!= null && term instanceof ColumnTemplateTerm)
								{
									columnsCovered.add(term.getTemplateTermValue());
								}
							}
						}
						
						if(columnsCovered.isEmpty())
						{
							//String blankNodeUriPrefix = kr2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().get(subjMap.getId());
							//throw new KarmaException("You need to define a URI for "+blankNodeUriPrefix+ ".");
						}
						break;
					}
				}
		
				logger.debug("Adding columns for blank node" + subjMap.getId() + " List: " + 
						columnsCovered);
				kr2rmlMapping.getAuxInfo().getBlankNodesColumnCoverage().put(subjMap.getId(), columnsCovered);
				kr2rmlMapping.getAuxInfo().getSubjectMapIdToTemplateAnchor().put(subjMap.getId(), KR2RMLMappingAuxillaryInformation.findSubjectMapTemplateAnchor(columnsCovered));
				// Get the blank node prefix
				
			}
		}
	}


}
