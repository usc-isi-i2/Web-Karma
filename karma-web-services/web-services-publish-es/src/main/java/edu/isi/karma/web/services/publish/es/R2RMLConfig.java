package edu.isi.karma.web.services.publish.es;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;

import edu.isi.karma.rdf.GenericRDFGenerator.InputType;

public class R2RMLConfig {

	private InputStream input;
	private InputType contentType;
	private String encoding;
	private int maxNumLines;
	private String columnDelimiter;
	private String textQualifier;
	private int dataStartIndex;
	private int headerStartIndex;
	private int worksheetIndex;
	
	private URL r2rmlUrl;
	
	private URL contextUrl;
	private String contextRoot;
	
	
	public static R2RMLConfig parse(ServletContext context, MultivaluedMap<String, String> formParams) throws MalformedURLException, IOException {
		R2RMLConfig config = new R2RMLConfig();
	
		if (formParams != null && formParams.containsKey(FormParameters.DATA_URL)
				&& formParams.getFirst(FormParameters.DATA_URL).trim() != "")
			config.input = (new URL(formParams.getFirst(FormParameters.DATA_URL)).openStream());
		else if(formParams != null && formParams.containsKey(FormParameters.RAW_DATA)
				&& formParams.getFirst(FormParameters.RAW_DATA).trim() != "")
			config.input =(IOUtils.toInputStream(formParams.getFirst(FormParameters.RAW_DATA)));
		
		
		if(formParams != null && formParams.containsKey(FormParameters.R2RML_URL))
			config.r2rmlUrl = (new URL(formParams.getFirst(FormParameters.R2RML_URL)));
		else
			config.r2rmlUrl = (new URL(context.getInitParameter(FormParameters.R2RML_URL)));
		
		if(formParams != null && formParams.containsKey(FormParameters.CONTEXT_URL))
			config.contextUrl = (new URL(formParams.getFirst(FormParameters.CONTEXT_URL)));
		else
			config.contextUrl = (new URL(context.getInitParameter(FormParameters.CONTEXT_URL)));
		
		String dataType = InputType.JSON.toString();
		if(formParams != null && formParams.containsKey(FormParameters.CONTENT_TYPE))
			dataType = formParams.getFirst(FormParameters.CONTENT_TYPE);
		config.contentType = (InputType.valueOf(dataType));
		
		if (formParams != null && formParams.containsKey(FormParameters.MAX_NUM_LINES))
			config.maxNumLines = (Integer.parseInt(formParams.getFirst(FormParameters.MAX_NUM_LINES)));
		else
			config.maxNumLines = (-1);
		
		if (formParams != null && formParams.containsKey(FormParameters.ENCODING))
			config.encoding = (formParams.getFirst(FormParameters.ENCODING));
		else
			config.encoding = "utf-8";
		
		if (formParams != null && formParams.containsKey(FormParameters.COLUMN_DELIMITER))
			config.columnDelimiter = (formParams.getFirst(FormParameters.COLUMN_DELIMITER));
		else
			config.columnDelimiter = ",";
		
		if (formParams != null && formParams.containsKey(FormParameters.TEXT_QUALIFIER))
			config.textQualifier = (formParams.getFirst(FormParameters.TEXT_QUALIFIER));
		else
			config.textQualifier = "\"";
		
		if (formParams != null && formParams.containsKey(FormParameters.DATA_START_INDEX))
			config.dataStartIndex = (Integer.parseInt(formParams.getFirst(FormParameters.DATA_START_INDEX)));
		else
			config.dataStartIndex = 2;
		
		if (formParams != null && formParams.containsKey(FormParameters.HEADER_START_INDEX))
			config.headerStartIndex = (Integer.parseInt(formParams.getFirst(FormParameters.HEADER_START_INDEX)));
		else
			config.headerStartIndex = 1;
		
		if (formParams != null && formParams.containsKey(FormParameters.WORKSHEET_INDEX))
			config.worksheetIndex = (Integer.parseInt(formParams.getFirst(FormParameters.WORKSHEET_INDEX)));
		else
			config.worksheetIndex = 0;
		
		if (formParams != null && formParams.containsKey(FormParameters.CONTEXT_ROOT)) {
			config.contextRoot = formParams.getFirst(FormParameters.CONTEXT_ROOT);
		} else {
			config.contextRoot = context.getInitParameter(FormParameters.CONTEXT_ROOT);
		}
			
			
		
		return config;
	}


	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream is) {
		this.input = is;
	}
	
	public InputType getContentType() {
		return contentType;
	}


	public String getEncoding() {
		return encoding;
	}


	public int getMaxNumLines() {
		return maxNumLines;
	}


	public String getColumnDelimiter() {
		return columnDelimiter;
	}


	public String getTextQualifier() {
		return textQualifier;
	}


	public int getDataStartIndex() {
		return dataStartIndex;
	}


	public int getHeaderStartIndex() {
		return headerStartIndex;
	}


	public int getWorksheetIndex() {
		return worksheetIndex;
	}


	public URL getR2rmlUrl() {
		return r2rmlUrl;
	}


	public URL getContextUrl() {
		return contextUrl;
	}


	public String getContextRoot() {
		return contextRoot;
	}
	
	
}
