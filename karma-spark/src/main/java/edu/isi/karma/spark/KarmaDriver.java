package edu.isi.karma.spark;

import edu.isi.karma.rdf.JSONImpl;
import edu.isi.karma.util.JSONLDUtil;
import org.apache.commons.cli.*;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengyey on 12/6/15.
 */
public class KarmaDriver {
    private static Logger logger = LoggerFactory.getLogger(KarmaDriver.class);
    private static final String propertyPath = "job.properties";
    private static final JSONImpl mapper = new JSONImpl(propertyPath);
    private static final int defaultPartitions = 100;
    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException {
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
        SparkConf conf = new SparkConf().setAppName("Karma");
        conf.set("spark.executor.userClassPathFirst", "true");
        conf.set("spark.driver.userClassPathFirst", "true");
        conf.set("spark.files.userClassPathFirst", "true");
        conf.set("spark.io.compression.codec", "lz4");
        conf.set("spark.yarn.dist.archives", "karma.zip");
        conf.set("spark.yarn.dist.files", "job.properties,extractionfiles-webpage.json");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaPairRDD<String, String> pairs;
        if (inputFormat.equals("text")) {
            JavaRDD<String> input = sc.textFile(filePath, partitions);
            pairs = input.mapToPair(new PairFunction<String, String, String>() {
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
        pairs.flatMapToPair(new PairFlatMapFunction<Tuple2<String,String>, String, String>() {
            @Override
            public Iterable<Tuple2<String, String>> call(Tuple2<String, String> writableIterableTuple2) throws Exception {
                List<Tuple2<String, String>> results = new LinkedList<>();
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
            @Override
            public String call(String text, String text2) throws Exception {
                JSONObject left = new JSONObject(text);
                JSONObject right = new JSONObject(text2);
                return JSONLDUtil.mergeJSONObjects(left, right).toString();
            }
        }).mapToPair(new PairFunction<Tuple2<String,String>, Text, Text>() {
            @Override
            public Tuple2<Text, Text> call(Tuple2<String, String> stringStringTuple2) throws Exception {
                return new Tuple2<>(new Text(stringStringTuple2._1), new Text(stringStringTuple2._2));
            }
        }).saveAsNewAPIHadoopFile(outputPath, Text.class, Text.class, SequenceFileOutputFormat.class);

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
