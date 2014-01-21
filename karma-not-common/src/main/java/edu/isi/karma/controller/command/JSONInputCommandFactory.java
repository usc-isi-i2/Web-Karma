package edu.isi.karma.controller.command;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public interface JSONInputCommandFactory {
	public Command createCommand(JSONArray inputJson, Workspace workspace) throws JSONException, KarmaException;
}
