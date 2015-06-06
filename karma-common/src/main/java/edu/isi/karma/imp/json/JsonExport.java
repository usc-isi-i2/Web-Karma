package edu.isi.karma.imp.json;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class JsonExport {
	private Worksheet worksheet;
	private static final Logger logger = LoggerFactory.getLogger(JsonExport.class);

	public JsonExport(Worksheet worksheet) {
		this.worksheet = worksheet;
	}

	public String publishJSON(String json) {
		String outputFile = worksheet.getTitle() + ".json";

		try {
	        Writer outUTF8=null;
			try {
				outUTF8 = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(ContextParametersRegistry.getInstance().getDefault().getParameterValue(ContextParameter.JSON_PUBLISH_DIR) +outputFile), "UTF8"));
				outUTF8.append(json);
	    		outUTF8.flush();
	    		outUTF8.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("JSON file exported. Location: " + outputFile);
		return outputFile;
	}

}
