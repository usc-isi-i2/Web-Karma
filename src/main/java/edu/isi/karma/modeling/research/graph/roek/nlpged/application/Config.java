package edu.isi.karma.modeling.research.graph.roek.nlpged.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class Config {

	
	private Properties configFile;
	private InputStream is;

	public Config(String filename) {
		configFile = new Properties();
		try{
			is = new FileInputStream(filename);
			configFile.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	public String getProperty(String property) {
		return configFile.getProperty(property);
	}
}
