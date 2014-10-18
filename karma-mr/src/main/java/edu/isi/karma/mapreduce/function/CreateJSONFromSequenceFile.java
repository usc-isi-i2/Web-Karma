package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
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
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class CreateJSONFromSequenceFile {
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
			if (status.getLen() > 0) {
				String outputFileName = outputPath + File.separator + fileName + ".json";
				createJSONFromSequenceFileFrom(status.getPath(), hdfs.create(new Path(outputFileName)));
			}
		}
	}

	public static void createJSONFromSequenceFileFrom(Path input, FSDataOutputStream fsDataOutputStream) throws IOException {
		Path inputPath = input;
		Configuration conf = new Configuration();
		PrintWriter fw = new PrintWriter(fsDataOutputStream);
		SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(inputPath));
		Text key = new Text();
		Text val = new Text();
		fw.write("[\n");
		boolean writtenyet = false;
		while(reader.next(key, val))
		{
			if(!writtenyet)
			{
				writtenyet=true;
			}
			else
			{
				fw.write(",\n");
			}
			fw.write(val.toString());
		}
		fw.write("\n]\n");
		fw.close();
		reader.close();
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
