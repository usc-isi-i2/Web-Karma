package edu.isi.karma.rdf;

import java.io.File;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

import com.uwyn.jhighlight.tools.FileUtils;

import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.webserver.KarmaException;

public class LoadRDFToTripleStore {
	
	public static void main(String args[]) {
		TripleStoreUtil util = new TripleStoreUtil();
		Group options = createCommandLineOptions();
        Parser parser = new Parser();
        parser.setGroup(options);
        parser.setGroup(options);
        HelpFormatter hf = new HelpFormatter();
        parser.setHelpFormatter(hf);
        parser.setHelpTrigger("--help");
        CommandLine cl = parser.parseAndHelp(args);
        if (cl == null || cl.getOptions().size() == 0 || cl.hasOption("--help")) {
            hf.setGroup(options);
            hf.print();
            return;
        }
        String filepath = (String) cl.getValue("--filepath");
        String tripleStoreUrl = (String) cl.getValue("--modelurl");
        String context = (String) cl.getValue("--context");
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
	
	private static Group createCommandLineOptions() {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();

		Group options =
				gbuilder
				.withName("options")
				.withOption(buildOption("filepath", "location of the input file directory", "filepath", obuilder, abuilder))
				.withOption(buildOption("modelurl", "location of the model", "modelurl", obuilder, abuilder))
				.withOption(buildOption("context", "the context uri", "context", obuilder, abuilder))
				.withOption(obuilder
						.withLongName("help")
						.withDescription("print this message")
						.create())
						.create();

		return options;
	}

	public static Option buildOption(String shortName, String description, String argumentName,
			DefaultOptionBuilder obuilder, ArgumentBuilder abuilder) {
		return obuilder
				.withLongName(shortName)
				.withDescription(description)
				.withArgument(
						abuilder
						.withName(argumentName)
						.withMinimum(1)
						.withMaximum(1)
						.create())
						.create();
	}
}
