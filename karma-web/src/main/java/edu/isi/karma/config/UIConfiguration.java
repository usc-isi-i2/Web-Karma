package edu.isi.karma.config;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UIConfiguration {

	private static UIConfiguration instance = null;
	private boolean googleEarthEnabled = true;
	private static Logger logger = LoggerFactory.getLogger(UIConfiguration.class);
	
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
        URL propFile = UIConfiguration.class.getClassLoader().getResource("ui.properties");
        try {
			uiProperties.load(propFile.openStream());
			
			googleEarthEnabled = Boolean.parseBoolean(uiProperties.getProperty("google.earth.enabled"));
			
		} catch (IOException e) {
			logger.error("Could not load ui.properties, using defaults", e);
		}
	}
	
	public boolean isGoogleEarthEnabled() {
		return googleEarthEnabled;
	}
}
