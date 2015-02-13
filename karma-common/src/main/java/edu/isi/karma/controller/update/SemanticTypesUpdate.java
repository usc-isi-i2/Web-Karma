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
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SemanticTypesUpdate extends AbstractUpdate {
	private Worksheet worksheet;
	private String worksheetId;
	private Alignment alignment;

	public enum JsonKeys {
		HNodeId, FullType, ConfidenceLevel, Origin, DisplayLabel, 
		DisplayDomainLabel, DomainId, DomainUri, SemanticTypesArray, isPrimary, isPartOfKey, 
		Types, isMetaProperty, rdfLiteralType
	}

	private static Logger logger = LoggerFactory
			.getLogger(SemanticTypesUpdate.class);

	public SemanticTypesUpdate(Worksheet worksheet, String worksheetId) {
		super();
		this.worksheet = worksheet;
		this.worksheetId = worksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		Workspace workspace = vWorkspace.getWorkspace();
		alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
		SemanticTypes types = worksheet.getSemanticTypes();
		Map<String, ColumnNode> hNodeIdTocolumnNodeMap = createColumnNodeMap();
		Map<String, SemanticTypeNode> hNodeIdToDomainNodeMap = createDomainNodeMap();
		
		JSONStringer jsonStr = new JSONStringer();
		try {
			JSONWriter writer = jsonStr.object();
			writer.key("worksheetId").value(worksheetId).key("updateType")
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
					SemanticTypeNode domainNode = hNodeIdToDomainNodeMap.get(type.getHNodeId());
					
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
							.key(JsonKeys.isPrimary.name())
							.value(true);
					
					// Add the RDF literal type to show in the text box
					String rdfLiteralType = alignmentColumnNode.getRdfLiteralType() == null? "" : 
						alignmentColumnNode.getRdfLiteralType().getDisplayName();
					writer.key(JsonKeys.rdfLiteralType.name())
						.value(rdfLiteralType);
					
//					String domainDisplayLabel = (domainNode.getLabel().getPrefix() != null && (!domainNode.getLabel().getPrefix().equals(""))) ?
//							(domainNode.getLabel().getPrefix() + ":" + domainNode.getLocalId()) : domainNode.getLocalId();
					if (!type.isClass()) {
						writer
							.key(JsonKeys.FullType.name()).value(type.getType().getUri())
							.key(JsonKeys.DisplayLabel.name()).value(type.getType().getDisplayName())
							.key(JsonKeys.DomainId.name()).value(domainNode.getId())
							.key(JsonKeys.DomainUri.name()).value(domainNode.getUri())
							.key(JsonKeys.DisplayDomainLabel.name()).value(domainNode.getDisplayId());
					} else {
						writer
							.key(JsonKeys.FullType.name()).value(domainNode.getId())
							.key(JsonKeys.DisplayLabel.name()).value(domainNode.getDisplayId())
							.key(JsonKeys.DomainId.name()).value("")
							.key(JsonKeys.DomainUri.name()).value("")
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
								writer.key(JsonKeys.DomainUri.name()).value(synType.getDomain().getUri())
									.key(JsonKeys.DomainId.name()).value("")
									.key(JsonKeys.DisplayDomainLabel.name())
									.value(synType.getDomain().getDisplayName());
							} else {
								writer.key(JsonKeys.DomainId.name()).value("")
									.key(JsonKeys.DomainUri.name()).value("")
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
		Set<LabeledLink> incomingLinks = alignment.getCurrentIncomingLinksToNode(alignmentColumnNode.getId());
		if (incomingLinks != null && !incomingLinks.isEmpty()) {
			LabeledLink incomingLink = incomingLinks.iterator().next();
			if (incomingLink != null && (incomingLink instanceof ClassInstanceLink) 
					&& incomingLink.getKeyType().equals(LinkKeyInfo.UriOfInstance))
				return true;
		}
		return false;
	}

	private Map<String, SemanticTypeNode> createDomainNodeMap() {
		Map<String, SemanticTypeNode> hNodeIdToDomainNodeMap = new HashMap<String, SemanticTypeNode>();
		Set<Node> alignmentColumnNodes = alignment.getNodesByType(NodeType.ColumnNode);
		if (alignmentColumnNodes == null)
			return hNodeIdToDomainNodeMap;
		for (Node cNode : alignmentColumnNodes) {
			Set<LabeledLink> incomingLinks = alignment.getCurrentIncomingLinksToNode(cNode.getId());
			if (incomingLinks != null && !incomingLinks.isEmpty()) {
				LabeledLink incomingLink = alignment.getCurrentIncomingLinksToNode(cNode.getId()).iterator().next();
				if (incomingLink!= null && incomingLink.getSource() instanceof InternalNode) {
					String hNodeId = ((ColumnNode)cNode).getHNodeId();
					if(incomingLink instanceof DataPropertyOfColumnLink || incomingLink instanceof ObjectPropertySpecializationLink) {
						String id = null;
						if(incomingLink instanceof DataPropertyOfColumnLink)
							id = ((DataPropertyOfColumnLink)incomingLink).getSpecializedLinkId();
						else 
							id = ((ObjectPropertySpecializationLink)incomingLink).getSpecializedLinkId();
						hNodeIdToDomainNodeMap.put(hNodeId, new SemanticTypeNode(id, id, id));
					} else {
						InternalNode source = (InternalNode)incomingLink.getSource();
						hNodeIdToDomainNodeMap.put(hNodeId, new SemanticTypeNode(source.getId(), 
								source.getUri(), source.getDisplayId()));
					}
				}
			}
			
		}
		return hNodeIdToDomainNodeMap;
	}

	private Map<String, ColumnNode> createColumnNodeMap() {
		Set<Node> alignmentColumnNodes = alignment.getNodesByType(NodeType.ColumnNode);
		Map<String, ColumnNode> hNodeIdToColumnNodeMap = new HashMap<String, ColumnNode>();
		if (alignmentColumnNodes == null)
			return hNodeIdToColumnNodeMap;
		for (Node cNode : alignmentColumnNodes) {
			ColumnNode columnNode = (ColumnNode) cNode;
			hNodeIdToColumnNodeMap.put(columnNode.getHNodeId(), columnNode);
		}
		return hNodeIdToColumnNodeMap;
	}
	
	public boolean equals(Object o) {
		if (o instanceof SemanticTypesUpdate) {
			SemanticTypesUpdate t = (SemanticTypesUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
		
	}
	
	private class SemanticTypeNode {
		private String id;
		private String uri;
		private String displayId;
		
		SemanticTypeNode(String id, String uri, String displayId) {
			this.id = id;
			this.uri = uri;
			this.displayId = displayId;
		}

		public String getId() {
			return id;
		}

		public String getUri() {
			return uri;
		}

		public String getDisplayId() {
			return displayId;
		}
		
	}
}
