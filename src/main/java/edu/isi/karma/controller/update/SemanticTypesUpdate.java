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

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.rep.semantictypes.SynonymSemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SemanticTypesUpdate extends AbstractUpdate {
	private Worksheet worksheet;
	private String vWorksheetId;

	public enum JsonKeys {
		HNodeId, FullType, ConfidenceLevel, Origin, FullCRFModel, DisplayLabel, DisplayDomainLabel, Domain, SemanticTypesArray, isPrimary, isPartOfKey, Types
	}

	private static Logger logger = LoggerFactory
			.getLogger(SemanticTypesUpdate.class);

	public SemanticTypesUpdate(Worksheet worksheet, String vWorksheetId) {
		super();
		this.worksheet = worksheet;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		SemanticTypes types = worksheet.getSemanticTypes();

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
				if (type != null
						&& type.getConfidenceLevel() != SemanticType.ConfidenceLevel.Low) {
					writer.key(JsonKeys.HNodeId.name())
							.value(type.getHNodeId())
							.key(JsonKeys.SemanticTypesArray.name()).array();
					// Add the primary semantic type
					writer.object()
							.key(JsonKeys.FullType.name())
							.value(type.getType().getUriString())
							.key(JsonKeys.Origin.name())
							.value(type.getOrigin().name())
							.key(JsonKeys.ConfidenceLevel.name())
							.value(type.getConfidenceLevel().name())
							.key(JsonKeys.DisplayLabel.name())
							.value(type.getType().getLocalNameWithPrefixIfAvailable())
							.key(JsonKeys.Domain.name())
							.value(type.getDomain().getUriString())
							.key(JsonKeys.isPartOfKey.name())
							.value(type.isPartOfKey())
							.key(JsonKeys.DisplayDomainLabel.name())
							.value(type.getDomain().getLocalNameWithPrefixIfAvailable())
							.key(JsonKeys.isPrimary.name()).value(true);
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
									.value(synType.getType().getUriString())
									.key(JsonKeys.Origin.name())
									.value(synType.getOrigin().name())
									.key(JsonKeys.ConfidenceLevel.name())
									.value(synType.getConfidenceLevel().name())
									.key(JsonKeys.DisplayLabel.name())
									.value(synType.getType().getLocalNameWithPrefixIfAvailable())
									.key(JsonKeys.Domain.name())
									.value(synType.getDomain().getUriString())
									.key(JsonKeys.DisplayDomainLabel.name())
									.value(synType.getDomain().getLocalNameWithPrefixIfAvailable())
									.key(JsonKeys.isPrimary.name())
									.value(false);
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
					writer.key(JsonKeys.FullCRFModel.name()).value(colModel.getAsJSONObject(vWorkspace.getWorkspace().getOntologyManager()));
				}

				writer.endObject();
			}
			writer.endArray();
			writer.endObject();

			pw.print(writer.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error("Error occured while writing to JSON!", e);
		}
	}
}
