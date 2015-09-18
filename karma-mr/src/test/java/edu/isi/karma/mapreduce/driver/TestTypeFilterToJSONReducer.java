package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Before;
import org.junit.Test;

public class TestTypeFilterToJSONReducer extends TestRDFMapReduce{
	MapDriver<Writable, Text, Text, Text> mapDriver;
	ReduceDriver<Text, Text, Text, Text> reduceDriver;
	MapReduceDriver<Writable, Text, Text, Text, Text, Text> mapReduceDriver;
	@Before
	public void setUp() throws Exception {
		Mapper<Writable,Text, Text, Text> mapper = new JSONTypeFilterMapper();
		Reducer<Text,Text,Text,Text> reducer = new JSONReducer();

		mapDriver = MapDriver.newMapDriver(mapper);
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}

	@Test
	public void testMapper() throws IOException 
	{
		mapDriver.getConfiguration().set("type.filter", "http://lod.isi.edu/ontology/syllabus/Person");
		mapDriver.addAll(this.getNullTextPairsFromFile("data/people.flat.json"));
		mapDriver.addAllOutput(this.getPairsFromFile("output/people.flat.filtered.json"));
	}
	@Test
	public void testReduce() throws IOException 
	{
		
		List<Pair<Text,List<Text>>> inputs = getReducerPairsFromFile("data/people.flat.reduced.json");


		reduceDriver.withAll(inputs);
		reduceDriver.addAllOutput(this.getPairsFromFile("output/people.flat.reduced.output.json"));
		reduceDriver.runTest(false);

	}

	
	
}
