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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.ReportMessage;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.webserver.KarmaException;

public class InvokeRubenReconciliationService extends WorksheetCommand {
	private static Logger logger = LoggerFactory.getLogger(InvokeRubenReconciliationService.class);
	private final String alignmentNodeId;
	private String rdfPrefix;
	private String rdfNamespace;

	private final String reconciliationServiceUrl = "http://entities.restdesc.org/disambiguations/";
	
	public InvokeRubenReconciliationService(String id, String alignmentNodeId, String worksheetId) {
		super(id, worksheetId);
		this.alignmentNodeId = alignmentNodeId;
		
		addTag(CommandTag.Transformation);
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		RepFactory f = workspace.getFactory();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
						worksheetId));
	
		// Set the prefix and namespace to be used while generating RDF
		fetchRdfPrefixAndNamespaceFromPreferences(workspace);
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();

		KR2RMLMappingGenerator mappingGen = null;
		
		try{
			mappingGen = new KR2RMLMappingGenerator(workspace, worksheet,
		
				alignment, worksheet.getSemanticTypes(), rdfPrefix, rdfNamespace, 
				true, errorReport);
		}
		catch (KarmaException e)
		{
			logger.error("Unable to generate mapping for Ruben Reconciliation Service!", e);
			return new UpdateContainer(new ErrorUpdate("Unable to generate mapping for Ruben Reconciliation Service!: " + e.getMessage()));
		}
		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
		TriplesMap trMap = mapping.getTriplesMapIndex().get(alignmentNodeId);
		
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
			int count = 1;
			for (Node node:nodes) {
				if (count % 5 ==0) {
					logger.debug("Done invoking linking service for " + count + " rows");
				}
				
				Row row = node.getBelongsToRow();
				
				// Generate the RDF
				StringWriter outRdf = new StringWriter();
				PrintWriter pw = new PrintWriter(outRdf);
				
				KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
						workspace.getFactory(), workspace.getOntologyManager(),
						pw, mapping, errorReport, false);
				
				rdfGen.generateTriplesForRow(row, new HashSet<String>(), new HashSet<String>(),
						new HashMap<String, ReportMessage>(), new HashSet<String>());
				
				pw.flush();
				String rdf = outRdf.toString();
				// Sanity check
				if (rdf == null || rdf.trim().isEmpty()) continue;
				
				String keyUri = URIFormatter.normalizeUri(rdfGen.getTemplateTermSetPopulatedWithValues(node
						, trMap.getSubject().getTemplate()));
				rowToUriMap.put(row, keyUri);
				
				// Check if the matches already exist in the triple store
				if (checkTripleStoreIfMatchAlreadyExists(keyUri)) {
					logger.debug("Match already exists!");
					outRdf.close();
					pw.close();
					count++;
					continue;
				}
				
				// Invoke the linking service if no match exists in the triple store
				String serviceInput = rdf.replaceAll('<' + keyUri + '>', "?x");
				String res = invokeReconcilitaionService(serviceInput);
				
				if (res == null || res.isEmpty()) {
					logger.debug("No linking output for " + serviceInput);
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
				count++;
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
						"} ORDER BY DESC(?score)";

				String sData = TripleStoreUtil.invokeSparqlQuery(query, TripleStoreUtil.defaultDataRepoUrl, "application/sparql-results+json", null);
				if (sData == null | sData.isEmpty()) {
					logger.debug("Empty response object from query : " + query);
				}
				JSONObject queryRes = new JSONObject(sData);
				
				if (queryRes != null) {
					Table linkingDataTable = row.getNode(linkingHNode.getId()).getNestedTable();
					
					JSONArray bindings = queryRes.getJSONObject("results").getJSONArray("bindings");
					// No bindings present, add empty row
					if (bindings == null || bindings.length() == 0) {
						Row r1 = linkingDataTable.addRow(f);
						r1.setValue(entityColHNode.getId(), CellValue.getEmptyValue(), NodeStatus.original, f);
						r1.setValue(scoreColHNode.getId(), CellValue.getEmptyValue(), NodeStatus.original, f);
						continue;
					}
					
					for (int i=0; i<bindings.length(); i++) {
						JSONObject binding = bindings.getJSONObject(i);
						Row r1 = linkingDataTable.addRow(f);
						String score = binding.getJSONObject("score").getString("value");
						if (score.length() > 5) {
							score = score.substring(0, 4);
						}
						r1.setValue(entityColHNode.getId(), binding.getJSONObject("entity").getString("value"), f);
						r1.setValue(scoreColHNode.getId(), score, f);
						
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
		// Prepare the output container
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		c.add(new InfoUpdate("Linking complete"));
		return c;
	}

//	private void filterTripleMapsAndAuxillaryInformation() {
//		
//	}

	private boolean checkTripleStoreIfMatchAlreadyExists(String keyUri) 
			throws ClientProtocolException, IOException, JSONException {
		// Query the triple store to get a list of matches
		String query = "PREFIX d:<http://entities.restdesc.org/terms#> " +
				"SELECT ?match WHERE " +
				"{ <" + keyUri + "> <" + Uris.KM_LINKING_MATCHES_URI + "> ?x ." +
				"  ?x d:possibleMatch ?match . " +
				"}";
		
		String sData = TripleStoreUtil.invokeSparqlQuery(query, TripleStoreUtil.defaultDataRepoUrl, "application/sparql-results+json", null);
		if (sData == null | sData.isEmpty()) {
			logger.debug("Empty response object from query : " + query);
		}
		JSONObject queryRes = new JSONObject(sData);
		
		if (queryRes != null
				&& queryRes.getJSONObject("results") != null 
				&& queryRes.getJSONObject("results").getJSONArray("bindings") != null
				&& queryRes.getJSONObject("results").getJSONArray("bindings").length() != 0) {
			return true;
		}
		return false;
	}

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
		for (LabeledLink outgoingLink:alignment.getCurrentOutgoingLinksToNode(alignmentNodeId)) {
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
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}
	
	private void fetchRdfPrefixAndNamespaceFromPreferences(Workspace workspace) {
		//get the rdf prefix from the preferences
		JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
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
	
}
