package edu.isi.karma.spark;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

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
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class JSONContextDriver {
private static Logger logger = LoggerFactory.getLogger(JSONContextDriver.class);

    private JSONContextDriver() {
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
        String contextUrl = cl.getOptionValue("contextUrl");
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
      
        
      	applyContext(sc, pairs, contextUrl)
        		.saveAsNewAPIHadoopFile(outputPath, Text.class, Text.class, SequenceFileOutputFormat.class);
    }
    
    public static JavaPairRDD<String, String> applyContext(JavaSparkContext jsc, 
    		JavaPairRDD<String, String> input,
    		final String contextUrl) throws IOException {
    	
    	InputStream in = new URL(contextUrl).openStream();
        final Object contextObject = JsonUtils.fromInputStream(in);
        in.close();
		
        final Broadcast<Object> context = jsc.broadcast(contextObject);
		
		return input.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			private static final long serialVersionUID = 2878941073410454935L;

			@SuppressWarnings("unchecked")
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> t)
					throws Exception {
				String key = t._1();
				String value = t._2();
				
				JSONObject obj = new JSONObject(value);
				Object outobj = JsonLdProcessor.compact(JsonUtils.fromString(value), 
						context.getValue(), 
						new JsonLdOptions(""));
				if(outobj instanceof Map) {
					@SuppressWarnings("rawtypes")
					Map outjsonobj = (Map) outobj;
					outjsonobj.put("@context", contextUrl);
				}
				
				value = JsonUtils.toString(outobj);
				if (obj.has("uri")) {
					key = obj.getString("uri");
				}
				else if (obj.has("@id")) {
					key = obj.getString("@id");
				}
				else {
					key = obj.toString();
				}
				return new Tuple2<>(key, value);
			}
		});
    }
    
    public static JavaRDD<String> applyContext(JavaSparkContext jsc, 
    		JavaRDD<String> input, String contextUrl) throws IOException {
    	JavaPairRDD<String, String> inputPair = input.mapToPair(new PairFunction<String, String, String>() {
            private static final long serialVersionUID = -4153068088292891034L;

			public Tuple2<String, String> call(String s) throws Exception {
                int tabIndex = s.indexOf("\t");
                return new Tuple2<>(s.substring(0, tabIndex), s.substring(tabIndex + 1));
            }
        });
    	
    	JavaPairRDD<String, String> pairs = applyContext(jsc, inputPair, contextUrl);
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
        options.addOption(new Option("contextUrl", "contextUrl", true, "Path to context url"));
        options.addOption(new Option("outputpath", "outputpath", true, "Path to output directory"));
        options.addOption(new Option("inputformat", "inputformat", true, "Path to output directory"));
        options.addOption(new Option("partitions", "partitions", true, "Number of partitions"));
        return options;
    }
}
