package edu.isi.karma.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public class UIConfiguration {

	private static UIConfiguration instance = null;
	private boolean googleEarthEnabled = true;
	private static Logger logger = LoggerFactory.getLogger(UIConfiguration.class);
	
	private static final String newLine = System.getProperty("line.separator").toString();
	private static String defaultUIProperties = "google.earth.enabled=true" + newLine;
	
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
					mgr.register(new UserConfigMetadata());
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
			
		} catch (IOException e) {
			logger.error("Could not load ui.properties, using defaults", e);
		}
	}
	
	public boolean isGoogleEarthEnabled() {
		return googleEarthEnabled;
	}
}
