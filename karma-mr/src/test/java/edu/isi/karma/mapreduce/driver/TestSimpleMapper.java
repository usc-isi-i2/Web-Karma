package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSimpleMapper {
	MapDriver<Text, Text, Text, Text> mapDriver;
	  ReduceDriver<Text, Text, Text, Text> reduceDriver;
	  MapReduceDriver<Text, Text, Text, Text, Text, Text> mapReduceDriver;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Mapper<Text,Text, Text, Text> mapper = new SimpleMapper();
		Reducer<Text,Text,Text,Text> reducer = new SimpleReducer();
		
		   mapDriver = MapDriver.newMapDriver(mapper);
		   org.apache.hadoop.conf.Configuration conf = mapDriver.getConfiguration();
			conf.set("model.uri", TestSimpleMapper.class.getClassLoader().getResource("people-model.ttl").toURI().toString());   
		    reduceDriver = ReduceDriver.newReduceDriver(reducer);
		    mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		
		mapDriver.withInput(new Text("people.json"), new Text(IOUtils.toString(TestSimpleMapper.class.getClassLoader().getResourceAsStream("data/people.json"))));
		List<Pair<Text,Text>> results = mapDriver.run();
		assertTrue(results.size() > 1);
	}

}
