package edu.isi.karma.controller.command;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public interface JSONInputCommandFactory {
	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace) throws JSONException, KarmaException;
}
