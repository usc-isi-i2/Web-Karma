package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.isi.karma.kr2rml.ContextGenerator;
import edu.isi.karma.webserver.KarmaException;

public class GenerateContextFromModel {

	public static void main(String[] args) throws JSONException, KarmaException, IOException {
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, GenerateContextFromModel.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
		String fileName = (String) cl.getOptionValue("modelpath");
		String output = (String) cl.getOptionValue("outputfile");
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
        JSONObject top = new ContextGenerator(model, true).generateContext();
        PrintWriter pw = new PrintWriter(output);
        pw.println(top.toString(4));
        pw.close();
	}
	
	private static Options createCommandLineOptions() {

		Options options = new Options();
				

		options.addOption(new Option("modelpath", "modelpath", true, "location of modelfile"));
		options.addOption(new Option("outputfile", "outputfile", true, "output file name (optional)"));
		options.addOption(new Option("help", "help", false, "print this message"));

		return options;
	}

}
