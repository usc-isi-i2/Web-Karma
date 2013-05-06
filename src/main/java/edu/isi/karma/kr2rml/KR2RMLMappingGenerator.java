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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;

public class KR2RMLMappingGenerator {
	
//	private RepFactory factory;
//	private String sourcePrefix;
//	private Worksheet worksheet;
	private R2RMLMapping r2rmlMapping;
	private OntologyManager ontMgr;
	private String sourceNamespace;
	private DirectedWeightedMultigraph<Node, Link> steinerTree;
	
	// Internal data structures required
	Map<String, SubjectMap> subjectMapIndex = new HashMap<String, SubjectMap>();
	Map<String, TriplesMap> triplesMapIndex = new HashMap<String, TriplesMap>();
	
	private static Logger logger = LoggerFactory.getLogger(KR2RMLMappingGenerator.class);
	
	public KR2RMLMappingGenerator(Workspace workspace, Alignment alignment, 
			Worksheet worksheet, String sourcePrefix, String sourceNamespace, 
			boolean generateInverse) {
//		this.worksheet = worksheet;
//		this.sourcePrefix = sourcePrefix;
//		this.factory = workspace.getFactory();
		this.sourceNamespace = sourceNamespace;
		this.ontMgr = workspace.getOntologyManager();
		this.steinerTree = alignment.getSteinerTree();
		
		// Generate the R2RML data structures
		generateMappingFromSteinerTree(generateInverse);
	}
	
	public R2RMLMapping getR2rmlMapping() {
		return this.r2rmlMapping;
	}

	private void generateMappingFromSteinerTree(boolean generateInverse) {
		// Generate TriplesMap for each InternalNode in the tree
		createSubjectMaps();
		
		// Identify the object property links
		createPredicateObjectMaps(generateInverse);
	}

