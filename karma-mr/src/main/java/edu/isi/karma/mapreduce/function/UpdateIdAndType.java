package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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

import edu.isi.karma.rdf.CommandLineArgumentParser;

public class UpdateIdAndType {
	static String filePath = null;
	static String outputPath = null;
	
	public static void main(String[] args) throws IOException {
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, UpdateIdAndType.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
		filePath = (String) cl.getOptionValue("--filepath");
		outputPath = filePath;
		if (cl.hasOption("--outputpath")) {
			outputPath = (String) cl.getOptionValue("--outputpath");
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
	
	@SuppressWarnings("unchecked")
	private static void processJSONObject(JSONObject obj) {
		if (obj.has("@id")) {
			obj.put("uri", obj.getString("@id"));
			obj.remove("@id");
		}
		if (obj.has("@type")) {
			obj.put("a", obj.get("@type"));
			obj.remove("@type");
		}
		for (Iterator<String> keysIterator = obj.keys(); keysIterator.hasNext(); ) {
			String key = keysIterator.next();
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
	
	private static Options createCommandLineOptions() {

		Options options = new Options();
		options.addOption(new Option("filepath", "filepath", true, "location of the input file directory"));
		options.addOption(new Option("outputpath", "outputpath", true, "location of output file directory"));
		options.addOption(new Option("help", "help", false, "print this message"));
		return options;
	}


}
