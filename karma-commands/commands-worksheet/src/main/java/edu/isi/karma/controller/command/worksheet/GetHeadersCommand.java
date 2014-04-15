package edu.isi.karma.controller.command.worksheet;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.AllWorksheetHeadersUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class GetHeadersCommand extends WorksheetCommand {
	private String hNodeId;
	private static Logger logger = LoggerFactory
			.getLogger(FoldCommand.class);
	protected GetHeadersCommand(String id, String worksheetId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
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
		HTable ht = getHTable(worksheet.getHeaders(), hNodeId);
		final JSONArray array = new JSONArray();
		for (HNode hn : ht.getHNodes()) {
			JSONObject obj = new JSONObject();
			obj.put("ColumnName", hn.getColumnName());
			obj.put("HNodeId", hn.getId());
			array.put(obj);
		}
		System.out.println(hNodeId);
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
		// TODO Auto-generated method stub
		return null;
	}
	
	private HTable getHTable(HTable ht, String HNodeId) {
		for (HNode hn : ht.getHNodes()) {
			if (hn.getId().compareTo(HNodeId) == 0)
				return ht;
			if (hn.hasNestedTable()) {
				HTable tmp = getHTable(hn.getNestedTable(), HNodeId);
				if (tmp != null)
					return tmp;
			}		
		}
		return null;
	}

}
