package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class UpdateIdAndType {
	static String filePath = null;
	static String outputPath = null;
	
	public static void main(String[] args) throws IOException {
		Group options = createCommandLineOptions();
        Parser parser = new Parser();
        parser.setGroup(options);
        HelpFormatter hf = new HelpFormatter();
        parser.setHelpFormatter(hf);
        parser.setHelpTrigger("--help");
        CommandLine cl = parser.parseAndHelp(args);
        if (cl == null || cl.getOptions().size() == 0 || cl.hasOption("--help") || !cl.hasOption("--filepath")) {
            hf.setGroup(options);
            hf.print();
            return;
        }
		filePath = (String) cl.getValue("--filepath");
		outputPath = filePath;
		if (cl.hasOption("--outputpath")) {
			outputPath = (String) cl.getValue("--outputpath");
		}
		FileSystem hdfs = FileSystem.get(new Configuration());
		RemoteIterator<LocatedFileStatus> itr = hdfs.listFiles(new Path(filePath), true);
		while (itr.hasNext()) {
			LocatedFileStatus status = itr.next();
			String fileName = status.getPath().getName();
			Path filePath = status.getPath();
			if (status.getLen() > 0) {
				String outputFileName = outputPath + File.separator + fileName + ".json";
				createSequenceFileFromJSON(hdfs.open(status.getPath()), hdfs.create(new Path(outputFileName)));
				hdfs.delete(status.getPath(), false);
				hdfs.rename(new Path(outputFileName), filePath);
			}
		}
	}

	public static void createSequenceFileFromJSON(FSDataInputStream fsDataInputStream, FSDataOutputStream fsDataOutputStream) throws IOException {
		JSONTokener tokener = new JSONTokener(new InputStreamReader(fsDataInputStream, "UTF-8"));
		PrintWriter pw = new PrintWriter(fsDataOutputStream);
		pw.println("[");
		tokener.nextClean();
		char tmp = '[';
		while(tmp != ']') {
			JSONObject obj = (JSONObject) tokener.nextValue();
			processJSONObject(obj);
			pw.println(obj.toString(4));
			tmp = tokener.nextClean();
			if (tmp != ']') {
				pw.println(",");
			}
		}
		pw.println("]");
		pw.close();		
	}
	
	private static void processJSONObject(JSONObject obj) {
		if (obj.has("@id")) {
			obj.put("uri", obj.getString("@id"));
			obj.remove("@id");
		}
		if (obj.has("@type")) {
			obj.put("a", obj.get("@type"));
			obj.remove("@type");
		}
		for (Object key : obj.keySet()) {
			Object o = obj.get((String) key);
			if (o instanceof JSONObject) {
				processJSONObject((JSONObject) o);
			}
			if (o instanceof JSONArray) {
				JSONArray array = (JSONArray) o;
				for (int i = 0; i < array.length(); i++) {
					Object tmp = array.get(i);
					if (tmp instanceof JSONObject)
						processJSONObject((JSONObject) tmp);
				}
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
				.withOption(buildOption("outputpath", "location of output file directory", "outputpath", obuilder, abuilder))
				.withOption(obuilder
						.withLongName("help")
						.withDescription("print this message")
						.create())
						.create();

		return options;
	}

	private static Option buildOption(String shortName, String description, String argumentName,
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
