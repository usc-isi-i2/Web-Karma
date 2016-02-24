package edu.isi.karma.spark;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import edu.isi.karma.rdf.JSONImpl;
import edu.isi.karma.util.JSONLDUtil;

/**
 * Created by chengyey on 12/6/15.
 */
public class KarmaDriver {
    private static Logger logger = LoggerFactory.getLogger(KarmaDriver.class);
    
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
                private static final long serialVersionUID = -9042224661662821670L;

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
    			JavaPairRDD<String, String> input, 
    			final Properties karmaSettings,
        		final int batchSize) {
    
		String input_type = karmaSettings.getProperty("karma.input.type");
		if (input_type != null && input_type.toUpperCase().equals("JSON")) {
			input = input.values().glom().flatMapToPair(
					new PairFlatMapFunction<List<String>, String, String>() {

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

		JavaPairRDD<Text, Text> pairs = input.flatMapToPair(
						new PairFlatMapFunction<Tuple2<String,String>, String, String>() {
        	private static final long serialVersionUID = -3533063264900721773L;
        	
			@Override
            public Iterable<Tuple2<String, String>> call(Tuple2<String, String> writableIterableTuple2) throws Exception {
                List<Tuple2<String, String>> results = new LinkedList<>();
                final JSONImpl mapper = new JSONImpl(karmaSettings);
            	String result = mapper.mapResult(writableIterableTuple2._1, writableIterableTuple2._2);
                JSONArray generatedObjects = new JSONArray(result);
                for (int i = 0; i < generatedObjects.length(); i++) {
                    try {
                        String key;
                        String value;
                        if (generatedObjects.getJSONObject(i).has(mapper.getAtId())) {
                            key = generatedObjects.getJSONObject(i).getString(mapper.getAtId());
                        } else {
                            key = generatedObjects.getJSONObject(i).toString();
                        }
                        value = generatedObjects.getJSONObject(i).toString();
                        results.add(new Tuple2<>(key, value));
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        logger.error("************ARRAYEXCEPTION*********:" + ae.getMessage() + "SOURCE: " + generatedObjects.getJSONObject(i).toString());
                    }
                }
                return results;
            }
        }).reduceByKey(new Function2<String, String, String>() {
            private static final long serialVersionUID = -3238789305990222436L;

			@Override
            public String call(String text, String text2) throws Exception {
                JSONObject left = new JSONObject(text);
                JSONObject right = new JSONObject(text2);
                return JSONLDUtil.mergeJSONObjects(left, right).toString();
            }
        }).mapToPair(new PairFunction<Tuple2<String,String>, Text, Text>() {
            private static final long serialVersionUID = 2787821808872176951L;

			@Override
            public Tuple2<Text, Text> call(Tuple2<String, String> stringStringTuple2) throws Exception {
                return new Tuple2<>(new Text(stringStringTuple2._1), new Text(stringStringTuple2._2));
            }
        });
        return pairs;
    }
    
    public static JavaRDD<String> applyModel(JavaSparkContext jsc, 
    		JavaRDD<String> input, 
    		String propertiesStr,
    		final int batchSize) {
    	JSONObject properties = new JSONObject(propertiesStr);
	    Properties prop = new Properties();
	    for (Iterator<String> keysIterator = properties.keys(); keysIterator.hasNext(); ) {
			String objPropertyName = keysIterator.next();
			String propertyName = objPropertyName.toString();
			String value = properties.getString(propertyName);
			logger.info("Set " + propertyName + "=" + value);
			prop.setProperty(propertyName, value);
		}
		JavaPairRDD<Text, Text> pairs = applyModel(jsc, input, prop, batchSize);
		return pairs.map(new Function<Tuple2<Text,Text>, String>() {

			private static final long serialVersionUID = 5833358013516510838L;

			@Override
			public String call(Tuple2<Text, Text> arg0) throws Exception {
				return (arg0._1() + "\t" + arg0._2());
			}
		});
    }
	
    public static JavaPairRDD<Text, Text> applyModel(JavaSparkContext jsc, 
    		JavaRDD<String> input, 
    		Properties properties,
    		final int batchSize) {
    	JavaPairRDD<String, String> pairRDD = input.mapToPair(new PairFunction<String, String, String>() {
            private static final long serialVersionUID = -4153068088292891034L;

			public Tuple2<String, String> call(String s) throws Exception {
                int tabIndex = s.indexOf("\t");
                return new Tuple2<>(s.substring(0, tabIndex), s.substring(tabIndex + 1));
            }
        });
    	return applyModel(jsc, pairRDD, properties, batchSize);
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
