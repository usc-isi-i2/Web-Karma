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
package edu.isi.karma.controller.command.publish;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

@Deprecated
public class PublishRDFCellCommand extends WorksheetCommand {
	
	//private final String nodeId;
	//private String rdfSourcePrefix;
	//private String rdfSourceNamespace;
	// rdf for this cell
	private StringWriter outRdf = new StringWriter();
	//private PrintWriter pw = new PrintWriter(outRdf);

	public enum JsonKeys {
		updateType, cellRdf, worksheetId
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishRDFCellCommand.class);

	protected PublishRDFCellCommand(String id, String model, String worksheetId,
			String nodeId, String rdfSourcePrefix, String rdfSourceNamespace) {
		super(id, model, worksheetId);
		//this.nodeId = nodeId;
		//this.rdfSourcePrefix = rdfSourcePrefix;
		//this.rdfSourceNamespace = rdfSourceNamespace;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish RDF Cell";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}
	
	public boolean isDeprecated()
	{
		return true;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		if(isDeprecated())
		{
			return new UpdateContainer(new ErrorUpdate("PublishRDFCellCommand is currently not supported"));
		}
		//Worksheet worksheet = workspace.getWorksheet(worksheetId);

		// Get the alignment for this worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
						worksheetId));
		
		if (alignment == null || alignment.isEmpty()) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(
					new ErrorUpdate("Worksheet not modeled!"));
		}

		try {
			// Generate the KR2RML data structures for the RDF generation
			/*final ErrorReport errorReport = new ErrorReport();
			KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(
					workspace, worksheet,
					alignment, worksheet.getSemanticTypes(), rdfSourcePrefix, rdfSourceNamespace, 
					false, errorReport);
			KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
			KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet, 
					workspace.getFactory(), workspace.getOntologyManager(),
					pw, mapping, errorReport, false);
			
			// Create empty data structures
			Set<String> existingTopRowTriples = new HashSet<String>();
			Set<String> predicatesCovered = new HashSet<String>();
			Map<String, ReportMessage> predicatesFailed = new HashMap<String, ReportMessage>();
			Set<String> predicatesSuccessful = new HashSet<String>();
			
			Node node = workspace.getFactory().getNode(nodeId);
			rdfGen.generateTriplesForCell(node, existingTopRowTriples, node.getHNodeId(), 
				predicatesCovered, predicatesFailed, predicatesSuccessful);*/
			
			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					
					try {
						outputObject.put(JsonKeys.updateType.name(),
								"PublishCellRDFUpdate");
						String rdfCellEscapeString = StringEscapeUtils
								.escapeHtml(outRdf.toString());
						outputObject.put(JsonKeys.cellRdf.name(),
								rdfCellEscapeString.replaceAll("\\n", "<br />"));
						outputObject.put(JsonKeys.worksheetId.name(),
								worksheetId);
						pw.println(outputObject.toString(4));
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
