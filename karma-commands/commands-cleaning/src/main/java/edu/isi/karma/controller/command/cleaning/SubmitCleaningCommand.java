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

package edu.isi.karma.controller.command.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.UtilTools;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.worksheet.AddColumnCommand;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.cleaning.RamblerTransformationInputs;
import edu.isi.karma.rep.cleaning.RamblerTransformationOutput;
import edu.isi.karma.rep.cleaning.RamblerValueCollection;
import edu.isi.karma.rep.cleaning.TransformationExample;
import edu.isi.karma.rep.cleaning.ValueCollection;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class SubmitCleaningCommand extends WorksheetSelectionCommand {
	private String hNodeId = "";
	private String newHNodeId = "";
	private String hTableId = "";
	private String columnName;

	private static Logger logger = LoggerFactory
			.getLogger(SubmitCleaningCommand.class);
	private Vector<TransformationExample> examples = new Vector<TransformationExample>();

	public SubmitCleaningCommand(String id, String hNodeId, String worksheetId,
			String Examples, String selectionId) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.examples = GenerateCleaningRulesCommand.parseExample(Examples);

		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Transform Column";
	}

	@Override
	public String getDescription() {
		return columnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	public JSONArray creatNewColumnCommand(String worksheetId, String hTableId,
			String colname) {
		colname = colname.replace("\"", "\\\"");
		String cmdString = String
				.format("[{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"}]",
						"id", this.id, "worksheetId", worksheetId, "hTableId",
						hTableId, "hNodeId", this.hNodeId, "newColumnName",
						colname);
		logger.debug("" + cmdString);
		JSONArray jsonArray = new JSONArray();
		try {
			jsonArray = new JSONArray(cmdString);
		} catch (Exception e) {
			logger.debug("Creating AddColumn Error: "+e.toString());
		}
		return jsonArray;
	}

	public JSONArray createMultiCellCmd(ValueCollection vc, String nHNodeId) {
		JSONArray strData = new JSONArray();
		for (String key : vc.getNodeIDs()) {
			String value = vc.getValue(key);
			JSONObject jsonObject;
			try {
				value = value.replaceAll("\"", "\\\\\"");
				jsonObject = new JSONObject(String.format(
						"{\"rowID\":\"%s\",\"value\":\"%s\"}", key, value));
				strData.put(jsonObject);
			} catch (JSONException e) {
				logger.info(e.toString());
			}
		}
		String cmdString = String
				.format("[{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":\"%s\"},"
						+ "{\"name\":\"%s\",\"type\":\"other\",\"value\":%s}]",
						"id", this.id, "hNodeID", nHNodeId, "worksheetId",
						worksheetId, "rows", strData.toString());
		JSONArray cmdArray = new JSONArray();
		try {
			cmdArray = new JSONArray(cmdString);
		} catch (JSONException e) {
		}
		return cmdArray;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) {
		// create new column command
		String Msg = String.format("submit end, Time,%d, Worksheet,%s",
				System.currentTimeMillis(), worksheetId);
		logger.info(Msg);
		String colnameString = "";
		UpdateContainer c = new UpdateContainer();
		HNodePath selectedPath = null;
		try {
			// obtain transformed results
			HashMap<String, String> rows = new HashMap<String, String>();
			colnameString = obtainTransformedResultsAndFindNewColumnName(
					workspace, rows);
			createAndExecuteNewAddColumnCommand(workspace, colnameString);
			Worksheet wk = workspace.getWorksheet(worksheetId);
			selectedPath = findPathForNewColumn(workspace, colnameString);
			DataPreProcessor dpp = (DataPreProcessor) wk.getDpp();
			if(dpp == null)
			{
				dpp = new DataPreProcessor(rows.values());
			}
			Messager msg = (Messager) wk.getMsg();
			if(msg == null)
			{
				msg = new Messager();
			}
			RamblerTransformationOutput rtf = applyRamblerTransformation(rows,dpp,msg);
			if (rtf.getTransformations().keySet().size() <= 0) {
				c.append(WorksheetUpdateFactory
						.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace)));
				c.add(new InfoUpdate("No Result Submitted"));
				return c;
			}


			ValueCollection rvco = getValueCollectionFromRamblerTranformationOutput(rtf);
			
			findNewHNodeIdAndHNodeAsDerived(workspace, selectedPath);
			// create edit multiple cells command
			createAndExecuteMultiCellCmd(workspace, selectedPath, rvco);
		} catch (Exception e) {
			logger.error("Unable to complete processing of cleaning command", e);
			c.add(new ErrorUpdate(
					"Unable to complete processing of cleaning command"));
			// TODO do we need to clean up?
		}

		if (selectedPath != null) {
			c.append(WorksheetUpdateFactory
					.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace)));
			/** Add the alignment update **/
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(
					workspace));
		}

		c.add(new InfoUpdate("Column transformation complete"));
		return c;
	}

	private HNodePath findPathForNewColumn(Workspace workspace,
			String colnameString) {
		List<HNodePath> columnPaths = workspace.getFactory()
				.getWorksheet(worksheetId).getHeaders().getAllPaths();
		HNodePath selectedPath = null;
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getColumnName().compareTo(colnameString) == 0) {
				selectedPath = path;
				break;
			}
		}

		return selectedPath;
	}

	private void findNewHNodeIdAndHNodeAsDerived(Workspace workspace,
			HNodePath selectedPath) {
		this.newHNodeId = selectedPath.getLeaf().getId();
		// Set the new column as derived from original column
		HNode newHNode = workspace.getFactory().getHNode(newHNodeId);
		if (newHNode != null) {
			newHNode.setAsDerivedFromAnotherColumn(hNodeId);
		}
	}

	private ValueCollection getValueCollectionFromRamblerTranformationOutput(
			RamblerTransformationOutput rtf) {
		Iterator<String> iter = rtf.getTransformations().keySet().iterator();
		Vector<ValueCollection> vvc = new Vector<ValueCollection>();
		String tpid = iter.next();
		ValueCollection rvco = rtf.getTransformedValues(tpid);
		vvc.add(rvco);
		return rvco;
	}

	private RamblerTransformationOutput applyRamblerTransformation(
			HashMap<String, String> rows,DataPreProcessor dpp, Messager msg) {
		RamblerValueCollection vc = new RamblerValueCollection(rows);
		RamblerTransformationInputs inputs = new RamblerTransformationInputs(
				examples, vc,dpp,msg);
		// generate the program
		boolean results = false;
		int iterNum = 0;
		RamblerTransformationOutput rtf = null;
		while (iterNum < 1 && !results) // try to find any rule during 1 times
										// running
		{
			rtf = new RamblerTransformationOutput(inputs);
			if (rtf.getTransformations().keySet().size() > 0) {
				results = true;
			}
			iterNum++;
		}
		return rtf;
	}

	private String obtainTransformedResultsAndFindNewColumnName(
			Workspace workspace, HashMap<String, String> rows) {

		String colnameString = "";
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = workspace.getFactory()
				.getWorksheet(worksheetId).getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				hTableId = path.getLeaf().getHTableId();
				this.columnName = path.getLeaf().getColumnName();
				HTable hTable = path.getLeaf()
						.getHTable(workspace.getFactory());
				colnameString = hTable.getNewColumnName(this.columnName);
				selectedPath = path;

			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		SuperSelection selection = getSuperSelection(workspace);
		workspace.getFactory().getWorksheet(worksheetId).getDataTable()
				.collectNodes(selectedPath, nodes, selection);
		for (Node node : nodes) {
			String id = node.getBelongsToRow().getId();
			String originalVal = node.getValue().asString();
			if (!rows.containsKey(id))
				rows.put(id, originalVal);
		}

		return colnameString;
	}

	private void createAndExecuteMultiCellCmd(Workspace workspace,
			HNodePath selectedPath, ValueCollection rvco) throws JSONException,
			KarmaException, CommandException {
		ExecutionController ctrl = WorkspaceRegistry.getInstance()
				.getExecutionController(workspace.getId());
		JSONArray inputParamArr = this.createMultiCellCmd(rvco, selectedPath
				.getLeaf().getId());
		CommandFactory cf = ctrl.getCommandFactoryMap().get(
				MultipleValueEditColumnCommand.class.getSimpleName());
		JSONInputCommandFactory scf = (JSONInputCommandFactory) cf;

		// TODO handle exceptions intelligently
		Command comm = scf.createCommand(inputParamArr, workspace);
		if (comm != null) {
			// logger.info("Executing command: " +
			// commObject.get(HistoryArguments.commandName.name()));
			workspace.getCommandHistory().doCommand(comm, workspace);
		}
	}

	private void createAndExecuteNewAddColumnCommand(Workspace workspace,
			String colnameString) {
		// add a new column
		JSONArray inputParamArr0 = this.creatNewColumnCommand(worksheetId,
				hTableId, colnameString);
		ExecutionController ctrl = WorkspaceRegistry.getInstance()
				.getExecutionController(workspace.getId());
		CommandFactory cf0 = ctrl.getCommandFactoryMap().get(
				AddColumnCommand.class.getSimpleName());
		JSONInputCommandFactory scf1 = (JSONInputCommandFactory) cf0;
		Command comm1 = null;

		// TODO handle exceptions intelligently
		try {
			comm1 = scf1.createCommand(inputParamArr0, workspace);
		} catch (JSONException e1) {
			logger.error(
					"Error creating new "
							+ AddColumnCommand.class.getSimpleName(), e1);
		} catch (KarmaException e1) {
			logger.error(
					"Error creating new "
							+ AddColumnCommand.class.getSimpleName(), e1);
		}
		if (comm1 != null) {
			try {
				comm1.saveInHistory(false);
				workspace.getCommandHistory().doCommand(comm1, workspace);
			} catch (CommandException e) {
				logger.error(
						"Error executing new "
								+ AddColumnCommand.class.getSimpleName(), e);
			}
		}
	}

	// remove the added column
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		HTable currentTable = workspace.getFactory().getHTable(hTableId);
		// remove the new column
		currentTable.removeHNode(newHNodeId, worksheet);

		UpdateContainer c = (WorksheetUpdateFactory
				.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet)));
		// TODO is it necessary to compute alignment and semantic types for
		// everything?
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}
}
