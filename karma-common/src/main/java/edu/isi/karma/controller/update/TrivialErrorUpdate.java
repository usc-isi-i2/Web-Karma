package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class TrivialErrorUpdate extends AbstractUpdate {

	String errorMessage;

	private enum JsonKeys {
		TrivialError
	}
	
	private enum JsonValues {
		KarmaTrivialError
	}
	
	private static Logger logger = LoggerFactory.getLogger(ErrorUpdate.class);
	
	public TrivialErrorUpdate(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), JsonValues.KarmaTrivialError.name());
			obj.put(JsonKeys.TrivialError.name(), errorMessage);
			pw.println(obj.toString());
		} catch (JSONException e) {
			logger.error("Error generating JSON for ErrorUpdate", e);
		}
	}

}
