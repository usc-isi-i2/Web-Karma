package edu.isi.karma.mapreduce.function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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

import edu.isi.karma.rdf.CommandLineArgumentParser;

public class CreateSequenceFile {
	Map<String, Writer> writers = new ConcurrentHashMap<>();
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
		List<Future<Boolean>> results = new LinkedList<>();
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
		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, CreateSequenceFile.class.getSimpleName());
		if(cl == null)
		{
			return;
		}
		filePath = (String) cl.getOptionValue("filepath");
		outputPath = filePath;
		useKey = Boolean.parseBoolean((String) cl.getOptionValue("usekey"));
		outputFileName = Boolean.parseBoolean((String) cl.getOptionValue("outputfilename"));
		if (cl.hasOption("outputpath")) {
			outputPath = (String) cl.getOptionValue("outputpath");
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
			} else if(c == '{') {
				tokener.back();
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
	
	private static Options createCommandLineOptions() {
		Options options = new Options();
		options.addOption(new Option("filepath", "filepath", true, "location of the input file directory"));
		options.addOption(new Option("usekey", "usekey", true,"whether use key for sequence file"));
		options.addOption(new Option("outputfilename", "outputfilename",true,  "whether output file name as key"));
		options.addOption(new Option("outputpath", "outputpath", true, "location of output file directory"));
		options.addOption(new Option("help", "help", false, "print this message"));

		return options;
	}
}
