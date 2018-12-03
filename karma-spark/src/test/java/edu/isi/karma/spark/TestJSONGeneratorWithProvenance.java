package edu.isi.karma.spark;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import com.holdenkarau.spark.testing.SharedJavaSparkContext;

@Ignore
public class TestJSONGeneratorWithProvenance  extends SharedJavaSparkContext implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7981683598336701496L;

	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}

	@Test 
	public void testWithoutProvenance() throws IOException, ParseException {
		JavaRDD<String> karmaRDD = KarmaDriver.applyModel(jsc(), getInputRDD(), 
				getKarmaSettings(false).toString(), 1000, 2);
		JavaRDD<String> result = JSONReducerDriver.reduceJSON(jsc(), karmaRDD, 1, 
				getKarmaSettings(false).toString());
		
		List<String> lines = result.collect();
		JSONParser jsonParser = new JSONParser();
		for(String line: lines) {
			String[] keyValue = line.split("\t");
			if(keyValue[0].equals("http://effect.isi.edu/data/forum/100/topic/C73751DBA59047F08EE1664859163237AE9F7780")) {
				JSONObject jsonObj = (JSONObject)jsonParser.parse(keyValue[1]);
				String source = jsonObj.get("source").toString();
//				System.out.println(source);
//				System.out.println(line);
				assertEquals(source, "[\"asu-hacking-post_76b7327d0bd1846e3e153530f9be7722\",\"asu-hacking-post_89ecd75389778f4c37ddf3f71e42ba63\",\"asu-hacking-post_b22bc37a0e1d5a92b86726d2df067d5b\",\"asu-hacking-post_c0bdcf1482cec7959a5de0f5b47eeedc\"]");
			}
		}
		
		assertEquals(result.count(), 12);
	}
	
	@Test 
	public void testProvenance() throws IOException, ParseException {
		JavaRDD<String> karmaRDD = KarmaDriver.applyModel(jsc(), 
										getInputRDD(), 
										getKarmaSettings(true).toString(), 1000, 2);
		JavaRDD<String> result = JSONReducerDriver.reduceJSON(jsc(), karmaRDD, 1, getKarmaSettings(true).toString());
		List<String> lines = result.collect();
		JSONParser jsonParser = new JSONParser();
		for(String line: lines) {
			String[] keyValue = line.split("\t");
			if(keyValue[0].equals("http://effect.isi.edu/data/forum/100/topic/C73751DBA59047F08EE1664859163237AE9F7780")) {
				JSONObject jsonObj = (JSONObject)jsonParser.parse(keyValue[1]);
				String source = jsonObj.get("source").toString();
//				System.out.println(source);
//				System.out.println(line);
				assertEquals(source, "asu-hacking-post_c0bdcf1482cec7959a5de0f5b47eeedc");
			}
		}
		
		assertEquals(result.count(), 12);
	}
	
	private JavaRDD<String> getInputRDD() {
		JavaRDD<String> inputRDD = jsc().textFile(getTestResource("provenance/hacking_posts.jl").toString());
		assertEquals(inputRDD.count(), 4);
		
		JavaRDD<String> tabRDD = inputRDD.map(new Function<String, String>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String call(String arg0) throws Exception {
				return "karma\t" + arg0;
			}
		});
		return tabRDD;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getKarmaSettings(boolean provenance) {
		JSONObject karmaSettings = new JSONObject();
		karmaSettings.put("karma.input.type", "JSON");
		karmaSettings.put("base.uri", "http://effect.isi.edu/data/");
		karmaSettings.put("rdf.generation.root", "http://schema.dig.isi.edu/ontology/Topic1");
		karmaSettings.put("model.uri", getTestResource("provenance/hacking_posts-model.ttl").toString());
		karmaSettings.put("is.model.in.json", "true");
		karmaSettings.put("context.uri", getTestResource("provenance/karma-context.json").toString());
		karmaSettings.put("is.root.in.json", "true");
		karmaSettings.put("read.karma.config", "false");
		karmaSettings.put("rdf.generation.disable.nesting", "true");
		karmaSettings.put("karma.reducer.run", "false");
		if(provenance)
			karmaSettings.put("karma.provenance.properties", "source,publisher,dateRecorded:date");
		return karmaSettings;
	}
}
