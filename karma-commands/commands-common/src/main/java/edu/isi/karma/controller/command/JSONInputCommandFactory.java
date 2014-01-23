package edu.isi.karma.controller.command;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;

public abstract class JSONInputCommandFactory extends CommandFactory {
	@Override
	public abstract Command createCommand(JSONArray inputJson, Workspace workspace) throws JSONException, KarmaException;
}
