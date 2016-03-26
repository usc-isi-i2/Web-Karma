package edu.isi.karma.rdf;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineArgumentParser {

	private static Logger logger = LoggerFactory.getLogger(CommandLineArgumentParser.class);

	private CommandLineArgumentParser() {
	}

	public static CommandLine parse(String args[], Options options, String commandName)
	{
		CommandLineParser parser = new BasicParser();
		 CommandLine cl = null;
		try {
			/**
			 * PARSE THE COMMAND LINE ARGUMENTS *
			 */
	        cl = parser.parse(options, args);
	        if (cl == null || cl.getOptions().length == 0 || cl.hasOption("help")) {
	        	HelpFormatter hf = new HelpFormatter();
	        	hf.printHelp(commandName, options);
	            return null;
	        }
		
	} catch (Exception e) {
		logger.error("Error occured while parsing arguments!", e);
		return cl;
	}
		return cl;
	}
}
