package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.types.Pair;
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
	public void testMapper() throws IOException, URISyntaxException 
	{
		String contextURL = TestJSONCompactMapper.class.getClassLoader().getResource("bsbm.context.json").toURI().toString();
		mapDriver.getConfiguration().set("jsonld.context.url", contextURL);
		mapDriver.addAll(this.getNullTextPairsFromFile("data/bsbm.compact.input.json"));
		List<Pair<Text,Text>> outputPairs = this.getPairsFromFile("output/bsbm.compact.output.json");
		for(Pair<Text, Text> outputPair : outputPairs)
		{
			outputPair.getSecond().set((outputPair.getSecond().toString().replace("$CONTEXT",contextURL).toString()));
		}
		mapDriver.addAllOutput(outputPairs);
		mapDriver.runTest(false);
	}

}
