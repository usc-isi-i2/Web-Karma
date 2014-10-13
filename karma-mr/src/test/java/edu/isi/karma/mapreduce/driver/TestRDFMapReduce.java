package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class TestRDFMapReduce {
	MapDriver<Text, Text, Text, Text> mapDriver;
	  ReduceDriver<Text, Text, Text, Text> reduceDriver;
	  MapReduceDriver<Text, Text, Text, Text, Text, Text> mapReduceDriver;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	public List<Pair<Text,Text>> getPairsFromFile(String file) throws IOException
	{
		String fileText = IOUtils.toString(TestJSONMapReduce.class.getClassLoader().getResourceAsStream(file));
		String[] serializedPairs = fileText.split("\\)(\r\n|\n)\\(");
		List<Pair<Text,Text>> pairs = new LinkedList<Pair<Text,Text>>();
		for(String serializedPair : serializedPairs)
		{
			int firstComma = serializedPair.indexOf(",");
			String key = serializedPair.substring(serializedPair.startsWith("(")?1: 0, firstComma);
			String value = serializedPair.substring(firstComma + 2, serializedPair.length()-(serializedPair.endsWith(")")? 1:0));
			pairs.add(new Pair<Text,Text>(new Text(key), new Text(value)));
		}
		return pairs;
	}
}
