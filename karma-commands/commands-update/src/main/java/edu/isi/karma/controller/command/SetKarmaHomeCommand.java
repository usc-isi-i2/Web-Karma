package edu.isi.karma.controller.command;

import java.io.File;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import org.json.JSONObject;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class SetKarmaHomeCommand extends Command {

	private String directory;
	enum PreferenceKeys {
		directory
	}
	protected SetKarmaHomeCommand(String id, String model, String directory) {
		super(id, model);
		if(!directory.endsWith(File.separator))
			this.directory = directory + File.separator;
		else
			this.directory = directory;
	}
	
	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
	return "Set Karma Home";
	}

	@Override
	public String getDescription() {
		return this.directory;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
	
		File dir = new File(directory);
		if(dir.exists()) {
			final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
			contextParameters.setParameterValue(ContextParameter.USER_DIRECTORY_PATH, directory);
			Preferences preferences = Preferences.userRoot().node("WebKarma");
			preferences.put("KARMA_USER_HOME", directory);
			
			
			uc.add(new InfoUpdate("Successfully changed Karma Home Directory"));
			uc.add(new AbstractUpdate() {
	
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					obj.put(GenericJsonKeys.updateType.name(), "ReloadPageUpdate");
					pw.println(obj.toString());
				}
				
			});
		} else {
			uc.add(new ErrorUpdate("Karma home could not be changed. The specified directory does not exist"));
		}
		return uc;
	}

	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
