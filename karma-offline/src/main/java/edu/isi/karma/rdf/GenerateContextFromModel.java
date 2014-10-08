package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.isi.karma.kr2rml.ContextGenerator;
import edu.isi.karma.webserver.KarmaException;

public class GenerateContextFromModel {

	public static void main(String[] args) throws JSONException, KarmaException, IOException {
		Group options = createCommandLineOptions();
        Parser parser = new Parser();
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
		String fileName = (String) cl.getValue("--modelpath");
		String output = (String) cl.getValue("--outputfile");
		if (fileName == null) {
			System.out.println("No model file specified!");
			return;
		}
		if (output == null) {
			output = fileName + ".json";
		}
		File file = new File(fileName);
		Model model = ModelFactory.createDefaultModel();
        InputStream s = new FileInputStream(file);
        model.read(s, null, "TURTLE");
        JSONObject top = new ContextGenerator(model).generateContext();
        PrintWriter pw = new PrintWriter(output);
        pw.println(top.toString(4));
        pw.close();
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
	
	private static Group createCommandLineOptions() {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();

		Group options =
				gbuilder
				.withName("options")
				.withOption(buildOption("modelpath", "location of modelfile", "modelpath", obuilder, abuilder))
				.withOption(buildOption("outputfile", "output file name (optional)", "outputfile", obuilder, abuilder))
				.withOption(obuilder
						.withLongName("help")
						.withDescription("print this message")
						.create())
						.create();

		return options;
	}

}
