package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class UpdateModelConfigurationUICommandFactory
		extends
			JSONInputCommandFactory {

	enum Arguments {
		show_super_class
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model,
			Workspace workspace) throws JSONException, KarmaException {
		
		boolean show_super_class = false;
		if(HistoryJsonUtil.valueExits(Arguments.show_super_class.name(), inputJson))
			show_super_class = Boolean.parseBoolean(HistoryJsonUtil.getStringValue(Arguments.show_super_class.name(), inputJson));

		UpdateModelConfigurationUICommand cmd = new UpdateModelConfigurationUICommand(getNewId(workspace), model, show_super_class);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return UpdateModelConfigurationUICommand.class;
	}

}


