package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJSONMapReduce extends TestRDFMapReduce {


	@Before
	public void setUp() throws Exception {
		Mapper<Writable, Text, Text, Text> mapper = new JSONMapper();
		Reducer<Text,Text,Text,Text> reducer = new JSONReducer();

		mapDriver = MapDriver.newMapDriver(mapper);
		org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
		conf.set("model.uri", TestJSONMapReduce.class.getClassLoader().getResource("people-model.ttl").toURI().toString());
		conf.set("rdf.generation.root", "http://isi.edu/integration/karma/dev#TriplesMap_c6f9c495-90e4-4c83-aa62-0ab1841a1871");
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}
	
	

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMap() throws IOException {

		mapDriver.addInput(new Text("people.json"), new Text(IOUtils.toString(TestJSONMapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		List<Pair<Text,Text>> results = mapDriver.run();
		System.out.println(results);
		assertTrue(results.size() > 1);
	}
	
	@Test
	public void testMapDisableNesting() throws IOException {

		mapDriver.addInput(new Text("people.json"), new Text(IOUtils.toString(TestJSONMapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
		conf.set("rdf.generation.disable.nesting", "true");
		List<Pair<Text,Text>> results = mapDriver.run();
		System.out.println(results);
		assertTrue(results.size() == 27);
		conf.set("rdf.generation.disable.nesting", "false");
	}
	
	@Test
	public void testReduce() throws IOException 
	{
		List<Pair<Text,List<Text>>> inputs = new LinkedList<>();

		List<Text> jasonTriples = new LinkedList<>();
		jasonTriples.add(new Text(IOUtils.toString(TestN3MapReduce.class.getClassLoader().getResourceAsStream("jason.json"))));
		jasonTriples.add(new Text(IOUtils.toString(TestN3MapReduce.class.getClassLoader().getResourceAsStream("jason3.json"))));

		inputs.add(new Pair<>(new Text("http://lod.isi.edu/cs548/person/Slepicka"), jasonTriples));
		reduceDriver.withAllOutput(getPairsFromFile("output/jason.output.json"));
		reduceDriver.withAll(inputs);
		reduceDriver.runTest();
	}

	@Test
	public void testMapReduce() throws IOException, URISyntaxException
	{
		org.apache.hadoop.conf.Configuration conf = mapReduceDriver.getConfiguration();
		conf.set("karma.input.type", "JSON");
		conf.set("model.uri", TestJSONMapReduce.class.getClassLoader().getResource("people-model.ttl").toURI().toString());
		conf.set("rdf.generation.root", "http://isi.edu/integration/karma/dev#TriplesMap_c6f9c495-90e4-4c83-aa62-0ab1841a1871");
		mapReduceDriver.addInput(new Text("people.json"), new Text(IOUtils.toString(TestJSONMapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		mapReduceDriver.addAllOutput(getPairsFromFile("output/people.output.json"));
		mapReduceDriver.runTest();
	}

}
