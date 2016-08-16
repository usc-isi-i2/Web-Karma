package edu.isi.karma.spark;

import java.io.IOException;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.json.JSONObject;
import org.json.XML;

import scala.Tuple2;

public class UtilitiesDriver {

	public static JavaPairRDD<String, String> XMLToJSON(JavaSparkContext jsc, 
    		JavaPairRDD<String, String> input) throws IOException {
		return input.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			private static final long serialVersionUID = 2878941073410454935L;

			@Override
			public Tuple2<String, String> call(Tuple2<String, String> t)
					throws Exception {
				String key = t._1();
				JSONObject value = new JSONObject(t._2());
				String raw = value.getString("_rawContent");
				JSONObject json = getJsonFromXml(raw);
				value.put("_jsonRep", json);
				return new Tuple2<String, String>(key, value.toString());
			}
		});
	}
	
	public static JavaRDD<String> XMLToJSON(JavaSparkContext jsc, 
    		JavaRDD<String> input) throws IOException {
    	JavaPairRDD<String, String> inputPair = input.mapToPair(new PairFunction<String, String, String>() {
            private static final long serialVersionUID = -4153068088292891034L;

			public Tuple2<String, String> call(String s) throws Exception {
                int tabIndex = s.indexOf("\t");
                return new Tuple2<>(s.substring(0, tabIndex), s.substring(tabIndex + 1));
            }
        });
    	
    	JavaPairRDD<String, String> pairs = XMLToJSON(jsc, inputPair);
		return pairs.map(new Function<Tuple2<String, String>, String>() {

			private static final long serialVersionUID = 5833358013516510838L;

			@Override
			public String call(Tuple2<String, String> arg0) throws Exception {
				return (arg0._1() + "\t" + arg0._2());
			}
		});
    }
	
	public static org.json.JSONObject getJsonFromXml(String xmlStr) {
		return XML.toJSONObject(xmlStr);
	}
	
	/*
	 * method to convert xml to json
	 */
	public static String getJsonFromXml(String xmlStr, boolean prettyOutput) {
		org.json.JSONObject xmlJSONObj = XML.toJSONObject(xmlStr);
        String jsonStr = "";
        if(prettyOutput)
        	jsonStr = xmlJSONObj.toString(4);
        else
        	jsonStr = xmlJSONObj.toString();
        
        
        return jsonStr;
	}
}