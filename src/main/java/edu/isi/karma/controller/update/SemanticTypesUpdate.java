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
package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SemanticTypesUpdate extends AbstractUpdate {
	private Worksheet worksheet;
	private String vWorksheetId;
	private Alignment alignment;

	public enum JsonKeys {
		HNodeId, FullType, ConfidenceLevel, Origin, FullCRFModel, DisplayLabel, 
		DisplayDomainLabel, Domain, SemanticTypesArray, isPrimary, isPartOfKey, 
		Types, isMetaProperty, rdfLiteralType
	}

	private static Logger logger = LoggerFactory
			.getLogger(SemanticTypesUpdate.class);

	public SemanticTypesUpdate(Worksheet worksheet, String vWorksheetId, Alignment alignment) {
		super();
		this.worksheet = worksheet;
		this.vWorksheetId = vWorksheetId;
		this.alignment = alignment;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		
		SemanticTypes types = worksheet.getSemanticTypes();
		Map<String, ColumnNode> hNodeIdTocolumnNodeMap = createColumnNodeMap();
		Map<String, InternalNode> hNodeIdToDomainNodeMap = createDomainNodeMap();
		
		JSONStringer jsonStr = new JSONStringer();
		try {
			JSONWriter writer = jsonStr.object();

			writer.key("worksheetId").value(vWorksheetId).key("updateType")
					.value("SemanticTypesUpdate");

			writer.key(JsonKeys.Types.name());
			writer.array();
			// Iterate through all the columns
			for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
				HNode node = path.getLeaf();
				String nodeId = node.getId();

				writer.object();

				// Check if a semantic type exists for the HNode
				SemanticType type = types.getSemanticTypeForHNodeId(nodeId);
				if (type != null && type.getConfidenceLevel() != SemanticType.ConfidenceLevel.Low) {
					writer.key(JsonKeys.HNodeId.name())
							.value(type.getHNodeId())
							.key(JsonKeys.SemanticTypesArray.name()).array();
					
					ColumnNode alignmentColumnNode = hNodeIdTocolumnNodeMap.get(type.getHNodeId());
					InternalNode domainNode = hNodeIdToDomainNodeMap.get(type.getHNodeId());
					
					if (alignmentColumnNode == null || domainNode == null) {
						logger.error("Column node or domain node not found in alignment." +
								" (This should not happen conceptually!):" + type);
						continue;
					}
					
					// Add the primary semantic type
					writer.object()
							.key(JsonKeys.Origin.name())
							.value(type.getOrigin().name())
							.key(JsonKeys.ConfidenceLevel.name())
							.value(type.getConfidenceLevel().name())
							.key(JsonKeys.isPartOfKey.name())
							.value(type.isPartOfKey())
							.key(JsonKeys.isPrimary.name())
							.value(true);
					
					// Add the RDF literal type to show in the text box
					String rdfLiteralType = alignmentColumnNode.getRdfLiteralType();
					rdfLiteralType = (rdfLiteralType != null && !rdfLiteralType.equals("")) ? rdfLiteralType : "";
					writer.key(JsonKeys.rdfLiteralType.name())
						.value(rdfLiteralType);
					
//					String domainDisplayLabel = (domainNode.getLabel().getPrefix() != null && (!domainNode.getLabel().getPrefix().equals(""))) ?
//							(domainNode.getLabel().getPrefix() + ":" + domainNode.getLocalId()) : domainNode.getLocalId();
					if (!type.isClass()) {
						writer
							.key(JsonKeys.FullType.name())
							.value(type.getType().getUri())
							.key(JsonKeys.DisplayLabel.name())
							.value(type.getType().getDisplayName())
							.key(JsonKeys.Domain.name())
							.value(domainNode.getId())
							.key(JsonKeys.DisplayDomainLabel.name())
							.value(domainNode.getDisplayId());
					} else {
						writer
							.key(JsonKeys.FullType.name())
							.value(domainNode.getId())
							.key(JsonKeys.DisplayLabel.name())
							.value(domainNode.getDisplayId())
							.key(JsonKeys.Domain.name())
							.value("")
							.key(JsonKeys.DisplayDomainLabel.name())
							.value("");
					}
					
					// Mark the special properties
					writer
						.key(JsonKeys.isMetaProperty.name())
						.value(isMetaProperty(type.getType(), alignmentColumnNode));
					
					
					writer.endObject();

					// Iterate through the synonym semantic types
					SynonymSemanticTypes synTypes = types
							.getSynonymTypesForHNodeId(nodeId);

					if (synTypes != null) {
						for (SemanticType synType : synTypes.getSynonyms()) {
							writer.object()
									.key(JsonKeys.HNodeId.name())
									.value(synType.getHNodeId())
									.key(JsonKeys.FullType.name())
									.value(synType.getType().getUri())
									.key(JsonKeys.Origin.name())
									.value(synType.getOrigin().name())
									.key(JsonKeys.ConfidenceLevel.name())
									.value(synType.getConfidenceLevel().name())
									.key(JsonKeys.DisplayLabel.name())
									.value(synType.getType().getDisplayName())
									.key(JsonKeys.isPrimary.name())
									.value(false);
							if (!synType.isClass()) {
								writer.key(JsonKeys.Domain.name())
									.value(synType.getDomain().getUri())
									.key(JsonKeys.DisplayDomainLabel.name())
									.value(synType.getDomain().getDisplayName());
							} else {
								writer.key(JsonKeys.Domain.name())
									.value("")
									.key(JsonKeys.DisplayDomainLabel.name())
									.value("");
							}
							writer.endObject();
						}
					}
					writer.endArray();
				} else {
					writer.key(JsonKeys.HNodeId.name()).value(nodeId);
					writer.key(JsonKeys.SemanticTypesArray.name()).array()
							.endArray();
				}

				// Populate the CRF Model
				CRFColumnModel colModel = worksheet.getCrfModel().getModelByHNodeId(nodeId);
				if (colModel != null) {
					writer.key(JsonKeys.FullCRFModel.name()).value(colModel.getAsJSONObject(vWorkspace.getWorkspace().getOntologyManager(), alignment));
				}

				writer.endObject();
			}
			writer.endArray();
			writer.endObject();

			pw.print(writer.toString());
		} catch (JSONException e) {
			logger.error("Error occured while writing to JSON!", e);
		}
	}

	private boolean isMetaProperty(Label type, ColumnNode alignmentColumnNode) {
		// Check for the case of DataPropertyOfColumnLink and ColumnSubClassLink
		boolean case1 =  (type.getUri().equals(DataPropertyOfColumnLink.getFixedLabel().getUri()) 
				|| type.getUri().equals(ColumnSubClassLink.getFixedLabel().getUri()));
		if (case1)
			return true;
		else {	// Check for the class instance link with LinkKeyInfo as UriOfInstance
			Set<Link> incomingLinks = alignment.getCurrentLinksToNode(alignmentColumnNode.getId());
			if (incomingLinks != null && !incomingLinks.isEmpty()) {
				Link incomingLink = incomingLinks.iterator().next();
				if (incomingLink != null && (incomingLink instanceof ClassInstanceLink) 
						&& incomingLink.getKeyType().equals(LinkKeyInfo.UriOfInstance))
					return true;
			}
			
		}
		return false;
	}

	private Map<String, InternalNode> createDomainNodeMap() {
		Map<String, InternalNode> hNodeIdToDomainNodeMap = new HashMap<String, InternalNode>();
		List<Node> alignmentColumnNodes = alignment.getNodesByType(NodeType.ColumnNode);
		if (alignmentColumnNodes == null)
			return hNodeIdToDomainNodeMap;
		for (Node cNode : alignmentColumnNodes) {
			Set<Link> incomingLinks = alignment.getCurrentLinksToNode(cNode.getId());
			if (incomingLinks != null && !incomingLinks.isEmpty()) {
				Link incomingLink = alignment.getCurrentLinksToNode(cNode.getId()).iterator().next();
				if (incomingLink!= null && incomingLink.getSource() instanceof InternalNode) {
					hNodeIdToDomainNodeMap.put(((ColumnNode)cNode).getHNodeId()
							, (InternalNode)incomingLink.getSource());
				}
			}
			
		}
		return hNodeIdToDomainNodeMap;
	}

	private Map<String, ColumnNode> createColumnNodeMap() {
		List<Node> alignmentColumnNodes = alignment.getNodesByType(NodeType.ColumnNode);
		Map<String, ColumnNode> hNodeIdToColumnNodeMap = new HashMap<String, ColumnNode>();
		if (alignmentColumnNodes == null)
			return hNodeIdToColumnNodeMap;
		for (Node cNode : alignmentColumnNodes) {
			ColumnNode columnNode = (ColumnNode) cNode;
			hNodeIdToColumnNodeMap.put(columnNode.getHNodeId(), columnNode);
		}
		return hNodeIdToColumnNodeMap;
	}
}
