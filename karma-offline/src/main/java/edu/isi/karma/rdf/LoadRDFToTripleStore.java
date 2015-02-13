package edu.isi.karma.rdf;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uwyn.jhighlight.tools.FileUtils;

import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.webserver.KarmaException;

public class LoadRDFToTripleStore {
	private static Logger logger = LoggerFactory.getLogger(LoadRDFToTripleStore.class);
	
	public static void main(String args[]) {
		TripleStoreUtil util = new TripleStoreUtil();
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, LoadRDFToTripleStore.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
        String filepath = (String) cl.getOptionValue("filepath");
        String tripleStoreUrl = (String) cl.getOptionValue("triplestoreurl");
        String context = (String) cl.getOptionValue("context");
        if (filepath == null || tripleStoreUrl == null || context == null)
        	return;
		File file = new File(filepath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				System.out.println(f.getName());
				if (FileUtils.getExtension(f.getName()) != null)
					try {
						util.saveToStoreFromFile(f.getAbsolutePath(), tripleStoreUrl, context, false, null);
					} catch (KarmaException e) {
						System.err.println(e.getMessage());
					}
			}
		}
		else {
			if (FileUtils.getExtension(file.getName()) != null)
				try {
					util.saveToStoreFromFile(file.getAbsolutePath(), tripleStoreUrl, context, false, null);
				} catch (KarmaException e) {
					System.err.println(e.getMessage());
				}
		}
		
	}
	
	private static Options createCommandLineOptions() {

		Options options = new Options();
				
		options.addOption(new Option("filepath", "filepath", true, "location of the input file directory"));
		options.addOption(new Option("triplestoreurl", "triplestoreurl", true, "location of the triplestore"));
		options.addOption(new Option("context", "context", true, "the context uri"));
		options.addOption(new Option("help", "help", false, "print this message"));

		return options;
	}
}
