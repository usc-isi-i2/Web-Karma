package edu.isi.karma.util;

import org.json.JSONArray;
import org.json.JSONObject;

import org.json.JSONException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KarmaStats {

	/* Instance Variables*/
	private String inputFile;
	private String outputFile;
	boolean isPretty;
	
	public KarmaStats(CommandLine cl)
	{
		isPretty = false;
	}
	
	public static void main(String[] args) {
		
		
		Options options = createCommandLineOptions();
		CommandLine cl = parseCommandLine(args, options);
		if(cl==null)
		{
			System.out.println("Error Parsing Command Line Arguments");
			return;
		}
		
		try {
			
			KarmaStats stats = new KarmaStats(cl);
			if(!stats.parseCommandLineOptions(cl)) {
				System.out.println("Parse ERROR. Please use \"java -cp JAR_NAME edu.isi.karma.util.KarmaStats --inputfile INPUT_FILE  --outputfile OUTPUT_FILE --pretty(optional)\" ");
				return;
			}
			karmaStats(stats.inputFile, stats.outputFile,stats.isPretty);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public static void karmaStats(String inputFile, String outputFile,boolean isPretty) {
		try {
			FileReader filereader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(filereader);
			String tmpString = null;
			StringBuffer buf = new StringBuffer();
			String[] fileSplit = inputFile.split("/");

			String[] fname = fileSplit[fileSplit.length - 1].split("\\.");
			StringBuffer nameBuf = new StringBuffer();
			/*
			 * If model name has "." in the name.
			 * */

				for(int j=0;j<fname.length-1;j++)
				{
					nameBuf.append(fname[j]);
				}

			String modelName = nameBuf.toString();

			final String matchDoublequote = "\\\"";
			final String newDoublequote = "\"";
			final String matchSlash = "\\\\";
			final String newSlash = "\\";
			final String pyTransformCommandName = "SubmitPythonTransformationCommand";
			final String setSemanticCommandName = "SetSemanticTypeCommand";
			final String setPropertyCommandName = "SetMetaPropertyCommand";
			final String addLinkCommandName = "AddLinkCommand";
			final String changeLinkCommandName = "ChangeInternalNodeLinksCommand";
			final String unassignSemeticCommandName = "UnassignSemanticTypeCommand";
			final String selectionCommandName = "OperateSelectionCommand";
			final String glueCommandName = "GlueCommand";
			final String unfoldCommandName = "UnfoldCommand";

			boolean isJSON = false;
			int pyTransformCount = 0;
			int SemanticTypeCount = 0;
			int classCount = 0;
			int linkCount = 0; // Except sementic type links
			int filterCount = 0;
			int glueCount = 0;
			int unFoldCount = 0;

			while ((tmpString = bufferedReader.readLine()) != null) {
				if (isJSON) {
					if (tmpString.contains("]\"\"\"")) {
						buf.append(']');
						break;
					}
					buf.append(tmpString);
				} else {
					if (tmpString.contains("hasWorksheetHistory")) {
						buf.append('[');
						isJSON = true;
					}
				}

			}

			if (isJSON) {
				classCount = countClass(bufferedReader);

				String bufString = buf.toString();
				String removedQuote = bufString.replace(matchDoublequote,
						newDoublequote);
				String removedSlash = removedQuote
						.replace(matchSlash, newSlash);

				JSONArray commands = new JSONArray(removedSlash);

				/*
				 * Compare all commands and classify according to names
				 */
				for (int i = 0; i < commands.length(); i++) {
					JSONObject command = (JSONObject) commands.get(i);
					String commandName = command.getString("commandName");

					if (commandName.equals(pyTransformCommandName)) {
						pyTransformCount++;
					} else if (commandName.equals(setSemanticCommandName)) {
						SemanticTypeCount++;
					} else if (commandName.equals(setPropertyCommandName)) {
						SemanticTypeCount++;
					} else if (commandName.equals(addLinkCommandName)) {
						linkCount++;
					} else if (commandName.equals(glueCommandName)) {
						glueCount++;
					} else if (commandName.equals(selectionCommandName)) {
						filterCount++;
					} else if (commandName.equals(unassignSemeticCommandName)) {
						SemanticTypeCount--;
					} else if(commandName.equals(changeLinkCommandName)) {
						linkCount++;
					} else if(commandName.equals(unfoldCommandName)) {
						unFoldCount++;
					}
				}
			}
			FileWriter out = new FileWriter(outputFile);
			BufferedWriter bufferedWriter = new BufferedWriter(out);

			StringBuffer writeBuffer = new StringBuffer();
			JSONObject output = new JSONObject();
			JSONObject modelStat = new JSONObject();
			modelStat.put("modelName", modelName);
			output.put("pyTransformations", pyTransformCount);
			output.put("semanticTypes", SemanticTypeCount);
			output.put("class", classCount);
			output.put("links", linkCount);
			output.put("filters", filterCount);
			output.put("Unfold", unFoldCount);
			output.put("Glue", glueCount);
			modelStat.put("modelStatistics", output);
			if(isPretty) {
				writeBuffer.append(modelStat.toString(4));
			}
			else {
				writeBuffer.append(modelStat.toString(0));
			}
			bufferedWriter.write(writeBuffer.toString());

			bufferedReader.close();
			bufferedWriter.close();
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("Failed to Parse JSON");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in reading/writing File");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method counts number of classes based on rr-class count in TTL file
	 */
	public static int countClass(BufferedReader reader) {
		int classCount = 0;
		try {
			String tmpString = null;
			while ((tmpString = reader.readLine()) != null) {
				if (tmpString.contains("rr:class")) {
					classCount++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classCount;
	}
	
	private static CommandLine parseCommandLine(String args[], Options options)
	{
		CommandLineParser parser = new BasicParser();
		CommandLine cl = null;
		try {
			/**
			 * PARSE THE COMMAND LINE ARGUMENTS *
			 */
	        cl = parser.parse(options, args);
	        if (cl == null || cl.getOptions().length == 0) {
	            return null;
	        }
		
	} catch (Exception e) {
		return cl;
	}
		return cl;
	}
	
	private static Options createCommandLineOptions() {

		Options options = new Options();
	
		options.addOption(new Option("inputfile", "inputfile", true, "Input TTL File"));
		options.addOption(new Option("outputfile", "outputfile", true, "location of the output file with name"));
		options.addOption(new Option("pretty", "pretty", false, "JSON or JSONLines selection"));
		
		return options;
	}
	
	private boolean parseCommandLineOptions(CommandLine cl)
	{

		inputFile = (String) cl.getOptionValue("inputfile");
		if(inputFile==null)
		{
			System.out.println("Please provide input File");
			return false;
		}
		
		outputFile = (String) cl.getOptionValue("outputfile","KarmaStats.json");

		if(cl.hasOption("pretty"))
		{
			this.isPretty = true;
		}

		return true;
	}

}
