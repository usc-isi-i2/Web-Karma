package edu.isi.karma.controller.command.worksheet;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;

public class ApplyModelFromURLCommand extends WorksheetCommand{

	private String URL;
	private static Logger logger = LoggerFactory.getLogger(ApplyModelFromURLCommand.class);
	public ApplyModelFromURLCommand(String id, String worksheetId, String URL) {
		super(id, worksheetId);
		this.URL = URL;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return ApplyModelFromURLCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Apply Models";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return URL;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		ApplyHistoryFromR2RMLModelCommandFactory factory = new ApplyHistoryFromR2RMLModelCommandFactory();
		try {
			URL url = new URL(URL);
			File file = new File("tmp.ttl");	
			FileUtils.copyURLToFile(url, file);
			Command cmd = factory.createCommandFromFile(worksheetId, file, workspace);
			UpdateContainer uc = cmd.doIt(workspace);
			workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelUrl, URL);
			file.delete();
			return uc;
		}catch(Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
