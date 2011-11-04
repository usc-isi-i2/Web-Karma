package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class ErrorUpdate extends AbstractUpdate {

	String updateType;
	String errorMessage;

	public enum JsonKeys {
		Error
	}
	
	private static Logger logger = LoggerFactory.getLogger(ErrorUpdate.class);
	
	public ErrorUpdate(String updateType, String errorMessage) {
		super();
		this.updateType = updateType;
		this.errorMessage = errorMessage;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), updateType);
			obj.put(JsonKeys.Error.name(), errorMessage);
			pw.println(obj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON for ErrorUpdate", e);
		}
	}

}
