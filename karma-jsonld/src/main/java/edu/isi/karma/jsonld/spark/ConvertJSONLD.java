package edu.isi.karma.jsonld.spark;

import com.jayway.jsonpath.JsonPath;
import edu.isi.karma.jsonld.helper.JSONLDConverter;
import org.apache.commons.cli.*;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by chengyey on 10/27/15.
 */
public class ConvertJSONLD {
    private static Logger logger = LoggerFactory.getLogger(ConvertJSONLD.class);
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MMM");
    private static final int defaultNumPartition = 100;
    private static final int defaultOutputPartition = 10;
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
        String tmpNumpartition = cl.getOptionValue("numpartition");
        final String dateFilter = cl.getOptionValue("datefilter", "");
        System.out.println("dateFilter: " + dateFilter);
        String tmpOutputPartition = cl.getOptionValue("outputpartition");
        if (filePath == null || outputPath == null) {
            logger.error("No file path provided!");
            return;
        }
        int numPartition = defaultNumPartition;
        if (tmpNumpartition != null) {
            try {
                numPartition = Integer.parseInt(tmpNumpartition);
            } catch (Exception e) {
                logger.error("Invalid number", e);
            }
        }

        int outputPartition = defaultOutputPartition;
        if (tmpNumpartition != null) {
            try {
                outputPartition = Integer.parseInt(tmpOutputPartition);
            } catch (Exception e) {
                logger.error("Invalid number", e);
            }
        }
        SparkConf conf = new SparkConf().setAppName("ConvertJSONLD");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaPairRDD<Text, Text> input = sc.sequenceFile(filePath, Text.class, Text.class, numPartition);
        input.filter(new Function<Tuple2<Text, Text>, Boolean>() {
            @Override
            public Boolean call(Tuple2<Text, Text> textTextTuple2) throws Exception {
                List<String> results = JsonPath.read(textTextTuple2._2.toString(), "$..dateCreated");
                for (String s : results) {
                    String date = outputFormat.format(inputFormat.parse(s));
                    if (date.contains(dateFilter)) {
                        return true;
                    }
                }
                if (results.isEmpty() && dateFilter.isEmpty()) {
                    return true;
                }
                return false;
            }
        }).mapToPair(new PairFunction<Tuple2<Text,Text>, String, String>() {
            @Override
            public Tuple2<String, String> call(Tuple2<Text, Text> textTextTuple2) throws Exception {
                return new Tuple2<>(textTextTuple2._1().toString(), new JSONLDConverter().convertJSONLD(textTextTuple2._2().toString()));
            }
        }).reduceByKey(new Function2<String, String, String>() {
            @Override
            public String call(String text, String text2) throws Exception {
                return new JSONLDConverter().deduplicateTriples(text, text2);
            }
        }).values().coalesce(outputPartition).saveAsTextFile(outputPath);
    }

    private static Options createCommandLineOptions() {
        Options options = new Options();
        options.addOption(new Option("filepath", "filepath", true, "Path to coordinate sequence file"));
        options.addOption(new Option("numpartition", "numpartition", true, "Minimum number of partitions"));
        options.addOption(new Option("outputpartition", "outputpartition", true, "Number of partitions for output"));
        options.addOption(new Option("outputpath", "outputpath", true, "Path to output directory"));
        options.addOption(new Option("datefilter", "date", true, "Date to filter in yyyy-MMM or MMM format"));
        return options;
    }
}
