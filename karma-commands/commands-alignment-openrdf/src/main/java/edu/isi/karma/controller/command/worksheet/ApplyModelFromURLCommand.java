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

	private String modelURL;
	private String modelContext;
	private String modelRepository;
	private String baseUrl = "http://localhost:8080/R2RMLMapping/local/repository/";
	private boolean override;
	private static Logger logger = LoggerFactory.getLogger(ApplyModelFromURLCommand.class);
	public ApplyModelFromURLCommand(String id, String model, String worksheetId, String modelURL, String modelContext, String modelRepository, String baseURL, boolean override) {
		super(id, model, worksheetId);
		this.modelURL = modelURL;
		this.modelContext = modelContext;
		this.modelRepository = modelRepository;
		this.baseUrl = baseURL;
		this.override = override;
	}

	@Override
	public String getCommandName() {
		return ApplyModelFromURLCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Apply Models";
	}

	@Override
	public String getDescription() {
		return modelURL;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		ApplyHistoryFromR2RMLModelCommandFactory factory = new ApplyHistoryFromR2RMLModelCommandFactory();
		try {

			String context = (modelContext != null && !modelContext.isEmpty()? (modelContext + "/") : "");
			URL url = new URL(baseUrl + context + modelURL + "?modelRepository=" + modelRepository);

			File file = new File("tmp.ttl");	
			FileUtils.copyURLToFile(url, file);
			Command cmd = factory.createCommandFromFile(model, worksheetId, file, workspace, override);
			UpdateContainer uc = cmd.doIt(workspace);
			workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelUrl, modelURL);
			workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelContext, modelContext);
			workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelRepository, modelRepository);
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
