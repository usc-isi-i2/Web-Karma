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
import java.util.ArrayList;
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
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SemanticTypesUpdate extends AbstractUpdate {
	private Worksheet worksheet;
	private String worksheetId;
	private Alignment alignment;

	public enum JsonKeys {
		HNodeId, FullType, ConfidenceLevel, Origin, DisplayLabel, DisplayRDFSLabel, DisplayRDFSComment,
		DisplayDomainLabel, DomainRDFSLabel, DomainRDFSComment, DomainId, DomainUri, SemanticTypesArray, isPrimary, isPartOfKey, 
		Types, isMetaProperty, rdfLiteralType, language, isProvenance
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
				ArrayList<SemanticType> semTypes = types.getSemanticTypeForHNodeId(nodeId);
				writer.key(JsonKeys.HNodeId.name()).value(nodeId);
				writer.key(JsonKeys.SemanticTypesArray.name()).array();
				for(SemanticType type : semTypes)
					writeType(type, writer, hNodeIdTocolumnNodeMap);
				writer.endArray();
				
				
				writer.endObject();
			}
			writer.endArray();
			writer.endObject();

			pw.print(writer.toString());
		} catch (JSONException e) {
			logger.error("Error occured while writing to JSON!", e);
		}
	}

	private void writeType(SemanticType type, JSONWriter writer, Map<String, ColumnNode> hNodeIdTocolumnNodeMap) {
		if (type != null && type.getConfidenceLevel() != SemanticType.ConfidenceLevel.Low) {
			
			ColumnNode alignmentColumnNode = hNodeIdTocolumnNodeMap.get(type.getHNodeId());
			
			if (alignmentColumnNode == null) {
				logger.error("Column node or domain node not found in alignment." +
						" (This should not happen conceptually!):" + type);
				return;
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
			String language = alignmentColumnNode.getLanguage() == null ? "" :
							alignmentColumnNode.getLanguage();
			writer.key(JsonKeys.rdfLiteralType.name()).value(rdfLiteralType);
			writer.key(JsonKeys.language.name()).value(language);
			
			String index = type.getDomainId().substring(type.getDomain().getUri().length());
			String domainDisplayLabel = type.getDomain().getDisplayName() + index;
			if (!type.isClass()) {
				writer
					.key(JsonKeys.FullType.name()).value(type.getType().getUri())
					.key(JsonKeys.DisplayLabel.name()).value(type.getType().getDisplayName())
					.key(JsonKeys.DisplayRDFSLabel.name()).value(type.getType().getRdfsLabel())
					.key(JsonKeys.DisplayRDFSComment.name()).value(type.getType().getRdfsComment())
					.key(JsonKeys.DomainId.name()).value(type.getDomainId())
					.key(JsonKeys.DomainUri.name()).value(type.getDomain().getUri())
					.key(JsonKeys.DisplayDomainLabel.name()).value(domainDisplayLabel)
					.key(JsonKeys.DomainRDFSLabel.name()).value(type.getDomain().getRdfsLabel())
					.key(JsonKeys.DomainRDFSComment.name()).value(type.getDomain().getRdfsComment())
					.key(JsonKeys.isProvenance.name()).value(type.isProvenance())
					;
			} else {
				writer
					.key(JsonKeys.FullType.name()).value(type.getDomainId())
					.key(JsonKeys.DisplayLabel.name()).value(domainDisplayLabel)
					.key(JsonKeys.DisplayRDFSLabel.name()).value(type.getDomain().getRdfsLabel())
					.key(JsonKeys.DisplayRDFSComment.name()).value(type.getDomain().getRdfsComment())
					.key(JsonKeys.DomainId.name()).value("")
					.key(JsonKeys.DomainUri.name()).value("")
					.key(JsonKeys.DisplayDomainLabel.name()).value("")
					.key(JsonKeys.DomainRDFSLabel.name()).value("")
					.key(JsonKeys.DomainRDFSComment.name()).value("")
					.key(JsonKeys.isProvenance.name()).value(type.isProvenance())
					;
			}
			
			// Mark the special properties
			writer
				.key(JsonKeys.isMetaProperty.name())
				.value(isMetaProperty(type.getType(), alignmentColumnNode));
			
			
			writer.endObject();
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

	private Map<String, ColumnNode> createColumnNodeMap() {
		Set<Node> alignmentColumnNodes = alignment.getNodesByType(NodeType.ColumnNode);
		Map<String, ColumnNode> hNodeIdToColumnNodeMap = new HashMap<>();
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
	
	
}