	private void createSubjectMaps() {
		Set<Node> nodes = steinerTree.vertexSet();
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				SubjectMap subj = new SubjectMap(node.getId());
				// Add the user provided namespace as the first template term
				subj.getTemplate().addTemplateTermToSet(new StringTemplateTerm(sourceNamespace));
				
				subjectMapIndex.put(node.getId(), subj);
				
				Set<Link> outgoingLinks = steinerTree.outgoingEdgesOf(node);
				for (Link link:outgoingLinks) {
					
					if (link instanceof ClassInstanceLink || link instanceof ColumnSubClassLink
							|| (link instanceof DataPropertyLink && 
								link.getKeyType() == LinkKeyInfo.PartOfKey)) {
						Node tNode = link.getTarget();
						if (tNode instanceof ColumnNode) {
							ColumnNode cnode = (ColumnNode) tNode;
							String hNodeId = cnode.getHNodeId();
							ColumnNameTemplateTerm cnTerm = new ColumnNameTemplateTerm(hNodeId);
							
							// Identify classInstance links to set the template
							if (link instanceof ClassInstanceLink) {
								subj.getTemplate().clear().addTemplateTermToSet(cnTerm);
							}
							
							// Identify the isSubclassOfClass links to set the correct type
							else if (link instanceof ColumnSubClassLink) {
								TemplateTermSet typeTermSet = new TemplateTermSet();
								typeTermSet.addTemplateTermToSet(cnTerm);
								subj.addRdfsType(typeTermSet);
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
		Set<Node> nodes = steinerTree.vertexSet();
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				// Create a TriplesMap corresponding to the Internal node
				SubjectMap subjMap = subjectMapIndex.get(node.getId());
				TriplesMap trMap = new TriplesMap(subjMap);
				triplesMapIndex.put(node.getId(), trMap);
				this.r2rmlMapping.addTriplesMap(trMap);
				
				// Create the predicate object map for each outgoing link
				Set<Link> outgoingEdges = steinerTree.outgoingEdgesOf(node);
				for (Link olink:outgoingEdges) {
					if (olink instanceof ObjectPropertySpecializationLink 
							|| olink instanceof DataPropertyOfColumnLink)
						continue;
					
					PredicateObjectMap poMap = new PredicateObjectMap();
					Node target = olink.getTarget();
					
					// Create an object property map
					if (target instanceof InternalNode) {
						// Get the RefObjMap object for the objectmap
						TriplesMap objTrMap = triplesMapIndex.get(target.getId());
						RefObjectMap refObjMap = new RefObjectMap(objTrMap);
						ObjectMap objMap = new ObjectMap(target.getId(), refObjMap);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						
						// Check if a specialization link exists
						Link specializedEdge = getSpecializationLinkIfExists(olink, node);
						if (specializedEdge != null) {
							Node specializedEdgeTarget = specializedEdge.getTarget();
							if (specializedEdgeTarget instanceof ColumnNode) {
								ColumnNameTemplateTerm cnTerm = 
										new ColumnNameTemplateTerm(
												((ColumnNode) specializedEdgeTarget).getHNodeId());
								pred.getTemplate().addTemplateTermToSet(cnTerm);
							}
						} else {
							pred.getTemplate().addTemplateTermToSet(
									new StringTemplateTerm(olink.getLabel().getUri()));
						}
						poMap.setPredicate(pred);
						
						if (generateInverse)
							addInversePropertyIfExists(subjMap, poMap, olink, trMap);
					}
					
					// Create a data property map
					else if(target instanceof ColumnNode) {
						// Create the object map
						ColumnNode cnode = (ColumnNode) target;
						String hNodeId = cnode.getHNodeId();
						ColumnNameTemplateTerm cnTerm = new ColumnNameTemplateTerm(hNodeId);
						TemplateTermSet termSet = new TemplateTermSet();
						termSet.addTemplateTermToSet(cnTerm);
						ObjectMap objMap = new ObjectMap(hNodeId, termSet);
						poMap.setObject(objMap);
						
						// Create the predicate
						Predicate pred = new Predicate(olink.getId());
						
						// Check if a specialization link exists
						Link specializedEdge = getSpecializationLinkIfExists(olink, node);
						if (specializedEdge != null) {
							Node specializedEdgeTarget = specializedEdge.getTarget();
							if (specializedEdgeTarget instanceof ColumnNode) {
								ColumnNameTemplateTerm cnsplTerm = 
										new ColumnNameTemplateTerm(
												((ColumnNode) specializedEdgeTarget).getHNodeId());
								pred.getTemplate().addTemplateTermToSet(cnsplTerm);
							}
						} else {
							pred.getTemplate().addTemplateTermToSet(
									new StringTemplateTerm(olink.getLabel().getUri()));
						}
						poMap.setPredicate(pred);
					}
					// Add the predicateobjectmap to the triples map after a sanity check
					if (poMap.getObject() != null && poMap.getPredicate() != null)
						trMap.addPredicateObjectMap(poMap);
				}
			}
		}
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
			// Create the Subject Map
			SubjectMap invSubjMap = subjectMapIndex.get(poMap.getObject().getId());
			// Create the triples map
			TriplesMap inverseTrMap = new TriplesMap(invSubjMap);
			// Create the predicate object map
			PredicateObjectMap invPoMap = new PredicateObjectMap();
			// Create the predicate
			Predicate pred = new Predicate(olink.getId()+"+inverse");
			pred.getTemplate().addTemplateTermToSet(
					new StringTemplateTerm(inversePropLabel.getUri()));
			invPoMap.setPredicate(pred);
			// Create the object using RefObjMap
			RefObjectMap refObjMap = new RefObjectMap(subjTrMap);
			ObjectMap invObjMap = new ObjectMap(subjMap.getId(), refObjMap);
			invPoMap.setObject(invObjMap);
			inverseTrMap.addPredicateObjectMap(invPoMap);
			
			// Add to mapping
			r2rmlMapping.addTriplesMap(inverseTrMap);
		}
		if (inverseOfPropLabel != null) {
			// Create the Subject Map
			SubjectMap invOfSubjMap = subjectMapIndex.get(poMap.getObject().getId());
			// Create the triples map
			TriplesMap inverseOfTrMap = new TriplesMap(invOfSubjMap);
			// Create the predicate object map
			PredicateObjectMap invOfPoMap = new PredicateObjectMap();
			// Create the predicate
			Predicate pred = new Predicate(olink.getId()+"+inverseOf");
			pred.getTemplate().addTemplateTermToSet(
					new StringTemplateTerm(inverseOfPropLabel.getUri()));
			invOfPoMap.setPredicate(pred);
			// Create the object using RefObjMap
			RefObjectMap refObjMap = new RefObjectMap(subjTrMap);
			ObjectMap invOfObjMap = new ObjectMap(subjMap.getId(), refObjMap);
			invOfPoMap.setObject(invOfObjMap);
			inverseOfTrMap.addPredicateObjectMap(invOfPoMap);
			
			// Add to mapping
			r2rmlMapping.addTriplesMap(inverseOfTrMap);
		}
	}

	private Link getSpecializationLinkIfExists(Link link, Node sourceNode) {
		Set<Link> outgoingEdges = this.steinerTree.outgoingEdgesOf(sourceNode);
		for (Link olink:outgoingEdges) {
			// Check for the object property specialization
			if (olink instanceof ObjectPropertySpecializationLink ) {
				ObjectPropertySpecializationLink splLink = (ObjectPropertySpecializationLink) olink;
				if (splLink.getSpecializedLink().getId().equals(link.getId()))
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





















