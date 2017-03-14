package edu.isi.karma.spark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.spark.api.java.function.PairFunction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import edu.isi.karma.util.JSONLDUtilSimple;

public class JSONReducerDriver {
	private static Logger logger = LoggerFactory.getLogger(JSONReducerDriver.class);

    private JSONReducerDriver() {
    }

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException {
    	int defaultPartitions = 100;
    	
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
        conf.set("spark.executor.userClassPathFirst", "true");
        conf.set("spark.driver.userClassPathFirst", "true");
        conf.set("spark.files.userClassPathFirst", "true");
        conf.set("spark.io.compression.codec", "lz4");
        
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
        
        Properties karmaSettings = new Properties();
        reduceJSON(sc, pairs, karmaSettings)
        		.saveAsNewAPIHadoopFile(outputPath, Text.class, Text.class, SequenceFileOutputFormat.class);
    }
    
   
    
    public static JavaPairRDD<String, String> reduceJSON(JavaSparkContext sc, 
			JavaPairRDD<String, String> input, final Properties karmaSettings) {
    	return reduceJSON(sc, input, sc.getConf().getInt("spark.default.parallelism", 1), karmaSettings);
    }
    public static JavaPairRDD<String, String> reduceJSON(JavaSparkContext sc, 
    			JavaPairRDD<String, String> input, int numPartitions, final Properties karmaSettings) {
    	JavaPairRDD<String, JSONObject> pairs = input.mapToPair(new PairFunction<Tuple2<String, String>, String, JSONObject>() {

			private static final long serialVersionUID = 8884768697918036449L;
			
			@Override
			public Tuple2<String, JSONObject> call(Tuple2<String, String> tuple)
					throws Exception {
				JSONParser parser = new JSONParser();
				String key = tuple._1();
				String value = tuple._2();
				JSONObject obj = (JSONObject)parser.parse(value);
				if (obj.containsKey("uri")) {
					key = (String)obj.get("uri");
				} else if (obj.containsKey("@id")) {
					key = (String)obj.get("@id");
				}
				
				return new Tuple2<>(key, obj);
			}
		});
		
		return reduceJSON(numPartitions, pairs, karmaSettings);
		
    	
    }

	public static JavaPairRDD<String, String> reduceJSON(int numPartitions,
			JavaPairRDD<String, JSONObject> pairs, final Properties karmaSettings) {
		String provenancePropertiesStr = karmaSettings.getProperty("karma.provenance.properties");
		HashMap<String, String> provenaceProperties = new HashMap<>();
		if(provenancePropertiesStr != null) {
			String[] provProps = provenancePropertiesStr.split(",");
			for(String provProp : provProps) {
				String[] provType = provProp.split(":");
				String property = provType[0];
				String type = "string";
				if(provType.length > 1)
					type = provType[1].toLowerCase();
				provenaceProperties.put(property, type);
			}
		}
		return reduceJSON(numPartitions, pairs, provenaceProperties);
	}
	
	public static JavaPairRDD<String, String> reduceJSON(int numPartitions,
				JavaPairRDD<String, JSONObject> pairs, final HashMap<String, String> provenaceProperties) {
		JavaPairRDD<String, JSONObject> reducedPairs = pairs
		.reduceByKey(new Function2<JSONObject, JSONObject, JSONObject>() {
			private static final long serialVersionUID = -3238789305990222436L;

			@Override
			public JSONObject call(JSONObject left, JSONObject right)
					throws Exception {
				return JSONLDUtilSimple.mergeJSONObjects(left, right, provenaceProperties);
			}
		}, numPartitions);
		return reducedPairs
		.mapValues(new Function<JSONObject, String>() {

			private static final long serialVersionUID = -1945629738808728265L;

			@Override
			public String call(JSONObject object) throws Exception {

				return object.toJSONString();
			}
		});
	}
   
    public static JavaRDD<String> reduceJSON(JavaSparkContext jsc, 
    		JavaRDD<String> input, final Properties karmaSettings) {
    	return reduceJSON(jsc, input, jsc.getConf().getInt("spark.default.parallelism", 1), karmaSettings);
    }
    
    public static JavaRDD<String> reduceJSON(JavaSparkContext sc,
    		JavaRDD<String> input, int numPartitions) throws org.json.simple.parser.ParseException {
    	return reduceJSON(sc, input, numPartitions, "{}");
    }
    
    public static JavaRDD<String> reduceJSON(JavaSparkContext sc,
    		JavaRDD<String> input, int numPartitions, String propertiesStr) throws org.json.simple.parser.ParseException {
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
    	return reduceJSON(sc, input, numPartitions, prop);
    }
    
    public static JavaRDD<String> reduceJSON(JavaSparkContext jsc, 
    		JavaRDD<String> input, int numPartitions, final Properties karmaSettings) {
    	JavaPairRDD<String, String> inputPair = input.mapToPair(new PairFunction<String, String, String>() {
            private static final long serialVersionUID = -4153068088292891034L;

			public Tuple2<String, String> call(String s) throws Exception {
                int tabIndex = s.indexOf("\t");
                return new Tuple2<>(s.substring(0, tabIndex), s.substring(tabIndex + 1));
            }
        });
		JavaPairRDD<String, String> pairs = reduceJSON(jsc, inputPair, numPartitions, karmaSettings);
		return pairs.map(new Function<Tuple2<String, String>, String>() {

			private static final long serialVersionUID = 5833358013516510838L;

			@Override
			public String call(Tuple2<String, String> arg0) throws Exception {
				return (arg0._1() + "\t" + arg0._2());
			}
		});
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
