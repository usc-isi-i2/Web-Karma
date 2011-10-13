package edu.isi.karma.controller.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ImportJSONFileCommand extends Command {
	File jsonFile;
	
	private static Logger logger = LoggerFactory.getLogger(ImportJSONFileCommand.class);

	public ImportJSONFileCommand(String id, File file) {
		super(id);
		this.jsonFile = file;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Import JSON File";
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			return jsonFile.getName() + " imported";
		} else {
			return "";
		}
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Workspace ws = vWorkspace.getWorkspace();
		UpdateContainer c = new UpdateContainer();
		try {
			FileReader reader = new FileReader(jsonFile);
			Object json = Util.createJson(reader);
			JsonImport imp = new JsonImport(json, jsonFile.getName(), ws);
			
			Worksheet wsht = imp.generateWorksheet();
			vWorkspace.addAllWorksheets();
			
			c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
			VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
			c.add(new WorksheetHeadersUpdate(vw));
			c.add(new WorksheetDataUpdate(vw));
			c.add(new WorksheetHierarchicalHeadersUpdate(vw));
			c.add(new WorksheetHierarchicalDataUpdate(vw));
			
		} catch (FileNotFoundException e) {
			logger.error("File Not Found", e);
		} catch (JSONException e) {
			logger.error("JSON Exception!", e);
		}
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}
}
