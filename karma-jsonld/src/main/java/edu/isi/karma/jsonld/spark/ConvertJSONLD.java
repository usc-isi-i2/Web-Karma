package edu.isi.karma.jsonld.spark;

import edu.isi.karma.jsonld.helper.JSONLDConverter;
import org.apache.commons.cli.*;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 * Created by chengyey on 10/27/15.
 */
public class ConvertJSONLD {
    private static Logger logger = LoggerFactory.getLogger(ConvertJSONLD.class);
    public static void main(String[] args) throws ParseException {
        Options options = createCommandLineOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine cl = null;
        cl = parser.parse(options, args);
        if (cl == null || cl.getOptions().length == 0 || cl.hasOption("help")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(ConvertJSONLD.class.getSimpleName(), options);
        }
        String filePath = cl.getOptionValue("filepath");
        String outputPath = cl.getOptionValue("outputpath");
        String tmp = cl.getOptionValue("numpartition");
        if (filePath == null || outputPath == null) {
            logger.error("No file path provided!");
            return;
        }
        int numPartition = 100;
        if (tmp != null) {
            try {
                numPartition = Integer.parseInt(tmp);
            } catch (Exception e) {
                logger.error("Invalid number", e);
            }
        }
        SparkConf conf = new SparkConf().setAppName("ConvertJSONLD");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaPairRDD<Text, Text> input = sc.sequenceFile(filePath, Text.class, Text.class, numPartition);
        input.mapToPair(new PairFunction<Tuple2<Text,Text>, String, String>() {
            @Override
            public Tuple2<String, String> call(Tuple2<Text, Text> textTextTuple2) throws Exception {
                return new Tuple2<>(textTextTuple2._1().toString(), new JSONLDConverter().convertJSONLD(textTextTuple2._2().toString()));
            }
        }).reduceByKey(new Function2<String, String, String>() {
            @Override
            public String call(String text, String text2) throws Exception {
                return new JSONLDConverter().deduplicateTriples(text, text2);
            }
        }).values().saveAsTextFile(outputPath);
    }

    private static Options createCommandLineOptions() {
        Options options = new Options();
        options.addOption(new Option("filepath", "filepath", true, "Path to coordinate sequence file"));
        options.addOption(new Option("numpartition", "numpartition", true, "Minimum number of partitions"));
        options.addOption(new Option("outputpath", "outputpath", true, "Path to output directory"));
        return options;
    }
}
