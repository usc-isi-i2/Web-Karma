package edu.isi.karma.mapreduce.tripleparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Neo4jCSVGenerator {

//	private final static Logger logger = LoggerFactory.getLogger(Neo4jCSVGenerator.class);

//	private static void parseTriples(Configuration conf, Path input, Path output) throws IOException {
//
//		FileSystem fs = FileSystem.get(conf);
//
//		try {
//
//			Job job = Job.getInstance(conf, "parse triples");
//
//			job.setJarByClass(Neo4jCSVGenerator.class);
//			job.setMapperClass(TripleMapper.class);
//			job.setReducerClass(TripleReducer.class);
//			job.setNumReduceTasks(6);
//
//			job.setMapOutputKeyClass(Text.class);
//			job.setMapOutputValueClass(NullWritable.class);
//
//			job.setOutputKeyClass(Text.class);
//			job.setOutputValueClass(Text.class);
//
//			if (fs.exists(output)) {
//				fs.delete(output, true);
//			}
//			job.setInputFormatClass(KeyValueTextInputFormat.class);
//			job.setOutputFormatClass(SequenceFileOutputFormat.class);
//			FileInputFormat.addInputPath(job, input);
//			FileOutputFormat.setOutputPath(job, output);
//			job.waitForCompletion(true);
//		}
//		catch (IOException e) {
//			logger.error("i/o exception loading data sources", e);
//		}
//		catch (InterruptedException e) {
//			logger.debug("Hadoop job interrupted", e);
//		}
//		catch (ClassNotFoundException e) {
//			logger.debug("Cannot find mapper/reducer class", e);
//		}
//
//	}
//	
//	private static void generateRelationships(Configuration conf, Path input, Path output) throws IOException {
//
//		FileSystem fs = FileSystem.get(conf);
//
//		try {
//
//			Job job = Job.getInstance(conf, "export triples to Neo4j relationships CSV file");
//
//			job.setJarByClass(Neo4jCSVGenerator.class);
//			job.setMapperClass(Mapper.class);
//			job.setReducerClass(RelationshipReducer.class);
//			job.setNumReduceTasks(1);
//
//			job.setMapOutputKeyClass(Text.class);
//			job.setMapOutputValueClass(Text.class);
//
//			job.setOutputKeyClass(Text.class);
//			job.setOutputValueClass(NullWritable.class);
//
//			if (fs.exists(output)) {
//				fs.delete(output, true);
//			}
//			job.setInputFormatClass(SequenceFileInputFormat.class);
//			job.setOutputFormatClass(TextOutputFormat.class);
//			FileInputFormat.addInputPath(job, input);
//			FileOutputFormat.setOutputPath(job, output);
//			job.waitForCompletion(true);
//		}
//		catch (IOException e) {
//			logger.error("i/o exception loading data sources", e);
//		}
//		catch (InterruptedException e) {
//			logger.debug("Hadoop job interrupted", e);
//		}
//		catch (ClassNotFoundException e) {
//			logger.debug("Cannot find mapper/reducer class", e);
//		}
//
//	}
//	
//	private static void generateNodes(Configuration conf, Path input, Path output) throws IOException {
//
//		FileSystem fs = FileSystem.get(conf);
//
//		try {
//
//			Job job = Job.getInstance(conf, "export triples to Neo4j nodes CSV file");
//
//			job.setJarByClass(Neo4jCSVGenerator.class);
//			job.setMapperClass(NodeMapper.class);
//			job.setReducerClass(NodeReducer.class);
//			job.setNumReduceTasks(1);
//
//			job.setMapOutputKeyClass(Text.class);
//			job.setMapOutputValueClass(NullWritable.class);
//
//			job.setOutputKeyClass(Text.class);
//			job.setOutputValueClass(NullWritable.class);
//
//			if (fs.exists(output)) {
//				fs.delete(output, true);
//			}
//			job.setInputFormatClass(SequenceFileInputFormat.class);
//			job.setOutputFormatClass(TextOutputFormat.class);
//			FileInputFormat.addInputPath(job, input);
//			FileOutputFormat.setOutputPath(job, output);
//			job.waitForCompletion(true);
//		}
//		catch (IOException e) {
//			logger.error("i/o exception loading data sources", e);
//		}
//		catch (InterruptedException e) {
//			logger.debug("Hadoop job interrupted", e);
//		}
//		catch (ClassNotFoundException e) {
//			logger.debug("Cannot find mapper/reducer class", e);
//		}
//
//	}
	
	private static void printUsage() {
		List<String> parameters = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		
		parameters.add("-base");   
		descriptions.add("path of the base folder, default is \".\"");
		
		parameters.add("-input");
		descriptions.add("path of input, default value is base/input");

		parameters.add("-output1");
		descriptions.add("path of nodes output, default value is base/nodes");

		parameters.add("-output2");
		descriptions.add("path of relationships output, default value is base/relationships");

		parameters.add("-parse");
		descriptions.add("the flag can be set to false to ignore parsing step, default value is true");

		parameters.add("-nodes");
		descriptions.add("the flag can be set to false to ignore exporting nodes, default value is true");

		parameters.add("-relationships");
		descriptions.add("the flag can be set to false to ignore exporting relationships, default value is true");

		parameters.add("-help");
		descriptions.add("prints the usage of this class");

		for (int i = 0; i < parameters.size(); i++) {
			String p = parameters.get(i);
			String d = descriptions.get(i);
			System.out.println("\t" + p + "\n\t\t" + d + "\n");
		}
	}
	
	public static void main(String[] args) {

//		String text = "Bad A	cid Jesus,	(Adam Starr, 12\\";
//		text = text.replace("\t", "");
//		text = text.replace(",", "");
//		text = text.replaceAll("\\\\", "");
//		System.out.println(text);
		
		Configuration conf = new Configuration();

		for (int i = 0; i < args.length; i++) {
			if (args[i].toLowerCase().trim().equalsIgnoreCase("-help")) {
				printUsage();
				return;
			}
		}
		
		HashMap<String, String> arguments = new HashMap<>();
		for (int i = 0; i < args.length; i+=2) {
			if ( i + 1 < args.length) {
				arguments.put(args[i].trim().toLowerCase(), args[i+1].trim());
			}
		}
		
		String arg_base = "-base";
		String arg_input = "-input";
		String arg_output_nodes = "-output1";
		String arg_output_relationships = "-output2";
		String arg_parse = "-parse";
		String arg_nodes = "-nodes";
		String arg_relationships = "-relationships";

		Path basePath = new Path(".");
		if (arguments.containsKey(arg_base))
			basePath = new Path(arguments.get(arg_base));

		Path input = new Path(basePath, "input/");
		if (arguments.containsKey(arg_input))
			input = new Path(arguments.get(arg_input));

		Path output_nodes = new Path(basePath, "nodes/");
		if (arguments.containsKey(arg_output_nodes))
			output_nodes = new Path(arguments.get(arg_output_nodes));

		Path output_relationships = new Path(basePath, "relationships/");
		if (arguments.containsKey(arg_output_relationships))
			output_relationships = new Path(arguments.get(arg_output_relationships));

		boolean parse = true;
		if (arguments.containsKey(arg_parse)) {
			try {
				parse = Boolean.valueOf(arguments.get(arg_parse));
			} catch (Exception e) {
				System.out.println("Illegal value for argument " + arg_parse);
			}
		}

		boolean exportNodes = true;
		if (arguments.containsKey(arg_nodes)) {
			try {
				exportNodes = Boolean.valueOf(arguments.get(arg_nodes));
			} catch (Exception e) {
				System.out.println("Illegal value for argument " + arg_nodes);
			}
		}

		boolean exportRelationships = true;
		if (arguments.containsKey(arg_relationships)) {
			try {
				exportRelationships = Boolean.valueOf(arguments.get(arg_relationships));
			} catch (Exception e) {
				System.out.println("Illegal value for argument " + arg_relationships);
			}
		}

		try {
			Path inputSeq = new Path(basePath, "seq/");
			if (parse) {
				TripleProcessor.parseTriples(conf, input, inputSeq);
			} if (exportNodes) {
				Thread t1 = new Thread(new NodeProcessor(conf, inputSeq, output_nodes));
				t1.start();
			} if (exportRelationships) {
				Thread t2 = new Thread(new RelationshipProcessor(conf, inputSeq, output_relationships));
				t2.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	
}
