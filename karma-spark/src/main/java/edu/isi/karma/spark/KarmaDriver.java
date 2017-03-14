package edu.isi.karma.spark;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import edu.isi.karma.rdf.JSONImpl;
import edu.isi.karma.rdf.N3Impl;
import edu.isi.karma.util.JSONLDUtilSimple;

/**
 * Created by chengyey on 12/6/15.
 */
public class KarmaDriver {
    private static Logger logger = LoggerFactory.getLogger(KarmaDriver.class);

	private KarmaDriver() {
	}

	public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException {
    	int defaultPartitions = 100;
    	final int batchSize = 200;
    	
    	Options options = createCommandLineOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine cl = null;
        cl = parser.parse(options, args);
        if (cl == null || cl.getOptions().length == 0 || cl.hasOption("help")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(KarmaDriver.class.getSimpleName(), options);
        }
        String filePath = cl.getOptionValue("filepath");
        String outputPath = cl.getOptionValue("outputpath");
        String inputFormat = cl.getOptionValue("inputformat");
        if (filePath == null || outputPath == null) {
            logger.error("No file path provided!");
            return;
        }
        int partitions = defaultPartitions;
        try {
            partitions = Integer.parseInt(cl.getOptionValue("partitions"));
        } catch (Exception e) {

        }
        
        final SparkConf conf = new SparkConf().setAppName("Karma");
        conf.set("spark.master", "local[*]");
        conf.set("spark.executor.userClassPathFirst", "true");
        conf.set("spark.driver.userClassPathFirst", "true");
        conf.set("spark.files.userClassPathFirst", "true");
        conf.set("spark.io.compression.codec", "lz4");
        conf.set("spark.yarn.dist.archives", "karma.zip");
        conf.set("spark.yarn.dist.files", "job.properties");
        
        final JavaSparkContext sc = new JavaSparkContext(conf);
        JavaPairRDD<String, String> pairs;
        if (inputFormat.equals("text")) {
            JavaRDD<String> input = sc.textFile(filePath, partitions);
            pairs = input.mapToPair(new PairFunction<String, String, String>() {
                private static final long serialVersionUID = 4170227232300260255L;

				@Override
                public Tuple2<String, String> call(String s) throws Exception {
                    int tabIndex = s.indexOf("\t");
                    return new Tuple2<>(s.substring(0, tabIndex), s.substring(tabIndex + 1));
                }
            });
        }
        else {
            JavaPairRDD<Writable, Text> input = sc.sequenceFile(filePath, Writable.class, Text.class, partitions);
            pairs = input.mapToPair(new PairFunction<Tuple2<Writable, Text>, String, String>() {
                

				@Override
                public Tuple2<String, String> call(Tuple2<Writable, Text> textTextTuple2) throws Exception {
                    return new Tuple2<>(textTextTuple2._1.toString(), textTextTuple2._2.toString());
                }
            });
        }
        
        Properties properties = new Properties();
        properties.load(new FileInputStream("job.properties"));
        applyModel(sc, pairs, properties, batchSize)
        		.saveAsNewAPIHadoopFile(outputPath, Text.class, Text.class, SequenceFileOutputFormat.class);
    }
 
	public static JavaPairRDD<Text, Text> applyModel(JavaSparkContext sc,
			JavaPairRDD<String, String> input, final Properties karmaSettings,
			final int batchSize) throws IOException {
		return applyModel(sc, input, karmaSettings, batchSize, sc.getConf()
				.getInt("spark.default.parallelism", 1));
	}
	   
