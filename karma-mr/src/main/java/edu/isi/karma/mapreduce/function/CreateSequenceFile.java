package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateSequenceFile {

	static boolean useKey = true;
	static boolean outputFileName = false;
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
		useKey = Boolean.parseBoolean((String) cl.getValue("--usekey"));
		outputFileName = Boolean.parseBoolean((String) cl.getValue("--outputfilename"));
		if (cl.hasOption("--outputpath")) {
			outputPath = (String) cl.getValue("--outputpath");
		}
		FileSystem hdfs = FileSystem.get(new Configuration());
		RemoteIterator<LocatedFileStatus> itr = hdfs.listFiles(new Path(filePath), true);
		while (itr.hasNext()) {
			LocatedFileStatus status = itr.next();
			String fileName = status.getPath().getName();
			if (fileName.substring(fileName.lastIndexOf(".") + 1).contains("json")) {
				createSequenceFileFromJSON(hdfs.open(status.getPath()), fileName);
			}
		}
	}

	public static void createSequenceFileFromJSON(InputStream stream, String fileName) throws IOException {
		JSONTokener tokener = new JSONTokener(new InputStreamReader(stream, "UTF-8"));
		String outputFileName = outputPath + File.separator +fileName.substring(0, fileName.lastIndexOf(".")) + ".seq";
		Path outputPath = new Path(outputFileName);
		SequenceFile.Writer writer = null;
		if(useKey)
		{
			writer = SequenceFile.createWriter(new Configuration(),Writer.keyClass(Text.class),
			Writer.valueClass(Text.class), Writer.file(outputPath),Writer.compression(CompressionType.NONE));
		}
		else
		{
			writer = 	SequenceFile.createWriter(new Configuration(),Writer.keyClass(BytesWritable.class),
					Writer.valueClass(Text.class), Writer.file(outputPath),Writer.compression(CompressionType.NONE));
		}
		addValuesToSequenceFile(tokener, writer, fileName);
		writer.close();
	}
	
	public static void addValuesToSequenceFile(JSONTokener tokener, SequenceFile.Writer writer, String fileName) throws JSONException, IOException {
		char c = tokener.nextClean();
		if (c == '[') {
			while (true) {
				Object o = tokener.nextValue();
				if (o instanceof JSONObject) {
					JSONObject obj = (JSONObject) o;
					if(useKey)
					{
						if(outputFileName)
						{
							writer.append(new Text(fileName), new Text(obj.toString()));
						}
						else
						{
							writer.append(new Text(obj.getString("@id")), new Text(obj.toString()));
						}
					}
					else
					{
						writer.append(new BytesWritable(), new Text(obj.toString()));
					}
						
				}
				char tmp = tokener.nextClean();
				if (tmp == ']')
					break;
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
				.withOption(buildOption("usekey", "whether use key for sequence file", "usekey", obuilder, abuilder))
				.withOption(buildOption("outputfilename", "whether output file name as key", "outputfilename", obuilder, abuilder))
				.withOption(buildOption("outputpath", "location of output file directory", "outputpath", obuilder, abuilder))
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
