package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class UpdateModelConfigurationCommandFactory
		extends
			JSONInputCommandFactory {

	enum Arguments {
		r2rml_export_superclass
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model,
			Workspace workspace) throws JSONException, KarmaException {
		
		boolean r2rml_export_superclass = false;
		if(HistoryJsonUtil.valueExits(Arguments.r2rml_export_superclass.name(), inputJson))
			r2rml_export_superclass = Boolean.parseBoolean(HistoryJsonUtil.getStringValue(Arguments.r2rml_export_superclass.name(), inputJson));

		UpdateModelConfigurationCommand cmd = new UpdateModelConfigurationCommand(getNewId(workspace), model, r2rml_export_superclass);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return UpdateModelConfigurationCommand.class;
	}

}


