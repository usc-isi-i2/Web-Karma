package edu.isi.karma.kr2rml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class KR2RMLConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(KR2RMLConfiguration.class);
	private static Properties properties;
	private static final String newLine = System.getProperty("line.separator").toString();
	private static String defaultKR2RMLProperties = "template.terms.no.minimum.for.blank.nodes=false"+ newLine;
	
	// WK-226 Adds the ability to generate blank nodes with out satisfying any column terms
	private static Boolean noMinimumNumberOfSatisifiedTerms = null;
	
	public static void load() {
		try {
			properties = loadParams();

			if (properties.getProperty("template.terms.no.minimum.for.blank.nodes") != null)
				setNoMinimumNumberOfSatisifiedTerms(Boolean.parseBoolean(properties
						.getProperty("template.terms.no.minimum.for.blank.nodes")));

		} catch (IOException e) {
			logger.error("Error occured while reading config file ...");
			System.exit(1);
		}
	}

	private static Properties loadParams() throws IOException {
		Properties prop = new Properties();

		File file = new File(
				ServletContextParameterMap
						.getParameterValue(ContextParameter.USER_CONFIG_DIRECTORY)
						+ "/kr2ml.properties");
		logger.info("Load kr2rml.properties: " + file.getAbsolutePath() + ":"
				+ file.exists());
		if (!file.exists()) {
			file.createNewFile();
			OutputStreamWriter fw = new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			logger.info(defaultKR2RMLProperties);
			bw.write(defaultKR2RMLProperties);
			bw.close();
			logger.info("Wrote default properties to kr2rml.properties");
		}

		prop.load(new FileInputStream(file));
		logger.info("Done loading kr2rml.properties");

		return prop;
	}

	public static Boolean getNoMinimumNumberOfSatisifiedTerms() {
		if(noMinimumNumberOfSatisifiedTerms == null)
		{
			load();
		}
		return noMinimumNumberOfSatisifiedTerms;
	}

	public static void setNoMinimumNumberOfSatisifiedTerms(
			Boolean noMinimumNumberOfSatisifiedTerms) {
		KR2RMLConfiguration.noMinimumNumberOfSatisifiedTerms = noMinimumNumberOfSatisifiedTerms;
	}

}
