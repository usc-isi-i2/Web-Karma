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

package edu.isi.karma.controller.command.reconciliation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.ReportMessage;
import edu.isi.karma.kr2rml.TriplesMap;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;

public class InvokeRubenReconciliationService extends Command {
	private final String alignmentNodeId;
	private final String vWorksheetId;
	private String rdfPrefix;
	private String rdfNamespace;

	private final String reconciliationServiceUrl = "http://entities.restdesc.org/disambiguations/";
	
	public InvokeRubenReconciliationService(String id, String alignmentNodeId, String vWorksheetId) {
		super(id);
		this.alignmentNodeId = alignmentNodeId;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Invoke Reconciliation";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		RepFactory f = vWorkspace.getRepFactory();
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId));
	
		// Set the prefix and namespace to be used while generating RDF
		fetchRdfPrefixAndNamespaceFromPreferences(vWorkspace);
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(
				vWorkspace.getWorkspace().getOntologyManager(), alignment, 
				worksheet.getSemanticTypes(), rdfPrefix, rdfNamespace,
				true, errorReport);
		TriplesMap trMap = mappingGen.getTriplesMapForNodeId(alignmentNodeId);
		
		// Remove the triple maps and info that we don't need
//		filterTripleMapsAndAuxillaryInformation();

		// Get the column that contains the key for the internal node
		String keyColumnHNodeId = getKeyColumnHNodeIdForAlignmentNode(alignment);
		if (keyColumnHNodeId == null) {
			return new UpdateContainer(new ErrorUpdate("Please assign a column as a key for the class"));
		}
		
		// Loop through each row that contains the column containing key
		HNode hNode = f.getHNode(keyColumnHNodeId);
		HNodePath path = hNode.getHNodePath(f);
		
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(path, nodes);
		
		Map<Row, String> rowToUriMap = new HashMap<Row, String>();
		// For through each row, generate the RDF, and invoke the service
		try {
			for (Node node:nodes) {
				System.out.println("*****************************" + node.getId());
				Row row = node.getBelongsToRow();
				
				// Generate the RDF
				StringWriter outRdf = new StringWriter();
				PrintWriter pw = new PrintWriter(outRdf);
				KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet, 
						vWorkspace.getRepFactory(), vWorkspace.getWorkspace().getOntologyManager(),
						pw, mappingGen.getMappingAuxillaryInformation(), errorReport, false);
				
				rdfGen.generateTriplesForRow(row, new HashSet<String>(), new HashSet<String>(),
						new HashMap<String, ReportMessage>(), new HashSet<String>());
				
				pw.flush();
				String rdf = outRdf.toString();
				// Sanity check
				if (rdf == null || rdf.trim().isEmpty()) continue;
				
				String keyUri = rdfGen.normalizeUri(rdfGen.getTemplateTermSetPopulatedWithValues(node.getColumnValues()
						, trMap.getSubject().getTemplate()));
				rowToUriMap.put(row, keyUri);
			
				String serviceInput = rdf.replaceAll('<' + keyUri + '>', "?x");
				String res = invokeReconcilitaionService(serviceInput);
				
				if (res == null || res.isEmpty()) {
					System.out.println("No linking output for " + serviceInput);
					continue;
				}
				
				// Insert the subject uri inside the service output
				int triplesStartIndex = res.indexOf("[");
				if (triplesStartIndex != -1) {
					String finalRdfOutput = res.substring(0, triplesStartIndex) + 
							"<" + keyUri + "> <" + Uris.KM_LINKING_MATCHES_URI + "> " +
							res.substring(triplesStartIndex);
					HTTPUtil.executeHTTPPostRequest(TripleStoreUtil.defaultDataRepoUrl + "/statements", "text/n3", 
							"", finalRdfOutput);
				}
				
				outRdf.close();
				pw.close();
			}
			
			// Add a column at the same level as key column
			HNode linkingHNode = hNode.getHTable(f).addNewHNodeAfter(hNode.getId(), f,
					"LinkingMatches", worksheet, true);
			// Add a nested table inside the linkingHNode
			HTable linkingNestedTable = linkingHNode.addNestedTable("Matches", worksheet, f);
			HNode entityColHNode = linkingNestedTable.addHNode("Entity", worksheet, f);
			HNode scoreColHNode = linkingNestedTable.addNewHNodeAfter(entityColHNode.getId(), 
					f, "Score", worksheet, true);
			
			// For each row, query the triple store to get the possible matches
			for (Row row:rowToUriMap.keySet()) {
				String subjUri = rowToUriMap.get(row);
				
				// Query the triple store to get a list of matches
				String query = "PREFIX d:<http://entities.restdesc.org/terms#> " +
						"SELECT ?entity ?score WHERE " +
						"{ <" + subjUri + "> <" + Uris.KM_LINKING_MATCHES_URI + "> ?x ." +
						"  ?x d:possibleMatch ?match . " +
						"  ?match d:entity ?entity . " +
						"  ?match d:similarity ?score . " +
						"}";
//				System.out.println(query);
				JSONObject queryRes = TripleStoreUtil.invokeSparqlQuery(query, TripleStoreUtil.defaultDataRepoUrl);
				if (queryRes != null) {
					Table linkingDataTable = row.getNode(linkingHNode.getId()).getNestedTable();
					
					JSONArray bindings = queryRes.getJSONObject("results").getJSONArray("bindings");
					if (bindings == null || bindings.length() == 0) continue;
					
					for (int i=0; i<bindings.length(); i++) {
						JSONObject binding = bindings.getJSONObject(i);
						Row r1 = linkingDataTable.addRow(f);
						String score = binding.getJSONObject("score").getString("value");
						if (score.length() > 4) {
							score = score.substring(0, 3);
						}
						r1.setValue(entityColHNode.getId(), binding.getJSONObject("entity").getString("value"), f);
						r1.setValue(scoreColHNode.getId(), score, f);
						
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Prepare the output container
		UpdateContainer c = new UpdateContainer();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,worksheet.getHeaders().getAllPaths(), vWorkspace);
		vWorkspace.getViewFactory().getVWorksheet(this.vWorksheetId).update(c);
		
		/** Add the alignment update **/
		 addAlignmentUpdate(c, vWorkspace, worksheet);
		
		c.add(new InfoUpdate("Linking complete"));
		return c;
	}

//	private void filterTripleMapsAndAuxillaryInformation() {
//		
//	}

	private String invokeReconcilitaionService(String serviceInput) {
		try {
			String output = HTTPUtil.executeHTTPPostRequest(reconciliationServiceUrl, "text/n3",
					null, serviceInput);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getKeyColumnHNodeIdForAlignmentNode(Alignment alignment) {
		for (Link outgoingLink:alignment.getCurrentOutgoingLinksToNode(alignmentNodeId)) {
			// Column contains uris for the internal node
			if (outgoingLink instanceof ClassInstanceLink 
					&& (outgoingLink.getKeyType() == LinkKeyInfo.UriOfInstance 
					    || outgoingLink.getKeyType() == LinkKeyInfo.PartOfKey)) {
				if (outgoingLink.getTarget() instanceof ColumnNode) {
					return ((ColumnNode) outgoingLink.getTarget()).getHNodeId();
				}
			}
			// Column link is a data property marked as key
			if (outgoingLink instanceof DataPropertyLink 
					&& outgoingLink.getKeyType() == LinkKeyInfo.PartOfKey) {
				if (outgoingLink.getTarget() instanceof ColumnNode) {
					return ((ColumnNode) outgoingLink.getTarget()).getHNodeId();
				}
			}
		}
		return null;
	}
	

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}
	
	private void fetchRdfPrefixAndNamespaceFromPreferences(VWorkspace vWorkspace) {
		//get the rdf prefix from the preferences
		ViewPreferences prefs = vWorkspace.getPreferences();
		JSONObject prefObject = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
		this.rdfNamespace = "http://localhost/source/";
		this.rdfPrefix = "s";
		if(prefObject!=null){
			this.rdfPrefix = prefObject.optString("rdfPrefix");
			this.rdfNamespace = prefObject.optString("rdfNamespace");
		}
		if(rdfPrefix==null || rdfPrefix.trim().isEmpty()) {
			this.rdfPrefix = "http://localhost/source/";
		}
	}
	
	private void addAlignmentUpdate(UpdateContainer c, VWorkspace vWorkspace, Worksheet worksheet) {
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				vWorkspace.getWorkspace().getId(), vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(vWorkspace.getWorkspace().getOntologyManager());
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}
		// Compute the semantic type suggestions
		SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet, vWorkspace.getWorkspace()
				.getCrfModelHandler(), vWorkspace.getWorkspace().getOntologyManager(), alignment);
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
		c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorkspace.getViewFactory().
				getVWorksheet(vWorksheetId), alignment));
	}
}
