package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.junit.Test;

public class TestN3GenerationLocal {

	
	@Test
	public void test() throws Exception {
		if(!new File("/tmp/loaded_data").exists())
		{
			
			Configuration conf = new Configuration();
			String[] args = {new File( getTestResource("InputFileDirectoryLoaderLocal.properties").toURI()).getAbsolutePath()};
			int res = ToolRunner.run(conf, new InputFileDirectoryLoader(),args);
			assertEquals(0, res);
		}
		
		if(!new File("/tmp/merged_data").exists())
		{

			Configuration conf = new Configuration();
			String [] jobArgs = {"-files", new File(getTestResource("people-model.ttl").toURI()).getAbsolutePath().toString(), "-libjars", System.getProperty("user.home") + "/.m2/repository/edu/isi/karma-offline/0.0.1-SNAPSHOT/karma-offline-0.0.1-SNAPSHOT-shaded.jar", new File(getTestResource("N3ProcessorLocal.properties").toURI()).getAbsolutePath().toString()}; 
			int res = ToolRunner.run(conf, new N3Processor(), jobArgs);
			assertEquals(0, res);
		}
	}

	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
