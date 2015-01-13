package edu.isi.karma.mapreduce.driver;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Before;
import org.junit.Test;

public class TestJSONCompactMapper extends TestRDFMapReduce{
	MapDriver<Writable, Text, Text, Text> mapDriver;
	@Before
	public void setUp() throws Exception {
		Mapper<Writable,Text, Text, Text> mapper = new JSONCompactMapper();

		mapDriver = MapDriver.newMapDriver(mapper);
	}

	@Test
	public void testMapper() throws IOException 
	{
		mapDriver.getConfiguration().set("jsonld.context.file", "/users/slepicka/projects/eswc-2015-nosparql/benchmark-tools/bsbm.context.json");
		mapDriver.addAll(this.getNullTextPairsFromFile("data/bsbm.compact.input.json"));
		mapDriver.addAllOutput(this.getPairsFromFile("output/bsbm.compact.output.json"));
		mapDriver.runTest();
	}
	
}
