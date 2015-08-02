package edu.isi.karma.controller.command;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.SavedModelURLs;
import edu.isi.karma.view.VWorkspace;

public class GetR2RMLModelURLsCommand extends Command {
	private static Logger logger = LoggerFactory.getLogger(GetR2RMLModelURLsCommand.class);

	
	protected GetR2RMLModelURLsCommand(String id, String model) {
		super(id, model);
	}

	@Override
	public String getCommandName() {
		return GetR2RMLModelURLsCommand.class.getName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Get R2RML Model URLs";
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
		UpdateContainer c = new UpdateContainer();
		c.add(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				
				try {
					JSONObject outputObject = (new SavedModelURLs()).getSavedModels(vWorkspace.getWorkspace().getContextId());
					JSONArray models = outputObject.getJSONArray("models");
					JSONArray revModels = new JSONArray();
					for(int i=models.length()-1; i>=0; i--)
						revModels.put(models.get(i));
					outputObject.put("models", revModels);
					pw.println(outputObject.toString());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Error occured while generating JSON!");
				}

			}

		});
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
