package edu.isi.karma.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public class UIConfiguration {

	private boolean googleEarthEnabled = true;
	private int maxLoadedClasses=-1;
	private int maxLoadedProperties=-1;
	private boolean d3ChartsEnabled = true;
	private boolean forceModelLayoutEnabled = true;
	private boolean showRDFSLabelWithIDFirst = false;
	private boolean showRDFSLabelWithLabelFirst = true;
	
	private static Logger logger = LoggerFactory.getLogger(UIConfiguration.class);
	
	private static final String newLine = System.getProperty("line.separator");
	
	private static String propGoogleEarthEnabled = "google.earth.enabled=true";
	private static String propMaxLoadedClasses = "max.loaded.classes=-1";
	private static String propMaxLoadedProperties = "max.loaded.properties=-1";
	private static String propD3ChartsEnabled = "d3.display.charts=true";
	private static String propModelForceLayout = "model.layout.force=true";
	private static String propShowRDFSLabelWithLabelFirst = "show.rdfs.label.label=true";
	private static String propShowRDFSLabelWithIDFirst = "show.rdfs.label.id=false";
	
	private static String defaultUIProperties = propGoogleEarthEnabled + newLine
											  + propMaxLoadedClasses + newLine
											  + propMaxLoadedProperties + newLine
											  + propD3ChartsEnabled + newLine
											  + propModelForceLayout + newLine
											  + propShowRDFSLabelWithIDFirst + newLine
											  + propShowRDFSLabelWithLabelFirst + newLine
			;
	
	private String contextId;
	private ServletContextParameterMap contextParameters;
	private Properties uiProperties;
	
	public UIConfiguration(String contextId) {
		this.contextId = contextId;
		this.loadConfig();
	}
	
	public void loadConfig() {
		
        try {
        	contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
        	this.uiProperties = new Properties();
        	String userConfigDir = contextParameters.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) ;
        	logger.info("UICOnfiguration:" + userConfigDir);
        	if(userConfigDir == null || userConfigDir.length() == 0) {
    			try {
    				
    				//TODO this should never be necessary. why did this happen?
    				KarmaMetadataManager mgr = new KarmaMetadataManager(contextParameters);
    				mgr.register(new UserConfigMetadata(contextParameters), new UpdateContainer());
    			} catch (KarmaException e) {
    				logger.error("Could not register with KarmaMetadataManager", e);
    			}
        		
        	}
        	logger.info("Load File:" + contextParameters.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/ui.properties");
        	File file = new File(contextParameters.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/ui.properties");
    		if(!file.exists()) {
    			file.createNewFile();
    			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    	        BufferedWriter bw = new BufferedWriter(fw);
    	        PrintWriter outWriter = new PrintWriter(bw);
    	        outWriter.println(defaultUIProperties);
    	        outWriter.close();
    		}
    			
    		FileInputStream fis = new FileInputStream(file);
    		try {
    			uiProperties.load(fis);
    		} finally {
    			fis.close();
    		}
			
			googleEarthEnabled = Boolean.parseBoolean(uiProperties.getProperty("google.earth.enabled"));
			String sMax = uiProperties.getProperty("max.loaded.classes");
			if(sMax != null)
				maxLoadedClasses = Integer.parseInt(sMax);
			else {
				addProperty(propMaxLoadedClasses);
			}
			
			sMax = uiProperties.getProperty("max.loaded.properties");
			if(sMax != null)
				maxLoadedProperties = Integer.parseInt(sMax);
			else {
				addProperty(propMaxLoadedProperties);
			}
			
			String sD3 = uiProperties.getProperty("d3.display.charts");
			if(sD3 != null)
				d3ChartsEnabled = Boolean.parseBoolean(sD3);
			else {
				addProperty(propD3ChartsEnabled);
			}
			
			String modelLayout = uiProperties.getProperty("model.layout.force");
			if(modelLayout != null)
				forceModelLayoutEnabled = Boolean.parseBoolean(modelLayout);
			else {
				addProperty(propModelForceLayout);
			}
			
			String showRDFSLabelWithIDFirst = uiProperties.getProperty("show.rdfs.label.id");
			if(showRDFSLabelWithIDFirst != null)
				this.showRDFSLabelWithIDFirst = Boolean.parseBoolean(showRDFSLabelWithIDFirst);
			else {
				addProperty(propShowRDFSLabelWithIDFirst);
			}
			
			String showRDFSLabelWithLabelFirst = uiProperties.getProperty("show.rdfs.label.label");
			if(showRDFSLabelWithLabelFirst != null)
				this.showRDFSLabelWithLabelFirst = Boolean.parseBoolean(showRDFSLabelWithLabelFirst);
			else {
				addProperty(propShowRDFSLabelWithLabelFirst);
			}
		} catch (IOException e) {
			logger.error("Could not load ui.properties, using defaults", e);
		}
	}
	
	public boolean isGoogleEarthEnabled() {
		return googleEarthEnabled;
	}
	
	public int getMaxClassesToLoad() {
		return maxLoadedClasses;
	}
	
	public int getMaxPropertiesToLoad() {
		return maxLoadedProperties;
	}
	
	public boolean isD3ChartsEnabled() {
		return d3ChartsEnabled;
	}
	
	public boolean isForceModelLayoutEnabled() {
		return forceModelLayoutEnabled;
	}
	
	public boolean showRDFSLabelWithIDFirst() {
		return this.showRDFSLabelWithIDFirst;
	}
	
	public boolean showRDFSLabelWithLabelFirst() {
		return this.showRDFSLabelWithLabelFirst;
	}
	
	public void updateShowRDFSLabelWithLabelFirst(boolean value) throws IOException {
		this.showRDFSLabelWithLabelFirst = value;
		this.updateProperty("show.rdfs.label.label", Boolean.toString(value));
	}
	
	public void updateShowRDFSLabelWithIDFirst(boolean value) throws IOException {
		this.showRDFSLabelWithIDFirst = value;
		this.updateProperty("show.rdfs.label.id", Boolean.toString(value));
	}
	
	private void addProperty(String propLine) throws IOException {
		File file = new File(contextParameters.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/ui.properties");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		out.println(propLine);
		out.close();
		String[] keyValue = propLine.split("=");
		this.uiProperties.put(keyValue[0], keyValue[1]);
	}
	
	private void updateProperty(String key, String value) throws IOException {
		this.uiProperties.put(key, value);
		File file = new File(contextParameters.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/ui.properties");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
		logger.info("Write Properties:" + this.uiProperties.toString());
		this.uiProperties.store(out, null);
		out.close();
	}
}
