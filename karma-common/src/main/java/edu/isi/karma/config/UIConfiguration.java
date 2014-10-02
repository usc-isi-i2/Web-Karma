package edu.isi.karma.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public class UIConfiguration {

	private static UIConfiguration instance = null;
	private boolean googleEarthEnabled = true;
	private int maxLoadedClasses=-1;
	private int maxLoadedProperties=-1;
	private boolean d3ChartsEnabled = true;
	private boolean forceModelLayoutEnabled = true;
	
	private static Logger logger = LoggerFactory.getLogger(UIConfiguration.class);
	
	private static final String newLine = System.getProperty("line.separator").toString();
	
	private static String propGoogleEarthEnabled = "google.earth.enabled=true";
	private static String propMaxLoadedClasses = "max.loaded.classes=-1";
	private static String propMaxLoadedProperties = "max.loaded.properties=-1";
	private static String propD3ChartsEnabled = "d3.display.charts=true";
	private static String propModelForceLayout = "model.layout.force=true";
	
	private static String defaultUIProperties = propGoogleEarthEnabled + newLine
											  + propMaxLoadedClasses + newLine
											  + propMaxLoadedProperties + newLine
											  + propD3ChartsEnabled + newLine
											  + propModelForceLayout + newLine
			;
	
	public static UIConfiguration Instance() {
		if(instance == null)
			instance = new UIConfiguration();
		return instance;
	}
	
	private UIConfiguration() {
		this.loadConfig();
	}
	
	public void loadConfig() {
		Properties uiProperties = new Properties();
		
        try {
        	String userConfigDir = ServletContextParameterMap.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) ;
        	logger.info("UICOnfiguration:" + userConfigDir);
        	if(userConfigDir == null || userConfigDir.length() == 0) {
				try {
					KarmaMetadataManager mgr = new KarmaMetadataManager();
					mgr.register(new UserConfigMetadata(), new UpdateContainer());
				} catch (KarmaException e) {
					logger.error("Could not register with KarmaMetadataManager", e);
				}
        		
        	}
        	logger.info("Load File:" + ServletContextParameterMap.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/ui.properties");
        	File file = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY) + "/ui.properties");
    		if(!file.exists()) {
    			file.createNewFile();
    			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    	        BufferedWriter bw = new BufferedWriter(fw);
    	        PrintWriter outWriter = new PrintWriter(bw);
    	        outWriter.println(defaultUIProperties);
    	        outWriter.close();
    		}
    			
    		uiProperties.load(new FileInputStream(file));
			
			googleEarthEnabled = Boolean.parseBoolean(uiProperties.getProperty("google.earth.enabled"));
			String sMax = uiProperties.getProperty("max.loaded.classes");
			if(sMax != null)
				maxLoadedClasses = Integer.parseInt(sMax);
			else {
				//need to add this property to the end
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				out.println(propMaxLoadedClasses);
				out.close();
			}
			
			sMax = uiProperties.getProperty("max.loaded.properties");
			if(sMax != null)
				maxLoadedProperties = Integer.parseInt(sMax);
			else {
				//need to add this property to the end
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				out.println(propMaxLoadedProperties);
				out.close();
			}
			
			String sD3 = uiProperties.getProperty("d3.display.charts");
			if(sD3 != null)
				d3ChartsEnabled = Boolean.parseBoolean(sD3);
			else {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				out.println(propD3ChartsEnabled);
				out.close();
			}
			
			String modelLayout = uiProperties.getProperty("model.layout.force");
			if(modelLayout != null)
				forceModelLayoutEnabled = Boolean.parseBoolean(modelLayout);
			else {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				out.println(propModelForceLayout);
				out.close();
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
}
