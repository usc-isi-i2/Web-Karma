package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJSONIdentityMapReduce {

	MapDriver<Writable, Text, Text, Text> mapDriver;
	ReduceDriver<Text, Text, Text, Text> reduceDriver;
	MapReduceDriver<Writable, Text, Text, Text, Text, Text> mapReduceDriver;
	@Before
	public void setUp() throws Exception {
		Mapper<Writable,Text, Text, Text> mapper = new IdentityJSONMapper();
		Reducer<Text,Text,Text,Text> reducer = new JSONReducer();

		mapDriver = MapDriver.newMapDriver(mapper);
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMap() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, URISyntaxException {
		Configuration config = mapDriver.getConfiguration();
		Path path = new Path(TestJSONMapReduce.class.getClassLoader().getResource("join.seq").toURI().toString());
		SequenceFile.Reader reader = new SequenceFile.Reader(config, Reader.file(path));		
		@SuppressWarnings("rawtypes")
		WritableComparable key = (WritableComparable) reader.getKeyClass().newInstance();
		Writable value = (Writable) reader.getValueClass().newInstance();
		while (reader.next(key, value)) {
			mapDriver.addInput((BytesWritable)key, (Text)value);
		}
		reader.close();
		List<Pair<Text,Text>> results = mapDriver.run();
		assertTrue(results.size() > 1);
	}
	
	@Test
	public void testMapReduce() throws IOException, URISyntaxException, InstantiationException, IllegalAccessException {
		org.apache.hadoop.conf.Configuration conf = mapReduceDriver.getConfiguration();
		Path path = new Path(TestJSONMapReduce.class.getClassLoader().getResource("join.seq").toURI().toString());
		SequenceFile.Reader reader = new SequenceFile.Reader(conf, Reader.file(path));
		@SuppressWarnings("rawtypes")
		WritableComparable key = (WritableComparable) reader.getKeyClass().newInstance();
		Writable value = (Writable) reader.getValueClass().newInstance();
		while (reader.next(key, value)) {
			mapReduceDriver.addInput((BytesWritable)key, (Text)value);
		}
		reader.close();
		List<Pair<Text,Text>> results = mapReduceDriver.run();
		assertTrue(results.size() > 1);
	}
}
