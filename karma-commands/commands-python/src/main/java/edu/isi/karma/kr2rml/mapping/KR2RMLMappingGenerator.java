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

import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.kr2rml.*;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatterFactory;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapLink;
import edu.isi.karma.kr2rml.template.*;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.transformation.tokenizer.PythonTransformationAsURITokenizer;
import edu.isi.karma.transformation.tokenizer.PythonTransformationAsURIValidator;
import edu.isi.karma.transformation.tokenizer.PythonTransformationToken;
import edu.isi.karma.webserver.KarmaException;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class KR2RMLMappingGenerator {

	private OntologyManager ontMgr;
	private String sourceNamespace;
	private KR2RMLMapping r2rmlMapping;
	private KR2RMLMappingColumnNameHNodeTranslator translator;
	private PythonTransformationToTemplateTermSetBuilder transformationToTemplateTermSet;
	private final Node steinerTreeRoot;
	private SemanticTypes semanticTypes;
	private DirectedWeightedMultigraph<Node, LabeledLink> alignmentGraph;
	private Worksheet worksheet;
	private Workspace workspace; 
	
	// Internal data structures required
	private int synonymIdCounter;
	
	private static Logger logger = LoggerFactory.getLogger(KR2RMLMappingGenerator.class);
	
	public KR2RMLMappingGenerator(Workspace workspace, Worksheet worksheet, Alignment alignment, 
			SemanticTypes semanticTypes, String sourcePrefix, String sourceNamespace, 
			boolean generateInverse) throws KarmaException{
		this(workspace, worksheet, alignment, semanticTypes, sourcePrefix, sourceNamespace, generateInverse, null, false);
	}
	
	public KR2RMLMappingGenerator(Workspace workspace, Worksheet worksheet, Alignment alignment, 
			SemanticTypes semanticTypes, String sourcePrefix, String sourceNamespace, 
			boolean generateInverse, JSONArray history, boolean onlyHistory) throws KarmaException{

		this.workspace = workspace;
		this.worksheet = worksheet;
		this.translator = new KR2RMLMappingColumnNameHNodeTranslator(workspace.getFactory(), worksheet);
		this.transformationToTemplateTermSet = new PythonTransformationToTemplateTermSetBuilder(translator, workspace.getFactory());
		this.ontMgr = workspace.getOntologyManager();
		this.semanticTypes = semanticTypes;
		this.sourceNamespace = sourceNamespace;
		R2RMLMappingIdentifier id = null;
		try {
			id = new R2RMLMappingIdentifier(sourceNamespace, new URL(sourceNamespace+ sourcePrefix + UUID.randomUUID()));
		} catch (MalformedURLException e) {
			logger.error("Unable to create mapping identifier", e);
		}
		this.r2rmlMapping = new KR2RMLMapping(id, KR2RMLVersion.getCurrent());
		this.alignmentGraph = alignment.getSteinerTree();
		this.steinerTreeRoot = alignment.GetTreeRoot();
		
		// Generate the R2RML data structures
		if (!onlyHistory) {
			generateMappingFromSteinerTree(generateInverse);
		}
		
		addWorksheetHistory(history);
		addSourceType(worksheet);
		if (!onlyHistory) {
			addColumnNameFormatter();
		}
		determineIfMappingIsR2RMLCompatible(worksheet);
	}

	private void addSourceType(Worksheet worksheet) {
		String sourceType = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.sourceType);
		r2rmlMapping.setSourceType(SourceTypes.valueOf(sourceType));
	}

	private void determineIfMappingIsR2RMLCompatible(Worksheet worksheet2) {
		
		boolean isRMLCompatible = KR2RMLWorksheetHistoryCompatibilityVerifier.verify(workspace, r2rmlMapping.getWorksheetHistory());
		r2rmlMapping.setRMLCompatible(isRMLCompatible);
		if(isRMLCompatible && r2rmlMapping.getSourceType().equals(SourceTypes.DB))
		{
			r2rmlMapping.setR2RMLCompatible(true);
		}
	}

	private void addColumnNameFormatter() {
		
		r2rmlMapping.setColumnNameFormatter(KR2RMLColumnNameFormatterFactory.getFormatter(r2rmlMapping.getSourceType()));
	}
	
	private void addWorksheetHistory(JSONArray history) {
		if(history != null) {
			r2rmlMapping.setWorksheetHistory(history);
		} else {
			String filename = CommandHistory.getHistorySaver(workspace.getId()).getHistoryFilepath(worksheet.getId());
			if(!HistoryJsonUtil.historyExists(workspace.getId(), worksheet.getId())) {
				logger.error("Worksheet history file not found! Can't write worksheet history " +
						"into R2RML model. Path:" + filename);
				return;
			}
			
			try {
				JSONArray historyArr = CommandHistory.getHistorySaver(workspace.getId()).loadHistory(filename);
				r2rmlMapping.setWorksheetHistory(historyArr);
			} catch(Exception e) {
				logger.error("Unable to read worksheet history from file: " + filename);
			}
		}
	}

	public Node getSteinerTreeRoot() {
		return steinerTreeRoot;
	}

	public KR2RMLMapping getKR2RMLMapping() {
		return this.r2rmlMapping;
	}

	private void generateMappingFromSteinerTree(boolean generateInverse) throws KarmaException {
		// Generate TriplesMap for each InternalNode in the tree
		createSubjectMaps();
		
		// Create TripleMaps
		createTripleMaps();
		
		// Identify the object property links
		createPredicateObjectMaps(generateInverse);
		
		// Identify blank nodes and generate backward links data structure for the triple maps
		identifyBlankNodes();
		
		// Calculate the nodes covered by each InternalNode
		calculateColumnNodesCoveredByBlankNodes();
		
		addPrefixes();
	}

	private void addPrefixes()
	{
		Map<String, String> prefixMap = workspace.getOntologyManager().getPrefixMap(); 
		for (Entry<String, String> entry :prefixMap.entrySet()) {
			Prefix p = new Prefix(entry.getValue(), entry.getKey());
			r2rmlMapping.addPrefix(p);
		}
	}
	private void identifyBlankNodes() {
		for (SubjectMap subjMap:r2rmlMapping.getSubjectMapIndex().values()) {
			if (subjMap.getTemplate().getAllTerms().size() == 1 &&
					(subjMap.getTemplate().getAllTerms().get(0) instanceof StringTemplateTerm)) {
				String str = subjMap.getTemplate().getAllTerms().get(0).getTemplateTermValue();
				if (str.equals(sourceNamespace)) 
					subjMap.setAsBlankNode(true);
			}
		}
	}
	
	private void calculateColumnNodesCoveredByBlankNodes() throws KarmaException {
		DisplayModel dm = new DisplayModel(alignmentGraph);
		
		for (Node treeNode:alignmentGraph.vertexSet()) {
			if (treeNode instanceof InternalNode && r2rmlMapping.getSubjectMapIndex().containsKey(treeNode.getId())) {
				SubjectMap subjMap = r2rmlMapping.getSubjectMapIndex().get(treeNode.getId());
				
				if (subjMap.isBlankNode()) {
					List<String> columnsCovered = new ArrayList<>();
					Set<LabeledLink> links = dm.getOutgoingEdgesOf(treeNode);
					Iterator<LabeledLink> linkIterator = links.iterator();
					while(linkIterator.hasNext())
					{
						Node n = linkIterator.next().getTarget();	
						if(n instanceof ColumnNode)
						{
							String columnName = translator.getColumnNameForHNodeId(((ColumnNode)n).getId());
							columnsCovered.add(columnName);
						}
					}
					if(columnsCovered.isEmpty())
					{
						//throw new KarmaException("You need to define a URI for "+treeNode.getDisplayId()+ ".");
					}
					r2rmlMapping.getAuxInfo().getBlankNodesColumnCoverage().put(treeNode.getId(), columnsCovered);
					r2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().put(treeNode.getId(), treeNode.getDisplayId());
					r2rmlMapping.getAuxInfo().getSubjectMapIdToTemplateAnchor().put(treeNode.getId(), KR2RMLMappingAuxillaryInformation.findSubjectMapTemplateAnchor(columnsCovered));
				}
			}
		}
	}

	private void createTripleMaps() {
		Set<Node> nodes = alignmentGraph.vertexSet();
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				// Create a TriplesMap corresponding to the Internal node
				SubjectMap subjMap = r2rmlMapping.getSubjectMapIndex().get(node.getId());
				TriplesMap trMap = new TriplesMap(TriplesMap.getNewId(), subjMap);
				r2rmlMapping.getTriplesMapIndex().put(node.getId(), trMap);
				this.r2rmlMapping.addTriplesMap(trMap);
				r2rmlMapping.getAuxInfo().getTriplesMapGraph().addTriplesMap(trMap);
			}
		}
	}

	private void createSubjectMaps() {
		Set<Node> nodes = alignmentGraph.vertexSet();
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				SubjectMap subj = new SubjectMap(node.getId());
				
				if (node.getId().equals(steinerTreeRoot.getId()))
					subj.setAsSteinerTreeRootNode(true);
				
				// Add the user provided namespace as the first template term
				subj.getTemplate().addTemplateTermToSet(new StringTemplateTerm(sourceNamespace));
				StringTemplateTerm typeTerm = new StringTemplateTerm(node.getLabel().getUri(), true);
				TemplateTermSet typeTermSet = new TemplateTermSet();
				typeTermSet.addTemplateTermToSet(typeTerm);
				subj.addRdfsType(typeTermSet);

				ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
				ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());
				if(modelingConfiguration.getR2rmlExportSuperClass())
				{
					OntologyManager ontMgr = workspace.getOntologyManager();
					HashMap<String,Label> superClassLabelsMap = ontMgr.getSuperClasses(node.getLabel().getUri(), true);
					for(String key: superClassLabelsMap.keySet())
					{
						Label superClassLabel = superClassLabelsMap.get(key);
						StringTemplateTerm supertypeTerm = new StringTemplateTerm(superClassLabel.getUri(), false);
						TemplateTermSet supertypeTermSet = new TemplateTermSet();
						supertypeTermSet.addTemplateTermToSet(supertypeTerm);
						subj.addRdfsType(supertypeTermSet);
					}
				}
				r2rmlMapping.getSubjectMapIndex().put(node.getId(), subj);
				

				Set<LabeledLink> outgoingLinks = alignmentGraph.outgoingEdgesOf(node);
				for (LabeledLink link:outgoingLinks) {
					
					if (link instanceof ClassInstanceLink || link instanceof ColumnSubClassLink) {
						Node tNode = link.getTarget();
						if (tNode instanceof ColumnNode) {
							ColumnNode cnode = (ColumnNode) tNode;
							String hNodeId = cnode.getHNodeId();
							String columnName = translator.getColumnNameForHNodeId(hNodeId);
							ColumnTemplateTerm cnTerm = new ColumnTemplateTerm(columnName);
							
							// Identify classInstance links to set the template
							if (link instanceof ClassInstanceLink) {
								
								TemplateTermSet tts = expandColumnTemplateTermForPyTransforms(
									 hNodeId, cnTerm);
								subj.setTemplate(tts);
							}
							
							// Identify the isSubclassOfClass links to set the correct type
							else if (link instanceof ColumnSubClassLink) {
								TemplateTermSet typeTermSet2 = new TemplateTermSet();
								typeTermSet2.addTemplateTermToSet(cnTerm);
								subj.addRdfsType(typeTermSet2);
							}
							
							List<String> columnsCovered = new LinkedList<>();
							for(TemplateTerm term : subj.getTemplate().getAllColumnNameTermElements())
							{
								columnsCovered.add(term.getTemplateTermValue());
							}
							r2rmlMapping.getAuxInfo().getSubjectMapIdToTemplateAnchor().put(subj.getId(), KR2RMLMappingAuxillaryInformation.findSubjectMapTemplateAnchor(columnsCovered));
						} else {
							logger.error("Target node of Class Instance link should always be a " +
									"column node.");
						}
					}
				}
			} else if(node instanceof LiteralNode) {
				LiteralNode literalNode = (LiteralNode)node;
				
				SubjectMap subj = new SubjectMap(literalNode.getId());
				
				if (literalNode.getId().equals(steinerTreeRoot.getId()))
					subj.setAsSteinerTreeRootNode(true);
				
				StringTemplateTerm typeTerm = new StringTemplateTerm(literalNode.getLabel().getUri(), true);
				TemplateTermSet typeTermSet = new TemplateTermSet();
				typeTermSet.addTemplateTermToSet(typeTerm);
				subj.addRdfsType(typeTermSet);
				
				TemplateTermSet templateTermSet = new TemplateTermSet();
				StringTemplateTerm tempTerm = new StringTemplateTerm(literalNode.getValue(), literalNode.isUri());
				templateTermSet.addTemplateTermToSet(tempTerm);
				subj.setTemplate(templateTermSet);
				
				r2rmlMapping.getSubjectMapIndex().put(node.getId(), subj);
			}
		}
	}

	private TemplateTermSet expandColumnTemplateTermForPyTransforms(
			String hNodeId, ColumnTemplateTerm cnTerm) {
		TemplateTermSet tts = null;
		String pythonCommand = worksheet.getMetadataContainer().getColumnMetadata().getColumnPython(hNodeId);
		List<PythonTransformationToken> tokens = PythonTransformationAsURITokenizer.tokenize(pythonCommand);
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		if(validator.validate(tokens))
		{
			tts = this.transformationToTemplateTermSet.translate(tokens, hNodeId);
		}
		else
		{
			tts = new TemplateTermSet();
			tts.addTemplateTermToSet(cnTerm);
		}
		return tts;
	}
	
	private void createPredicateObjectMaps(boolean generateInverse) {
		Set<Node> nodes = alignmentGraph.vertexSet();
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				// Create a TriplesMap corresponding to the Internal node
				SubjectMap subjMap = r2rmlMapping.getSubjectMapIndex().get(node.getId());
				TriplesMap subjTrMap = r2rmlMapping.getTriplesMapIndex().get(node.getId());
				
				// Create the predicate object map for each outgoing link
				Set<LabeledLink> outgoingEdges = alignmentGraph.outgoingEdgesOf(node);
				for (LabeledLink olink:outgoingEdges) {
					if (olink instanceof ObjectPropertySpecializationLink 
							|| olink instanceof DataPropertyOfColumnLink  
							|| olink instanceof ColumnSubClassLink)
						continue;
					
					PredicateObjectMap poMap = new PredicateObjectMap(PredicateObjectMap.getNewId(), subjTrMap);
					Node target = olink.getTarget();
					
					// Create an object property map
					if (target instanceof InternalNode) {
						// Get the RefObjMap object for the objectmap
						TriplesMap objTrMap = r2rmlMapping.getTriplesMapIndex().get(target.getId());
						RefObjectMap refObjMap = new RefObjectMap(RefObjectMap.getNewRefObjectMapId(), objTrMap);
						ObjectMap objMap = new ObjectMap(target.getId(), refObjMap);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						
						// Check if a specialization link exists
						LabeledLink specializedEdge = getSpecializationLinkIfExists(olink, node);
						if (specializedEdge != null) {
							Node specializedEdgeTarget = specializedEdge.getTarget();
							if (specializedEdgeTarget instanceof ColumnNode) {
								String columnName = translator.getColumnNameForHNodeId(((ColumnNode) specializedEdgeTarget).getHNodeId());
								ColumnTemplateTerm cnTerm = 
										new ColumnTemplateTerm(columnName);
								pred.getTemplate().addTemplateTermToSet(cnTerm);
							}
						} else {
							pred.getTemplate().addTemplateTermToSet(
									new StringTemplateTerm(olink.getLabel().getUri(), true));
						}
						poMap.setPredicate(pred);
						if (generateInverse)
							addInversePropertyIfExists(subjMap, poMap, olink, subjTrMap);
						
						// Add the links in the graph links data structure
						TriplesMapLink link = new TriplesMapLink(subjTrMap, objTrMap, poMap);  
						r2rmlMapping.getAuxInfo().getTriplesMapGraph().addLink(link);
					}
					
					else if(target instanceof LiteralNode) {
						LiteralNode lnode = (LiteralNode) target;
						
						//Create the object
						TemplateTermSet termSet = new TemplateTermSet();
						StringTemplateTerm literalTerm = new StringTemplateTerm(lnode.getValue(), lnode.isUri());
						termSet.addTemplateTermToSet(literalTerm);
						
						StringTemplateTerm rdfLiteralTypeTerm = new StringTemplateTerm(lnode.getLabel().getUri(), true);
						TemplateTermSet rdfLiteralTypeTermSet = new TemplateTermSet();
						rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);
						
						String language = lnode.getLanguage();
						StringTemplateTerm languageTerm = new StringTemplateTerm(language, false);
						TemplateTermSet languageTermSet = new TemplateTermSet();
						languageTermSet.addTemplateTermToSet(languageTerm);
						
						ObjectMap objMap = new ObjectMap(target.getId(), termSet, rdfLiteralTypeTermSet, languageTermSet);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						pred.getTemplate().addTemplateTermToSet(
									new StringTemplateTerm(olink.getLabel().getUri(), true));
						
						poMap.setPredicate(pred);
						if (generateInverse)
							addInversePropertyIfExists(subjMap, poMap, olink, subjTrMap);
						
					}
					
					// Create a data property map
					else if(target instanceof ColumnNode) {
						// Create the object map
						ColumnNode cnode = (ColumnNode) target;
						String hNodeId = cnode.getHNodeId();
						String columnName = translator.getColumnNameForHNodeId(hNodeId);
						ColumnTemplateTerm cnTerm = new ColumnTemplateTerm(columnName);
						TemplateTermSet termSet = expandColumnTemplateTermForPyTransforms(
								hNodeId, cnTerm);
						
						String rdfLiteralUri = 	cnode.getRdfLiteralType() == null? "" : cnode.getRdfLiteralType().getUri();
						StringTemplateTerm rdfLiteralTypeTerm = new StringTemplateTerm(rdfLiteralUri, true);
						TemplateTermSet rdfLiteralTypeTermSet = new TemplateTermSet();
						rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);

						String language = cnode.getLanguage();
						StringTemplateTerm languageTerm = new StringTemplateTerm(language, false);
						TemplateTermSet languageTermSet = new TemplateTermSet();
						languageTermSet.addTemplateTermToSet(languageTerm);
						
						ObjectMap objMap = new ObjectMap(hNodeId, termSet, rdfLiteralTypeTermSet, languageTermSet);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						
						// Check if a specialization link exists
						LabeledLink specializedEdge = getSpecializationLinkIfExists(olink, node);
						if (specializedEdge != null) {
							Node specializedEdgeTarget = specializedEdge.getTarget();
							if (specializedEdgeTarget instanceof ColumnNode) {
								String targetColumnName = translator.getColumnNameForHNodeId(((ColumnNode) specializedEdgeTarget).getHNodeId());
								ColumnTemplateTerm cnsplTerm = 
										new ColumnTemplateTerm(targetColumnName);
								pred.getTemplate().addTemplateTermToSet(cnsplTerm);
							}
						} else {
							pred.getTemplate().addTemplateTermToSet(
									new StringTemplateTerm(olink.getLabel().getUri(), true));
						}
						poMap.setPredicate(pred);
						
						// Save link from the columnName to the its PredicateObjectMap in the auxiliary information
						saveLinkFromColumnNameToPredicateObjectMap(columnName, poMap);
						
						// Check for synonym types for this column
						addSynonymTypesPredicateObjectMaps(subjTrMap, hNodeId);
					}
					// Add the predicateobjectmap to the triples map after a sanity check
					if (poMap.getObject() != null && poMap.getPredicate() != null && !doesPredicateAlreadyExist(subjTrMap,
							poMap, poMap.getObject().getRefObjectMap()))
						subjTrMap.addPredicateObjectMap(poMap);
				}
			}
		}
	}

	private void saveLinkFromColumnNameToPredicateObjectMap(String columnName, PredicateObjectMap poMap) {
		
		List<PredicateObjectMap> pomList = r2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().get(columnName);  
		if (pomList == null) {
			pomList = new ArrayList<>();
		}
		pomList.add(poMap);
		r2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().put(columnName, pomList);
	}

	private void addSynonymTypesPredicateObjectMaps(TriplesMap subjTrMap, String hNodeId) {
		SynonymSemanticTypes synonyms = this.semanticTypes.getSynonymTypesForHNodeId(hNodeId);
		if (synonyms != null && !synonyms.getSynonyms().isEmpty()){
			for (SemanticType synType:synonyms.getSynonyms()) {
				if (synType.isClass()) {
					logger.error("Synonym type as class with no property are not allowed.");
					continue;
				}
				
				PredicateObjectMap poMap = new PredicateObjectMap(PredicateObjectMap.getNewId(),subjTrMap);
				
				// Create the object map
				String columnName = translator.getColumnNameForHNodeId(hNodeId);
				ColumnTemplateTerm cnTerm = new ColumnTemplateTerm(columnName);
				TemplateTermSet termSet = new TemplateTermSet();
				termSet.addTemplateTermToSet(cnTerm);
				ObjectMap objMap = new ObjectMap(hNodeId, termSet, null);
				poMap.setObject(objMap);
				
				// Create the predicate
				Predicate pred = new Predicate(synType.getType().getUri() + "-synonym" + 
						getNewSynonymIdCount());
				pred.getTemplate().addTemplateTermToSet(
						new StringTemplateTerm(synType.getType().getUri(), true));
				poMap.setPredicate(pred);
				
				// Add the predicate object map to the triples map
				subjTrMap.addPredicateObjectMap(poMap);
				
				// Save the link from hNodeId to PredicateObjectMap
				saveLinkFromColumnNameToPredicateObjectMap(columnName, poMap);
			}
		}
	}

	private int getNewSynonymIdCount() {
		return synonymIdCounter++;
	}

	private void addInversePropertyIfExists(SubjectMap subjMap,
			PredicateObjectMap poMap, LabeledLink olink, TriplesMap subjTrMap) {
		String propUri = olink.getLabel().getUri();
		//this can happen if propertyName is not an Object property; it could be a subclass
		if (!ontMgr.isObjectProperty(propUri))
			return;
		
		Label inversePropLabel = ontMgr.getInverseProperty(propUri);
		Label inverseOfPropLabel = ontMgr.getInverseOfProperty(propUri);
		
		if (inversePropLabel != null) {
			TriplesMap inverseTrMap = r2rmlMapping.getTriplesMapIndex().get(poMap.getObject().getId());
			// Create the predicate object map
			PredicateObjectMap invPoMap = new PredicateObjectMap(PredicateObjectMap.getNewId(),inverseTrMap);
			// Create the predicate
			Predicate pred = new Predicate(olink.getId()+"+inverse");
			pred.getTemplate().addTemplateTermToSet(
					new StringTemplateTerm(inversePropLabel.getUri(), true));
			invPoMap.setPredicate(pred);
			// Create the object using RefObjMap
			RefObjectMap refObjMap = new RefObjectMap(RefObjectMap.getNewRefObjectMapId(), subjTrMap);
			ObjectMap invObjMap = new ObjectMap(subjMap.getId(), refObjMap);
			invPoMap.setObject(invObjMap);
			
			boolean alreadyExists = doesPredicateAlreadyExist(inverseTrMap,
					invPoMap, refObjMap);
			if(alreadyExists)
			{
				return;
			}
			
			inverseTrMap.addPredicateObjectMap(invPoMap);
			// Add the link to the link set
			r2rmlMapping.getAuxInfo().getTriplesMapGraph().addLink(new TriplesMapLink(inverseTrMap, subjTrMap, invPoMap));
		}
		if (inverseOfPropLabel != null) {
			// Create the triples map
			// Get the object's triples map
			TriplesMap inverseOfTrMap = r2rmlMapping.getTriplesMapIndex().get(poMap.getObject().getId());
			
			PredicateObjectMap invOfPoMap = new PredicateObjectMap(PredicateObjectMap.getNewId(),inverseOfTrMap);
			// Create the predicate
			Predicate pred = new Predicate(olink.getId()+"+inverseOf");
			pred.getTemplate().addTemplateTermToSet(
					new StringTemplateTerm(inverseOfPropLabel.getUri(), true));
			invOfPoMap.setPredicate(pred);
			// Create the object using RefObjMap
			RefObjectMap refObjMap = new RefObjectMap(RefObjectMap.getNewRefObjectMapId(), subjTrMap);
			ObjectMap invOfObjMap = new ObjectMap(subjMap.getId(), refObjMap);
			invOfPoMap.setObject(invOfObjMap);
			
			boolean alreadyExists = doesPredicateAlreadyExist(inverseOfTrMap,
					invOfPoMap, refObjMap);
			if(alreadyExists)
			{
				return;
			}
			inverseOfTrMap.addPredicateObjectMap(invOfPoMap);
			// Add the link to the link set
			r2rmlMapping.getAuxInfo().getTriplesMapGraph().addLink(new TriplesMapLink(inverseOfTrMap, subjTrMap, invOfPoMap));
		}
	}

	private boolean doesPredicateAlreadyExist(TriplesMap triplesMap,
			PredicateObjectMap poMap, RefObjectMap refObjMap) {
		boolean alreadyExists = false;
		for(PredicateObjectMap pom : triplesMap.getPredicateObjectMaps())
		{
			if(pom.getPredicate().getTemplate().isSingleUriString() && poMap.getPredicate().getTemplate().isSingleUriString())
			{
				if(pom.getPredicate().getTemplate().toString().equalsIgnoreCase(poMap.getPredicate().getTemplate().toString()))
				{
					if(pom.getObject().hasRefObjectMap() && pom.getObject().getRefObjectMap().getParentTriplesMap().getId().equalsIgnoreCase(refObjMap.getParentTriplesMap().getId()))
					{
						alreadyExists = true;
					}
					else if(!pom.getObject().hasRefObjectMap() && !poMap.getObject().hasRefObjectMap() &&
							pom.getObject().getTemplate().toString().compareTo(poMap.getObject().getTemplate().toString())== 0)
					{
						alreadyExists = true;
					}
				}
			}
		}
		return alreadyExists;
	}

	private LabeledLink getSpecializationLinkIfExists(LabeledLink link, Node sourceNode) {
		Set<LabeledLink> outgoingEdges = this.alignmentGraph.outgoingEdgesOf(sourceNode);
		for (LabeledLink olink:outgoingEdges) {
			// Check for the object property specialization
			if (olink instanceof ObjectPropertySpecializationLink ) {
				String splLinkId = ((ObjectPropertySpecializationLink) olink).getSpecializedLinkId();
				if (splLinkId.equals(link.getId()))
					return olink;
			}
			// Check for the data property specialization
			else if (olink instanceof DataPropertyOfColumnLink) {
				DataPropertyOfColumnLink dlink = (DataPropertyOfColumnLink) olink;
				Node target = link.getTarget();
				if (target instanceof ColumnNode) {
					ColumnNode cnode = (ColumnNode) target;
					if (dlink.getSpecializedColumnHNodeId().equals(cnode.getId()))
						return dlink;
				}
			}
		}
		return null;
	}

}





