    public static JavaPairRDD<Text, Text> applyModel(JavaSparkContext sc, 
    			JavaPairRDD<String, String> input, 
    			final Properties karmaSettings,
        		final int batchSize, int numPartitions) throws IOException {
    
		String input_type = karmaSettings.getProperty("karma.input.type");
		String modelUrl = karmaSettings.getProperty("model.uri");
		String contextUrl = karmaSettings.getProperty("context.uri");
		
		String modelTxt = IOUtils.toString(new URL(modelUrl));
		final Broadcast<String> model = sc.broadcast(modelTxt);
		
		String contextTxt = IOUtils.toString(new URL(contextUrl));
		final Broadcast<String> context = sc.broadcast(contextTxt);
        
		final String outputFormat = karmaSettings.getProperty("karma.output.format");
		
		logger.info("Load model:" + modelUrl);
		logger.info("Load context:" + contextUrl);
		
		if (input_type != null && input_type.toUpperCase().equals("JSON")) {
			input = input.values().glom().flatMapToPair(
					new PairFlatMapFunction<List<String>, String, String>() {

						private static final long serialVersionUID = 7257511573596956635L;

						@Override
						public Iterable<Tuple2<String, String>> call(
								List<String> t) throws Exception {
							List<Tuple2<String, String>> results = new LinkedList<>();
							String key = "";
							Iterable<String> values = t;
							int count = 0;
							StringBuilder builder = new StringBuilder();
							builder.append("[");
							boolean isFirst = true;
							for (String value : values) {
								if (isFirst) {
									builder.append(value);
									isFirst = false;
								} else {
									builder.append(",").append(value);
								}
								count++;
								if (count == batchSize) {
									builder.append("]");
									results.add(new Tuple2<>(key,
											builder.toString()));
									builder = new StringBuilder();
									builder.append("[");
									isFirst = true;
									count = 0;
								}
							}
							String last = builder.append("]").toString();
							results.add(new Tuple2<>(key, last));
							return results;
						}
					});
		}

		if(outputFormat != null && outputFormat.equals("n3")) {
			return applyModelToGetN3(input, 
		    		 karmaSettings,model, context,
		    		outputFormat, numPartitions);
		}
		else
		{
		    return applyModelToGetJSON(input, karmaSettings, model, context, outputFormat, numPartitions);	
		}
		
    }
    
    public static JavaRDD<String> applyModel(JavaSparkContext jsc, 
    		JavaRDD<String> input, 
    		String propertiesStr,
    		final int batchSize, int numPartitions) throws IOException, org.json.simple.parser.ParseException {
    	JSONParser parser = new JSONParser();
    	JSONObject properties = (JSONObject) parser.parse(propertiesStr);
	    Properties prop = new Properties();
	    for (@SuppressWarnings("unchecked")
		Iterator<String> keysIterator = properties.keySet().iterator(); keysIterator.hasNext(); ) {
			String objPropertyName = keysIterator.next();
			String propertyName = objPropertyName;
			String value = ((String)properties.get(propertyName));
			logger.info("Set " + propertyName + "=" + value);
			prop.setProperty(propertyName, value);
		}
		JavaPairRDD<Text, Text> pairs = applyModel(jsc, input, prop, batchSize, numPartitions);
		return pairs.map(new Function<Tuple2<Text,Text>, String>() {

			private static final long serialVersionUID = 5833358013516510838L;

			@Override
			public String call(Tuple2<Text, Text> arg0) throws Exception {
				return (arg0._1() + "\t" + arg0._2());
			}
		});
    }

	public static JavaPairRDD<Text, Text> applyModelToGetJSON(
			JavaPairRDD<String, String> input, final Properties karmaSettings,
			final Broadcast<String> model, final Broadcast<String> context,
			final String outputFormat, int numPartitions) {

		JavaPairRDD<String, JSONObject> pairs = input
				.flatMapToPair(new PairFlatMapFunction<Tuple2<String, String>, String, JSONObject>() {
					private static final long serialVersionUID = -3533063264900721773L;

					@Override
					public Iterable<Tuple2<String, JSONObject>> call(
							Tuple2<String, String> writableIterableTuple2)
							throws Exception {
						List<Tuple2<String, JSONObject>> results = new LinkedList<>();
						Properties karmaContentSettings = new Properties();
						for (Map.Entry<Object, Object> objectObjectEntry : karmaSettings
								.entrySet())
							karmaContentSettings.put(
									objectObjectEntry.getKey(),
									objectObjectEntry.getValue());
						karmaContentSettings.put("model.content", model.value());
						karmaContentSettings.put("context.content",
								context.getValue());

						final JSONImpl mapper = new JSONImpl(
								karmaContentSettings);
						String result = mapper.mapResult(
								writableIterableTuple2._1,
								writableIterableTuple2._2);
						JSONParser parser = new JSONParser();
						JSONArray generatedObjects = ((JSONArray) parser
								.parse(result));
						for (int i = 0; i < generatedObjects.size(); i++) {
							try {
								String key;
								JSONObject value;
								if (((JSONObject) generatedObjects.get(i))
										.containsKey(mapper.getAtId())) {
									key = (String) ((JSONObject) generatedObjects
											.get(i)).get(mapper.getAtId());
								} else {
									key = generatedObjects.get(i).toString();
								}
								value = ((JSONObject)generatedObjects.get(i));
								results.add(new Tuple2<>(key, value));
							} catch (ArrayIndexOutOfBoundsException ae) {
								logger.error("************ARRAYEXCEPTION*********:"
										+ ae.getMessage()
										+ "SOURCE: "
										+ generatedObjects.get(i).toString());
							}
						}

						return results;
					}
				});

		boolean runReducer = true;
		if(karmaSettings.containsKey("karma.reducer.run")) {
			runReducer = Boolean.parseBoolean(karmaSettings.getProperty("karma.reducer.run"));
		}
		
		if(runReducer) {
			JavaPairRDD<String, String> reducedSerializedPairs = JSONReducerDriver.reduceJSON(numPartitions, pairs, karmaSettings);
			return reducedSerializedPairs
					.mapToPair(new PairFunction<Tuple2<String, String>, Text, Text>() {
						private static final long serialVersionUID = 2787821808872176951L;
	
						@Override
						public Tuple2<Text, Text> call(
								Tuple2<String, String> stringStringTuple2)
								throws Exception {
							return new Tuple2<>(new Text(stringStringTuple2._1),
									new Text(stringStringTuple2._2));
						}
					});
		} else {
			//To return without running the reducer:
			return pairs.mapToPair(new PairFunction<Tuple2<String,JSONObject>, Text, Text>() {
	
				@Override
				public Tuple2<Text, Text> call(Tuple2<String, JSONObject> arg0)
						throws Exception {
					return new Tuple2<>(new Text(arg0._1),
							new Text(arg0._2.toJSONString()));
				}
			});
		}
	}

