package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.isi.karma.rdf.CommandLineArgumentParser;

public class CreateJSONFromSequenceFile {
	static String filePath = null;
	static String outputPath = null;
	static String outputtype = "0"; //by default output json array
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, CreateJSONFromSequenceFile.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
		filePath = (String) cl.getOptionValue("filepath");
		outputPath = filePath;
		if (cl.hasOption("outputpath")) {
			outputPath = (String) cl.getOptionValue("outputpath");
		}
		
		if(cl.hasOption("outputtype")){
			outputtype = (String) cl.getOptionValue("outputtype");
		}
		FileSystem hdfs = FileSystem.get(new Configuration());
		RemoteIterator<LocatedFileStatus> itr = hdfs.listFiles(new Path(filePath), true);
		while (itr.hasNext()) {
			LocatedFileStatus status = itr.next();
			String fileName = status.getPath().getName();
			if (status.getLen() > 0) {
				String outputFileName = outputPath + File.separator + fileName;// + ".json";
				List<FSDataOutputStream> streams = new LinkedList<>();
				if(cl.hasOption("splits"))
				{
					Integer splits = Integer.parseInt((String) cl.getOptionValue("splits"));
					for(int i = 0; i < splits; i ++)
					{
						streams.add(hdfs.create(new Path(outputFileName+"."+i + ".json")));
					}
				}
				else
				{
					streams.add(hdfs.create(new Path(outputFileName+ ".json")));
				}
				createJSONFromSequenceFileFrom(status.getPath(), streams);
			}
		}
	}

	public static void createJSONFromSequenceFileFrom(Path input, List<FSDataOutputStream> streams) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Path inputPath = input;
		Configuration conf = new Configuration();
		List<PrintWriter> fws = new LinkedList<>();
		for(FSDataOutputStream stream : streams)
		{
			PrintWriter fw = new PrintWriter(stream);
			fws.add(fw);
			if (outputtype.equals("0")){
				fw.write("[\n");
			}
		}
		SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(inputPath));
		Writable key = (Writable) Class.forName(reader.getKeyClass().getCanonicalName()).newInstance();
		Text val = new Text();
		
		int writtenTo = 0;
		Iterator<PrintWriter> pwIterator = fws.iterator();
		while(reader.next(key, val))
		{
			if(!pwIterator.hasNext())
			{
				pwIterator = fws.iterator();
			}
			PrintWriter fw = pwIterator.next();
			if(writtenTo < fws.size())
			{
				writtenTo++;
			}
			else
			{
				if (outputtype.equals("0")){
					fw.write(",\n");
				}
			}
			fw.write(val.toString());
			fw.write("\n");
		}
		for(PrintWriter fw : fws)
		{
			if(outputtype.equals("0")){
				fw.write("\n]\n");
			}
			
			fw.close();
		}
		reader.close();
	}
	
	private static Options createCommandLineOptions() {
		Options options = new Options();
				options.addOption(new Option("filepath", "filepath", true, "location of the input file directory"));
				options.addOption(new Option("outputpath", "outputpath", true, "location of output file directory"));
				options.addOption(new Option("splits", "splits", true, "number of splits per file"));
				options.addOption(new Option("outputtype", "outputtype", true, "0 for JSON Array or 1 for json lines"));
				options.addOption(new Option("help", "help", false, "print this message"));

		return options;
	}

}
