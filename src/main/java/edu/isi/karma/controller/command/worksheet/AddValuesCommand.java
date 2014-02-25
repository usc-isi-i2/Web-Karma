package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AddColumnUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.json.JsonImportValues;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;
import edu.isi.karma.webserver.KarmaException;

public class AddValuesCommand extends WorksheetCommand{


	private final String hNodeId;
	//add column to this table
	private String hTableId;

	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;

	private static Logger logger = LoggerFactory
			.getLogger(AddValuesCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected AddValuesCommand(String id,String worksheetId, 
			String hTableId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return AddValuesCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add Values Command";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		System.out.println("here");
		Worksheet worksheet = workspace.getWorksheet(
				worksheetId);
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		String addValues = null;	
		HNode ndid = null;
		try{
			if (para instanceof JSONArray) {
				//System.out.println("JSONArray:" + para);
				addValues = CommandInputJSONUtil.getStringValue("AddValues", (JSONArray)para);
				Object t = JSONUtil.createJson(addValues);
				if (t instanceof JSONArray) {
					JSONArray a = (JSONArray) t;
					ndid = addColumn(workspace, worksheet, "", a);
				}
			}
			UpdateContainer c =  new UpdateContainer(new AddColumnUpdate(newHNodeId, worksheetId));		
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
			if (ndid == null) {
				System.err.println("error: ndid");
			}
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace, ndid.getHNodePath(workspace.getFactory())));
			return c;
		} catch (Exception e) {
			logger.error("Error in AddColumnCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		HTable currentTable = workspace.getFactory().getHTable(hTableId);
		//remove the new column
		currentTable.removeHNode(newHNodeId, worksheet);

		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
	}


	public String getNewHNodeId() {
		return newHNodeId;
	}

	private HNode addColumn(Workspace workspace, Worksheet worksheet, String newColumnName, JSONArray array) throws KarmaException {
		if(hTableId==null || hTableId.isEmpty()){
			//get table id based on the hNodeId
			if(hNodeId==null)
				throw new KarmaException("TableId and NodeId are empty. Can't add column.");
			hTableId = workspace.getFactory().getHNode(hNodeId).getHTableId();
		}
		HTable hTable = workspace.getFactory().getHTable(hTableId);
		if(hTable == null)
		{
			logger.error("No HTable for id "+ hTableId);
			throw new KarmaException("No HTable for id "+ hTableId );
		}
		
		if (null != hTable.getHNodeFromColumnName(newColumnName)) {
			logger.error("Add column failed to create " + newColumnName
					+ " because it already exists!");
		}   
		//add new column to this table
		//add column after the column with hNodeId
		HNode ndid = hTable.addNewHNodeAfter(hNodeId, workspace.getFactory(), newColumnName, worksheet,true);
		if(ndid == null)
		{
			logger.error("Unable to add new HNode!");
			throw new KarmaException("Unable to add new HNode!");
		}

		newHNodeId = ndid.getId();
		//add as first column in the table if hNodeId is null
		//HNode ndid = currentTable.addNewHNodeAfter(null, vWorkspace.getRepFactory(), newColumnName, worksheet,true);
		if (array != null) {
			populateRowsWithDefaultValues(worksheet, workspace.getFactory(), array);
		}
		//save the new hNodeId for undo

		return ndid;
	}

	private void populateRowsWithDefaultValues(Worksheet worksheet, RepFactory factory, JSONArray array) {
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(newHNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>(Math.max(1000, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(selectedPath, nodes);	
		for (Node node : nodes) {
			for (int i = 0; i < array.length(); i++) {
				if (array.get(i) instanceof JSONObject) {
					JSONObject obj = (JSONObject)array.get(i);
					if (node.getBelongsToRow().getId().compareTo(obj.getString("rowId")) == 0) {
						Object t = obj.get("values");
						if (t instanceof String) {
							String value = (String)t;
							addValues(node, value, factory);
						}
						else if (t instanceof JSONArray) {
							addJSONValues((JSONArray)t, worksheet, factory, node.getBelongsToRow());
						}
					}
				}
			}
			
		}
	}

	private void addJSONValues(JSONArray array, Worksheet worksheet, RepFactory factory, Row row) {
		HNode ndid = worksheet.getHeaders().getHNode(newHNodeId);
		HTable nestedHTable = ndid.getNestedTable();
		if (nestedHTable == null)
			nestedHTable = ndid.addNestedTable("Table for test",
					worksheet, factory);
		Table nestedTable = row.getNode(newHNodeId).getNestedTable();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = (JSONObject)array.get(i);
			System.out.println("JObject: " + obj);
			String name = obj.getString("name");
			String value = obj.getString("value");
			HNode h = nestedHTable.getHNodeFromColumnName(name);
			Row r = null;
			if (nestedTable.getNumRows() == 0) 
				r = nestedTable.addRow(factory);
			else
				r = nestedTable.getRows(0, nestedTable.getNumRows()).get(0);
			if ( h == null)
				h = nestedHTable.addHNode(name, worksheet, factory);
			r.getNode(h.getId()).setValue(value, NodeStatus.original, factory);
		}
	}
	private void addValues(Node node, String value, RepFactory factory) {
		node.setValue(value, NodeStatus.original, factory);
	}

}