    public static JavaPairRDD<Text, Text>applyModelToGetN3(JavaPairRDD<String, String> input, 
    		final Properties karmaSettings,final Broadcast<String> model, final Broadcast<String> context,
    		final String outputFormat, int numPartitions )
    {
    	JavaPairRDD<String, String> pairs = input.flatMapToPair(
				new PairFlatMapFunction<Tuple2<String,String>, String, String>() {
	private static final long serialVersionUID = -3533063264900721773L;
	
	@Override
    public Iterable<Tuple2<String, String>> call(Tuple2<String, String> writableIterableTuple2) throws Exception {
        List<Tuple2<String, String>> results = new LinkedList<>();
        Properties karmaContentSettings = new Properties();
        for(Map.Entry<Object, Object> objectObjectEntry : karmaSettings.entrySet())
        	karmaContentSettings.put(objectObjectEntry.getKey(), objectObjectEntry.getValue());
        karmaContentSettings.put("model.content", model.value());
        karmaContentSettings.put("context.content", context.getValue());
        
        if(outputFormat != null && outputFormat.equals("n3")) {
        	final N3Impl mapper = new N3Impl(karmaContentSettings);
        	String result = mapper.mapResult(writableIterableTuple2._1, writableIterableTuple2._2);
        	String[] lines = result.split("(\r\n|\n)");
    		for(String line : lines)
    		{
    			if((line = line.trim()).isEmpty())
    			{
    				continue;
    			}
    			int splitBetweenSubjectAndPredicate = line.indexOf(' ');
    			String key = (line.substring(0, splitBetweenSubjectAndPredicate));
    			String value = line;
    			results.add(new Tuple2<>(key, value));
    		}
        }
        return results;
    }
});
	
		return pairs.mapToPair(new PairFunction<Tuple2<String,String>, Text, Text>() {
            private static final long serialVersionUID = 2787821808872176951L;

			@Override
            public Tuple2<Text, Text> call(Tuple2<String, String> stringStringTuple2) throws Exception {
                return new Tuple2<>(new Text(stringStringTuple2._1), new Text(stringStringTuple2._2));
            }
        });
    }
    
    
    public static JavaPairRDD<Text, Text> applyModel(JavaSparkContext jsc, 
    		JavaRDD<String> input, 
    		Properties properties,
    		final int batchSize, int numPartitions) throws IOException {
    	JavaPairRDD<String, String> pairRDD = input.mapToPair(new PairFunction<String, String, String>() {
            private static final long serialVersionUID = -4153068088292891034L;

			public Tuple2<String, String> call(String s) throws Exception {
                int tabIndex = s.indexOf("\t");
                return new Tuple2<>(s.substring(0, tabIndex), s.substring(tabIndex + 1));
            }
        });
    	return applyModel(jsc, pairRDD, properties, batchSize, numPartitions);
    }
    
    private static Options createCommandLineOptions() {
        Options options = new Options();
        options.addOption(new Option("filepath", "filepath", true, "Path to coordinate sequence file"));
        options.addOption(new Option("outputpath", "outputpath", true, "Path to output directory"));
        options.addOption(new Option("inputformat", "inputformat", true, "Path to output directory"));
        options.addOption(new Option("partitions", "partitions", true, "Number of partitions"));
        return options;
    }

}
