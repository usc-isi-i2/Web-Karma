package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	ConcurrentHashMap<String, SequenceFile.Writer> writers = new ConcurrentHashMap<String, SequenceFile.Writer>();
	boolean useKey = true;
	boolean outputFileName = false;
	String filePath = null;
	String outputPath = null;
	public static void main(String[] args) throws IOException {
		CreateSequenceFile csf = new CreateSequenceFile();
		csf.setup(args);
		csf.execute();
	}

	public void execute() throws IOException, FileNotFoundException {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		FileSystem hdfs = FileSystem.get(new Configuration());
		RemoteIterator<LocatedFileStatus> itr = hdfs.listFiles(new Path(filePath), true);
		List<Future<Boolean>> results = new LinkedList<Future<Boolean>>();
		while (itr.hasNext()) {
			LocatedFileStatus status = itr.next();
			String fileName = status.getPath().getName();
			if (fileName.substring(fileName.lastIndexOf(".") + 1).contains("json")) {
				results.add(executor.submit(getNewJSONProcessor(hdfs, status, fileName)));
			}
		}
		
		for(Future<Boolean> result : results)
		{
			try {
				result.get(5, TimeUnit.MINUTES);
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		executor.shutdown();
		for(SequenceFile.Writer writer : writers.values())
		{
			writer.close();
		}
	}

	protected JSONFileProcessor getNewJSONProcessor(FileSystem hdfs,
			LocatedFileStatus status, String fileName) throws IOException {
		return new JSONFileProcessor(hdfs.open(status.getPath()), fileName);
	}

	public void setup(String[] args) {
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
	}

	protected class JSONFileProcessor implements Callable<Boolean>
	{
		protected InputStream stream;
		protected String fileName;
		protected String defaultOutputFileName;
		public JSONFileProcessor(InputStream stream, String fileName)
		{
			this.stream = stream;
			this.fileName = fileName;
			defaultOutputFileName = outputPath + File.separator +fileName.substring(0, fileName.lastIndexOf(".")) + ".seq";
		}
		@Override
		public Boolean call() throws Exception {
			
			JSONTokener tokener = new JSONTokener(new InputStreamReader(stream, "UTF-8"));

			addValuesToSequenceFile(tokener);
			return true;
		}
		

		public SequenceFile.Writer createSequenceFile(Path outputPath)
				throws IOException {
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
			return writer;
		}
		
		public void addValuesToSequenceFile(JSONTokener tokener) throws JSONException, IOException {
			char c = tokener.nextClean();
			if (c == '[') {
				while (!tokener.end()) {
					Object o = tokener.nextValue();
					if (o instanceof JSONObject) {
						JSONObject obj = (JSONObject) o;
						SequenceFile.Writer writer = getWriter(obj);
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
		
		public SequenceFile.Writer getWriter(JSONObject obj) throws IOException
		{
			if(!writers.containsKey(defaultOutputFileName))
			{
				Path outputPath = new Path(defaultOutputFileName);
				synchronized(writers)
				{
					writers.put(defaultOutputFileName, createSequenceFile(outputPath));
				}
			}
			return writers.get(defaultOutputFileName);
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
