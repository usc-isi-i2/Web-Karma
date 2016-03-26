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

public class TestN3MapReduce extends TestRDFMapReduce {

	@Before
	public void setUp() throws Exception {
		Mapper<Writable, Text, Text, Text> mapper = new N3Mapper();
		Reducer<Text,Text,Text,Text> reducer = new N3Reducer();

		mapDriver = MapDriver.newMapDriver(mapper);
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMap() throws IOException, URISyntaxException {

		org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
		conf.set("model.uri", TestN3MapReduce.class.getClassLoader().getResource("people-model.ttl").toURI().toString());
		mapDriver.withInput(new Text("people.json"), new Text(IOUtils.toString(TestN3MapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		List<Pair<Text,Text>> results = mapDriver.run();
		assertTrue(results.size() > 1);
	}

	@Test
	public void testMapWithInputTypeSpecified() throws IOException, URISyntaxException {

		org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
		conf.set("model.uri", TestN3MapReduce.class.getClassLoader().getResource("people-model.ttl").toURI().toString());
		conf.set("karma.input.type", "JSON");
		mapDriver.withInput(new Text("people.somethingsomething"), new Text(IOUtils.toString(TestN3MapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		List<Pair<Text,Text>> results = mapDriver.run();
		assertTrue(results.size() > 1);
	}

	@Test
	public void testMapWithBadInputTypeSpecified() throws IOException, URISyntaxException {

		org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
		conf.set("model.uri", TestN3MapReduce.class.getClassLoader().getResource("people-model.ttl").toURI().toString());
		conf.set("karma.input.type", "XML");
		mapDriver.withInput(new Text("people.somethingsomething"), new Text(IOUtils.toString(TestN3MapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		List<Pair<Text,Text>> results = mapDriver.run();
		assertTrue(results.size() == 0);
	}

	@Test
	public void testReduce() throws IOException 
	{
		List<Pair<Text,List<Text>>> inputs = new LinkedList<>();
		List<Pair<Text,Text>> outputs = new LinkedList<>();

		List<Text> jasonTriples = new LinkedList<>();
		List<Text> sufjanTriples = new LinkedList<>();
		jasonTriples.add(new Text("<http://ex.com/jason> foaf:firstName \"Jason\" ."));
		jasonTriples.add(new Text("<http://ex.com/jason> foaf:lastName \"Slepicka\" ."));
		jasonTriples.add(new Text("<http://ex.com/jason> foaf:lastName \"Slepicka\" ."));

		sufjanTriples.add(new Text("<http://ex.com/sufjan> foaf:firstName \"Sufjan\" ."));
		sufjanTriples.add(new Text("<http://ex.com/sufjan> foaf:firstName \"Sufjan\" ."));
		sufjanTriples.add(new Text("<http://ex.com/sufjan> foaf:lastName \"Slepicka\" ."));


		inputs.add(new Pair<>(new Text("<http://ex.com/jason>"), jasonTriples));
		inputs.add(new Pair<>(new Text("<http://ex.com/sufjan>"), sufjanTriples));
		reduceDriver.withAll(inputs);
		outputs.add(new Pair<>(new Text("<http://ex.com/jason>"), new Text("<http://ex.com/jason> foaf:lastName \"Slepicka\" .\n<http://ex.com/jason> foaf:firstName \"Jason\" .\n")));
		outputs.add(new Pair<>(new Text("<http://ex.com/sufjan>"), new Text("<http://ex.com/sufjan> foaf:lastName \"Slepicka\" .\n<http://ex.com/sufjan> foaf:firstName \"Sufjan\" .\n")));
		reduceDriver.addAllOutput(outputs);
		reduceDriver.runTest();

	}

	@Test
	public void testMapReduce() throws IOException, URISyntaxException
	{
		org.apache.hadoop.conf.Configuration conf = mapReduceDriver.getConfiguration();
		conf.set("model.uri", TestN3MapReduce.class.getClassLoader().getResource("people-model.ttl").toURI().toString());

		mapReduceDriver.withInput(new Text("people.json"), new Text(IOUtils.toString(TestN3MapReduce.class.getClassLoader().getResourceAsStream("data/json/people.json"))));
		mapReduceDriver.addAllOutput(getPairsFromFile("output/people.output.ttl"));
		mapReduceDriver.runTest(false);

	}

}
