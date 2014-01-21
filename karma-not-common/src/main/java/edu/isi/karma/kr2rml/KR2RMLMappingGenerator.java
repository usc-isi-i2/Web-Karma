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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DisplayModel;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;

public class KR2RMLMappingGenerator {

	private RepFactory factory;
	private OntologyManager ontMgr;
	private String sourceNamespace;
//	private ErrorReport errorReport;
	private KR2RMLMapping r2rmlMapping;
	private final Node steinerTreeRoot;
	private SemanticTypes semanticTypes;
	private DirectedWeightedMultigraph<Node, Link> alignmentGraph;
	private Map<String, String> hNodeIdsToColumnName = new HashMap<String, String>();
	
	// Internal data structures required
	private int synonymIdCounter;
	
	private final static String TRIPLES_MAP_PREFIX = "TriplesMap";
	private final static String REFOBJECT_MAP_PREFIX = "RefObjectMap";
	private static Logger logger = LoggerFactory.getLogger(KR2RMLMappingGenerator.class);
	
	public KR2RMLMappingGenerator(Workspace workspace, Alignment alignment, 
			SemanticTypes semanticTypes, String sourcePrefix, String sourceNamespace, 
			boolean generateInverse, ErrorReport errorReport) {

		this.factory = workspace.getFactory();
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
		generateMappingFromSteinerTree(generateInverse);
	}
	
	public Node getSteinerTreeRoot() {
		return steinerTreeRoot;
	}

	public KR2RMLMapping getKR2RMLMapping() {
		return this.r2rmlMapping;
	}

