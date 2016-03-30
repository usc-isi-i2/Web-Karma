package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class TestRDFMapReduce {
	MapDriver<Writable, Text, Text, Text> mapDriver;
	ReduceDriver<Text, Text, Text, Text> reduceDriver;
	MapReduceDriver<Writable, Text, Text, Text, Text, Text> mapReduceDriver;
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
		List<Pair<Text,Text>> pairs = new LinkedList<>();
		for(String serializedPair : serializedPairs)
		{
			int firstComma = serializedPair.indexOf(",");
			String key = serializedPair.substring(serializedPair.startsWith("(")?1: 0, firstComma);
			String value = serializedPair.substring(firstComma + 2, serializedPair.length()-(serializedPair.endsWith(")")? 1:0));
			pairs.add(new Pair<>(new Text(key), new Text(value)));
		}
		return pairs;
	}
	public List<Pair<Writable,Text>> getNullTextPairsFromFile(String file) throws IOException
	{
		String fileText = IOUtils.toString(TestJSONMapReduce.class.getClassLoader().getResourceAsStream(file));
		String[] values = fileText.split("(\r\n|\n)");
		List<Pair<Writable,Text>> pairs = new LinkedList<>();
		for(String value : values)
		{
			pairs.add(new Pair<Writable,Text>(NullWritable.get(), new Text(value)));
		}
		return pairs;
	}
	public List<Pair<Text,List<Text>>> getReducerPairsFromFile(String file) throws IOException
	{
		List<Pair<Text, Text>> unsplitPairs = this.getPairsFromFile(file);
		List<Pair<Text, List<Text>>> splitPairs = new LinkedList<>();
		for(Pair<Text, Text> unsplitPair : unsplitPairs)
		{
			List<Text> splitValues = new LinkedList<>();
			String [] splits = unsplitPair.getSecond().toString().split("(\r\n|\n)");
			for(String split : splits)
			{
				splitValues.add(new Text(split));
			}
			splitPairs.add(new Pair<>(unsplitPair.getFirst(), splitValues));
			
		}
		return splitPairs;
	}
}
