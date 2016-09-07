package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class UpdateUIConfigurationCommandFactory
		extends
			JSONInputCommandFactory {

	enum Arguments {
		show_rdfs_label_first,
		show_rdfs_id_first
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model,
			Workspace workspace) throws JSONException, KarmaException {
		
		boolean show_rdfs_label_first = false;
		if(HistoryJsonUtil.valueExits(Arguments.show_rdfs_label_first.name(), inputJson))
			show_rdfs_label_first = Boolean.parseBoolean(HistoryJsonUtil.getStringValue(Arguments.show_rdfs_label_first.name(), inputJson));
		boolean show_rdfs_id_first = false;
		if(HistoryJsonUtil.valueExits(Arguments.show_rdfs_id_first.name(), inputJson))
			show_rdfs_id_first = Boolean.parseBoolean(HistoryJsonUtil.getStringValue(Arguments.show_rdfs_id_first.name(), inputJson));
		
		UpdateUIConfigurationCommand cmd = new UpdateUIConfigurationCommand(getNewId(workspace), model, show_rdfs_label_first, show_rdfs_id_first);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return UpdateUIConfigurationCommand.class;
	}

}