	private void generateMappingFromSteinerTree(boolean generateInverse) {
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
	
	private void calculateColumnNodesCoveredByBlankNodes() {
		DisplayModel dm = new DisplayModel(alignmentGraph);
		
		for (Node treeNode:alignmentGraph.vertexSet()) {
			if (treeNode instanceof InternalNode && r2rmlMapping.getSubjectMapIndex().containsKey(treeNode.getId())) {
				SubjectMap subjMap = r2rmlMapping.getSubjectMapIndex().get(treeNode.getId());
				
				if (subjMap.isBlankNode()) {
					List<String> columnsCovered = new ArrayList<String>();
					Set<Link> links = dm.getModel().outgoingEdgesOf(treeNode);
					Iterator<Link> linkIterator = links.iterator();
					while(linkIterator.hasNext())
					{
						Node n = linkIterator.next().getTarget();	
						if(n instanceof ColumnNode)
						{
							columnsCovered.add(getHNodeIdToColumnNameMapping(((ColumnNode)n).getId()));
						}
					}
					
					r2rmlMapping.getAuxInfo().getBlankNodesColumnCoverage().put(treeNode.getId(), columnsCovered);
					r2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().put(treeNode.getId(), treeNode.getDisplayId());
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
				TriplesMap trMap = new TriplesMap(getNewTriplesMapId(), subjMap);
				r2rmlMapping.getTriplesMapIndex().put(node.getId(), trMap);
				this.r2rmlMapping.addTriplesMap(trMap);
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
				r2rmlMapping.getSubjectMapIndex().put(node.getId(), subj);
				
				Set<Link> outgoingLinks = alignmentGraph.outgoingEdgesOf(node);
				for (Link link:outgoingLinks) {
					
					if (link instanceof ClassInstanceLink || link instanceof ColumnSubClassLink
							|| (link instanceof DataPropertyLink && 
								link.getKeyType() == LinkKeyInfo.PartOfKey)) {
						Node tNode = link.getTarget();
						if (tNode instanceof ColumnNode) {
							ColumnNode cnode = (ColumnNode) tNode;
							String hNodeId = cnode.getHNodeId();
							String columnName = getHNodeIdToColumnNameMapping(hNodeId);
							ColumnTemplateTerm cnTerm = new ColumnTemplateTerm(columnName);
							
							// Identify classInstance links to set the template
							if (link instanceof ClassInstanceLink) {
								subj.getTemplate().clear().addTemplateTermToSet(cnTerm);
							}
							
							// Identify the isSubclassOfClass links to set the correct type
							else if (link instanceof ColumnSubClassLink) {
								TemplateTermSet typeTermSet2 = new TemplateTermSet();
								typeTermSet2.addTemplateTermToSet(cnTerm);
								subj.addRdfsType(typeTermSet2);
							}
							
							// Identify the link which has been chosen as the key
							else if (link instanceof DataPropertyLink && 
									link.getKeyType() == LinkKeyInfo.PartOfKey) {
								subj.getTemplate().addTemplateTermToSet(cnTerm);
							}
						} else {
							logger.error("Target node of Class Instance link should always be a " +
									"column node.");
						}
					}
				}
			}
		}
	}
	
	private void createPredicateObjectMaps(boolean generateInverse) {
		Set<Node> nodes = alignmentGraph.vertexSet();
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				// Create a TriplesMap corresponding to the Internal node
				SubjectMap subjMap = r2rmlMapping.getSubjectMapIndex().get(node.getId());
				TriplesMap subjTrMap = r2rmlMapping.getTriplesMapIndex().get(node.getId());
				
				// Create the predicate object map for each outgoing link
				Set<Link> outgoingEdges = alignmentGraph.outgoingEdgesOf(node);
				for (Link olink:outgoingEdges) {
					if (olink instanceof ObjectPropertySpecializationLink 
							|| olink instanceof DataPropertyOfColumnLink  
							|| olink instanceof ColumnSubClassLink)
						continue;
					
					PredicateObjectMap poMap = new PredicateObjectMap(subjTrMap);
					Node target = olink.getTarget();
					
					// Create an object property map
					if (target instanceof InternalNode) {
						// Get the RefObjMap object for the objectmap
						TriplesMap objTrMap = r2rmlMapping.getTriplesMapIndex().get(target.getId());
						RefObjectMap refObjMap = new RefObjectMap(getNewRefObjectMapId(), objTrMap);
						ObjectMap objMap = new ObjectMap(target.getId(), refObjMap);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						
						// Check if a specialization link exists
						Link specializedEdge = getSpecializationLinkIfExists(olink, node);
						if (specializedEdge != null) {
							Node specializedEdgeTarget = specializedEdge.getTarget();
							if (specializedEdgeTarget instanceof ColumnNode) {
								String columnName = getHNodeIdToColumnNameMapping(((ColumnNode) specializedEdgeTarget).getHNodeId());
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
					
					// Create a data property map
					else if(target instanceof ColumnNode) {
						// Create the object map
						ColumnNode cnode = (ColumnNode) target;
						String hNodeId = cnode.getHNodeId();
						String columnName = this.getHNodeIdToColumnNameMapping(hNodeId);
						ColumnTemplateTerm cnTerm = new ColumnTemplateTerm(columnName);
						TemplateTermSet termSet = new TemplateTermSet();
						termSet.addTemplateTermToSet(cnTerm);

						String rdfLiteralUri = 	cnode.getRdfLiteralType() == null? "" : cnode.getRdfLiteralType().getUri();
						StringTemplateTerm rdfLiteralTypeTerm = new StringTemplateTerm(rdfLiteralUri, true);
						TemplateTermSet rdfLiteralTypeTermSet = new TemplateTermSet();
						rdfLiteralTypeTermSet.addTemplateTermToSet(rdfLiteralTypeTerm);

						ObjectMap objMap = new ObjectMap(hNodeId, termSet, rdfLiteralTypeTermSet);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						
						// Check if a specialization link exists
						Link specializedEdge = getSpecializationLinkIfExists(olink, node);
						if (specializedEdge != null) {
							Node specializedEdgeTarget = specializedEdge.getTarget();
							if (specializedEdgeTarget instanceof ColumnNode) {
								String targetColumnName = getHNodeIdToColumnNameMapping(((ColumnNode) specializedEdgeTarget).getHNodeId());
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
					if (poMap.getObject() != null && poMap.getPredicate() != null)
						subjTrMap.addPredicateObjectMap(poMap);
				}
			}
		}
	}

	private void saveLinkFromColumnNameToPredicateObjectMap(String columnName, PredicateObjectMap poMap) {
		
		List<PredicateObjectMap> pomList = r2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().get(columnName);  
		if (pomList == null) {
			pomList = new ArrayList<PredicateObjectMap>();
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
				
				PredicateObjectMap poMap = new PredicateObjectMap(subjTrMap);
				
				// Create the object map
				String columnName = this.getHNodeIdToColumnNameMapping(hNodeId);
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
			PredicateObjectMap poMap, Link olink, TriplesMap subjTrMap) {
		String propUri = olink.getLabel().getUri();
		//this can happen if propertyName is not an Object property; it could be a subclass
		if (!ontMgr.isObjectProperty(propUri))
			return;
		
		Label inversePropLabel = ontMgr.getInverseProperty(propUri);
		Label inverseOfPropLabel = ontMgr.getInverseOfProperty(propUri);
		
		if (inversePropLabel != null) {
			TriplesMap inverseTrMap = r2rmlMapping.getTriplesMapIndex().get(poMap.getObject().getId());
			// Create the predicate object map
			PredicateObjectMap invPoMap = new PredicateObjectMap(inverseTrMap);
			// Create the predicate
			Predicate pred = new Predicate(olink.getId()+"+inverse");
			pred.getTemplate().addTemplateTermToSet(
					new StringTemplateTerm(inversePropLabel.getUri(), true));
			invPoMap.setPredicate(pred);
			// Create the object using RefObjMap
			RefObjectMap refObjMap = new RefObjectMap(getNewRefObjectMapId(), subjTrMap);
			ObjectMap invObjMap = new ObjectMap(subjMap.getId(), refObjMap);
			invPoMap.setObject(invObjMap);
			inverseTrMap.addPredicateObjectMap(invPoMap);
			// Add the link to the link set
			r2rmlMapping.getAuxInfo().getTriplesMapGraph().addLink(new TriplesMapLink(inverseTrMap, subjTrMap, invPoMap));
		}
		if (inverseOfPropLabel != null) {
			// Create the triples map
			// Get the object's triples map
			TriplesMap inverseOfTrMap = r2rmlMapping.getTriplesMapIndex().get(poMap.getObject().getId());
			
			PredicateObjectMap invOfPoMap = new PredicateObjectMap(inverseOfTrMap);
			// Create the predicate
			Predicate pred = new Predicate(olink.getId()+"+inverseOf");
			pred.getTemplate().addTemplateTermToSet(
					new StringTemplateTerm(inverseOfPropLabel.getUri(), true));
			invOfPoMap.setPredicate(pred);
			// Create the object using RefObjMap
			RefObjectMap refObjMap = new RefObjectMap(getNewRefObjectMapId(), subjTrMap);
			ObjectMap invOfObjMap = new ObjectMap(subjMap.getId(), refObjMap);
			invOfPoMap.setObject(invOfObjMap);
			inverseOfTrMap.addPredicateObjectMap(invOfPoMap);
			// Add the link to the link set
			r2rmlMapping.getAuxInfo().getTriplesMapGraph().addLink(new TriplesMapLink(inverseOfTrMap, subjTrMap, invOfPoMap));
		}
	}

	private Link getSpecializationLinkIfExists(Link link, Node sourceNode) {
		Set<Link> outgoingEdges = this.alignmentGraph.outgoingEdgesOf(sourceNode);
		for (Link olink:outgoingEdges) {
			// Check for the object property specialization
			if (olink instanceof ObjectPropertySpecializationLink ) {
				String splLinkId = ((ObjectPropertySpecializationLink) olink).getId();
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
	
	private String getHNodeIdToColumnNameMapping(String hNodeId)
	{
		
		if(!this.hNodeIdsToColumnName.containsKey(hNodeId))
		{
			hNodeIdsToColumnName.put(hNodeId, translateHNodeIdToColumnName(hNodeId));
		}
		return hNodeIdsToColumnName.get(hNodeId);
		
	}
	private String translateHNodeIdToColumnName(String hNodeId)
	{
		HNode hNode = factory.getHNode(hNodeId);
		String colNameStr = "";
		try {
			JSONArray colNameArr = hNode.getJSONArrayRepresentation(factory);
			if (colNameArr.length() == 1) {
				colNameStr = (String) 
						(((JSONObject)colNameArr.get(0)).get("columnName"));
			} else {
				JSONArray colNames = new JSONArray();
				for (int i=0; i<colNameArr.length();i++) {
					colNames.put((String)
							(((JSONObject)colNameArr.get(i)).get("columnName")));
				}
				colNameStr = colNames.toString();
			}
			return colNameStr;
		} catch (JSONException e) {
			logger.debug("unable to find hnodeid to column name mapping for hnode: " + hNode.getId() + " " + hNode.getColumnName(), e);
		}
		return null;
	}
	
	private String getNewRefObjectMapId() {
		return REFOBJECT_MAP_PREFIX + "_" + UUID.randomUUID();
	}
	
	private String getNewTriplesMapId() {
		return TRIPLES_MAP_PREFIX + "_" + UUID.randomUUID();
	}
	
}





















