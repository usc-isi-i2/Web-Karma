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

public class ClearTripleStoreCommand extends Command{
	private String tripleStoreUrl;
	private String graphUrl;
	public ClearTripleStoreCommand(String id, String model, String tripleStoreUrl, String context) {
		super(id, model);
		this.tripleStoreUrl = tripleStoreUrl;
		if (context != null && !context.trim().isEmpty()) {
			context = context.trim();
			context.replaceAll(">", "");
			context.replaceAll("<", "");
		}
		this.graphUrl = "<" + context + ">";
	}

	
	public enum JsonKeys {
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
		return "Clear Triple Store";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Clear " + graphUrl;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	private static Logger logger = LoggerFactory.getLogger(ClearTripleStoreCommand.class);

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		boolean result = TripleStoreUtil.clearContexts(tripleStoreUrl, graphUrl);
		if (!result) {
			return new UpdateContainer(new ErrorUpdate("Error occured while clearing R2RML model!"));
		}
		UpdateContainer uc = new UpdateContainer();
		uc.add(new AbstractUpdate() {
			public void generateJson(String prefix, PrintWriter pw,	
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(), "ClearGraph");
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
