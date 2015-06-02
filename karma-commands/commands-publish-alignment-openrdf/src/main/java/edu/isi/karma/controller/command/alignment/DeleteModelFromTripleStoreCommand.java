package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class DeleteModelFromTripleStoreCommand extends Command{
	private String tripleStoreURL;
	private String context;
	private String mappingURI;
	private static Logger logger = LoggerFactory.getLogger(DeleteModelFromTripleStoreCommand.class);

	public DeleteModelFromTripleStoreCommand(String id, String model, String tripleStoreURL, String context, String mappingURI) {
		super(id, model);
		this.tripleStoreURL = tripleStoreURL;
		this.context = context;
		this.mappingURI = mappingURI;
	}
	
	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Delete Model";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		TripleStoreUtil util = new TripleStoreUtil();
		try {
			util.deleteMappingFromTripleStore(tripleStoreURL, context, mappingURI);
		} catch (KarmaException e) {
			return new UpdateContainer(new ErrorUpdate("Error occured while deleting R2RML model!"));
		}
		UpdateContainer uc = new UpdateContainer();
		uc.add(new AbstractUpdate() {
			public void generateJson(String prefix, PrintWriter pw,	
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(), "DeleteModel");
					//.put(JsonKeys.fileUrl.name(), graphUrl);
					pw.println(outputObject.toString());
				} catch (JSONException e) {
					//e.printStackTrace();
					logger.error("Error occured while generating JSON!");
				}
			}
		});
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
