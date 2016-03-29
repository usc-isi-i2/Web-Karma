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


//TODO create a registry for this
public class KR2RMLConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(KR2RMLConfiguration.class);
	private static Properties properties;
	private static final String newLine = System.getProperty("line.separator");
	private static String defaultKR2RMLProperties = "template.terms.no.minimum.for.blank.nodes=false"+ newLine;
	
	// WK-226 Adds the ability to generate blank nodes with out satisfying any column terms
	private Boolean noMinimumNumberOfSatisifiedTerms = null;
	private ServletContextParameterMap contextParameters;
	
	public KR2RMLConfiguration(ServletContextParameterMap contextParameters)
	{
		this.contextParameters = contextParameters;
	}
	public void load(ServletContextParameterMap contextParameters) {
		try {
			properties = loadParams(contextParameters);

			if (properties.getProperty("template.terms.no.minimum.for.blank.nodes") != null)
				setNoMinimumNumberOfSatisifiedTerms(Boolean.parseBoolean(properties
						.getProperty("template.terms.no.minimum.for.blank.nodes")));

		} catch (IOException e) {
			logger.error("Error occured while reading config file ...");
			System.exit(1);
		}
	}

	private Properties loadParams(ServletContextParameterMap contextParameters) throws IOException {
		Properties prop = new Properties();

		File file = new File(
				contextParameters
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

		FileInputStream fis = new FileInputStream(file);
		try {
			prop.load(fis);
			logger.info("Done loading kr2rml.properties");
		} finally {
			fis.close();
		}

		return prop;
	}

	public Boolean getNoMinimumNumberOfSatisifiedTerms() {
		if(noMinimumNumberOfSatisifiedTerms == null)
		{
			load(contextParameters);
		}
		return noMinimumNumberOfSatisifiedTerms;
	}

	public void setNoMinimumNumberOfSatisifiedTerms(
			Boolean noMinimumNumberOfSatisifiedTerms) {
		this.noMinimumNumberOfSatisifiedTerms = noMinimumNumberOfSatisifiedTerms;
	}

}
