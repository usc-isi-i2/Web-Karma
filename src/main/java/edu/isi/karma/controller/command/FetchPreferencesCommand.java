package edu.isi.karma.controller.command;

import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.FetchPreferencesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;

/**
 * Class responsible for fetching saved preferences for the command "commandName"
 */
public class FetchPreferencesCommand extends Command {
	//the command for which we retrieve the preferences
	private String commandName;
	

	protected FetchPreferencesCommand(String id, String commandName){
		super(id);
		this.commandName=commandName;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "FetchPreferences";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		
		System.out.println("FetchPreferences....");
		
		System.out.println("I get 1.....");
		ViewPreferences prefs = vWorkspace.getPreferences();
		JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
		System.out.println("I get 1....."+prefObject1);

		UpdateContainer c = new UpdateContainer();
		c.add(new FetchPreferencesUpdate(vWorkspace, commandName+"Preferences"));
		return c;
		
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
