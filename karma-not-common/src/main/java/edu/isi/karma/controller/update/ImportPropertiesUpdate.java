package edu.isi.karma.controller.update;

import java.io.File;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.view.VWorkspace;

public class ImportPropertiesUpdate extends AbstractUpdate {
	private String commandId;
	private String encoding;
	private File jsonFile;
	private int maxNumLines;
	
	public enum JsonKeys {
		commandId, fileName
	}
	
	private static Logger logger = LoggerFactory
			.getLogger(ImportPropertiesUpdate.class.getSimpleName());

	public ImportPropertiesUpdate(File jsonFile, String encoding, int maxNumLines, String id) {
		this.jsonFile = jsonFile;
		this.encoding = encoding;
		this.maxNumLines = maxNumLines;
		this.commandId = id;
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		logger.info("Got encoding: " + encoding);
		
		try {
			if(encoding == null) {
				encoding = EncodingDetector.detect(jsonFile);
			}

			JSONStringer jsonStr = new JSONStringer();
			JSONWriter writer = jsonStr.object()
					.key(JsonKeys.commandId.name()).value(commandId)
					.key(GenericJsonKeys.updateType.name()).value("ImportPropertiesUpdate")
					.key(JsonKeys.fileName.name()).value(jsonFile.getName())
					.key("encoding").value(encoding)
					.key("maxNumLines").value(maxNumLines)
					;
			
			
			writer.endObject();
			pw.println(jsonStr.toString());
			
	
		} catch (JSONException e) {
			logger.error("Error occured while writing to JSON", e);
		}
	}

}
