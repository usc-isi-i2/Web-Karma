package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJSONMapReduceMultiRoots extends TestRDFMapReduce {


	@Before
	public void setUp() throws Exception {
		Mapper<Writable, Text, Text, Text> mapper = new JSONMapper();
		Reducer<Text,Text,Text,Text> reducer = new JSONReducer();
		
		mapDriver = MapDriver.newMapDriver(mapper);
		
		org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
		
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}
	
	

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMultiRoot() throws URISyntaxException, IOException{
		

		org.apache.hadoop.conf.Configuration conf = mapReduceDriver.getConfiguration();
		conf.set("read.karma.config", "true");
		String string = TestJSONMapReduceMultiRoots.class.getClassLoader().getResource("karmaconfig.json").toURI().toString();
		conf.set("karma.config.file", string);
		String modelFileString = new File(TestJSONMapReduceMultiRoots.class.getClassLoader().getResource("people-model.ttl").toURI()).getAbsolutePath();
		mapReduceDriver.addCacheFile(modelFileString);
		System.out.println(conf.get(MRJobConfig.CACHE_FILES));
		conf.set("karma.input.type", "JSON");
		conf.set("model.uri", TestJSONMapReduceMultiRoots.class.getClassLoader().getResource("people-model.ttl").toURI().toString());
		
		JSONArray jObj = (JSONArray) JSONSerializer.toJSON(IOUtils.toString(TestJSONMapReduceMultiRoots.class.getClassLoader().getResourceAsStream("data/peoplev2.json")));
		
		List<Pair<Writable,Text>> inputs = new ArrayList<Pair<Writable,Text>>();
		
		for(int i=0;i<jObj.size();i++){
		
			inputs.add(new Pair<Writable,Text>(new Text(jObj.getJSONObject(i).getString("url")), new Text(jObj.getString(i))));
		}
		
		mapReduceDriver.addAll(inputs);
		
		mapReduceDriver.addAllOutput(getPairsFromFile("output/people.output.json"));
		
		mapReduceDriver.runTest();
		
	}


}
