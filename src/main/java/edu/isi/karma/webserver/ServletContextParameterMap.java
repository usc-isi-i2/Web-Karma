package edu.isi.karma.webserver;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextParameterMap {
	private static HashMap<ContextParameter, String> valuesMap = new HashMap<ContextParameter, String>();

	private static Logger logger = LoggerFactory
			.getLogger(ServletContextParameterMap.class);

	public enum ContextParameter {
		PUBLIC_KML_ADDRESS, KML_TRANSFER_SERVICE, WGS84_LAT_PROPERTY, WGS84_LNG_PROPERTY, POINT_POS_PROPERTY, POS_LIST_PROPERTY, POINT_CLASS, LINE_CLASS
	}

	public static void setParameterValue(ContextParameter param, String value) {
		valuesMap.put(param, value);
	}

	public static String getParameterValue(ContextParameter param) {
		if (valuesMap.containsKey(param))
			return valuesMap.get(param);
		else
			logger.error("Parameter value does not exist!");

		return "";
	}
}
