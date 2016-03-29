package edu.isi.karma.controller.command.worksheet;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class GetHeadersCommand extends WorksheetCommand {
	private String hNodeId;
	private String commandName;
	private static Logger logger = LoggerFactory
			.getLogger(FoldCommand.class);
	protected GetHeadersCommand(String id, String model, String worksheetId, String hNodeId, String commandName) {
		super(id, model, worksheetId);
		this.hNodeId = hNodeId;
		this.commandName = commandName;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCommandName() {
		return GetHeadersCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Headers";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		HTable ht = worksheet.getHeaders();		
		if (hNodeId.compareTo("") != 0) {
			HNode hNode = workspace.getFactory().getHNode(hNodeId);
			HTable parentHT = workspace.getFactory().getHTable(hNode.getHTableId());
			if (commandName.compareTo("GroupBy") == 0 || commandName.compareTo("Fold") == 0 || commandName.compareTo("Glue") == 0)
				ht = hNode.getNestedTable();
			else
				ht = parentHT;
		}
		if (ht == null) {
			return new UpdateContainer();
		}
		final JSONArray array = new JSONArray();
		for (HNode hn : ht.getHNodes()) {
			if (!commandName.equals("Unfold") || (!hn.hasNestedTable() && !hn.getId().equals(hNodeId)) ) {
				JSONObject obj = new JSONObject();
				obj.put("ColumnName", hn.getColumnName());
				obj.put("HNodeId", hn.getId());
				HashMap<String, HashSet<HNode>> appliedCommands = hn.getAppliedCommands();
				JSONArray commands = new JSONArray();
				for(Map.Entry<String, HashSet<HNode>> stringHashSetEntry : appliedCommands.entrySet()) {
					JSONObject com = new JSONObject();
					com.put("CommandName", stringHashSetEntry.getKey());
					HashSet<HNode> cNodes = stringHashSetEntry.getValue();
					JSONArray cols = new JSONArray();
					for(HNode cNode : cNodes) {
						JSONObject col = new JSONObject();
						col.put("HNodeId", cNode.getId());
						col.put("ColumnName", cNode.getColumnName());
						cols.put(col);
					}
					com.put("Columns", cols);
					commands.put(com);
				}
				obj.put("appliedCommands", commands);
				array.put(obj);
			}
		}
		try {
			return new UpdateContainer(new AbstractUpdate() {

				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					pw.print(array.toString());
				}
			});
		} catch (Exception e) {
			logger.error("Error occurred while fetching graphs!", e);
			return new UpdateContainer(new ErrorUpdate("Error occurred while fetching graphs!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}


}
