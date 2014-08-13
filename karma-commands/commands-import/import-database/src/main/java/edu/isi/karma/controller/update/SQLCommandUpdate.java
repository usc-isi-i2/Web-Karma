package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class SQLCommandUpdate extends AbstractUpdate {

	private String commandId;
	private static Logger logger = LoggerFactory.getLogger(SQLCommandUpdate.class);
	
	public enum JsonKeys {
		commandId
	}

	public SQLCommandUpdate(String commandId) {
		this.commandId = commandId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject responseObj = new JSONObject();
		try {
			responseObj.put(JsonKeys.commandId.name(), commandId);
			responseObj.put(GenericJsonKeys.updateType.name(), "ImportSQLCommandUpdate");
			pw.print(responseObj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON!", e);
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof SQLCommandUpdate) {
			SQLCommandUpdate t = (SQLCommandUpdate)o;
			return t.commandId.equals(commandId);
		}
		return false;
	}
}
