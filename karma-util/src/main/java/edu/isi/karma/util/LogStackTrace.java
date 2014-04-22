package edu.isi.karma.util;

import org.slf4j.Logger;

public class LogStackTrace {

	public LogStackTrace(Exception e, Logger logger)
	{
		logger.debug(e.getMessage());
		for(int i=0; i<e.getStackTrace().length; i++)
			logger.error(e.getStackTrace()[i].toString());
	}
}
