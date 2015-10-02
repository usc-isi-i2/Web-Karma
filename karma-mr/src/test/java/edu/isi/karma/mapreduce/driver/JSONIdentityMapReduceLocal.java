package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JSONIdentityMapReduceLocal {
	private static String filePath;
	private static String dirName;
	private static MapReduceDriver<Writable, Text, Text, Text, Text, Text> mapReduceDriver;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Mapper<Writable,Text, Text, Text> mapper = new IdentityJSONMapper();
		Reducer<Text,Text,Text,Text> reducer = new JSONReducer();
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
		filePath = System.getProperty("json.filepath");
	}
	
	@Before
	public void setUp() throws JSONException, FileNotFoundException {
		File file = new File(filePath);
		if (file.isDirectory()) {
			dirName = file.getName();
			for (File f : file.listFiles()) {
				String fileName = f.getName();
				if (fileName.substring(fileName.lastIndexOf(".") + 1).contains("json")) {
					processJSON(f, mapReduceDriver);
				}
			}
		}
	}
	
	@Test
	public void testJSONMapReduce() throws IOException {
		List<Pair<Text,Text>> results = mapReduceDriver.run();
		PrintWriter pw = new PrintWriter(filePath + File.separator + dirName + "_reduced.json");
		pw.println("[");
		boolean isFirst = true;
		for (Pair<Text,Text> pair : results) {
			if (!isFirst) {
				pw.println(",");
			}
			else {
				isFirst = false;
			}
			JSONObject obj = new JSONObject(pair.getSecond().toString());
			pw.println(obj.toString(4));
		}
		pw.println("]");
		pw.close();
	}
	
	private static void processJSON(File file, MapReduceDriver<Writable, Text, Text, Text, Text, Text> mapReduceDriver) throws JSONException, FileNotFoundException {
		JSONTokener tokener = new JSONTokener(new FileInputStream(file));
		char c = tokener.nextClean();
		if (c == '[') {
			while (true) {
				Object o = tokener.nextValue();
				mapReduceDriver.addInput(new BytesWritable(), new Text(o.toString()));
				char tmp = tokener.nextClean();
				if (tmp == ']')
					break;
			}
		}
	}
	
}
